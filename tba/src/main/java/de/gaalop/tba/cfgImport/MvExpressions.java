package de.gaalop.tba.cfgImport;

import de.gaalop.dfg.Expression;
import java.util.Collection;
import java.util.function.BiFunction;
//import java.util.TreeMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Stores blade expressions
 * @author Adrian Kiesthardt, adapted to using TreeMap by Christian Steinmetz,
 * adapted to use Eclipse-Collections by Oliver Rettig
 *
 * This class stores expressions in a data structure mapping bladeIndices (int) to Expressions.
 * May yield memory benefits if multivectors are sparse.
 */
public class MvExpressions {

    private String nameMv;
    private int bladeCount;
    
    //public TreeMap<Integer, Expression> bladeExpressions;   //Stores non-null blade expressions using their bladeIndex as key
    private MutableIntObjectMap<Expression> bladeExpressions; // Stores non-null blade expressions using their bladeIndex as key
            
    public MvExpressions(String nameMv, int bladeCount) {
        this.nameMv = nameMv;
        this.bladeCount = bladeCount;
        bladeExpressions = IntObjectHashMap.newMap(); //new TreeMap<>();
    }

    public MutableIntObjectMap<Expression> getBladeExpressions(){
        return bladeExpressions;
    }
    
    // WORKAROUND substitution of merge in Map<Integer, Expression>
    // inspired by https://nurkiewicz.com/2019/03/mapmerge-one-method-to-rule-them-all.html
    public Expression merge(int key, Expression value, 
            BiFunction<Expression, Expression, Expression> remappingFunction) {
        Expression oldValue = bladeExpressions.get(key);
        Expression newValue = (oldValue == null) ? value :
                   remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            bladeExpressions.remove(key);
        } else {
            bladeExpressions.put(key, newValue);
        }
        return newValue;
    }
    
    
    /**
     * @param bladeIndex bladeIndex of the expression to get
     * @return expression with given index, if found. Null otherwise
     */
    public Expression getExpression(int bladeIndex) {
        return bladeExpressions.get(bladeIndex);
    }

    /**
     *
     * @param bladeIndex The index to set the expression for
     * @param expression expression to set
     *
     */
    public void setExpression(int bladeIndex, Expression expression) {
        if (expression == null)
            bladeExpressions.remove(bladeIndex);    // We would like store only non-null blades -> We remove the blade from the map
        else
            bladeExpressions.put(bladeIndex, expression);
    }

    public Collection<Expression> getAllExpressions() {
        return this.bladeExpressions.values();
    }
}
