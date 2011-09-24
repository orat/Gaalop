package de.gaalop.tba.cfgImport;

import de.gaalop.cfg.AssignmentNode;
import de.gaalop.cfg.EmptyControlFlowVisitor;
import de.gaalop.dfg.BinaryOperation;
import de.gaalop.dfg.Expression;
import de.gaalop.dfg.ExpressionVisitor;
import de.gaalop.dfg.MathFunctionCall;
import de.gaalop.dfg.UnaryOperation;
import de.gaalop.dfg.Variable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author Christian Steinmetz
 */
public class MathFunctionSeparator extends EmptyControlFlowVisitor {
// Extract MathFunctions so that the operand has an own temporary multivector
            // and the result is an own Multivector

    private Expression resultValue = null;

    private int tempCounter = -1;

    private Variable getNewTemporaryVariable() {
        tempCounter++;
        return new Variable("temp"+tempCounter);
    }

    private HashMap<Variable, Expression> toInsert = new HashMap<Variable, Expression>();
    private LinkedList<Variable> toInsertVars = new LinkedList<Variable>();
    
    private ExpressionVisitor expressionVisitor = new ExpressionTypeVisitor() {

        @Override
        protected void visitBinaryOperation(BinaryOperation node) {
            resultValue = null;
            node.getLeft().accept(this);
            if (resultValue != null) {
                node.setLeft(resultValue);
                resultValue = null;
            }
            node.getRight().accept(this);
            if (resultValue != null) {
                node.setRight(resultValue);
                resultValue = null;
            }

        }

        @Override
        protected void visitUnaryOperation(UnaryOperation node) {
            resultValue = null;
            node.getOperand().accept(this);
            if (resultValue != null) {
                node.setOperand(resultValue);
                resultValue = null;
            }
        }

        @Override
        protected void visitTerminal(Expression node) {
        }

        @Override
        public void visit(MathFunctionCall node) {
            resultValue = null;

            node.getOperand().accept(this);
            if (resultValue != null) {
                node.setOperand(resultValue);
                resultValue = null;
            } else {

                Variable var = getNewTemporaryVariable();
                toInsert.put(var, node.getOperand());
                toInsertVars.add(var);
                node.setOperand(var);
            }

            Variable var2 = getNewTemporaryVariable();
            toInsert.put(var2, node);
            toInsertVars.add(var2);
            resultValue = var2;
        }
        
    };

    @Override
    public void visit(AssignmentNode node) {

        node.getValue().accept(expressionVisitor);
        if (resultValue != null) {
            node.setValue(resultValue);
            resultValue = null;
        }
        for (Variable var: toInsertVars) {
            AssignmentNode newNode = new AssignmentNode(node.getGraph(), var, toInsert.get(var));
            node.insertBefore(newNode);
        }
        toInsertVars.clear();
        toInsert.clear();

        super.visit(node);
    }




}
