package de.gaalop.latex;

import de.gaalop.DefaultCodeGeneratorVisitor;
import de.gaalop.cfg.*;
import de.gaalop.dfg.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static java.lang.Double.compare;

/**
 * TODO
 * - sin(theta1) --> wird nicht zu theta\_1 warum?
 * - Argumente theta_1, ... irgendwie identifizieren, brauche ich für maxima output
 */

/**
 * This class implements the CFG and DFG visitor that generate LaTeX code.
 */
public class LatexVisitor extends DefaultCodeGeneratorVisitor {

    // + einmal/mehrmals
    // \w alphanumerisches Zeichen oder Unterstrich
    // \d Ziffer
    private Pattern INDEXED_NAME = Pattern.compile("^(\\w+)(\\d+)$");

    //TODO
    private Pattern THETA_NAME = Pattern.compile("theta");
    private Pattern ALPHA_NAME = Pattern.compile("alpha");
    
    private final static FloatConstant HALF = new FloatConstant(0.5f);

    private final static Negation MINUS_HALF = new Negation(HALF);

    private final static Division ONE_HALF = new Division(new FloatConstant(1.0f), new FloatConstant(2.0f));

    private final static Negation MINUS_ONE_HALF = new Negation(ONE_HALF);

    @Override
    public void visit(StartNode node) {
        graph = node.getGraph();
        // usepackage{breqn}
        code.append("\\allowdisplaybreaks\n");
        code.append("\\begin{dgroup*}\n");
        //code.append("\\begin{align*}\n");
        
        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(AssignmentNode node) {
        code.append("\\begin{dmath*}\n");
        node.getVariable().accept(this);
        code.append("= ");
        node.getValue().accept(this);
        
        if (node.getVariable() instanceof MultivectorComponent) {
            //code.append(" \\\\ ");
            code.append(" % ");
            code.append(graph.getBladeString((MultivectorComponent) 
                    (node.getVariable())));
                    //(node.getVariable())).replaceAll("\\^", "\\\\wedge"));
            code.append(" ");
        }
        code.append("\n");
        //code.append("\\\\\n");
        //code.append("\n");
        code.append("\\end{dmath*}\n");
        
        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(ExpressionStatement node) {
        node.getExpression().accept(this);
        code.append("\\\\\n");
        //code.append("\n");
        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(StoreResultNode node) {
        /*
        code.append('?');
        code.append(node.getValue());
        code.append("\\\\\n");
        */

        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(IfThenElseNode node) {
        code.append("\\text{IF } (");
        node.getCondition().accept(this);
        code.append(") \\\\\n");
        //code.append(") \n");
        node.getPositive().accept(this);
        if (!(node.getNegative() instanceof BlockEndNode)) {
            code.append("\\text{ELSE} \\\\\n");
            //code.append("\\text{ELSE} \n");
            node.getNegative().accept(this);
        }
        code.append("\\text{END IF} \\\\\n");
        //code.append("\\text{END IF} \n");
        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(LoopNode node) {
        code.append("\\text{LOOP} \\\\\n");
        //code.append("\\text{LOOP} \n");
        node.getBody().accept(this);
        code.append("\\text{END LOOP} \\\\\n");
        //code.append("\\text{END LOOP} \n");
        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(BreakNode breakNode) {
        code.append("\\text{break}\\\\");
        breakNode.getSuccessor().accept(this);
    }

    @Override
    public void visit(EndNode node) {
        //code.append("\\end{align*}\n");
        code.append("\\end{dgroup*}\n");
    }

    @Override
    public void visit(Subtraction subtraction) {
        addBinaryInfix(subtraction, "-");
    }

    @Override
    public void visit(Addition addition) {
        addBinaryInfix(addition, "+");
    }

    @Override
    public void visit(Division division) {
        code.append("\\cfrac{");
        division.getLeft().accept(this);
        code.append("}{");
        division.getRight().accept(this);
        code.append("}");
    }

    @Override
    public void visit(InnerProduct innerProduct) {
        addBinaryInfix(innerProduct, "\\cdot");
    }

    @Override
    public void visit(Multiplication multiplication) {
        if (multiplication.getLeft().equals(HALF)) {
            ONE_HALF.accept(this);
            multiplication.getRight().accept(this);
        } else if (multiplication.getRight().equals(HALF)) {
            ONE_HALF.accept(this);
            multiplication.getLeft().accept(this);
        } else if (multiplication.getLeft().equals(MINUS_HALF)) {
            MINUS_ONE_HALF.accept(this);
            multiplication.getRight().accept(this);
        } else if (multiplication.getRight().equals(MINUS_HALF)) {
            MINUS_ONE_HALF.accept(this);
            multiplication.getLeft().accept(this);
        } else {
            addBinaryInfix(multiplication, "*");
        }
    }

    @Override
    public void visit(MathFunctionCall mathFunctionCall) {
        code.append(mathFunctionCall.getFunction().toString());
        code.append('(');
        mathFunctionCall.getOperand().accept(this);
        code.append(')');
    }

    @Override
    public void visit(Variable variable) {
        String name = variable.getName();
        code.append(name.replace("_", "\\_"));
    }

    // variable
    private void addIdentifier(String name) {
        // result: z_{1}_{3}&= 1 // e3 \\
        Matcher matcher = INDEXED_NAME.matcher(name);
        if (matcher.matches()) {
            //code.append(matcher.group(1).replace("_", "\\_"));
            code.append(matcher.group(1).replace("_", "^"));
            code.append("{");
            code.append(matcher.group(2));
            code.append("}");
        } else {
            code.append(name.replace("_", "\\_"));
        }
    }

    // add component index as subscription index
    @Override
    public void visit(MultivectorComponent component) {
        addIdentifier(component.getName().replace("_opt", ""));
        code.append("_{");
        //code.append("^{");
        code.append(component.getBladeIndex());
        code.append('}');
    }

    @Override
    public void visit(Exponentiation exponentiation) {
        exponentiation.getLeft().accept(this);
        code.append("^{");
        exponentiation.getRight().accept(this);
        code.append('}');
    }

    @Override
    public void visit(FloatConstant floatConstant) {
        if (Double.isNaN(floatConstant.getValue())) {
            code.append("undefined");
        } else if (compare(floatConstant.getValue(), Math.floor(floatConstant.getValue())) == 0) {
            code.append((int) floatConstant.getValue());
        } else {
            code.append(Double.toString(floatConstant.getValue()));
        }
    }

    @Override
    public void visit(OuterProduct outerProduct) {
        addBinaryInfix(outerProduct, "\\wedge");
    }

    //TODO 
    @Override
    public void visit(BaseVector baseVector) {
        String name = baseVector.getBaseName();
        //if (name.matches("[a-zA-Z]?.?")){
            code.append(name);
            code.append("_{");
            code.append(baseVector.getIndexName());
            code.append(')');
        //} else {}
    }

    @Override
    public void visit(Negation negation) {
        code.append('-');
        negation.getOperand().accept(this);
    }

    @Override
    public void visit(Reverse node) {
        code.append('~');
        node.getOperand().accept(this);
    }

    @Override
    public void visit(LogicalOr node) {
        addBinaryInfix(node, " \\vee ");
    }

    @Override
    public void visit(LogicalAnd node) {
        addBinaryInfix(node, " \\wedge ");
    }

    @Override
    public void visit(LogicalNegation node) {
        code.append("!");
        node.getOperand().accept(this);
    }

    @Override
    public void visit(Equality node) {
        addBinaryInfix(node, " == ");
    }

    @Override
    public void visit(Inequality node) {
        addBinaryInfix(node, " \neq ");
    }

    @Override
    public void visit(Relation relation) {
        addBinaryInfix(relation, relation.getTypeString());
    }

    @Override
    public void visit(ColorNode node) {
        node.getSuccessor().accept(this);
    }
}