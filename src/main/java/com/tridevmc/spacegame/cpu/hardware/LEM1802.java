package com.tridevmc.spacegame.cpu.hardware;

import com.tridevmc.spacegame.cpu.DCPU;
import com.tridevmc.spacegame.gl.texture.ImageUtil;
import org.lwjgl.opengl.GL33;

import java.util.Random;

public class LEM1802 implements IHardware {
    private DCPU _origin;
    private char _vramIndex = 0x0000;
    private char _framIndex = 0x0000;
    private int _bootTimer = 60 * 2;
    private boolean _bootDone = false;
    private static boolean _romInit = false;
    private static char[] _fontROM = new char[0x100];
    private static char[] _paletteROM = new char[]{
        0x000, 0x00a, 0x0a0, 0x0aa, 0xa00, 0xa0a, 0xa50, 0xaaa,
        0x555, 0x55f, 0x5f5, 0x5ff, 0xf55, 0xf5f, 0xff5, 0xfff
    };
    private int[] _screenBuffer = new int[136*112];
    private int _screenTex = 0;
    private Random _random = new Random();

    public LEM1802() {
        if(!_romInit) {
            ImageUtil.ImageBuffer font = ImageUtil.loadImage("/texture/font.png");

            for (int y = 0; y < font.height; y++) {
                for (int x = 0; x < font.width; x++) {
                    int relX = x % 2;
                    int relP = x % 4;
                    int plane = relP >= 2 ? 1 : 0;
                    int relY = y % 8;
                    int cX = x / 4;
                    int cY = y / 8;
                    int pixel = font.buf.getInt();
                    byte bit = (byte) (pixel == 0 ? 0 : 1);
                    _fontROM[(cY * 32 * 2) + (cX * 2) + plane] |= bit << ((8 - (8 * relX)) + relY);
                }
            }

            // why tf does this sigabrt?
            //font.destroy();

            ImageUtil.ImageBuffer boot = ImageUtil.loadImage("/texture/real_boot.png");

            for (int y = 0; y < boot.height; y++) {
                for (int x = 0; x < boot.width; x++) {
                    _screenBuffer[(y*boot.width)+x] = boot.buf.getInt();
                }
            }

            //boot.destroy();

            _screenTex = GL33.glGenTextures();
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_REPEAT);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_REPEAT);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, 136, 112, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, _screenBuffer);

            /* int pos = 70 * 2;

            System.out.println(String.format("%8s", Integer.toBinaryString(((dat[pos] >> 8) & 0x00FF))).replace(" ", "0"));
            System.out.println(String.format("%8s", Integer.toBinaryString(dat[pos] & 0x00FF)).replace(" ", "0"));
            System.out.println(String.format("%8s", Integer.toBinaryString(((dat[pos+1] >> 8) & 0x00FF))).replace(" ", "0"));
            System.out.println(String.format("%8s", Integer.toBinaryString(dat[pos+1] & 0x00FF)).replace(" ", "0"));*/
        }
    }

    @Override
    public void connect(DCPU origin) {
        _origin = origin;
        for(int i =_vramIndex;i < _vramIndex+0x180;i++) {
            _origin.ram[i] = (char)_random.nextInt();
        }
    }

    @Override
    public void interrupt() {
        switch(_origin.a) {
            case 0:
                _vramIndex = _origin.b;
                break;
            case 1:
                _framIndex = _origin.b;
                break;
        }
    }

    @Override
    public int id() {
        return 0x7349f615;
    }

    @Override
    public char version() {
        return 0x1802;
    }

    @Override
    public int manufacturer() {
        return 0x1c6c8b36;
    }

    private int fromPaletteEntry(int entry) {
        int val = _paletteROM[entry];
        int r = ((val & 0x0F00) >> 8) & 0xF;
        int g = ((val & 0x00F0) >> 4) & 0xF;
        int b = (val & 0x000F) & 0xF;
        r = r + (r << 4);
        g = g + (g << 4);
        b = b + (b << 4);
        int a = 0xFF;

        return (a << 24) + (r) + (g << 8) + (b << 16);
    }

    public void clear() {
        for (int y = 0; y < 136; y++) {
            for (int x = 0; x < 112; x++) {
                _screenBuffer[(y*112)+x] = 0;
            }
        }
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
        GL33.glTexSubImage2D(GL33.GL_TEXTURE_2D, 0, 0, 0, 136, 112, GL33.GL_RGBA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, _screenBuffer);
    }

    public void render() {
        if(_bootTimer > 0) {
            _bootTimer--;
        } else if(_bootDone) {
            if(_vramIndex == 0x0000) return;

            for(int i =_vramIndex;i<_vramIndex+0x180;i++) {

                int fg = ((_origin.ram[i] & 0b1111_0000_0_0000000) >> 12) & 0xF;
                int bg = ((_origin.ram[i] & 0b0000_1111_0_0000000) >> 8) & 0xF;
                boolean blink = (_origin.ram[i] & 0b0000_0000_1_0000000) != 0;
                int cr = (_origin.ram[i] & 0b0000_0000_0_1111111) & 0xFF;

                int cX = (i-_vramIndex) % 32;
                int cY = (i-_vramIndex) / 32;

                int bg32 = fromPaletteEntry(bg);
                int fg32 = fromPaletteEntry(fg);

                int chrX = cr % 32;
                int chrY = cr / 32;

                for(int x =0;x < 4;x++) {
                    int plane = x >= 2 ? 1 : 0;
                    int bit = _fontROM[(chrY * 32 * 2) + (chrX * 2) + plane];
                    int mask = x % 2 == 0 ? 0x100 : 0x01;
                    for(int y = 0;y < 8;y++) {
                        int border = ((136 * 8) + 4) + (cY * 8 * 8) + (y * 8);
                        int pos = border + (cY * 32 * 4 * 8) + (y * 32 * 4) + (cX * 4) + x;
                        if((bit & mask) != 0) {
                            _screenBuffer[pos] = fg32;
                        } else {
                            _screenBuffer[pos] = bg32;
                        }

                        mask <<= 1;
                    }
                }
            }
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
            GL33.glTexSubImage2D(GL33.GL_TEXTURE_2D, 0, 0, 0, 136, 112, GL33.GL_RGBA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, _screenBuffer);
        }

        if(_bootTimer == 0 && !_bootDone) {
            clear();
            _bootDone = true;
            return;
        }


    }
}
