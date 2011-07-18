package de.gaalop.gapp.variables;

import java.util.Vector;

/**
 *
 * @author christian
 */
public class GAPPVariableCopier implements GAPPVariableVisitor {

    @Override
    public Object visitConstant(GAPPConstant gappConstant, Object arg) {
        return new GAPPConstant(gappConstant.getValue());
    }

    @Override
    public Object visitMultivector(GAPPMultivector gappMultivector, Object arg) {
        return new GAPPMultivector(gappMultivector.getName()); //Strings are immutable!
    }

    @Override
    public Object visitMultivectorComponent(GAPPMultivectorComponent gappMultivectorComponent, Object arg) {
        GAPPMultivector mvCopy = (GAPPMultivector) gappMultivectorComponent.getParent().accept(this, null);
        return new GAPPMultivectorComponent(mvCopy, gappMultivectorComponent.getBladeIndex());
    }

    @Override
    public Object visitScalarVariable(GAPPScalarVariable gappScalarVariable, Object arg) {
        return new GAPPScalarVariable(gappScalarVariable.getName()); //Strings are immutable!
    }

    @Override
    public Object visitSignedMultivectorComponent(GAPPSignedMultivectorComponent gappSignedMultivectorComponent, Object arg) {
        GAPPMultivector mvCopy = (GAPPMultivector) gappSignedMultivectorComponent.getParent().accept(this, null);
        return new GAPPSignedMultivectorComponent(
                mvCopy,
                gappSignedMultivectorComponent.getBladeIndex(),
                gappSignedMultivectorComponent.getSign()
                );
    }

    @Override
    public Object visitVector(GAPPVector gappVector, Object arg) {
        Vector<GAPPSignedMultivectorComponent> slots = new Vector<GAPPSignedMultivectorComponent>();

        for (GAPPSignedMultivectorComponent cur: gappVector.slots)
            slots.add((GAPPSignedMultivectorComponent) cur.accept(this, null));

        return new GAPPVector(gappVector.getName(),slots);
    }

}
