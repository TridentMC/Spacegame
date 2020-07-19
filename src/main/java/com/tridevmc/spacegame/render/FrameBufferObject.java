package com.tridevmc.spacegame.render;

import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.util.HashMap;
import java.util.Map;

public class FrameBufferObject {
    private final int _fbo;
    private Map<Integer, Integer> _fboTextureBuffers;
    private Map<Integer, Integer> _fboRenderBuffers;
    private int _width, _height;

    public FrameBufferObject(int width, int height) {
        _fbo = GL33.glGenFramebuffers();
        _fboTextureBuffers = new HashMap<>();
        _fboRenderBuffers = new HashMap<>();
        _width = width;
        _height = height;
    }

    public void genTextureBuffer(int format, int type, int attachment) {
        if(_fboTextureBuffers.containsKey(attachment)) {
            throw new RuntimeException("Duplicate registry of texture buffer attachment ID '" + attachment + "'!");
        }

        bind();

        int t = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, t);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, format, _width, _height,
                0, GL33.GL_RGBA, type, MemoryUtil.NULL);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, attachment, GL33.GL_TEXTURE_2D, t, 0);
        _fboTextureBuffers.put(attachment, t);
    }

    public void genRenderBuffer(int format, int attachment) {
        if(_fboRenderBuffers.containsKey(attachment)) {
            throw new RuntimeException("Duplicate registry of render buffer attachment ID '" + attachment + "'!");
        }

        bind();

        int r = GL33.glGenRenderbuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, _fbo);
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, r);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, format, _width, _height);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, attachment, GL33.GL_RENDERBUFFER, r);
        _fboRenderBuffers.put(attachment, r);
    }

    public void setDrawBuffers(int ...draw) {
        bind();
        GL33.glDrawBuffers(draw);
    }

    public int getTextureAttachment(int attachment) {
        if(!_fboTextureBuffers.containsKey(attachment)) {
            throw new RuntimeException("Unknown texture attachment ID '" + attachment + "' for framebuffer '"+_fbo+"'!");
        }
        return _fboTextureBuffers.get(attachment);
    }

    public boolean isValid() {
        bind();
        return GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) == GL33.GL_FRAMEBUFFER_COMPLETE;
    }

    public void bind() {
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, _fbo);
    }

    public void destroy() {
        GL33.glDeleteFramebuffers(_fbo);
    }

    public void bindTextureAttachment(int attachment, int target) {
        GL33.glActiveTexture(target);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, getTextureAttachment(attachment));
    }
}
