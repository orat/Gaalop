package de.gaalop.visualizer.engines.lwjgl;

import de.gaalop.visualizer.engines.lwjgl.graph.Render;
import de.gaalop.visualizer.engines.lwjgl.scene.Scene;
import de.gaalop.visualizer.engines.lwjgl.scene.Window;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public interface IAppLogic {

    void cleanup();

    void init(Window window, Scene scene, Render render);

    void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed);

    void update(Window window, Scene scene, long diffTimeMillis);
}