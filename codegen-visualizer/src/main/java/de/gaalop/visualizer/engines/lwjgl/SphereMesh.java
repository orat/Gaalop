package de.gaalop.visualizer.engines.lwjgl;

import java.util.ArrayList;
import java.util.List;

/**
 * Alternative
 * 
 * glPointSize(10.0f);
 * glDrawArrays(GL_POINTS, 0, 1);
 * 
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */

public class SphereMesh {
    
    public static float[] createVertices(float radius, int stacks, int slices) {
        List<Float> verts = new ArrayList<>();

        for (int i = 0; i <= stacks; i++) {
            double lat = Math.PI * (-0.5 + (double) i / stacks);
            double sinLat = Math.sin(lat);
            double cosLat = Math.cos(lat);

            for (int j = 0; j <= slices; j++) {
                double lon = 2 * Math.PI * (double) j / slices;
                double sinLon = Math.sin(lon);
                double cosLon = Math.cos(lon);

                float x = (float) (cosLon * cosLat);
                float y = (float) sinLat;
                float z = (float) (sinLon * cosLat);

                verts.add(radius * x);
                verts.add(radius * y);
                verts.add(radius * z);
            }
        }

        float[] vertices = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) {
            vertices[i] = verts.get(i);
        }
        return vertices;
    }

    public static int[] createIndices(int stacks, int slices) {
        List<Integer> inds = new ArrayList<>();

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = (i * (slices + 1)) + j;
                int second = first + slices + 1;

                inds.add(first);
                inds.add(second);
                inds.add(first + 1);

                inds.add(second);
                inds.add(second + 1);
                inds.add(first + 1);
            }
        }

        return inds.stream().mapToInt(i -> i).toArray();
    }
    
    /*float[] vertices = SphereMesh.createVertices(1.0f, 32, 32);
int[] indices   = SphereMesh.createIndices(32, 32);

// Upload vertices to a VBO
int vao = glGenVertexArrays();
glBindVertexArray(vao);

int vbo = glGenBuffers();
glBindBuffer(GL_ARRAY_BUFFER, vbo);
glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

glEnableVertexAttribArray(0);
glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

// Upload indices
int ebo = glGenBuffers();
glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
*/
  
}
