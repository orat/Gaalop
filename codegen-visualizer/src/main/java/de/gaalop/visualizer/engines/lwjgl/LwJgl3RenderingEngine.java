package de.gaalop.visualizer.engines.lwjgl;

import de.gaalop.visualizer.PointCloud;
import de.gaalop.visualizer.PointClouds;
import de.gaalop.visualizer.Rendering;
import de.gaalop.visualizer.engines.lwjgl.recording.GIFRecorder;
import de.gaalop.visualizer.engines.lwjgl.recording.Recorder;
import static de.gaalop.visualizer.engines.lwjgl.util.GLU.gluLookAt;
import static de.gaalop.visualizer.engines.lwjgl.util.GLU.gluPerspective;
import de.gaalop.visualizer.engines.lwjgl.util.SyncTimer;
//import java.nio.IntBuffer;

import java.util.HashMap;
import java.util.HashSet;
//import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL30.*;
//import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

/**
 * Implements a rendering engine based on LwJgl V3
 * @author Christian Steinmetz and Oliver Rettig
 * 
 * @see https://github.com/LWJGL/lwjgl3-wiki/wiki/2.6.6-LWJGL3-migration
 * @see https://github.com/LWJGL/lwjgl3-wiki/wiki/2.6.6-LWJGL3-migration
 * @see https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/util/par/ParShapesDemo.java
 * 
 * springs with opengl
 * https://stackoverflow.com/questions/70553735/how-to-draw-a-perfect-3d-spring-using-cylinders
 * 
 * TODO
 * - if window closed than deinit() must be invoked!
 */
public abstract class LwJgl3RenderingEngine extends RenderingEngine {

    //FIXME use java std class instead
    public static final Colour 
            
            BLACK = new Colour(0, 0, 0), 
            WHITE = new Colour(1, 1, 1),
            GREY = new Colour(0.5f, 0.5f, 0.5f),
            ORANGE = new Colour(1, 0.55f, 0),
            RED = new Colour(1, 0, 0);
    
    private double near = 0.1, far = 30;
    
    // Camera information
    private Vec3f camPos = new Vec3f(0.0f, 2.0f, -10.0f); // camera position
    private Vec3f camDir = new Vec3f(0.0f, 0.0f, 1.0f); // camera lookat (always Z)
    private Vec3f camUp = new Vec3f(0.0f, 1.0f, 0.0f);  // camera up direction (always Y)
    private float camAngleX = 0.0f, camAngleY = 0.0f;         // camera angles
    
    // Mouse information
    private int mouseX, mouseY, mouseButton;
    private float mouseSensitivy = 1.0f;
    private boolean[] buttonDown = new boolean[]{false, false, false};
    
    private static final int STATE_DOWN = 1;
    private static final int STATE_UP = 2;
    
    /**
     * Constants regarding aspect ration dimensions of the screen.
     */
    public static final float SCREEN_WIDTH = 5, SCREEN_HEIGHT = 3;
            
    protected Rendering rendering;
    
    public Recorder recorder;
    
    private boolean changed = false;
    private boolean firstFrame = true;
    
    private int list = -1;

    /**
     * The handle of the window.
     */
    private long window;
    
    /**
     * A reference to the error callback so it doesn't get GCd.
     */
    private GLFWErrorCallback errorCallback;
    
    /**
     * A reference to the framebuffer size callback.
     */
    private GLFWFramebufferSizeCallback framebufferSizeCallback;
   
    /**
     * A reference to the key callback.
     */
    private GLFWKeyCallback keyCallback;
    
    /**
     * A reference to the cursor pos callback.
     */
    private GLFWCursorPosCallback cursorPosCallback;
    
    /**
     * A reference to the mouse buttom callback.
     */
    private GLFWMouseButtonCallback mouseButtonCallback;
    
    /**
     * The current window position of the cursor.
     */
    private final CursorPos cursorPos = new CursorPos();
    
