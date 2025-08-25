package de.gaalop.visualizer.engines.lwjgl;

import de.gaalop.visualizer.PointCloud;
import de.gaalop.visualizer.PointClouds;
import de.gaalop.visualizer.Pong;
import static de.gaalop.visualizer.Pong.BLACK;
import static de.gaalop.visualizer.Pong.ORANGE;
import static de.gaalop.visualizer.Pong.RED;
import de.gaalop.visualizer.Rendering;
import de.gaalop.visualizer.engines.lwjgl.recording.GIFRecorder;
import de.gaalop.visualizer.engines.lwjgl.recording.Recorder;
import java.nio.IntBuffer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import org.lwjgl.BufferUtils;
//import org.lwjgl.LWJGLException;
//import org.lwjgl.input.Keyboard;
//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.Display;
//import org.lwjgl.opengl.DisplayMode;
//import org.lwjgl.opengl.GL11;
//import org.lwjgl.util.glu.GLU;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import java.nio.FloatBuffer;
import org.lwjgl.glfw.GLFWVidMode;

/**
 * Implements a rendering engine based on LwJgl V3
 * @author Christian Steinmetz and Oliver Rettig
 * 
 * @see https://github.com/LWJGL/lwjgl3-wiki/wiki/2.6.6-LWJGL3-migration
 * @see https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/util/par/ParShapesDemo.java
 * 
 */
public abstract class LwJglRenderingEngine extends RenderingEngine {

    //FIXME use java std class instead
    public static final Colour 
            
            BLACK = new Colour(0, 0, 0), 
            WHITE = new Colour(1, 1, 1),
            GREY = new Colour(0.5f, 0.5f, 0.5f),
            ORANGE = new Colour(1, 0.55f, 0),
            RED = new Colour(1, 0, 0);
    
    private double near = 0.1, far = 30;
    
    // Camera information
    private Vec3f camPos = new Vec3f(0.0f, 2.0f, -10.0f);       // camera position
    private Vec3f camDir = new Vec3f(0.0f, 0.0f, 1.0f);         // camera lookat (always Z)
    private Vec3f camUp = new Vec3f(0.0f, 1.0f, 0.0f);          // camera up direction (always Y)
    private float camAngleX = 0.0f, camAngleY = 0.0f;   // camera angles
    
    // Mouse information
    private int mouseX, mouseY, mouseButton;
    private float mouseSensitivy = 1.0f;
    private boolean[] buttonDown = new boolean[]{false, false, false};
    
    private static final int STATE_DOWN = 1;
    private static final int STATE_UP = 2;
    
    /**
     * Constants regarding dimensions in game.
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
     * The current window position of the cursor.
     */
    private final CursorPos cursorPos = new CursorPos();
    
    /**
     * Has there been a close request not coming from the window itself.
     */
    private boolean remainOpen = true;
    
    /**
     * A reference to the mouse buttom callback.
     */
    private GLFWMouseButtonCallback mouseButtonCallback;
    
    /**
     * Wrapper for the orthographic projection currently used. For transforming
     * mouse click coords.
     */
    private final Projection projection = new Projection();
    
    /**
     * The time of the start of the last loop. Used for delta time calculation.
     */
    private double lastTime;
    
    /**
     * A struct representing an orthographic projection.
     */
    public static class Projection {
        float left, right, bottom, top;
    }
    
    /**
     * OpenGL object handles.
     */
    private int program, vao, vbo;
    
    /**
     * The location and a buffer representing the modelViewMatrix uniform.
     */
    private int modelViewLoc;
    
    /**
     * The location and a buffer representing the projectionMatrix uniform.
     */
    private int projectionLoc;
    private FloatBuffer projectionMatrix;
    
    private FloatBuffer modelViewMatrix;
    
    /**
     * Wrapper for the framebuffer dimensions. For transforming
     * mouse click coords.
     */
    private final Framebuffer framebuffer = new Framebuffer();
    
    
    /**
     * Shaders.
     */
    private final String vertexSrc =
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
    
    /**
     * The initial width and height of the window.
     */
    public static final int WINDOW_WIDTH = 1000, WINDOW_HEIGHT = 600;
    
    public LwJglRenderingEngine(String lwJglNativePath, Rendering rendering) {
        this.rendering = rendering;

        Path lwjglLibraryPath = Paths.get(lwJglNativePath);

        System.setProperty("org.lwjgl.librarypath", lwjglLibraryPath.toAbsolutePath().toString());
        
    }

