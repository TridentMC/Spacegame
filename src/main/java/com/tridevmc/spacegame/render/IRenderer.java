package com.tridevmc.spacegame.render;

import java.io.IOException;

/**
 * A standard interface for rendering code, such that all render state can be abstracted down into
 * a set of stages and their requisite logic.
 */
public interface IRenderer {
    /**
     * Initializes the render, setting up all state that needs to be present on
     * app initialization. This is called before {@link #loadContent()}.
     * @param width the width of the viewport
     * @param height the height of the viewport
     */
    void init(int width, int height);

    /**
     * Loads external content required for rendering, including any
     * {@link com.tridevmc.spacegame.render.shader.ShaderProgram}s that should be pre-cached.
     * @throws IOException if file loading call fails for some reason
     */
    void loadContent() throws IOException;

    /**
     * Performs any logic necessary to begin rendering the provided stage.
     * @param stage the stage whose logic should be performed
     */
    void pre(RenderStage stage);

    /**
     * Performs any extra logic required after rendering the provided stage.
     * @param stage the stage whose logic should be performed
     */
    void post(RenderStage stage);

    /**
     * Sets the current viewport width and height, and adjusts renderer state to match.
     * @param width the width of the viewport
     * @param height the height of the viewport
     */
    void setViewport(int width, int height);

    /**
     * Destroys the renderer, and performs all clean-up logic necessary.
     *
     * DO NOT CALL THIS UNTIL THE PROGRAM HAS COMPLETED!
     */
    void destroy();
}
