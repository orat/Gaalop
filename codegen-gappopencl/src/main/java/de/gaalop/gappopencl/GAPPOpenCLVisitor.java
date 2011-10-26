/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gaalop.gappopencl;

import de.gaalop.gapp.Selector;
import de.gaalop.gapp.instructionSet.GAPPAssignMv;
import de.gaalop.gapp.instructionSet.GAPPAssignVector;
import de.gaalop.gapp.instructionSet.GAPPCalculateMv;
import de.gaalop.gapp.instructionSet.GAPPCalculateMvCoeff;
import de.gaalop.gapp.instructionSet.GAPPDotVectors;
import de.gaalop.gapp.instructionSet.GAPPResetMv;
import de.gaalop.gapp.instructionSet.GAPPSetMv;
import de.gaalop.gapp.instructionSet.GAPPSetVector;
import de.gaalop.gapp.variables.GAPPVector;
import java.util.Iterator;


/**
 *
 * @author patrick
 */
public class GAPPOpenCLVisitor extends de.gaalop.gapp.visitor.CFGGAPPVisitor {
    
    private StringBuilder result = new StringBuilder();
    
    @Override
    public Object visitResetMv(GAPPResetMv gappResetMv, Object arg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitSetMv(GAPPSetMv gappSetMv, Object arg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitSetVector(GAPPSetVector gappSetVector, Object arg) {
        result.append("float");
        // append the right size here
        result.append(gappSetVector.getDestination());
        result.append(" = make_float");
        // append the right size here
        result.append("(");

        Iterator<Selector> it = gappSetVector.getSelectorsSrc().iterator();
        result.append(gappSetVector.getSource()).append(".s").append(it.next());
        while(it.hasNext()) {
            result.append(",");
            result.append(gappSetVector.getSource()).append(".s").append(it.next());
        }
        result.append(");\n");

        return null;
    }

    @Override
    public Object visitCalculateMv(GAPPCalculateMv gappCalculateMv, Object arg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitAssignVector(GAPPAssignVector gappAssignVector, Object arg) {
        result.append("float");
        // size
        result.append(gappAssignVector.getDestination().getName());
        result.append(" = ");

        return null;
    }

    @Override
    public Object visitCalculateMvCoeff(GAPPCalculateMvCoeff gappCalculateMvCoeff, Object arg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Object visitAssignMv(GAPPAssignMv gappAssignMv, Object arg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitDotVectors(GAPPDotVectors gappDotVectors, Object arg) {
        Iterator<GAPPVector> it = gappDotVectors.getParts().iterator();
        
        result.append(gappDotVectors.getDestination().getName());
        result.append(" = ");
        result.append(it.next().getName());

        while(it.hasNext()) {
            result.append(" * ");
            result.append(it.next().getName());
        }
        result.append(";\n");
        
        return null;
    }

    String getCode() {
        return result.toString();
    }
}