    /**
     * Starts the lwjgl engine and shows a window, where the point clouds are rendered
     * 
     * corresponding to init() in the "Pong" example
     */
    public void startEngine() {
        //int width = 800;
        //int height = 600;
        
        try {
            //Initialize GLFW.
            glfwInit();
            //Setup an error callback to print GLFW errors to the console.
            glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        
            //Set resizable
            glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
            //Request an OpenGL 3.3 Core context.
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); 
            int windowWidth = WINDOW_WIDTH;
            int windowHeight = WINDOW_HEIGHT;
            long monitor = 0;
        
            //Create the window with the specified title.
            window = glfwCreateWindow(windowWidth, windowHeight, 
                        "Gaalop 3D Visualization Window\"", monitor, 0);       
            // Display.setTitle("Gaalop 3D Visualization Window");
            // Display.setDisplayMode(new DisplayMode(width, height));
            
            if(window == 0) {
                throw new RuntimeException("Failed to create window");
            }
        
            //Make this window's context the current on this thread.
            glfwMakeContextCurrent(window);
            //Let LWJGL know to use this current context.
            GL.createCapabilities();

            initGL();
        
            //Setup the framebuffer resize callback.
            glfwSetFramebufferSizeCallback(window, (framebufferSizeCallback = new GLFWFramebufferSizeCallback() {

                @Override
                public void invoke(long window, int width, int height) {
                    onResize(width, height);
                }

            }));
        
            
            // old code
            
            //Display.setFullscreen(false);
            
            //Create buffers to put the framebuffer width and height into.
            IntBuffer framebufferWidth = BufferUtils.createIntBuffer(1), 
                    framebufferHeight = BufferUtils.createIntBuffer(1);
            //Put the framebuffer dimensions into these buffers.
            glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
            //Intialize the projection matrix with the framebuffer dimensions.
            onResize(framebufferWidth.get(), framebufferHeight.get());

            //Setup the framebuffer resize callback.
            glfwSetKeyCallback(window, (keyCallback = new GLFWKeyCallback() {

                @Override
                public void invoke(long window, int key, int scancode, int action, int mods) {
                    //If current event key is Space and is up event.
                    //Else If current event key is F5 and is key up event.
                    //Else If current event key is Escape and is key up event.
                    if(key == GLFW_KEY_SPACE && action == GLFW_RELEASE) {
                        onPlayPauseToggle();
                    } else if(key == GLFW_KEY_F5 && action == GLFW_RELEASE) {

                    } else if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                        //Request close.
                        remainOpen = false;
                    }
                }

            }));

            //Setup the cursor pos callback.
            glfwSetCursorPosCallback(window, (cursorPosCallback = new GLFWCursorPosCallback() {

                @Override
                public void invoke(long window, double xpos, double ypos) {
                    cursorPos.x = xpos;
                    cursorPos.y = framebuffer.height - ypos;
                }

            }));

            //Setup the cursor pos callback.
            /*glfwSetMouseButtonCallback(window, (mouseButtonCallback = new GLFWMouseButtonCallback() {

                @Override
                public void invoke(long window, int button, int action, int mods) {
                    if(button == 0) {
                        //If this event is down event and no current to-add-ball.
                        //Else If this event is up event and there is a current to-add-ball.
                        if(action == GLFW_PRESS && addBall == null) {
                            onNewBall(cursorPos.x, cursorPos.y);
                        } else if(action == GLFW_RELEASE && addBall != null) {
                            onNewBallRelease(cursorPos.x, cursorPos.y);
                        }
                    }
                }

            }));*/

            //Make this window visible.
            glfwShowWindow(window);
            // olde code?
            //Display.create();
            
