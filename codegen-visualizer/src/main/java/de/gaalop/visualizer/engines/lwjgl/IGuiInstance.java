package de.gaalop.visualizer.engines.lwjgl;

import de.gaalop.visualizer.engines.lwjgl.scene.Scene;
import de.gaalop.visualizer.engines.lwjgl.scene.Window;

/**
 *
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */

public interface IGuiInstance {
    
    void drawGui();
    boolean handleGuiInput(Scene scene, Window window);
}
