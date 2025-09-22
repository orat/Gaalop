package de.gaalop.visualizer.engines.lwjgl;

import de.gaalop.visualizer.Point3d;
import de.gaalop.visualizer.PointCloud;
import de.gaalop.visualizer.PointClouds;
import de.gaalop.visualizer.Rendering;
import de.gaalop.visualizer.engines.lwjgl.scene.Window;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
//import org.lwjgl.util.glu.Sphere;

/**
 * Implements a simple drawing of the points of a point cloud.
 * @author Christian Steinmetz
 * 
 * example:
 * https://stackoverflow.com/questions/29113631/lwjgl-3-wont-draw-a-line
 * rendering of lines
 * 
 * https://gamedev.stackexchange.com/questions/100860/drawing-fonts-with-lwjgl3-with-opengl
 * rendering fonts
 * 
 * https://github.com/AlfonsoLRz/PointCloudRendering
 * rendering point clouds
 * 
 * To create a surface from points in OpenGL 4, you typically use a Tessellation 
 * Shader for dynamic subdivision or a Geometry Shader to generate surface 
 * primitives from points, which are then processed by a Fragment Shader to 
 * color and define the surface. You define points in a Vertex Buffer Object 
 * (VBO), link them in a Vertex Array Object (VAO), and then send these to the 
 * GPU, where the Tessellation or Geometry shaders will construct the surface 
 * before the Fragment Shader renders its final color.
 * 
 * https://learnopengl.com/Guest-Articles/2021/Tessellation/Tessellation
 * 
 */
public class LwJglPointCloudRenderingEngine extends LwJgl3RenderingEngine {
    
    public LwJglPointCloudRenderingEngine(/*String lwJglNativePath, */Rendering rendering,
            String windowTitle, Window.WindowOptions opts, IAppLogic appLogic) {
        super(/*lwJglNativePath, */rendering, windowTitle, opts, appLogic);
    }

    @Override
    public void draw(Map<String, PointCloud> clouds, Set<String> visibleObjects, 
            PointClouds loadedClouds) {
    
        if (clouds == null) return;

        //draw axes

        // In LWJGL 3, you can’t use the old fixed-function OpenGL pipeline 
        // (glBegin/glEnd) anymore, because OpenGL 3.2+ core profile removed 
        // immediate mode rendering.
        /*GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4d(1,0,0,0);//Red
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(1, 0, 0);
        GL11.glColor4d(0,1,0,0);//Green
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 1, 0);
        GL11.glColor4d(0,0,1,0);//Blue
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 0, 1);
        GL11.glEnd();*/

        //  Instead, you have to use Vertex Buffer Objects (VBOs), Vertex 
        // Array Objects (VAOs), and a shader program to draw your geometry.
        renderAxes();

        for (String cloud: clouds.keySet()) 
            if (visibleObjects.contains(cloud)){
                 paintPointCloud(clouds.get(cloud));
            }

        for (PointCloud cloud: loadedClouds.values())
            paintPointCloud(cloud);

    }

    private void paintPointCloud(PointCloud pointCloud) {
        //Sphere s = new Sphere();
        
        // TODO test
        //SphereMesh s = new SphereMesh();
        
        //Use the color
        GL11.glColor4d(pointCloud.color.getRed()/255.0d, pointCloud.color.getGreen()/255.0d, 
                       pointCloud.color.getBlue()/255.0d, pointCloud.color.getAlpha()/255.0d);

        //
        glPointSize(0.04f);
         
        for (Point3d p: pointCloud.points) {
            GL11.glPushMatrix();
            GL11.glTranslated(p.x,p.y,p.z);
            
            // gibts immer noch!!!
            glDrawArrays(GL_POINTS, 0, 1);
 
            //s.draw(0.04f, 3, 3);
            GL11.glPopMatrix();
        }
    }
}
