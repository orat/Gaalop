package de.gaalop.gappc;

import de.gaalop.cfg.AssignmentNode;
import de.gaalop.gapp.ConstantSetVectorArgument;
import de.gaalop.gapp.PairSetOfVariablesAndIndices;
import de.gaalop.gapp.PosSelectorset;
import de.gaalop.gapp.Selector;
import de.gaalop.gapp.PosSelector;
import de.gaalop.gapp.Selectorset;
import de.gaalop.gapp.SetVectorArgument;
import de.gaalop.gapp.Valueset;
import de.gaalop.gapp.Variableset;
import de.gaalop.gapp.instructionSet.GAPPAssignMv;
import de.gaalop.gapp.instructionSet.GAPPAssignInputsVector;
import de.gaalop.gapp.instructionSet.GAPPCalculateMv;
import de.gaalop.gapp.instructionSet.GAPPCalculateMvCoeff;
import de.gaalop.gapp.instructionSet.GAPPDotVectors;
import de.gaalop.gapp.instructionSet.GAPPResetMv;
import de.gaalop.gapp.instructionSet.GAPPSetMv;
import de.gaalop.gapp.instructionSet.GAPPSetVector;

import de.gaalop.gapp.variables.GAPPMultivector;
import de.gaalop.gapp.variables.GAPPSetOfVariables;
import de.gaalop.gapp.variables.GAPPValueHolder;
import de.gaalop.gapp.variables.GAPPVector;
import de.gaalop.gapp.variables.GAPPVariable;
import de.gaalop.gapp.variables.GAPPVariableVisitor;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Implements a visitor,
 * that returns a pretty-formed string of all GAPP members in a ControlFlowGraph
 * @author author
 */
public class GAPPCVisitor extends de.gaalop.gapp.visitor.CFGGAPPVisitor {
    private StringBuilder result;
	private String INDENT = "\t";
	private GAPPAssignInputsVector globalGAPPAssignInputsVector;

    public GAPPCVisitor() {
        result = new StringBuilder("#include <stdio.h>\n#include <math.h>\n\n");
    }

    @Override
    public void visit(AssignmentNode node) {

        if (node.getGAPP() != null) {
            node.getGAPP().accept(this, null);
        }

		String valuestring = node.getValue().toString();

		int index = 0;
		for (GAPPValueHolder value : globalGAPPAssignInputsVector.getValues()) {
			valuestring = valuestring.replace("inputsVector[" + index + "]", value.prettyPrint());
			index++;
		}

        result.append(INDENT + "# ");
        result.append(node.getVariable().toString());
        result.append(" = ");
        result.append(valuestring);
        result.append('\n');
        result.append('\n');

        node.getSuccessor().accept(this);
    }

    /**
     * Clears the result code
     */
    public void clear() {
        result.setLength(0);
    }

    /**
     * Return the result of pretty printing instructions
     * @return The pretty printed string of instructions
     */
    public String getResultString() {
        return result.toString();
    }

    /**
     * Pretty prints a vector at the end of result
     * @param vector The vector
     */
    private void printVector(GAPPVector vector) {
        result.append(vector.prettyPrint());
    }

    /**
     * Pretty prints a multivector at the end of result
     * @param multiVector The multivector
     */
    private void printMultivector(GAPPMultivector multiVector) {
        result.append(multiVector.prettyPrint());
    }

    /**
     * Pretty prints a GAPPSetOfVariables at the end of result
     * @param setOfVariables The GAPPSetOfVariables
     */
    private void printSetOfVariables(GAPPSetOfVariables setOfVariables) {
        result.append(setOfVariables.prettyPrint());
    }

    /**
     * Pretty prints a selector at the end of result
     * @param selector The selector
     */
    private void printSelector(Selector selector) {
        if (selector.getSign() == (byte) -1) {
            result.append('-');
        }
        result.append(selector.getIndex());
    }

