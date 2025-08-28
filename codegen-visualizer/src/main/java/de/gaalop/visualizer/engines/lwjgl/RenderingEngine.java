package de.gaalop.visualizer.engines.lwjgl;

/**
 *
 * @author Christian Steinmetz
 */
public abstract class RenderingEngine extends Thread {

    public float pointSize = 0.5f;
    
    public abstract int getWidth();
    public abstract int getHeight();
}
