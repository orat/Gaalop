/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.gaalop.visualizer.engines.lwjgl.graph;

import org.joml.Vector3f;

/**
 *
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class MeshData {

    private Vector3f aabbMax;
    private Vector3f aabbMin;
    private float[] bitangents;
    private int[] boneIndices;
    private int[] indices;
    private int materialIdx;
    private float[] normals;
    private float[] positions;
    private float[] tangents;
    private float[] textCoords;
    private float[] weights;

    public MeshData(float[] positions, float[] normals, float[] tangents, float[] bitangents,
                    float[] textCoords, int[] indices, int[] boneIndices, float[] weights,
                    Vector3f aabbMin, Vector3f aabbMax) {
        materialIdx = 0;
        this.positions = positions;
        this.normals = normals;
        this.tangents = tangents;
        this.bitangents = bitangents;
        this.textCoords = textCoords;
        this.indices = indices;
        this.boneIndices = boneIndices;
        this.weights = weights;
        this.aabbMin = aabbMin;
        this.aabbMax = aabbMax;
    }

    public Vector3f getAabbMax() {
        return aabbMax;
    }

    public Vector3f getAabbMin() {
        return aabbMin;
    }

    public float[] getBitangents() {
        return bitangents;
    }

    public int[] getBoneIndices() {
        return boneIndices;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getMaterialIdx() {
        return materialIdx;
    }

    public float[] getNormals() {
        return normals;
    }

    public float[] getPositions() {
        return positions;
    }

    public float[] getTangents() {
        return tangents;
    }

    public float[] getTextCoords() {
        return textCoords;
    }

    public float[] getWeights() {
        return weights;
    }

    public void setMaterialIdx(int materialIdx) {
        this.materialIdx = materialIdx;
    }
}