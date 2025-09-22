package de.gaalop.visualizer.engines.lwjgl;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * @author Christian Steinmetz
 */
public abstract class RenderingEngine /*extends Thread*/ {

    public float pointSize = 0.5f;
    
    //public abstract int getWidth();
    //public abstract int getHeight();
    
    
    // Visualization of coordinate axes
    
    static float[] axes_vertices = {
        // X axis (red)
        0f, 0f, 0f,   1f, 0f, 0f,   // start
        1f, 0f, 0f,   1f, 0f, 0f,   // end

        // Y axis (green)
        0f, 0f, 0f,   0f, 1f, 0f,   // start
        0f, 1f, 0f,   0f, 1f, 0f,   // end

        // Z axis (blue)
        0f, 0f, 0f,   0f, 0f, 1f,   // start
        0f, 0f, 1f,   0f, 0f, 1f    // end
    };
    
    static String axes_vertex_shader = 
        "#version 330 core\n" + // core is default, without core it is the same
        "layout(location = 0) in vec3 aPos;\n" +
        "layout(location = 1) in vec3 aColor;\n" +
        "\n" +
        "out vec3 ourColor;\n" +
        "\n" +
        "uniform mat4 projection;\n" +
        "uniform mat4 view;\n" +
        "uniform mat4 model;\n" +
        "\n" +
        "void main() {\n" +
        "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
        "    ourColor = aColor;\n" +
        "}";
    static String axes_fragment_shader =
            "#version 330 core\n"+
            "in vec3 ourColor;\n"+
            "out vec4 FragColor;\n"+
            "\""+
            "void main() {\n"+
            "    FragColor = vec4(ourColor, 1.0);\n"+
            "}";
    
    // Create a vertex array object (vao) for visualization of the axes
    int createAxesVAO(){
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Upload vertex data
        glBufferData(GL_ARRAY_BUFFER, axes_vertices, GL_STATIC_DRAW);

        // Position attribute (location=0 in shader)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Color attribute (location=1 in shader)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        return vao;
    }
    
    // generic shader creation 
    static int createShaderProgram(String vertexSrc, String fragmentSrc) {
        int vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderId, vertexSrc);
        glCompileShader(vertexShaderId);
        if (glGetShaderi(vertexShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(glGetShaderInfoLog(vertexShaderId, Integer.MAX_VALUE));  
            throw new IllegalStateException("Vertex shader compile error: " + glGetShaderInfoLog(vertexShaderId));
        }

        int fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderId, fragmentSrc);
        glCompileShader(fragmentShaderId);
        if (glGetShaderi(fragmentShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(glGetShaderInfoLog(fragmentShaderId, Integer.MAX_VALUE));
            throw new IllegalStateException("Fragment shader compile error: " + glGetShaderInfoLog(fragmentShaderId));
        }

        int program = glCreateProgram();
        glAttachShader(program, vertexShaderId);
        glAttachShader(program, fragmentShaderId);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.out.println(glGetProgramInfoLog(program, Integer.MAX_VALUE));
            throw new RuntimeException("Program link error: " + glGetProgramInfoLog(program));
        }

        // Shaders no longer needed after linking
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);

        return program;
    }
    
    abstract public void start();

}
