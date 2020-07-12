package com.tridevmc.spacegame.cpu.hardware;

import com.tridevmc.spacegame.cpu.DCPU;
import com.tridevmc.spacegame.gl.texture.ImageUtil;

import java.util.concurrent.locks.ReentrantLock;

public class LEM1802 implements IHardware, I2DScreen {

    public static final int PIXELS_X = 136;
    public static final int PIXELS_Y = 112;
    public static final int CHAR_W = 4;
    public static final int CHAR_H = 8;
    public static final int CHAR_COL = 32;
    public static final int CHAR_ROW = 12;

    private static final int BLINK_TIMER_MAX = 60;

    private static final char[] _fontROM = new char[0x100];
    private static final char[] _paletteROM = new char[]{
            0x000, 0x00a, 0x0a0, 0x0aa, 0xa00, 0xa0a, 0xa50, 0xaaa,
            0x555, 0x55f, 0x5f5, 0x5ff, 0xf55, 0xf5f, 0xff5, 0xfff
    };

    private DCPU _origin;
    private char _vramIndex = 0x8000;
    private char _framIndex = 0x0000;
    private char _pramIndex = 0x0000;
    private int _bootTimer = 60;
    private int _blinkTimer = BLINK_TIMER_MAX;
    private boolean _blink = false;
    private boolean _bootDone = false;
    private static boolean _romInit = false;
    private int[] _screenBuffer = new int[PIXELS_X*PIXELS_Y];
    private int _borderIndex = 0;

    private ReentrantLock _lock = new ReentrantLock();

    public LEM1802() {
        if(!_romInit) {
            ImageUtil.ImageBuffer font = ImageUtil.loadImage("/texture/font.png");

            for (int y = 0; y < font.height; y++) {
                for (int x = 0; x < font.width; x++) {
                    int relX = x % (CHAR_W/2);
                    int relP = x % CHAR_W;
                    int plane = relP >= 2 ? 1 : 0;
                    int relY = y % CHAR_H;
                    int cX = x / CHAR_W;
                    int cY = y / CHAR_H;
                    int pixel = font.buf.getInt();
                    byte bit = (byte) (pixel == 0 ? 0 : 1);
                    _fontROM[(cY * CHAR_COL * 2) + (cX * 2) + plane] |= bit << ((8 - (8 * relX)) + relY);
                }
            }

            ImageUtil.ImageBuffer boot = ImageUtil.loadImage("/texture/real_boot.png");

            for (int y = 0; y < boot.height; y++) {
                for (int x = 0; x < boot.width; x++) {
                    _screenBuffer[(y*boot.width)+x] = boot.buf.getInt();
                }
            }
        }
    }

    @Override
    public void connect(DCPU origin) {
        _origin = origin;
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
            case 2:
                _pramIndex = _origin.b;
                break;
            case 3:
                _borderIndex = _origin.b;
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
    @Override
    public void update() {
        _lock.lock();
        if(_bootTimer > 0) {
            _bootTimer--;
        } else if(_bootDone) {
            if(_vramIndex != 0x0000) {

                int borderColor = fromPaletteEntry(_borderIndex);

                _blinkTimer--;
                if (_blinkTimer == 0) {
                    _blinkTimer = BLINK_TIMER_MAX;
                    _blink = !_blink;
                }

                // Border Rendering
                for (int x = 0; x < PIXELS_X; x++) {
                    for (int y = 0; y < PIXELS_Y; y++) {
                        if ((x < CHAR_W || x >= (PIXELS_X - CHAR_W)) || (y < CHAR_H || y >= (PIXELS_Y - CHAR_H))) {
                            _screenBuffer[(y * PIXELS_X) + x] = borderColor;
                        }
                    }
                }

                // VRAM Rendering
                for (int i = _vramIndex; i < _vramIndex + 0x180; i++) {
                    int fg = ((_origin.ram[i] & 0b1111_0000_0_0000000) >> 12) & 0xF;
                    int bg = ((_origin.ram[i] & 0b0000_1111_0_0000000) >> 8) & 0xF;
                    boolean blink = (_origin.ram[i] & 0b0000_0000_1_0000000) != 0;
                    int cr = (_origin.ram[i] & 0b0000_0000_0_1111111) & 0xFF;

                    int cX = (i - _vramIndex) % 32;
                    int cY = (i - _vramIndex) / 32;

                    int bg32 = fromPaletteEntry(bg);
                    int fg32 = fromPaletteEntry(fg);

                    int chrX = cr % 32;
                    int chrY = cr / 32;

                    for (int x = 0; x < 4; x++) {
                        int plane = x >= 2 ? 1 : 0;

                        // Each character is 2 words long, so all our indexing has to be done
                        // in multiples of two. We add `plane` to get the other word of the character.
                        int bit = 0;
                        if (_framIndex != 0x0000) {
                            bit = _origin.ram[_framIndex + (chrY * CHAR_COL * 2) + (chrX * 2) + plane];
                        } else {
                            bit = _fontROM[(chrY * CHAR_COL * 2) + (chrX * 2) + plane];
                        }

                        // The high byte of each word describes the first column of the character -
                        // therefore, our mask must start by indexing the lowest bit of the high byte if
                        // we're in the first column.
                        int mask = x % 2 == 0 ? 0x100 : 0x01;

                        for (int y = 0; y < 8; y++) {
                            // The border is 1 character size (4x8) on each side of the screen. We
                            // therefore do a bulk calculation to address this extra space in our screen texture.
                            int border = ((PIXELS_X * CHAR_H) + CHAR_W) + (cY * (CHAR_W * 2) * CHAR_H) + (y * (CHAR_W * 2));
                            int pos = border + (cY * CHAR_COL * CHAR_W * CHAR_H) + (y * CHAR_COL * CHAR_W) + (cX * CHAR_W) + x;
                            if ((bit & mask) != 0) {
                                _screenBuffer[pos] = (blink && _blink) ? bg32 : fg32;
                            } else {
                                _screenBuffer[pos] = bg32;
                            }

                            mask <<= 1;
                        }
                    }
                }
            }
        }

        if(_bootTimer == 0 && !_bootDone) {
            clear();
            _bootDone = true;
        }

        _lock.unlock();
    }

    private int fromPaletteEntry(int entry) {
        int val = 0;
        if(_pramIndex != 0x0000) {
            val = _origin.ram[_pramIndex + entry];
        } else {
            val = _paletteROM[entry];
        }
        int r = ((val & 0x0F00) >> 8) & 0xF;
        int g = ((val & 0x00F0) >> 4) & 0xF;
        int b = (val & 0x000F) & 0xF;
        r = r + (r << 4);
        g = g + (g << 4);
        b = b + (b << 4);
        int a = 0xFF;

        return (a << 24) + (r) + (g << 8) + (b << 16);
    }

    private void clear() {
        for (int y = 0; y < PIXELS_Y; y++) {
            for (int x = 0; x < PIXELS_X; x++) {
                _screenBuffer[(y*PIXELS_X)+x] = 0;
            }
        }
    }

    @Override
    public int[] getScreenBuffer() {
        return _screenBuffer;
    }

    @Override
    public int getWidth() {
        return PIXELS_X;
    }

    @Override
    public int getHeight() {
        return PIXELS_Y;
    }

    @Override
    public void beginRender() {
        _lock.lock();
    }

    @Override
    public void endRender() {
        _lock.unlock();
    }
}
