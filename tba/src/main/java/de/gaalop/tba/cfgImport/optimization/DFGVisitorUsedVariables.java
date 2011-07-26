package de.gaalop.tba.cfgImport.optimization;

import de.gaalop.dfg.MultivectorComponent;
import de.gaalop.dfg.Variable;
import java.util.LinkedList;

/**
 * Visitor for DFG tree to get all variables in an expression
 * @author christian
 */
public class DFGVisitorUsedVariables extends DFGTraversalVisitor {

    private LinkedList<VariableComponent> variables = new LinkedList<VariableComponent>();

    public LinkedList<VariableComponent> getVariables() {
        return variables;
    }

    @Override
    public void visit(Variable node) {
        variables.add(new VariableComponent(node.getName(), 0,node));
    }

    @Override
    public void visit(MultivectorComponent node) {
        variables.add(new VariableComponent(node.getName(), node.getBladeIndex(),node));
    }

}