    /**
     * Pretty prints a selectorindex at the end of result
     * @param selectorIndex The selectorindex
     */
    private void printSelectorIndex(PosSelector selectorIndex) {
        result.append(selectorIndex.getIndex());
    }

    /**
     * Pretty prints a selectorset at the end of result
     * @param selectorset The selectorset
     */
    private void printSelectors(Selectorset selectorset) {
        result.append("[");
        for (Selector cur : selectorset) {
            printSelector(cur);
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("]");
    }

    /**
     * Pretty prints a posSelectorset at the end of result
     * @param posSelectorset The posSelectorset
     */
    private void printPosSelectors(PosSelectorset posSelectorset) {
        result.append("[");
        for (PosSelector cur : posSelectorset) {
            printSelectorIndex(cur);
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("]");
    }

    /**
     * Pretty prints a variableset at the end of result
     * @param variableset The variableset
     */
    private void printVariableSet(Variableset variableset) {
        result.append("[");
        for (GAPPValueHolder cur : variableset) {
            result.append(cur.prettyPrint());
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("]");
    }

    /**
     * Pretty prints a variableset at the end of result
     * @param variableset The variableset
     */
    private void printVariableSetNoBrackets(Variableset variableset) {
        for (GAPPValueHolder cur : variableset) {
			result.append("float ");
            result.append(cur.prettyPrint());
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
    }

    /**
     * Pretty prints a valueset at the end of result
     * @param valueset The valueset
     */
    private void printValueSet(Valueset valueset) {
        result.append("[");
        for (GAPPValueHolder cur : valueset) {
            result.append(cur.prettyPrint());
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("]");
    }

    /**
     * Prints a SetVectorArgument instance
     * @param arg The argument
     */
    private void printArgument(SetVectorArgument arg) {
        if (arg.isConstant())
            result.append(((ConstantSetVectorArgument) arg).getValue());
        else {
            PairSetOfVariablesAndIndices pair = (PairSetOfVariablesAndIndices) arg;
            result.append(pair.getSetOfVariable().prettyPrint());
            printSelectors(pair.getSelectors());
        }
    }

    private void printArgumentDev(SetVectorArgument arg) {
        if (arg.isConstant())
            result.append(((ConstantSetVectorArgument) arg).getValue());
        else {
            PairSetOfVariablesAndIndices pair = (PairSetOfVariablesAndIndices) arg;

			if (!pair.getSetOfVariable().getName().equals("inputsVector")){
				for (Selector cur : pair.getSelectors()) {
					if (cur.getSign() == (byte) -1) {
						result.append('-');
					}
		            result.append(pair.getSetOfVariable().prettyPrint());
					result.append('[');
					result.append(cur.getIndex());
					result.append(']');
				}
			} else {
				for (Selector cur : pair.getSelectors()) {
					if (cur.getSign() == (byte) -1) {
						result.append('-');
					}
					int index = 0;
					for (GAPPValueHolder cur2 : globalGAPPAssignInputsVector.getValues()) {
						if (index == cur.getIndex()){
							result.append(cur2.prettyPrint());
							result.append(",");
						}
						index++;
					}
				}
				result.deleteCharAt(result.length() - 1);
			}
        }
    }


    /**
     * Prints a list of SetVectorArgument
     * @param list The list
     */
    private void printListOfArguments(LinkedList<SetVectorArgument> list) {		
//        result.append("{");
        for (SetVectorArgument cur : list) {
//            printArgument(cur);
            printArgumentDev(cur);
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
//        result.append("}");
    }

    /**
     * Pretty prints a dot product of vectors at the end of result
     * @param vectors The vectors of the dot product
     */
    private void printDotProduct(LinkedList<GAPPVector> vectors) {
        result.append("dot_vectors(");

//		int size = 0;
        ListIterator<GAPPVector> it = vectors.listIterator();
        while (it.hasNext()) {
			GAPPVector vector = it.next();
//			size = max(size, vector.size());
            printVector(vector);
//            if (it.hasNext()) {
            result.append(",");
//            }
        }
        result.deleteCharAt(result.length() - 1);
//        result.append(",");
//        result.append(size);

        result.append(")");
    }

    @Override
    public Object visitAssignMv(GAPPAssignMv gappAssignMv, Object arg) {
        result.append(INDENT);
        printMultivector(gappAssignMv.getDestination());
        printPosSelectors(gappAssignMv.getSelectors());
        result.append(" = ");
        printValueSet(gappAssignMv.getValues());
        result.append("\n");
        return null;
    }

    @Override
    public Object visitDotVectors(GAPPDotVectors gappDotVectors, Object arg) {
        result.append(INDENT);
        printMultivector(gappDotVectors.getDestination());
        result.append("[");
        printSelector(gappDotVectors.getDestSelector());
        result.append("]");
        result.append(" = ");
        printDotProduct(gappDotVectors.getParts());
        result.append("\n");
        return null;
    }

    @Override
    public Object visitResetMv(GAPPResetMv gappResetMv, Object arg) {
        result.append(INDENT);
		result.append("float ");
        printMultivector(gappResetMv.getDestination());
        result.append("[");
        result.append(gappResetMv.getSize());
        result.append("] = {");
		for (int i = 0; i < gappResetMv.getSize(); ++i){
			result.append("0,");
		}
        result.deleteCharAt(result.length() - 1);
        result.append("};\n");
        return null;
    }

    @Override
    public Object visitSetMv(GAPPSetMv gappSetMv, Object arg) {
        printMultivector(gappSetMv.getDestination());
        printPosSelectors(gappSetMv.getSelectorsDest());
        result.append(" = ");
        printSetOfVariables(gappSetMv.getSource());
        printSelectors(gappSetMv.getSelectorsSrc());
        result.append(";\n");
        return null;
    }

    @Override
    public Object visitSetVector(GAPPSetVector gappSetVector, Object arg) {
        result.append(INDENT);
		int size = gappSetVector.getEntries().size();
		result.append("float ");
		printVector(gappSetVector.getDestination());
		result.append("[");
		result.append(size);
		result.append("]");
		result.append(" = {");
		printListOfArguments(gappSetVector.getEntries());
		result.append("}");
        result.append(";\n");
        return null;
    }

    @Override
    public Object visitCalculateMv(GAPPCalculateMv gappCalculateMv, Object arg) {
        result.append("calculateMv ");
        printMultivector(gappCalculateMv.getDestination());
        result.append(" = ");
        result.append(gappCalculateMv.getType().toString());
        result.append("(");
        printMultivector(gappCalculateMv.getOperand1());
        if (gappCalculateMv.getOperand2() != null) {
            result.append(",");
            printMultivector(gappCalculateMv.getOperand2());
        }
        result.append(")\n");
        return null;
    }

    @Override
    public Object visitAssignInputsVector(GAPPAssignInputsVector gappAssignInputsVector, Object arg) {
		globalGAPPAssignInputsVector = gappAssignInputsVector;

        result.append("\nvoid script(");
        printVariableSetNoBrackets(gappAssignInputsVector.getValues());
        result.append("){\n");
//        result.append(INDENT + "inputsVector = np.array(");
//        printVariableSet(gappAssignInputsVector.getValues());
//        result.append(")\n");
        return null;
    }

    @Override
    public Object visitCalculateMvCoeff(GAPPCalculateMvCoeff gappCalculateMvCoeff, Object arg) {
        result.append("calculateMvCoeff ");
        result.append(gappCalculateMvCoeff.getDestination().getName());
        result.append("[");
        result.append(Integer.toString(gappCalculateMvCoeff.getDestination().getBladeIndex()));
        result.append("]");
        result.append(" = ");
        result.append(gappCalculateMvCoeff.getType().toString());
        result.append("(");
        printMultivector(gappCalculateMvCoeff.getOperand1());
        if (gappCalculateMvCoeff.getOperand2() != null) {
            result.append(",");
            printMultivector(gappCalculateMvCoeff.getOperand2());
        }
        result.append(")\n");
        return null;
    }





}
