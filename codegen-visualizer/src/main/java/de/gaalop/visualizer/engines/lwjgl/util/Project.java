package de.gaalop.visualizer.engines.lwjgl.util;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL15.glMultMatrixf;
        


public class Project {
   
    private static final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    
    private static final float[] forward = new float[3];
    private static final float[] side = new float[3];
    private static final float[] up = new float[3];
    
    private static final float[] IDENTITY_MATRIX =
            new float[] {
                    1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f };

    /**
     * Make matrix an identity matrix
     */
    private static void __gluMakeIdentityf(FloatBuffer m) {
           int oldPos = m.position();
           m.put(IDENTITY_MATRIX);
           m.position(oldPos);
    }
    
    /**
     * Normalize vector
     *
     * @param v
     *
     * @return float[]
     */
    protected static float[] normalize(float[] v) {
           float r;

           r = (float)Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
           if ( r == 0.0 )
                   return v;

           r = 1.0f / r;

           v[0] *= r;
           v[1] *= r;
           v[2] *= r;

           return v;
    }
    
    /**
     * Calculate cross-product
     *
     * @param v1
     * @param v2
     * @param result
     */
    protected static void cross(float[] v1, float[] v2, float[] result) {
            result[0] = v1[1] * v2[2] - v1[2] * v2[1];
            result[1] = v1[2] * v2[0] - v1[0] * v2[2];
            result[2] = v1[0] * v2[1] - v1[1] * v2[0];
    }
    
    
    /**
     * Method gluLookAt
     *
     * @param eyex
     * @param eyey
     * @param eyez
     * @param centerx
     * @param centery
     * @param centerz
     * @param upx
     * @param upy
     * @param upz
     */
    public static void gluLookAt(
        float eyex,
        float eyey,
        float eyez,
        float centerx,
        float centery,
        float centerz,
        float upx,
        float upy,
        float upz) {
        float[] forward = Project.forward;
        float[] side = Project.side;
        float[] up = Project.up;

        forward[0] = centerx - eyex;
        forward[1] = centery - eyey;
        forward[2] = centerz - eyez;

        up[0] = upx;
        up[1] = upy;
        up[2] = upz;

        normalize(forward);

        /* Side = forward x up */
        cross(forward, up, side);
        normalize(side);

        /* Recompute up as: up = side x forward */
        cross(side, forward, up);

        __gluMakeIdentityf(matrix);
        matrix.put(0 * 4 + 0, side[0]);
        matrix.put(1 * 4 + 0, side[1]);
        matrix.put(2 * 4 + 0, side[2]);

        matrix.put(0 * 4 + 1, up[0]);
        matrix.put(1 * 4 + 1, up[1]);
        matrix.put(2 * 4 + 1, up[2]);

        matrix.put(0 * 4 + 2, -forward[0]);
        matrix.put(1 * 4 + 2, -forward[1]);
        matrix.put(2 * 4 + 2, -forward[2]);

        //FIXME
        // Methode ist erst ab org.lwjgl.opengl.GL15.*; verfügar
        glMultMatrixf(matrix);
        glTranslatef(-eyex, -eyey, -eyez);
    }

}
