package de.gaalop.visualizer.engines.lwjgl.graph;

import de.gaalop.visualizer.engines.lwjgl.scene.Scene;
import de.gaalop.visualizer.engines.lwjgl.scene.Window;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class Render {

    //private AnimationRender animationRender;
    private GBuffer gBuffer;
    private GuiRender guiRender;
    //private LightsRender lightsRender;
    private RenderBuffers renderBuffers;
    private SceneRender sceneRender;
    //private ShadowRender shadowRender;
    //private SkyBoxRender skyBoxRender;

    public Render(Window window) {
        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);

        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        sceneRender = new SceneRender();
        guiRender = new GuiRender(window);
        //skyBoxRender = new SkyBoxRender();
        //shadowRender = new ShadowRender();
        //lightsRender = new LightsRender();
        //animationRender = new AnimationRender();
        gBuffer = new GBuffer(window);
        renderBuffers = new RenderBuffers();
    }

    public void cleanup() {
        sceneRender.cleanup();
        guiRender.cleanup();
        //skyBoxRender.cleanup();
        //shadowRender.cleanup();
        //lightsRender.cleanup();
        //animationRender.cleanup();
        gBuffer.cleanUp();
        renderBuffers.cleanup();
    }

    private void lightRenderFinish() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void lightRenderStart(Window window) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.getGBufferId());
    }

    public void render(Window window, Scene scene) {
        //animationRender.render(scene, renderBuffers);
        //shadowRender.render(scene, renderBuffers);
        sceneRender.render(scene, renderBuffers, gBuffer);
        lightRenderStart(window);
        //lightsRender.render(scene, shadowRender, gBuffer);
        //skyBoxRender.render(scene);
        lightRenderFinish();
        guiRender.render(scene);
    }

    public void resize(int width, int height) {
        guiRender.resize(width, height);
    }

    public void setupData(Scene scene) {
        renderBuffers.loadStaticModels(scene);
        renderBuffers.loadAnimatedModels(scene);
        sceneRender.setupData(scene);
        //shadowRender.setupData(scene);
        List<Model> modelList = new ArrayList<>(scene.getModelMap().values());
        modelList.forEach(m -> m.getMeshDataList().clear());
    }
}
