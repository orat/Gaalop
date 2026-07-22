package de.gaalop.gui;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class ChoosenAlgebra {
    
    boolean ressource;
    String algebraName;
    int dimension;
    
    public ChoosenAlgebra(boolean ressource, String algebraName, int dimension){
        this.ressource = ressource;
        this.algebraName = algebraName;
        this.dimension = dimension;
    };
}
