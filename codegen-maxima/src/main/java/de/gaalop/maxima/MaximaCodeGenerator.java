package de.gaalop.maxima;

import de.gaalop.DefaultCodeGenerator;
import de.gaalop.cfg.ControlFlowGraph;

/**
 * This class facilitates LaTeX code generation.
 */
public class MaximaCodeGenerator extends DefaultCodeGenerator {

    public MaximaCodeGenerator() {
        super("mac");
    }

    @Override
    protected String generateCode(ControlFlowGraph in) {
        MaximaVisitor visitor = new MaximaVisitor();
        in.accept(visitor);
        return visitor.getCode();
    }

}
