package com.tridevmc.spacegame.gl.texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final class ImageUtil {
    public static class ImageBuffer {
        public ByteBuffer buf;
        public int width;
        public int height;

        public ImageBuffer(ByteBuffer buf, int width, int height) {
            this.buf = buf;
            this.width = width;
            this.height = height;
        }
    }

    public static ImageBuffer loadImage(String file) {
        MemoryStack stack;
        ByteBuffer img;
        InputStream stream = ImageUtil.class.getResourceAsStream(file);

        long len;
        try {
            len = stream.available();

            byte[] bytes = new byte[(int) len];

            int offset = 0;
            int total;
            while (offset < bytes.length
                    && (total = stream.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += total;
            }


            if (offset < bytes.length) {
                throw new IOException("Could not completely read font file");
            }


            stream.close();
            ByteBuffer buf = BufferUtils.createByteBuffer(bytes.length);
            buf.put(bytes);
            buf.flip();

            stack = MemoryStack.stackPush();
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer chan = stack.mallocInt(1);
            img = STBImage.stbi_load_from_memory(buf, w, h, chan, 4);
            if (img == null)
                throw new RuntimeException("Unable to load font file\n" + STBImage.stbi_failure_reason());
            return new ImageBuffer(img, w.get(0), h.get(0));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
