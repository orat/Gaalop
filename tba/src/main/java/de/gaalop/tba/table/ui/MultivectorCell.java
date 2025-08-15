package de.gaalop.tba.table.ui;

import de.gaalop.tba.Algebra;
import de.gaalop.tba.BladeRef;
import de.gaalop.tba.Multivector;
import java.util.List;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class MultivectorCell {
     
    private final Multivector m;
    private final Algebra algebra;
    
    public MultivectorCell(Algebra algebra, Multivector m){
        this.m = m;
        this.algebra = algebra;
    }
    
    public int getGrade(){
        // the assumption is that all blades has the same grade
        List<BladeRef> blades = m.getBlades();
        if (!blades.isEmpty()){
            return getGrade(m.getBlades().getFirst().getIndex());
        } else {
            return -1;
        }
    }
    private int getGrade(int bladeIndex) {
        return algebra.getBlade(bladeIndex).getBases().size();
    }
    
    public boolean isNegative(){
        boolean result = false;
        if (m.getBlades().size() == 1){
            if (m.getBlades().getFirst().getPrefactor() == -1) result = true;
        }
        return result;
    }
    
    public String toString(String[] basisBladeNames){
        StringBuilder sb = new StringBuilder();
        for (BladeRef blade: m.getBlades()){
            sb.append(algebra.getBlade(blade.getIndex()).toString());
            if (isNegative()){
                sb.insert(0, "-");
            }
            return sb.toString();
        }
        return "";
    }
}