    /**
     * Has there been a close request not coming from the window itself.
     */
    private boolean remainOpen = true;
    
    
    /**
     * Wrapper for the orthographic projection currently used. For transforming
     * mouse click coords.
     * 
     * Orthographic camera control allows for a view of a 3D scene where objects 
     * maintain their size regardless of their distance from the camera, 
     * eliminating perspective foreshortening and vanishing points. This provides 
     * a fixed-scale, 2D-like representation of 3D space, which is useful in 
     * applications like CAD and engineering for accurate measurement and 
     * alignment, and in certain types of 2D or isometric games. In orthographic 
     * control, "zooming" is achieved by adjusting the orthographic scale, not 
     * by moving the camera closer or further away
     */
    //private final Projection projection = new Projection();
    
    /**
     * A struct representing an orthographic projection.
     */
    /*public static class Projection {
        float left, right, bottom, top;
    }*/
    
    
    /**
     * The time of the start of the last loop. Used for delta time calculation.
     */
    //private double lastTime;
    
    /**
     * OpenGL object handles.
     */
    //private int program, vao, vbo;
    
    /**
     * The location and a buffer representing the modelViewMatrix uniform.
     */
    //private int modelViewLoc;
    
    /**
     * The location and a buffer representing the projectionMatrix uniform.
     */
    //private int projectionLoc;
    //private FloatBuffer projectionMatrix;
    
    //private FloatBuffer modelViewMatrix;
    
    /**
     * Wrapper for the framebuffer dimensions. For transforming
     * mouse click coords.
     */
    //private final Framebuffer framebuffer = new Framebuffer();
    
    private transient float width;
    private transient float height;
    public int getWidth() {
        return (int) width;
        //return framebuffer.width;
    }
    public int getHeight() {
        //return framebuffer.height;
        return (int) height;
    }
    
    /**
     * Shaders.
     */
    /*private final String vertexSrc =
            "#version 330\n"
            + "layout(std140) uniform mat4 projection;\n"
            + "layout(std140) uniform mat4 modelView;\n"
            + "layout(location = 0) in vec2 position;\n"
            + "layout(location = 1) in vec3 colour;\n"
            + "varying vec3 vColour;"
            + "void main(void) {\n"
            + "    vColour = colour;"
            + "    gl_Position = projection * modelView * vec4(position.xy, 0, 1);\n"
            + "}\n";
    private final String fragmentSrc =
            "#version 330\n"
            + "varying vec3 vColour;"
            + "layout(location = 0) out vec4 colourOut;\n"
            + "void main(void) {\n"
            + "    colourOut = vec4(vColour.rgb, 1);\n"
            + "}\n";
    */
    
    /**
     * The initial width and height of the window.
     */
    public static final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 600;
    
    public LwJgl3RenderingEngine(/*String lwJglNativePath, */Rendering rendering) {
        this.rendering = rendering;

        //Path lwjglLibraryPath = Paths.get(lwJglNativePath);

        //System.setProperty("org.lwjgl.librarypath", lwjglLibraryPath.toAbsolutePath().toString());
        
    }

    /**
     * Starts the lwjgl engine and shows a window, where the point clouds are rendered.
     */
    public void startEngine() {
        try {
            
            //Setup an error callback to print GLFW errors to the console.
            glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

            //Initialize GLFW.
            // If this function fails, it calls glfwTerminate before returning. 
            // If it succeeds, you should call glfwTerminate before the application exits.
            if (!glfwInit()) throw new IllegalStateException("glfInit failed");
                
            //Set resizable
            glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
            
            //Request an OpenGL 3.3 Core context.
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); 
            
            //long monitor = 0; // main monitor
            // better detect the appropriate monitor
            long monitor = glfwGetPrimaryMonitor();

            //Create the window with the specified title.
            window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, 
                        "Gaalop 3D Visualization Window V2", monitor, 0);       
            // Display.setTitle("Gaalop 3D Visualization Window");
            // Display.setDisplayMode(new DisplayMode(width, height));
            if(window == 0) {
                throw new IllegalStateException("Failed to create window");
            }

            //Make this window's context the current on this thread.
            glfwMakeContextCurrent(window);
            //Let LWJGL know to use this current context.
            GL.createCapabilities();

