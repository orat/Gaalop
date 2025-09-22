package de.gaalop.visualizer.engines.lwjgl.scene;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 * 
 * https://stackoverflow.com/questions/59174586/opengl-and-awt-swing-user-interface
 * --> awt/swing
 * https://github.com/LWJGLX/lwjgl3-awt
 * --> awt/swing vermutlich ohne glfw
 */

import de.gaalop.visualizer.engines.lwjgl.LwJgl3RenderingEngine;
import de.gaalop.visualizer.engines.lwjgl.recording.GIFRecorder;
import de.gaalop.visualizer.engines.lwjgl.recording.Recorder;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final long windowHandle;
    private int height;
    private MouseInput mouseInput;
    private Callable<Void> resizeFunc;
    private int width;
    
    private Recorder recorder;
    private boolean changed = false;
    private boolean firstFrame = true;
    
    public Window(String title, WindowOptions opts, Callable<Void> resizeFunc) {
        this.resizeFunc = resizeFunc;
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        if (opts.antiAliasing) {
            glfwWindowHint(GLFW_SAMPLES, 4);
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        if (opts.compatibleProfile) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        if (opts.width > 0 && opts.height > 0) {
            this.width = opts.width;
            this.height = opts.height;
        } else {
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            width = vidMode.width();
            height = vidMode.height();
        }

        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> resized(w, h));

        glfwSetErrorCallback((int errorCode, long msgPtr) ->
                System.out.printf("Error code [{%d}], msg [{%s}]", errorCode, MemoryUtil.memUTF8(msgPtr))
        );

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            keyCallBack(key, action);
        });

        glfwMakeContextCurrent(windowHandle);

        if (opts.fps > 0) {
            glfwSwapInterval(0);
        } else {
            glfwSwapInterval(1);
        }

        glfwShowWindow(windowHandle);

        int[] arrWidth = new int[1];
        int[] arrHeight = new int[1];
        glfwGetFramebufferSize(windowHandle, arrWidth, arrHeight);
        width = arrWidth[0];
        height = arrHeight[0];

        mouseInput = new MouseInput(windowHandle);
    }

    public void cleanup() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }

    public int getHeight() {
        return height;
    }

    public MouseInput getMouseInput() {
        return mouseInput;
    }

    public int getWidth() {
        return width;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public void keyCallBack(int key, int action) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE/*GLFW_PRESS*/) {
            if (recorder != null) recorder.stopRecording();
            glfwSetWindowShouldClose(windowHandle, true); // We will detect this in the rendering loop
        }
        // neu
        if (key == GLFW_KEY_F3 && action == GLFW_PRESS){
            //Start recording
            if (recorder == null) {
                recorder = new GIFRecorder(Window.this);
                recorder.startRecording();
            }
        }
        if (key == GLFW_KEY_F4 && action == GLFW_PRESS){
            //Stop recording
            if (recorder != null) {
                recorder.stopRecording();
                recorder = null;
            }
        }
    }

    public void pollEvents() {
        glfwPollEvents();
    }
    // neu
    public void waitForEvents(){
        glfwWaitEvents();
    }

    protected void resized(int width, int height) {
        this.width = width;
        this.height = height;
        try {
            resizeFunc.call();
        } catch (Exception excp) {
            excp.printStackTrace();
            //System.out.println("Error calling resize callback", excp);
        }
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
        if (recorder != null){
            if (changed || firstFrame) {
                recorder.makeScreenshot();
                changed = false;
            }
            firstFrame = false;
            //timer.sync(25);
        } else {
            //timer.sync(60); // cap fps to 60fps
        }
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public static class WindowOptions {
        public boolean antiAliasing;
        public boolean compatibleProfile;
        public int fps;
        public int height;
        public int ups = LwJgl3RenderingEngine.TARGET_UPS;
        public int width;
    }
}