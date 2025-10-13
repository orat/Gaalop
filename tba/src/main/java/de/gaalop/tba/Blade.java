package de.gaalop.tba;

import de.gaalop.algebra.TCBlade;
import de.gaalop.dfg.BaseVector;
import de.gaalop.dfg.Expression;
import de.gaalop.dfg.ExpressionFactory;
import de.gaalop.dfg.FloatConstant;
import de.gaalop.visitors.DFGTraversalVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
//import java.util.Vector;

/**
 * Defines a blade with a sign and a coefficient as a mutable sorted list of
 * basis blades.
 * @author Christian Steinmetz
 */
public class Blade {

    private List/*Vector*/<String> bases;

    public Blade() {
        bases = new ArrayList/*Vector*/<>();
    }

    public Blade(String[] bases) {
        this.bases = new ArrayList<>(Arrays.asList(bases));
    }

    /*public Blade(Vector<String> bases) {
        this.bases = bases;
    }*/
    // invoked from Parser.parseBlade(String parse)
    public Blade(List/*Vector*/<String> bases){
        this.bases = bases;
    }

    public Blade(TCBlade b) {
        // ImmutableList --> ArrayList bzw. besser hier alles auf MutableInt oder ImmutableInt umstellen
        //this.bases = new ArrayList/*Vector*/<String>(Arrays.asList(b.getBase()));
        this.bases = new ArrayList(b.getBase().toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String b : bases) {
            sb.append("^");
            sb.append(b);
        }
        if (!bases.isEmpty()) {
            return sb.substring(1);
        } else {
            return "1.0";
        }
    }

    /**
     * Adds a basis to this blade
     * @param toAdd The basis to be added
     * 
     * Who needs this. If it is not needed, maybe impl auf Blade can be defined
     * as a immutable sorted list.
     * FIXME
     */
    public void addBasis(String toAdd) {
        bases.add(toAdd);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Blade) {
            Blade comp = (Blade) obj;
            return bases.equals(comp.bases);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.bases != null ? this.bases.hashCode() : 0);
        return hash;
    }

    public List/*Vector*/<String> getBases() {
        return bases;
    }

    /**
     * Creates an expression of this blade
     * @return The expression
     */
    public Expression getExpression() {
        if (bases.size() >= 1) {
            Expression result = getBaseVector(bases.get(0));
            for (int i = 1; i < bases.size(); i++) {
                result = ExpressionFactory.wedge(result, getBaseVector(bases.get(i)));
            }
            return result;
        } else {
            throw new IllegalStateException("Blade: Blade contains no base element!");
        }
    }

    /**
     * Converts a string into either an BaseVector or FloatConstant
     * @param string The string
     * @return The converted string
     */
    private Expression getBaseVector(String string) {
        if (string.equals("1")) {
            return new FloatConstant(1);
        } else {
            return new BaseVector(string.substring(1));
        }
    }

    public static Blade createBladeFromExpression(Expression expr) {
        final LinkedList<String> list = new LinkedList<String>();
        DFGTraversalVisitor visitor = new DFGTraversalVisitor() {
            @Override
            public void visit(BaseVector node) {
                list.add(node.toString());
                super.visit(node);
            }
        };
        expr.accept(visitor);
        return new Blade(list.toArray(new String[0]));
    }
}
