package de.gaalop.algebra;

/**
 * Stores a defined algebra by ID and name
 * @author Christian Steinmetz
 */
public class DefinedAlgebra {
    
    public String id;
    public String name;
    
    // used for generic algebra def w.g. qca, qra, qba
    public int minDimension = -1;
    public int maxDimension = -1;

    public DefinedAlgebra(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public DefinedAlgebra(String id, String name, int minDim, int maxDim) {
        this.id = id;
        this.name = name;
        this.minDimension = minDim;
        this.maxDimension = maxDim;
    }
    
    public boolean definesDimensions(){
        if ((minDimension > 0) && (maxDimension > minDimension)) return true;
        return false;
    }
}
