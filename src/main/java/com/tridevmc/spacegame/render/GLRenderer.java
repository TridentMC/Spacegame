package com.tridevmc.spacegame.render;

import com.tridevmc.spacegame.client.Camera;
import com.tridevmc.spacegame.render.shader.AttributeType;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import com.tridevmc.spacegame.render.shader.UniformType;
import com.tridevmc.spacegame.util.ResourceLocation;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.io.IOException;

public class GLRenderer implements IRenderer {

    private FrameBufferObject _gBuffer;
    private FrameBufferObject _lBuffer;
    private int _width, _height;

    // TODO: Implement a toggle switch for the final post-processing layer utilizing lBuffer.
    private static final boolean BLIT_PASS = false;

    private static ShaderProgram world;
    private static ShaderProgram screen;

    @Override
    public void init(int width, int height) {
        setViewport(width, height);

        _gBuffer = new FrameBufferObject(_width, _height);
        _gBuffer.bind();

        _gBuffer.genTextureBuffer(GL33.GL_RGBA16F, GL33.GL_FLOAT, GL33.GL_COLOR_ATTACHMENT0);
        _gBuffer.genTextureBuffer(GL33.GL_RGBA16F, GL33.GL_FLOAT, GL33.GL_COLOR_ATTACHMENT1);
        _gBuffer.genTextureBuffer(GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, GL33.GL_COLOR_ATTACHMENT2);
        _gBuffer.setDrawBuffers(GL33.GL_COLOR_ATTACHMENT0, GL33.GL_COLOR_ATTACHMENT1, GL33.GL_COLOR_ATTACHMENT2);
        _gBuffer.genRenderBuffer(GL33.GL_DEPTH_COMPONENT24, GL33.GL_DEPTH_ATTACHMENT);
        if(!_gBuffer.isValid()) {
            throw new RuntimeException("gBuffer didn't initialize properly!");
        }

        if(BLIT_PASS) {
            _lBuffer = new FrameBufferObject(_width, _height);
            _lBuffer.bind();

            _lBuffer.genTextureBuffer(GL33.GL_RGBA16F, GL33.GL_FLOAT, GL33.GL_COLOR_ATTACHMENT0);
            _lBuffer.genRenderBuffer(GL33.GL_DEPTH_COMPONENT24, GL33.GL_DEPTH_ATTACHMENT);
            if (!_lBuffer.isValid()) {
                throw new RuntimeException("lBuffer didn't initialize properly!");
            }
        }

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glEnable(GL33.GL_TEXTURE_2D);
        GL33.glEnable(GL33.GL_BLEND);
    }

