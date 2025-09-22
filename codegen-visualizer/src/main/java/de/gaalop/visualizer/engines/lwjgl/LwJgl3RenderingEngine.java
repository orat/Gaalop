package de.gaalop.visualizer.engines.lwjgl;

import de.gaalop.visualizer.PointCloud;
import de.gaalop.visualizer.PointClouds;
import de.gaalop.visualizer.Rendering;
import de.gaalop.visualizer.engines.lwjgl.graph.Render;
import de.gaalop.visualizer.engines.lwjgl.scene.Scene;
import de.gaalop.visualizer.engines.lwjgl.scene.Window;
import java.util.Map;
import java.util.Set;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public abstract class LwJgl3RenderingEngine extends RenderingEngine {

    public static final int TARGET_UPS = 30;
    private final IAppLogic appLogic;
    private final Window window;
    private Render render;
    private boolean running;
    private Scene scene;
    private int targetFps;
    private int targetUps;

    public LwJgl3RenderingEngine(Rendering rendering, 
            String windowTitle, Window.WindowOptions opts, IAppLogic appLogic) {
        window = new Window(windowTitle, opts, () -> {
            resize();
            return null;
        });
        targetFps = opts.fps; // frames per second
        targetUps = opts.ups;
        this.appLogic = appLogic;
        render = new Render(window);
        scene = new Scene(window.getWidth(), window.getHeight());
        appLogic.init(window, scene, render);
        running = true;
    }

    private void cleanup() {
        appLogic.cleanup();
        render.cleanup();
        window.cleanup();
    }

    private void resize() {
        int width = window.getWidth();
        int height = window.getHeight();
        scene.resize(width, height);
        render.resize(width, height);
    }

    private void run() {
        long initialTime = System.currentTimeMillis();
        float timeU = 1000.0f / targetUps;
        float timeR = targetFps > 0 ? 1000.0f / targetFps : 0;
        float deltaUpdate = 0;
        float deltaFps = 0;

        long updateTime = initialTime;
        IGuiInstance iGuiInstance = scene.getGuiInstance();
        while (running && !window.windowShouldClose()) {
            
            window.pollEvents();
            
            // neu 
            //window.waitForEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - initialTime) / timeU;
            deltaFps += (now - initialTime) / timeR;

            if (targetFps <= 0 || deltaFps >= 1) {
                window.getMouseInput().input();
                boolean inputConsumed = iGuiInstance != null && iGuiInstance.handleGuiInput(scene, window);
                appLogic.input(window, scene, now - initialTime, inputConsumed);
            }

            if (deltaUpdate >= 1) {
                long diffTimeMillis = now - updateTime;
                appLogic.update(window, scene, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (targetFps <= 0 || deltaFps >= 1) {
                render.render(window, scene);
                deltaFps--;
                // update ruft auch makeScreenshot() auf falls recorder != null
                window.update(); // glfSwapBuffers()
            }
            initialTime = now;
        }

        cleanup();
    }

    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }
    
    
    /**
     * Draws the concrete scene
     * @param clouds The point clouds
     * @param visibleObjects The visible point clouds
     */
    public abstract void draw(Map<String, PointCloud> clouds, 
            Set<String> visibleObjects, PointClouds loadedClouds);
}
