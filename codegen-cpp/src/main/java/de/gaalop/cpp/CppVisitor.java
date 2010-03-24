package de.gaalop.cpp;

import de.gaalop.cfg.*;
import de.gaalop.dfg.*;

import java.util.*;

/**
 * This visitor traverses the control and data flow graphs and generates C/C++ code.
 */
public class CppVisitor implements ControlFlowVisitor, ExpressionVisitor {

  private StringBuilder code = new StringBuilder();

  private ControlFlowGraph graph;

  // Maps the nodes that output variables to their result parameter names
  private Map<StoreResultNode, String> outputNamesMap = new IdentityHashMap<StoreResultNode, String>();

  private int indentation = 0;

  public String getCode() {
    return code.toString();
  }

  private void appendIndentation() {
    for (int i = 0; i < indentation; ++i) {
      code.append('\t');
    }
  }

  @Override
  public void visit(StartNode node) {
    graph = node.getGraph();

    code.append("void calculate(");

    // Input Parameters
    List<Variable> inputParameters = sortVariables(graph.getInputVariables());
    for (Variable var : inputParameters) {
      code.append("float "); // The assumption here is that they all are normal scalars
      code.append(var.getName());
      code.append(", ");
    }

    // Collect all output variables
    FindStoreOutputNodes findOutput = new FindStoreOutputNodes();
    graph.accept(findOutput);
    for (StoreResultNode var : findOutput.getNodes()) {
      code.append("float **");
      String outputName = var.getValue().getName() + "_out";
      code.append(outputName);
      code.append(", ");
      outputNamesMap.put(var, outputName);
    }

    if (!graph.getInputVariables().isEmpty() || !findOutput.getNodes().isEmpty()) {
      code.setLength(code.length() - 2);
    }

    code.append(") {\n");
    indentation++;

    // Declare local variables
    for (Variable var : graph.getLocalVariables()) {
      appendIndentation();
      code.append("float ");
      code.append(var.getName());
      code.append("[32];\n");
    }

    if (!graph.getLocalVariables().isEmpty()) {
      code.append("\n");
    }

    node.getSuccessor().accept(this);
  }

  /**
   * Sorts a set of variables by name to make the order deterministic.
   * 
   * @param inputVariables
   * @return
   */
  private List<Variable> sortVariables(Set<Variable> inputVariables) {
    List<Variable> variables = new ArrayList<Variable>(inputVariables);
    Comparator<Variable> comparator = new Comparator<Variable>() {

      @Override
      public int compare(Variable o1, Variable o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    };

    Collections.sort(variables, comparator);
    return variables;
  }

  @Override
  public void visit(AssignmentNode node) {
    appendIndentation();
    node.getVariable().accept(this);
    code.append(" = ");
    node.getValue().accept(this);
    code.append(";\n");

    node.getSuccessor().accept(this);
  }

  @Override
  public void visit(StoreResultNode node) {
    appendIndentation();
    code.append("memcpy(");
    code.append(outputNamesMap.get(node));
    code.append(", ");
    code.append(node.getValue().getName());
    code.append(", sizeof(");
    code.append(node.getValue().getName());
    code.append("));\n");

    node.getSuccessor().accept(this);
  }

  @Override
  public void visit(IfThenElseNode node) {
    appendIndentation();
    code.append("if (");
    node.getCondition().accept(this);
    code.append(") {\n");

    indentation++;
    node.getPositive().accept(this);
    indentation--;

    appendIndentation();
    code.append("}");

    if (node.getNegative() instanceof BlockEndNode) {
      code.append("\n");
    } else {
      code.append(" else {\n");
      
      indentation++;
      node.getNegative().accept(this);
      indentation--;
      
      appendIndentation();
      code.append("}\n");
    }
    
    node.getSuccessor().accept(this);
  }
  
  @Override
  public void visit(BlockEndNode node) {
    // nothing to do 
  }

  @Override
  public void visit(EndNode node) {
    indentation--;
    code.append("}\n");
  }

  private void addBinaryInfix(BinaryOperation op, String operator) {
    addChild(op, op.getLeft());
    code.append(operator);
    addChild(op, op.getRight());
  }

  private void addChild(Expression parent, Expression child) {
    if (OperatorPriority.hasLowerPriority(parent, child)) {
      code.append('(');
      child.accept(this);
      code.append(')');
    } else {
      child.accept(this);
    }
  }

  @Override
  public void visit(Subtraction subtraction) {
    addBinaryInfix(subtraction, " - ");
  }

  @Override
  public void visit(Addition addition) {
    addBinaryInfix(addition, " + ");
  }

  @Override
  public void visit(Division division) {
    addBinaryInfix(division, " / ");
  }

  @Override
  public void visit(InnerProduct innerProduct) {
    throw new UnsupportedOperationException("The C/C++ backend does not support the inner product.");
  }

  @Override
  public void visit(Multiplication multiplication) {
    addBinaryInfix(multiplication, " * ");
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
    code.append(variable.getName());
  }

  @Override
  public void visit(MultivectorComponent component) {
    code.append(component.getName());
    code.append('[');
    code.append(component.getBladeIndex());
    code.append(']');
  }

  @Override
  public void visit(Exponentiation exponentiation) {
    if (isSquare(exponentiation)) {
      Multiplication m = new Multiplication(exponentiation.getLeft(), exponentiation.getLeft());
      m.accept(this);
    } else {
      code.append("pow(");
      exponentiation.getLeft().accept(this);
      code.append(',');
      exponentiation.getRight().accept(this);
      code.append(')');
    }
  }

  private boolean isSquare(Exponentiation exponentiation) {
    final FloatConstant two = new FloatConstant(2.0f);
    return two.equals(exponentiation.getRight());
  }

  @Override
  public void visit(FloatConstant floatConstant) {
    code.append(Float.toString(floatConstant.getValue()));
    code.append('f');
  }

  @Override
  public void visit(OuterProduct outerProduct) {
    throw new UnsupportedOperationException("The C/C++ backend does not support the outer product.");
  }

  @Override
  public void visit(BaseVector baseVector) {
     throw new UnsupportedOperationException("The C/C++ backend does not support base vectors.");
  }

  @Override
  public void visit(Negation negation) {
    code.append('-');
    addChild(negation, negation.getOperand());
  }

  @Override
  public void visit(Reverse node) {
    throw new UnsupportedOperationException("The C/C++ backend does not support the reverse operation.");
  }

  @Override
  public void visit(LogicalOr node) {
    addBinaryInfix(node, " || ");
  }

  @Override
  public void visit(LogicalAnd node) {
    addBinaryInfix(node, " && ");
  }

  @Override
  public void visit(Equality node) {
    addBinaryInfix(node, " == ");
  }

  @Override
  public void visit(Inequality node) {
    addBinaryInfix(node, " != ");
  }

  @Override
  public void visit(Relation relation) {
    addBinaryInfix(relation, relation.getTypeString());
  }
}