    @Override
    public void loadContent() throws IOException {
        world = new ShaderProgram(new ResourceLocation("spacegame", "world"));
        world.registerAttribute(AttributeType.VERTEX, 3, GL33.GL_FLOAT, "position");
        world.registerAttribute(AttributeType.NORMAL, 3, GL33.GL_FLOAT, "normal");
        world.registerUniform(UniformType.MODEL, "model");
        world.registerUniform(UniformType.VIEW, "view");
        world.registerUniform(UniformType.PROJ, "proj");

        screen = new ShaderProgram(new ResourceLocation("spacegame", "world_screen"));
        screen.registerAttribute(AttributeType.VERTEX, 3, GL33.GL_FLOAT, "position");
        screen.registerAttribute(AttributeType.TEXCOORD, 2, GL33.GL_FLOAT, "texCoord");
        screen.registerUniform(UniformType.MODEL, "model");
        screen.registerUniform(UniformType.VIEW, "view");
        screen.registerUniform(UniformType.PROJ, "proj");

        ShaderProgram point = new ShaderProgram(new ResourceLocation("spacegame", "point_pass"));
        point.registerAttribute(AttributeType.VERTEX, 3, GL33.GL_FLOAT, "position");
        point.registerUniform(UniformType.MODEL, "model");
        point.registerUniform(UniformType.VIEW, "view");
        point.registerUniform(UniformType.PROJ, "proj");
        point.registerUniform(UniformType.SAMPLER0, "gPosition");
        point.registerUniform(UniformType.SAMPLER1, "gNormal");
        point.registerUniform(UniformType.SAMPLER2, "gAlbedo");
        point.registerUniform(UniformType.LIGHT_POS, "lightPos");
        point.registerUniform(UniformType.LIGHT_COL, "lightCol");
        point.registerUniform(UniformType.LIGHT_ATTENUATION, "lightAtten");
        point.registerUniform(UniformType.LIGHT_RADIUS, "lightRadius");
        point.registerUniform(UniformType.SCREEN_SIZE, "gScreenSize");
        point.setUniform(UniformType.SAMPLER0, 0);
        point.setUniform(UniformType.SAMPLER1, 1);
        point.setUniform(UniformType.SAMPLER2, 2);
        point.setUniform(UniformType.SCREEN_SIZE, new Vector3f(_width, _height, 0.0f));

        ShaderProgram directional = new ShaderProgram(new ResourceLocation("spacegame", "directional_pass"));
        directional.registerAttribute(AttributeType.VERTEX, 2, GL33.GL_FLOAT, "position");
        directional.registerUniform(UniformType.VIEW, "view");
        directional.registerUniform(UniformType.PROJ, "proj");
        directional.registerUniform(UniformType.SAMPLER0, "gPosition");
        directional.registerUniform(UniformType.SAMPLER1, "gNormal");
        directional.registerUniform(UniformType.SAMPLER2, "gAlbedo");
        directional.registerUniform(UniformType.LIGHT_COL, "lightCol");
        directional.registerUniform(UniformType.LIGHT_DIRECTION, "lightDirection");
        directional.registerUniform(UniformType.SCREEN_SIZE, "gScreenSize");
        directional.setUniform(UniformType.SAMPLER0, 0);
        directional.setUniform(UniformType.SAMPLER1, 1);
        directional.setUniform(UniformType.SAMPLER2, 2);
        directional.setUniform(UniformType.SCREEN_SIZE, new Vector3f(_width, _height, 0.0f));
    }

    @Override
    public void pre(RenderStage stage) {
        switch(stage) {
            case GEOMETRY:
                GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                _gBuffer.bind();
                GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

                GL33.glEnable(GL33.GL_DEPTH_TEST);
                GL33.glDisable(GL33.GL_BLEND);
                break;
            case LIGHTING:
                GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
                GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

                _gBuffer.bindTextureAttachment(GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE0); // gPosition
                _gBuffer.bindTextureAttachment(GL33.GL_COLOR_ATTACHMENT1, GL33.GL_TEXTURE1); // gNormal
                _gBuffer.bindTextureAttachment(GL33.GL_COLOR_ATTACHMENT2, GL33.GL_TEXTURE2); // gAlbedo

                GL33.glDisable(GL33.GL_DEPTH_TEST);
                GL33.glEnable(GL33.GL_BLEND);
                GL33.glBlendEquation(GL33.GL_FUNC_ADD);
                GL33.glBlendFunc(GL33.GL_ONE, GL33.GL_ONE);
                GL33.glCullFace(GL33.GL_FRONT);
                break;
            case FORWARD:
                GL33.glEnable(GL33.GL_DEPTH_TEST);
                GL33.glDisable(GL33.GL_BLEND);
                GL33.glCullFace(GL33.GL_BACK);
                break;

        }
    }

    @Override
    public void post(RenderStage stage) {

    }

    @Override
    public void setViewport(int width, int height) {
        _width = width;
        _height = height;
        GL33.glViewport(0, 0, _width, _height);

        // TODO: Keeping this for now, but let's just not have the camera's core projection shit be
        //       modulated by some arbitrary global state???
        Camera.ASPECT = (float)_width/(float)_height;
    }

    public void destroy() {
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        _gBuffer.destroy();
        if(BLIT_PASS) {
            _lBuffer.destroy();
        }
    }
}