            //For the first frame, take this time to be the last frame's start.
            lastTime = currentTimeMillis();
        
            

            
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(0);
        }
        
        
        
        // alter Code
        
        //GL11.glEnable(GL11.GL_DEPTH_TEST);
        //GL11.glShadeModel(GL11.GL_SMOOTH);
        //changeSize(width, height);
        //GL11.glDisable(GL11.GL_LIGHTING);

        // init OpenGL
        //GL11.glViewport(0, 0, width, height);
        //GL11.glMatrixMode(GL11.GL_PROJECTION);
        //GL11.glLoadIdentity();
        //GLU.gluPerspective((float) 65.0, (float) width / (float) height, (float) 0.1, 100);
        //GL11.glMatrixMode(GL11.GL_MODELVIEW);
        
    }

    //TODO
    // neu, unklar welchen code das ersetzen soll
    /**
     * Initializes the OpenGL state. Creating programs, VAOs and VBOs and sets 
     * appropriate state. 
     */
    public void initGL() {
        
        program = glCreateProgram();
        
        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexId, vertexSrc);
        glCompileShader(vertexId);
        if(glGetShaderi(vertexId, GL_COMPILE_STATUS) != GL_TRUE) {
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
        
        
        
        // scheint mir alles code zu sein, der etwas visualisiert. Das muss aber in die
        // SimpleLwJglRenderingEngine.
        //FIXME
        
        /*FloatBuffer fb = BufferUtils.createFloatBuffer(5 * (4 + BALL_N_VERTICES + 4 + 2));
        
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
        
        */
        
        
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
    }
    
    // neu/**
     
    
    /**
     * To be called when the visualizations's framebuffer is resized. Updates the projection
     * matrix.
     * 
     * TODO
     * brauche ich das? Verwendung für die Transformation von mouse-clicks
     * 
     * @param framebufferWidth The width of the new framebuffer
     * @param framebufferHeight  The height of the new framebuffer
     */
    public void onResize(int framebufferWidth, int framebufferHeight) {
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
    }
    
    
    /**
     * Sets the contents of the specified buffer to an orthographic projection matrix.
     * 
     * @param dest The buffer to set.
     * @param p The projection to use.
     */
    public static void setOrtho2D(FloatBuffer dest, Projection p) {
        float f1 = p.right - p.left;
        float f2 = p.top - p.bottom;
        dest.put(new float[]{
            2f / f1,                  0,                        0,  0,
            0,                        2f / f2,                  0,  0,
            0,                        0,                        -1, 0,
            -(p.right + p.left) / f1, -(p.top + p.bottom) / f2, 0,  1
        });
        dest.flip();
    }
    
    // neue
    // TODO
    // brauche ich das überhaupt
    /**
     * To be called when a play/pause toggle is requested.
     */
    public void onPlayPauseToggle() {
        switch(currentState) {
            case PLAYING: onPause(); break;
            case PAUSED: onPlay(); break;
            case LOST: break;
        }
    }
    
     /**
     * To be called when the game transitions from paused to playing.
     */
    public void onPlay() {
        currentState = State.PLAYING;
        setBackColour();
    }
    
    /**
     * To be called when the game transitions from playing to paused.
     */
    public void onPause() {
        currentState = State.PAUSED;
        setBackColour();
    }
    
    // alter Code
    @Override
    public void run() {
        startEngine();
        
        long start = System.currentTimeMillis();
        
        /*while (!Display.isCloseRequested()) {
            //System.out.println(System.currentTimeMillis()-start);
            //start = System.currentTimeMillis();
            
            if (rendering.isNewDataSetAvailable()) {
                if (list != -1) GL11.glDeleteLists(list, 1);
                list = GL11.glGenLists(1);
                GL11.glNewList(list, GL11.GL_COMPILE);
                draw(rendering.getDataSet(), rendering.getVisibleObjects(), rendering.getLoadedPointClouds());
                GL11.glEndList();
                changed = true;
            }
            
            
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the screen
            GL11.glLoadIdentity(); // apply camPos before rotation

            GL11.glTranslatef(0.0f, 0.0f, -5.0f);
            // draw
            GLU.gluLookAt(camPos.x, camPos.y, camPos.z, // Position
                    camPos.x + camDir.x, camPos.y + camDir.y, camPos.z + camDir.z, // Lookat
                    camUp.x, camUp.y, camUp.z);               // Up-direction
            // apply rotation
            GL11.glRotatef(camAngleX, 0, 1, 0); // window x axis rotates around up vector
            GL11.glRotatef(camAngleY, 1, 0, 0); // window y axis rotates around x

            //Render the scene
            if (list != -1) GL11.glCallList(list);

            // pollInput(); Tastatureingaben pollen und damit recording steuern
            Display.update();
            if (recorder != null) {
                if (changed || firstFrame) {
                    recorder.makeScreenshot();
                    changed = false;
                }
                firstFrame = false;
                Display.sync(25); // cap fps to 60fps
            }
            else 
                Display.sync(60); 
        }

        Display.destroy();*/
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

    /*
    
    alter code
    ich brauche vergleichbares um Recording zu implementieren
    
    */
    /**
     * Poll all inputs of the keyboard<br>
     * F3:Start recording
     * F4:Stop recording
     * ESC: Close window
     */
    /*private void pollInput() {
        
        if (Keyboard.isKeyDown(Keyboard.KEY_F3)) {
            //Start recording
            if (recorder == null) {
                recorder = new GIFRecorder();
                recorder.startRecording();
            }
        }
        
        if (Keyboard.isKeyDown(Keyboard.KEY_F4)) {
            //Stop recording
            if (recorder != null) {
                recorder.stopRecording();
                recorder = null;
            }
        }
        
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            if (recorder != null) 
                recorder.stopRecording();
            Display.destroy();
            System.exit(0);

        }

        int x = Mouse.getX();
        int y = Mouse.getY();

        for (int button = 0; button <= 2; button++) {
            if (Mouse.isButtonDown(button)) {
                if (!buttonDown[button]) {
                    mouseAction(button, STATE_DOWN, x, y);
                } else {
                    mouseMoved(x, y);
                }
                buttonDown[button] = true;
            } else {
                if (buttonDown[button]) {
                    mouseAction(button, STATE_UP, x, y);
                }
                buttonDown[button] = false;
            }
        }
    }*/

    /**
     * alter code
     * TODO
     * brauche ich den noch?
     * 
     * Changes the size of the lwjgl window
     * @param w The new width of the lwjgl window
     * @param h The new height of the lwjgl window
     */
    /*private void changeSize(float w, float h) {
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
        GLU.gluPerspective(45.0f, wRatio, (float) near, (float) far);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GLU.gluLookAt(camPos.x, camPos.y, camPos.z, // Position
                camPos.x + camDir.x, camPos.y + camDir.y, camPos.z + camDir.z, // Lookat
                camUp.x, camUp.y, camUp.z);               // Up-direction}
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

    // alt
    /**
     * Draws the concrete scene
     * @param clouds The point clouds
     * @param visibleObjects The visible point clouds
     */
    public abstract void draw(HashMap<String, PointCloud> clouds, HashSet<String> visibleObjects, PointClouds loadedClouds);
    
    
    // neu
    public static class CursorPos {
        double x, y;
    }
  
    // neu
    /**
     * A struct representing a framebuffer.
     */
    public static class Framebuffer {
        int width, height;
    }
    
     /**
     * Returns the current system time in milliseconds.
     * 
     * @return The current system time in milliseconds.
     */
    public static double currentTimeMillis() {
        return glfwGetTime() * 1000;
    }
    
    /**
     * Utility method which checks for an OpenGL error, throwing an exception if
     * one is found.
     */
    public static void checkError() {
        int err = glGetError();
        switch(err) {
            case GL_NO_ERROR: return;
            case GL_INVALID_OPERATION: throw new RuntimeException("Invalid Operation");
            case GL_INVALID_ENUM: throw new RuntimeException("Invalid Enum");
            case GL_INVALID_VALUE: throw new RuntimeException("Invalid Value");
            case GL_INVALID_FRAMEBUFFER_OPERATION: throw new RuntimeException("Invalid Framebuffer Operation");
            case GL_OUT_OF_MEMORY: throw new RuntimeException("Out of Memory");
        }
    }
     
    
    
    /**
     * An enum encompassing the game state.
     */
    public static enum State {
        
        PLAYING(BLACK), 
        PAUSED(ORANGE), 
        LOST(RED);

        private State(Colour backColour) {
            this.backColour = backColour;
        }
        
        /**
         * The back colour associated with this state.
         */
        final Colour backColour;
    }
   
     
    /**
     * A struct representing a colour.
     */
    public static class Colour {
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
    private State currentState = State.PLAYING;
    
    /**
     * Sets the appropriate back colour based on the game's current state.
     */
    public void setBackColour() {
        glClearColor(currentState.backColour.red, currentState.backColour.green, 
                currentState.backColour.blue, 0);
    }
    
    
    // wichtig herausgefunden
    //FIXME das Maximum irritiert, ist die impl überhaupt korrekt, bzw. equivalent zu lwjgl2?
    public static int getMaximumWidth(/*int hHint*/) {
        // lwjgl2
        // return Display.getWidth();

        //lwjgl3
        GLFWVidMode glfwGetVideoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return glfwGetVideoMode.width();
    }
    public static int getMaximumHeight(/*int hHint*/) {
        // lwjgl2
        // return Display.getWidth();

        //lwjgl3
        GLFWVidMode glfwGetVideoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return glfwGetVideoMode.height();
    }
    
}
