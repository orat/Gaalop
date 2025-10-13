package de.gaalop.tba.cfgImport;

import de.gaalop.cfg.AlgebraDefinitionFile;
import de.gaalop.cfg.AssignmentNode;
import de.gaalop.dfg.Expression;
import de.gaalop.dfg.MultivectorComponent;
import de.gaalop.dfg.Variable;
import de.gaalop.tba.UseAlgebra;
import de.gaalop.tba.cfgImport.optimization.UpdateLocalVariableSet;
import java.util.Map;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;

/**
 * Changes the control flow graph by building the mvExpressions.
 * This is the main transformer class for TBA plugin
 *
 * @author Christian Steinmetz
 */
public class CFGImporter extends MvExpressionsBuilder {

    public CFGImporter(UseAlgebra usedAlgebra, boolean scalarFunctions, AlgebraDefinitionFile alFile) {
        super(usedAlgebra, scalarFunctions, alFile);
    }

    @Override
    protected AssignmentNode changeGraph(AssignmentNode node, MvExpressions mvExpr, Variable variable) {
        AssignmentNode lastNode = node;

        // At first, output all assignments
        for (IntObjectPair<Expression> blade: mvExpr.getBladeExpressions().keyValuesView()){
        //for (Map.Entry<Integer, Expression> blade: mvExpr.bladeExpressions.entrySet()) {
            AssignmentNode insNode = new AssignmentNode(node.getGraph(), 
                    new MultivectorComponent(variable.getName(), blade.getOne()), blade.getTwo());
            lastNode.insertAfter(insNode);
            lastNode = insNode;
        }

        node.getGraph().removeNode(node);

        UpdateLocalVariableSet.updateVariableSets(node.getGraph());

        return lastNode;
    }
}
