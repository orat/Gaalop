package de.gaalop.visualizer.engines.lwjgl.util;

import static org.lwjgl.opengl.GL11.glFrustum;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class GLU {
    
    //WORKAROUND to substitute GLU.gluPerspective() for upgrade to lwjgl3
    /**
     * There is no direct equivalent in LWJGL3. GLU was removed from LWJGL since 
     * it is designed to work with deprecated features in OpenGL (such as the fixed 
     * function pipeline you are using).
     *
     * TODO
     * However, if you have JOML which optionally comes packaged with LWJGL3 then 
     * you can create an appropriate perspective matrix Java side and upload it 
     * to OpenGL with glLoadMatrix(). The final code snippet of the section linked 
     * here: https://github.com/JOML-CI/JOML#using-with-lwjgl, does almost exactly 
     * what you want (it also loads a model-view matrix equivalent to a gluLookAt()
     * call).
     * 
     * Hier ist eine weitere Implementierung:
     * https://github.com/LWJGL/lwjgl/blob/master/src/java/org/lwjgl/util/glu/Project.java
     */
    /**
     * Method gluPerspective
     * @param fovy
     * @param aspect
     * @param zNear
     * @param zFar
     */
    public static void gluPerspective(float fovy, float aspect, float near, float far) {
        float bottom = -near * (float) Math.tan(fovy / 2);
        float top = -bottom;
        float left = aspect * bottom;
        float right = -left;
        glFrustum(left, right, bottom, top, near, far);
    }
    
    /**
     * Method gluLookAt
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

        Project.gluLookAt(eyex, eyey, eyez, centerx, centery, centerz, upx, upy, upz);
    }
}
