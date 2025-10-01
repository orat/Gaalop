package de.gaalop.productComputer;

import java.util.Arrays;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.FastList;

/**
 * Blade represented by an array of strings
 * @author Christian Steinmetz
 */
public class BladeStr {

    private float prefactor;
    //private String[] baseVectors;
    private ImmutableList<String> baseVectors;

    public BladeStr(String baseVector) {
        this.prefactor = 1;
        //this.baseVectors = new String[]{baseVector};
        this.baseVectors = FastList.newListWith(baseVector).toImmutableList();
    }

    public BladeStr(/*String[]*/ImmutableList<String> baseVectors) {
        this.prefactor = 1;
        this.baseVectors = baseVectors;
    }

    public BladeStr(float prefactor, String baseVector) {
        this.prefactor = prefactor;
        //this.baseVectors = new String[]{baseVector};
        this.baseVectors = FastList.newListWith(baseVector).toImmutableList();
    }

    public BladeStr(float prefactor, ImmutableList<String>/*String[]*/ baseVectors) {
        this.prefactor = prefactor;
        this.baseVectors = baseVectors;
    }

    public /*String[]*/ImmutableList<String> getBaseVectors() {
        return baseVectors;
    }

    public float getPrefactor() {
        return prefactor;
    }

    public void setBaseVectors(/*String[]*/ImmutableList<String> baseVectors) {
        this.baseVectors = baseVectors;
    }

    public void setPrefactor(float prefactor) {
        this.prefactor = prefactor;
    }

    //TODO wer braucht das? muss das genauso implementiert werden wie das früher war?
    @Override
    public String toString() {
        //return prefactor+Arrays.toString(baseVectors); --> [1, 2, 3, 4]
        
        //TODO
        // vgl. mit test in ConformalGeometricAlgebra
        // vgl. mit baseVectors.toString()
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefactor);
        sb.append("[");
        //TODO durch foreach ersetzen mit von, bis als Argumente
        for (int i=0;i<baseVectors.size()-2;i++){
            sb.append(baseVectors.get(i));
            sb.append(", ");
        }
        sb.append(baseVectors.get(baseVectors.size()-1));
        sb.append("]");
        return sb.toString();
    }

}
