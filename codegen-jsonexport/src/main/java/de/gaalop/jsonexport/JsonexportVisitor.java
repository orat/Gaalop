package de.gaalop.jsonexport;

import de.gaalop.DefaultCodeGeneratorVisitor;
import de.gaalop.Notifications;
import de.gaalop.StringList;
//import de.gaalop.StringList;
import de.gaalop.cfg.*;
import de.gaalop.dfg.*;

import java.util.*;
//import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * This visitor traverses the control and data flow graphs and generates C/C++
 * code.
 */
public class JsonexportVisitor extends DefaultCodeGeneratorVisitor {



    protected Set<String> assigned = new HashSet<>();


    
    protected Set<String> libraries = new HashSet<>();

   

    public JsonexportVisitor() {
    
    }


    @Override
    public void visit(StartNode node) {
        graph = node.getGraph();
        int bladeCount = graph.getAlgebraDefinitionFile().getBladeCount();
        
       /* StringList outputs = graph.getOutputs();
        
        if (standalone) {
            appendIndentation();
            code.append("void " + graph.getSource().getName().split("\\.")[0] + "(");
            
            // Print parameters
            StringList parameters = new StringList();
            
            for (String var : graph.getInputs()) {
                parameters.add(variableType+" "+var); // The assumption here is that they all are normal scalars
            }
            
            for (String var : outputs) {
                parameters.add(variableType+" "+var+"["+bladeCount+"]");
            }
            
            code.append(parameters.join());

            code.append(") {\n");
            indentation++;
        } */

        /*for (String var : graph.getLocals()) 
            if (!outputs.contains(var)) {//declaration of arrays
                appendIndentation();
                code.append(variableType).append(" ");
                code.append(var);
                code.append("[" + bladeCount + "] = { 0.0 };\n");
            }

        if (graph.getScalarVariables().size() > 0) {//declaration of scalars
            appendIndentation();
            code.append(variableType).append(" ");
            code.append(graph.getScalars().join());
            code.append(";\n");
        }*/

        code.append("{\"type\": \"StartNode\",");
        
        code.append("\"name\": \""+graph.getSource().getName().split("\\.")[0]+"\",");

        
        List<Map.Entry<String, StringList>> sections = Arrays.asList(//java doesnt have tuples so we use a map entry instead
            new AbstractMap.SimpleEntry<>("inputScalars", graph.getInputs()),
            new AbstractMap.SimpleEntry<>("outputMultivectors", graph.getOutputs()),
            new AbstractMap.SimpleEntry<>("localMultivectors", graph.getLocals()),
            new AbstractMap.SimpleEntry<>("localScalars", graph.getScalars())
        );

        for (Map.Entry<String, StringList> entry : sections) {
            code.append("\"").append(entry.getKey()).append("\": [");
            code.append(entry.getValue().stream().map(x -> '"' + x + '"').collect(Collectors.joining(", ")));
            code.append("],");
        }

        code.append("\"").append("renderingExpressions").append("\": [");
        code.append(graph.getRenderingExpressions().entrySet().stream()
            .map(entry -> String.format("{\"name\": \"%s\", \"expression\": \"%s\"}", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(", ")));
        code.append("],");

        /*code.append("\"inputscalars\": [");
        //code.append(graph.getInputs().join(", "));
        code.append(graph.getInputs().stream().map(x -> '"'+x+'"').collect(Collectors.joining(", ")));
        code.append("],");

        code.append("\"outputs\": [");
        code.append(graph.getOutputs().stream().map(x -> '"'+x+'"').collect(Collectors.joining(", ")));
        //code.append(graph.getOutputs().join(", "));
        code.append("],");

        code.append("\"locals\": [");
        //code.append(graph.getLocals().stream().map(x -> x+"["+bladeCount+"]").collect(Collectors.joining(", ")));
        code.append(graph.getLocals().join(", "));
        code.append("],");

        code.append("\"localscalars\": [");
        code.append(graph.getScalars().join(", "));
        code.append("],");*/

        code.append("\"bladecount\": "+bladeCount+",");
        
        
        
        code.append("\"nodes\": [");
        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(AssignmentNode node) {
        
        code.append("{");
        code.append("\"type\": \"AssignmentNode\",");
        code.append("\"variable\": ");
        node.getVariable().accept(this);
        code.append(",");
        code.append("\"expression\": ");
        //appendIndentation();
        //
        //code.append(" = ");
        node.getValue().accept(this);
        code.append("}");
        code.append(",");
        appendIndentation();

        String variable = node.getVariable().getName();
        if (assigned.contains(variable)) {
            String message = "Variable " + variable + " has been reset for reuse.";
            log.warn(message);
            Notifications.addWarning(message);
            
            assigned.remove(variable);
        }
        /*String variable = node.getVariable().getName();
        if (assigned.contains(variable)) {
            String message = "Variable " + variable + " has been reset for reuse.";
            log.warn(message);
            Notifications.addWarning(message);
            appendIndentation();
            code.append("memset(");
            code.append(variable);
            code.append(", 0, sizeof(");
            code.append(variable);
            code.append(")); // Reset variable for reuse.\n");
            assigned.remove(variable);
        }

        appendIndentation();
        node.getVariable().accept(this);
        code.append(" = ");
        node.getValue().accept(this);
        code.append(";");

        if (node.getVariable() instanceof MultivectorComponent) {
            code.append(" // ");
            code.append(graph.getBladeString((MultivectorComponent) node.getVariable()));
        }

        code.append("\n"); */

        node.getSuccessor().accept(this);
    }

    protected void addChild(Expression parent, Expression child) {
        //this function should never be used because the functions using this are overridden
        throw new UnsupportedOperationException("addChild should not be called");
    }

    @Override
    protected void addBinaryInfix(BinaryOperation op, String operator) {

        code.append("{\"type\":\""+operator+"\",");
        code.append("\"left\": ");
        op.getLeft().accept(this);
        code.append(",");
        code.append("\"right\": ");
        op.getRight().accept(this);
        code.append("}");
    }

    @Override
    public void visit(Addition addition) {
        addBinaryInfix(addition, "Add");
    }

    @Override
    public void visit(Subtraction subtraction) {
        addBinaryInfix(subtraction, "Sub");
    }

    @Override
    public void visit(Multiplication multiplication) {
        addBinaryInfix(multiplication, "Mul");
    }

    @Override
    public void visit(Division division) {
        addBinaryInfix(division, "Div");
    }

    @Override
    public void visit(Exponentiation exponentiation) {
       addBinaryInfix(exponentiation, "Pow");
    }

    @Override
    public void visit(ExpressionStatement node) {
        code.append("{" + "\"type\": \"ExpressionStatement\",");
        code.append("\"expression\": ");
        node.getExpression().accept(this);
        code.append("},\n");

        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(StoreResultNode node) {
        assigned.add(node.getValue().getName());
        code.append("{" + "\"type\": \"StoreResultNode\"},\n");

        node.getSuccessor().accept(this);
    }

    public static void rstrip(StringBuilder sb, String charsToStrip) {
        // Remove trailing characters from the StringBuilder
        // by checking from the end of the StringBuilder
        int len = sb.length();
        while (len > 0 && charsToStrip.indexOf(sb.charAt(len - 1)) != -1) {
            len--;
        }
        sb.setLength(len); // Truncate the StringBuilder
    }

    @Override
    public void visit(EndNode node) {
        
        /*if (standalone) {
            indentation--;
            appendIndentation();
            code.append("}\n");
        }*/
        
        /*if (!libraries.isEmpty()) {
            LinkedList<String> libs = new LinkedList<>(libraries);
            libs.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.compareTo(o1);
                }
            });
            
            for (String lib: libs) {
                code.insert(0, "#"+lib+"\n");
            }
        }*/

        rstrip(code, "\n, ");//remove the last comma and newline so the json is valid
        code.append("\n]}");
    }

    @Override
    public void visit(ColorNode node) {
        code.append("{\"type\": \"ColorNode\",");
        code.append("\"r\": "+node.getR()+",");
        code.append("\"g\": "+node.getG()+",");
        code.append("\"b\": "+node.getB()+",");
        code.append("\"alpha\": "+node.getAlpha());
        code.append("},\n");
        node.getSuccessor().accept(this);
    }

    @Override
    public void visit(MathFunctionCall mathFunctionCall) {
        /*libraries.add("include <math.h>");
        String funcName;
        switch (mathFunctionCall.getFunction()) {
            case ABS:
                funcName = "fabs";
                break;
            case SQRT:
                funcName = "sqrtf";
                break;
            default:
                funcName = mathFunctionCall.getFunction().toString().toLowerCase();
        }
        code.append(funcName);
        code.append('(');
        mathFunctionCall.getOperand().accept(this);
        code.append(')');*/
        code.append("{\"type\": \"MathFunctionCall\",");
        code.append("\"function\": \""+mathFunctionCall.getFunction().toString().toLowerCase()+"\",");
        code.append("\"operand\": ");
        mathFunctionCall.getOperand().accept(this);
        code.append("}");
    }

    @Override
    public void visit(Variable variable) {
        //"name"

        code.append("{\"type\": \"Variable\",");
        code.append("\"name\": \""+variable.getName()+"\"}");
    }

    @Override
    public void visit(MultivectorComponent component) {

        code.append("{\"type\": \"MultivectorVariable\",");
        code.append("\"name\": \""+component.getName()+"\",");
        code.append("\"bladeString\": \""+graph.getBladeString(component)+"\",");
        //TODO maybe dont put this in the variable because it is redundant
        code.append("\"bladeIndex\": "+component.getBladeIndex()+"}");
    }

   

    @Override
    public void visit(FloatConstant floatConstant) {
        //code.append(Double.toString(floatConstant.getValue()));
        code.append("{\"type\": \"Const\",");
        code.append("\"value\": "+floatConstant.getValue()+"}");
    }

    @Override
    public void visit(Negation negation) {
        /*code.append('(');
        code.append('-');
        addChild(negation, negation.getOperand());
        code.append(')');*/
        code.append("{\"type\": \"Negation\",");
        code.append("\"operand\": ");
        negation.getOperand().accept(this);
        code.append("}");
    }

}