            //Setup the framebuffer resize callback to manage display size changes
            glfwSetFramebufferSizeCallback(window, (framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
                @Override
                public void invoke(long window, int width, int height) {
                    //onResize(width, height);
                    changeSize(width, height);
                }
            }));


            // wird vielleicht nur für fullscreen-mode benötigt?
            //FIXME
            //Create buffers to put the framebuffer width and height into.
            /*IntBuffer framebufferWidth = BufferUtils.createIntBuffer(1), 
                      framebufferHeight = BufferUtils.createIntBuffer(1);
            //Put the framebuffer dimensions into these buffers.
            glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
            //Intialize the projection matrix with the framebuffer dimensions.
            onResize(framebufferWidth.get(), framebufferHeight.get());*/
            // changeSize() wird weiter unten aufgerufen
            
            //Setup the framebuffer resize callback.
            
            /**
              * Callback inputs of the keyboard (instead of polling)<br>
              * F3:Start recording
              * F4:Stop recording
              * ESC: Close window
              */
            glfwSetKeyCallback(window, (keyCallback = new GLFWKeyCallback() {
                @Override
                public void invoke(long window, int key, int scancode, int action, int mods) {
                    if (key == GLFW_KEY_F3 && action == GLFW_PRESS){
                    //if (Keyboard.isKeyDown(Keyboard.KEY_F3)) {
                        //Start recording
                        if (recorder == null) {
                            recorder = new GIFRecorder(LwJgl3RenderingEngine.this);
                            recorder.startRecording();
                        }
                    }
                    if (key == GLFW_KEY_F4 && action == GLFW_PRESS){
                    //if (Keyboard.isKeyDown(Keyboard.KEY_F4)) {
                        //Stop recording
                        if (recorder != null) {
                            recorder.stopRecording();
                            recorder = null;
                        }
                    }
                    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS){
                    //if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                        if (recorder != null) 
                            recorder.stopRecording();
                        
                        //Display.destroy();
                        glfwDestroyWindow(window);
                        System.exit(0);
                    }
                }

            }));

            //Setup the cursor/mouse pos callback.
            glfwSetCursorPosCallback(window, (cursorPosCallback = new GLFWCursorPosCallback() {
                // The callback functions receives the cursor position, measured 
                // in screen coordinates but relative to the top-left corner of 
                // the window content area. On platforms that provide it, the full 
                // sub-pixel cursor position is passed on.
                @Override
                public void invoke(long window, double xpos, double ypos) {
                    cursorPos.x = xpos;
                    //cursorPos.y = framebuffer.height - ypos;
                    cursorPos.y = height - ypos;
                }
            }));

            //Setup the mouse button callback.
            glfwSetMouseButtonCallback(window, (mouseButtonCallback = new GLFWMouseButtonCallback() {
                @Override
                public void invoke(long window, int button, int action, int mods) {
                    if(action == GLFW_PRESS) {
                        if (!buttonDown[button]) {
                            mouseAction(button, STATE_DOWN, (int) cursorPos.x, (int) cursorPos.y);
                        } else {
                            mouseMoved((int) cursorPos.x, (int) cursorPos.y);
                        }
                        buttonDown[button] = true;
                    } else if(action == GLFW_RELEASE) {
                        if (buttonDown[button]) {
                            mouseAction(button, STATE_UP, (int) cursorPos.x, (int) cursorPos.y);
                        }
                        buttonDown[button] = false;
                    }
                }

            }));

            
            // init shaders for mouse camera orthographic control
            //initGL();
            
            // set up our OpenGL viewport to match the display size. We will also 
            // call this whenever the display is resized:
            GL11.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            // The depth buffer is used in 3D scenes to ensure that distant objects 
            // render correctly, and do not overlap with closer objects.
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            
            GL11.glShadeModel(GL11.GL_SMOOTH);
            changeSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            GL11.glDisable(GL11.GL_LIGHTING);

            
            //TODO In Pong wird statt dessen ein Mechanismus mit Shader verwendet
            // Der folgende code scheint mir der alte code mit lwjgl2 zu sein
            // This sets the current matrix mode, specifying which matrix stack 
            // subsequent matrix operations will affect. The GL11 prefix comes 
            // from Java's OpenGL bindings (LWJGL), while glMatrixMode is the 
            // standard OpenGL function. You use it with an argument like 
            // GL_PROJECTION or GL_MODELVIEW to switch between managing the 
            // projection matrix, which defines the view volume, and the modelview 
            // matrix, which handles object transformations (position, rotation, 
            // scale) and camera transformations
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            gluPerspective((float) 65.0, 
                    (float) WINDOW_WIDTH / (float) WINDOW_HEIGHT, (float) 0.1, 100);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        
            checkError(); // feuert ex wenn bei einem vorherigen Methodenaufruf eine Fehler aufgetreten sein sollte
            
            //Make this window visible.
            glfwShowWindow(window);
            //Display.create();

            //For the first frame, take this time to be the last frame's start.
            //lastTime = currentTimeMillis();


        } catch (IllegalStateException e) {
            //TODO
            // if initialisation failed:
            // Handle initialization failure
            // glfwTerminate ist automatically invoked before returning. 
            
            // if window opening failed
            //TODO

            e.printStackTrace();
            System.exit(0);
        }
        
        // you should call glfwTerminate before the application exits.
        //TODO
    }
    
    /**
     * Initializes the OpenGL state. Creating programs, VAOs and VBOs and sets 
     * 
     * Uses shaders. 
     * TODO substituion of the mouse camera control without shaders
     * 
     * appropriate state. 
     */
    /*public void initGL() {
        program = glCreateProgram();
        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexId, vertexSrc);
        glCompileShader(vertexId);
        if (glGetShaderi(vertexId, GL_COMPILE_STATUS) != GL_TRUE) {
            System.out.println(glGetShaderInfoLog(vertexId, Integer.MAX_VALUE));
            throw new RuntimeException();
        }
        
        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentId, fragmentSrc);
        glCompileShader(fragmentId);
        if(glGetShaderi(fragmentId, GL_COMPILE_STATUS) != GL_TRUE) {
            System.out.println(glGetShaderInfoLog(fragmentId, Integer.MAX_VALUE));
            throw new RuntimeException();
        }
        
        glAttachShader(program, vertexId);
        glAttachShader(program, fragmentId);
        glLinkProgram(program);
        if(glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE) {
            System.out.println(glGetProgramInfoLog(program, Integer.MAX_VALUE));
            throw new RuntimeException();
        }
        
        modelViewLoc = glGetUniformLocation(program, "modelView");
        if(modelViewLoc == -1) {
            throw new RuntimeException();
        }
        modelViewMatrix = BufferUtils.createFloatBuffer(16);
        projectionLoc = glGetUniformLocation(program, "projection");
        if(projectionLoc == -1) {
            throw new RuntimeException();
        }
        projectionMatrix = BufferUtils.createFloatBuffer(16);
        
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer fb = BufferUtils.createFloatBuffer(5 * (4 + BALL_N_VERTICES + 4 + 2));
        
        fb.put(new float[]{
            0,            0,             PADDLE_COLOUR.red, PADDLE_COLOUR.green, PADDLE_COLOUR.blue,
            PADDLE_WIDTH, 0,             PADDLE_COLOUR.red, PADDLE_COLOUR.green, PADDLE_COLOUR.blue,
            PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_COLOUR.red, PADDLE_COLOUR.green, PADDLE_COLOUR.blue,
            0,            PADDLE_HEIGHT, PADDLE_COLOUR.red, PADDLE_COLOUR.green, PADDLE_COLOUR.blue
        });
        paddleHandle = new RenderHandle(0, 4);
        
        double step = (Math.PI * 2d) / BALL_N_VERTICES;
        for(int i = 0; i < BALL_N_VERTICES; i++) {
            double theta = i * step;
            float x = (float) (BALL_RADIUS * Math.cos(theta));
            float y = (float) (BALL_RADIUS * Math.sin(theta));
            fb.put(new float[]{
                x, y, BALL_COLOUR.red, BALL_COLOUR.green, BALL_COLOUR.blue
            });
        }
        ballHandle = new RenderHandle(4, BALL_N_VERTICES);
        
        fb.put(new float[]{
            0,            0,             BORDER_COLOUR.red, BORDER_COLOUR.green, BORDER_COLOUR.blue,
            SCREEN_WIDTH, 0,             BORDER_COLOUR.red, BORDER_COLOUR.green, BORDER_COLOUR.blue,
            SCREEN_WIDTH, SCREEN_HEIGHT, BORDER_COLOUR.red, BORDER_COLOUR.green, BORDER_COLOUR.blue,
            0,            SCREEN_HEIGHT, BORDER_COLOUR.red, BORDER_COLOUR.green, BORDER_COLOUR.blue
        });
        boundsHandle = new RenderHandle(4 + BALL_N_VERTICES, 4);
        
        fb.put(new float[]{
            0,            0,             LINE_COLOUR.red, LINE_COLOUR.green, LINE_COLOUR.blue,
            SCREEN_WIDTH, SCREEN_HEIGHT, LINE_COLOUR.red, LINE_COLOUR.green, LINE_COLOUR.blue
        });
        lineHandle = new RenderHandle(4 + BALL_N_VERTICES + 4, 2);
        
        fb.flip();
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 20, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 20, 8);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        glBindVertexArray(0);
        
        setBackColour();
        glLineWidth(5);
        
        checkError();
    }*/
    
    /**
     * Sets the contents of the specified buffer to an orthographic projection matrix.
     * 
     * @param dest The buffer to set.
     * @param p The projection to use.
     */
    /*public static void setOrtho2D(FloatBuffer dest, Projection p) {
        float f1 = p.right - p.left;
        float f2 = p.top - p.bottom;
        dest.put(new float[]{
            2f / f1,                  0,                        0,  0,
            0,                        2f / f2,                  0,  0,
            0,                        0,                        -1, 0,
            -(p.right + p.left) / f1, -(p.top + p.bottom) / f2, 0,  1
        });
        dest.flip();
    }*/
   
    @Override
    public void run() {
        startEngine();
        
        SyncTimer timer  = new SyncTimer(SyncTimer.LWJGL_GLFW);
 
        while (!glfwWindowShouldClose(window) && remainOpen){
            if (rendering.isNewDataSetAvailable()) {
                if (list != -1) GL11.glDeleteLists(list, 1);
                list = GL11.glGenLists(1);
                GL11.glNewList(list, GL11.GL_COMPILE);
                draw(rendering.getDataSet(), rendering.getVisibleObjects(), 
                        rendering.getLoadedPointClouds());
                GL11.glEndList();
                changed = true;
            }
            
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the screen
            GL11.glLoadIdentity(); // apply camPos before rotation

            GL11.glTranslatef(0.0f, 0.0f, -5.0f);
            // draw
            gluLookAt(camPos.x, camPos.y, camPos.z, // Position
                    camPos.x + camDir.x, camPos.y + camDir.y, camPos.z + camDir.z, // Lookat
                    camUp.x, camUp.y, camUp.z);               // Up-direction
            // apply rotation
            GL11.glRotatef(camAngleX, 0, 1, 0); // window x axis rotates around up vector
            GL11.glRotatef(camAngleY, 1, 0, 0); // window y axis rotates around x

            //Render the scene
            if (list != -1) GL11.glCallList(list);

            // pollInput(); Tastatureingaben pollen und damit recording steuern
            // Umstellung auf callback, damit nur bei Änderungen neu gerendert wird
            
            // Update the window. If the window is visible clears the dirty flag 
            // and calls swapBuffers() and finally polls the input devices if 
            // processMessages is true.
            //Display.update();
            glfwWaitEvents();
            glfwSwapBuffers(window);
            //glfwPollEvents();
            
            // http://forum.lwjgl.org/index.php?topic=6057.0
            if (recorder != null){
                if (changed || firstFrame) {
                    recorder.makeScreenshot();
                    changed = false;
                }
                firstFrame = false;
                timer.sync(25);
            } else {
                timer.sync(60); // cap fps to 60fps
            }
        }
            
        glfwDestroyWindow(window);
        //Display.destroy();
    }
    
    /**
     * Implements the action which is done, when mouse is moved on the lwjgl window
     * @param x The x coordinate of the position of the mouse
     * @param y The y coordinate of the position of the mouse
     */
    public void mouseMoved(int x, int y) {
        switch (mouseButton) {
            // 1 => rotate
            case 1:
                // update angle with relative movement
                camAngleX = fmod(camAngleX + (x - mouseX) * mouseSensitivy, 360.0f);

                camAngleY -= (y - mouseY) * mouseSensitivy;
                // limit y angle by 85 degree
                if (camAngleY > 85) {
                    camAngleY = 85;
                }
                if (camAngleY < -85) {
                    camAngleY = -85;
                }
                changed = true;
                break;
            // 2 => zoom
            case 2:
                camPos.z -= 0.1f * (y - mouseY) * mouseSensitivy;
                changed = true;
                break;
            // 3 => translate
            case 3:
                // update camPos
                camPos.x += 0.1f * (x - mouseX) * mouseSensitivy;
                camPos.y -= 0.1f * (y - mouseY) * mouseSensitivy;
                changed = true;
                break;
            default:
                break;
        }
        // update mouse for next relative movement
        mouseX = x;
        mouseY = y;
    }

    /**
     * Implements the action which is done, when a mouse button is pressed on the lwjgl window
     * @param button The button which is pressed
     * @param state The state of the button
     * @param x The x coordinate of the position of the mouse
     * @param y The y coordinate of the position of the mouse
     */
    void mouseAction(int button, int state, int x, int y) {
        switch (button) {
            case 0:
                if (state == STATE_DOWN) {
                    mouseButton = 1;
                    mouseX = x;
                    mouseY = y;
                } else {
                    mouseButton = 0;
                }
                break;
            case 1:
                if (state == STATE_DOWN) {
                    mouseButton = 3;
                    mouseX = x;
                    mouseY = y;
                } else {
                    mouseButton = 0;
                }
                break;
            case 2:
                if (state == STATE_DOWN) {
                    mouseButton = 2;
                    mouseX = x;
                    mouseY = y;
                } else {
                    mouseButton = 0;
                }
                break;
        }
    }

    /**
     * Changes the size of the lwjgl window
     * @param w The new width of the lwjgl window
     * @param h The new height of the lwjgl window
     */
    private void changeSize(float w, float h) {
        this.width = w;
        this.height = h;
        
        //TODO das sollte ich noch in onRisize() übernehmen
        // Prevent a division by zero, when window is too short
        if (h == 0) {
            h = 1;
        }
        float wRatio = 1.0f * w / h;
        
        // Reset the coordinate system before modifying
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        // Set the viewport to be the entire window
        GL11.glViewport(0, 0, (int) w, (int) h);
        
        // Set the correct perspective.
        gluPerspective(45.0f, wRatio, (float) near, (float) far);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        // mouseMoved() setzt immer die aktuelle camPos/camDir
        gluLookAt(camPos.x, camPos.y, camPos.z, // Position
                camPos.x + camDir.x, camPos.y + camDir.y, camPos.z + camDir.z, // Lookat
                camUp.x, camUp.y, camUp.z);               // Up-direction}
    }

    
    /**
     * To be called when the visualizations's framebuffer is resized. Updates the projection
     * matrix.
     * 
     * @param framebufferWidth The width of the new framebuffer
     * @param framebufferHeight The height of the new framebuffer
     */
    /*public void onResize(int framebufferWidth, int framebufferHeight) {
        framebuffer.width = framebufferWidth;
        framebuffer.height = framebufferHeight;
        float aspectRatio = (float) framebufferHeight / framebufferWidth;
        float desiredAspectRatio = SCREEN_HEIGHT / SCREEN_WIDTH;
        projection.left = 0;
        projection.right = SCREEN_WIDTH;
        projection.bottom = 0;
        projection.top = SCREEN_HEIGHT;
        if(aspectRatio == desiredAspectRatio) {
        } else if(aspectRatio > desiredAspectRatio) {
            float newScreenHeight = SCREEN_WIDTH * aspectRatio;
            projection.bottom = -(newScreenHeight - SCREEN_HEIGHT) / 2f;
            projection.top = newScreenHeight + projection.bottom;
        } else if(aspectRatio < desiredAspectRatio) {
            float newScreenWidth = SCREEN_HEIGHT / aspectRatio;
            projection.left = -(newScreenWidth - SCREEN_WIDTH) / 2f;
            projection.right = newScreenWidth + projection.left;
        }
        setOrtho2D(projectionMatrix, projection);
        glViewport(0, 0, framebufferWidth, framebufferHeight);
    }*/
    
    
    /**
     * Implements a float modulo
     * @param value The float value
     * @param modulo The modulo value
     * @return The result of the modulo operation on the float value
     */
    private float fmod(float value, float modulo) {
        while (value < 0) {
            value += modulo;
        }
        while (value > modulo) {
            value -= modulo;
        }
        return value;
    }

    /**
     * Draws the concrete scene
     * @param clouds The point clouds
     * @param visibleObjects The visible point clouds
     */
    public abstract void draw(HashMap<String, PointCloud> clouds, HashSet<String> visibleObjects, PointClouds loadedClouds);
    
    
    public static class CursorPos {
        double x, y;
    }
  
    /**
     * A struct representing a framebuffer.
     */
    /*public static class Framebuffer {
        int width, height;
    }*/
    
     /**
     * Returns the current system time in milliseconds.
     * 
     * @return The current system time in milliseconds.
     */
    /*public static double currentTimeMillis() {
        return glfwGetTime() * 1000;
    }*/
    
    /**
     * Utility method which checks for an OpenGL error, throwing an exception if
     * one is found.
     */
    private static void checkError() {
        int err = glGetError();
        switch(err) {
            case GL_NO_ERROR: return;
            case GL_INVALID_OPERATION: throw new IllegalStateException("Invalid Operation");
            case GL_INVALID_ENUM: throw new IllegalStateException("Invalid Enum");
            case GL_INVALID_VALUE: throw new IllegalStateException("Invalid Value");
            case GL_INVALID_FRAMEBUFFER_OPERATION: throw new IllegalStateException("Invalid Framebuffer Operation");
            case GL_OUT_OF_MEMORY: throw new IllegalStateException("Out of Memory");
        }
    }
    
    /**
     * A struct representing a colour.
     */
    private static class Colour {
        final float red, green, blue;

        public Colour(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }
    
    /**
     * The current game state. PLAYING, PAUSED or LOST.
     */
    //private State currentState = State.PLAYING;
    
    /**
     * Sets the appropriate back colour based on the game's current state.
     */
    /*public void setBackColour() {
        glClearColor(currentState.backColour.red, currentState.backColour.green, 
                currentState.backColour.blue, 0);
    }*/
    
    // maximale Größen des primary monitors
    //FIXME Wer braucht das überhaupt, nur für fullscreen benötigt?
    /*public static int getMaximumWidth() {
        // lwjgl2
        // return Display.getWidth();

        //lwjgl3
        GLFWVidMode glfwGetVideoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return glfwGetVideoMode.width();
    }
    public static int getMaximumHeight() {
        // lwjgl2
        // return Display.getWidth();

        //lwjgl3
        GLFWVidMode glfwGetVideoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return glfwGetVideoMode.height();
    }*/
    
    //TODO
    // wer soll das aufrufen?
    /**
     * Releases game resources and window.
     */
    public void deinit() {
        deinitGL();
        glfwDestroyWindow(window);   
        glfwTerminate();
    }
    
    /**
     * Releases in use OpenGL resources.
     */
    private void deinitGL() {
        //glDeleteVertexArrays(vao);
        //glDeleteBuffers(vbo);
        //glDeleteProgram(program);
    }
}
