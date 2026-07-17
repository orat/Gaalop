package de.gaalop.gappc;

import de.gaalop.DefaultCodeGenerator;
import de.gaalop.cfg.ControlFlowGraph;

/**
 * This class facilitates Python code generation.
 */
public class GAPPCCodeGenerator extends DefaultCodeGenerator {
    
    public static Integer numBlocks = -1;
    public static final String inputsVector = "inputsVector";
    public static final String tempMv = "tempmv";
    public static final String dot = "dot";
    private final Plugin plugin;
    
    GAPPCCodeGenerator(Plugin plugin) {
        super("c");
    	this.plugin = plugin;
    }
    
    public static String getVarName(final String mvName) {
        if(mvName.startsWith(tempMv) || mvName.startsWith(dot) || mvName.startsWith(inputsVector))          
            return mvName + "_" + numBlocks;
        else
            return mvName;
    }

    @Override
    protected String generateCode(ControlFlowGraph in) {
        // new block
        ++numBlocks;
        
        // determine sizes of multivectors
        GAPPMvSizeVisitor mvSizeVisitor = new GAPPMvSizeVisitor();
        try {
        	in.accept(mvSizeVisitor);
        } catch (Throwable error) {
        	plugin.notifyError(error);
        }        
        
        // generate code
        GAPPCVisitor visitor = new GAPPCVisitor();
        try {
        	in.accept(visitor);
        } catch (Throwable error) {
        	plugin.notifyError(error);
        }
        return visitor.getResultString();
    }

}
