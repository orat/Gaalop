package de.gaalop.gui;

/**
 * @author Christian Steinmetz
 */
public class AlgebraChooserItem {

    public boolean ressource; // false == user defined
    private boolean hasDimension = false;
    public String algebraName;
    public String showString;
    private String signatureString;
    
    //record ChoosenAlgebra(boolean ressource, String algebraName, int dimension){};
    
    AlgebraChooserItem(boolean ressource, String algebraName, 
                       boolean hasDimension, String showString) {
        this.ressource = ressource;
        this.algebraName = algebraName;
        this.hasDimension = hasDimension;
        this.showString = showString;
    }

    public void setSignature(String signatureString){
        this.signatureString = signatureString;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (this.ressource ? 1 : 0);
        hash = 19 * hash + (this.algebraName != null ? this.algebraName.hashCode() : 0);
        hash = 19 * hash + (this.showString != null ? this.showString.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AlgebraChooserItem other = (AlgebraChooserItem) obj;
        if (this.ressource != other.ressource) {
            return false;
        }
        if ((this.algebraName == null) ? (other.algebraName != null) : !this.algebraName.equals(other.algebraName)) {
            return false;
        }
        if ((this.showString == null) ? (other.showString != null) : !this.showString.equals(other.showString)) {
            return false;
        }
        return true;
    }
   
    @Override
    public String toString() {
        if (hasDimension && signatureString != null && signatureString.length()>0){
            return showString + " "+signatureString;
        } else {
            return showString;
        }
    }
}
