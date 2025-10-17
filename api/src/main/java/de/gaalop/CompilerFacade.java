package de.gaalop;

import java.util.Observable;
import java.util.Set;
import de.gaalop.cfg.ControlFlowGraph;
import org.openjdk.jol.info.GraphLayout;

/**
 * Represents the high level compilation process.
 */
public class CompilerFacade extends Observable {

    private final CodeParser codeParser;
    
    private final GlobalSettingsStrategy globalSettingsStrategy;

    private final VisualCodeInserterStrategy visualizerStrategy;

    private final AlgebraStrategy algebraStrategy;

    private final OptimizationStrategy optimizationStrategy;

    private final CodeGenerator codeGenerator;
    
    private final String algebraName;
    private final boolean asRessource;
    private final String algebraBaseDirectory;

	/**
     * Constructs a new compiler facade.
     *
     * @param codeParser The code parser used by this facade to construct a dataflow graph from an input file.
     * @param globalSettingsStrategy
     * @param visualizerStrategy
     * @param algebraStrategy
     * @param optimizationStrategy The optimization strategy used to process the graph before generating code.
     * @param codeGenerator The code generator used to generate code from the previously optimized graph.
     * @param algebraName
     * @param asRessource
     * @param algebraBaseDirectory
     */
    public CompilerFacade(CodeParser codeParser, GlobalSettingsStrategy globalSettingsStrategy, VisualCodeInserterStrategy visualizerStrategy, AlgebraStrategy algebraStrategy, OptimizationStrategy optimizationStrategy, CodeGenerator codeGenerator, String algebraName, boolean asRessource, String algebraBaseDirectory) {
        this.codeParser = codeParser;
        this.globalSettingsStrategy = globalSettingsStrategy;
        this.visualizerStrategy = visualizerStrategy;
        this.algebraStrategy = algebraStrategy;
        this.optimizationStrategy = optimizationStrategy;
        this.codeGenerator = codeGenerator;
        this.algebraName = algebraName;
        this.asRessource = asRessource;
        this.algebraBaseDirectory = algebraBaseDirectory;
    }

    /**
     * Compiles an input file using the previously configured subsystems.
     *
     * @param input The input file that should be compiled.
     * @return A set of output files that represent the compilation result.
     * @throws CompilationException If any error occurs during compilation.
     */
    public Set<OutputFile> compile(InputFile input) throws CompilationException {
    	return realCompile(input);
    }
    
    
    // https://www.javacodegeeks.com/using-the-netbeans-profiler-programmatically-in-java.html
    private long usedMemory(){
        //System.out.println("max used heap = "+
        //        String.valueOf(Runtime.getRuntime().maxMemory()/1024/1024/1024)+" GByte");
        
        // Used memory may contain no longer referenced objects that will be 
        // swept away by the next GC
        Runtime.getRuntime().gc();
        
        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        // Get amount of free memory within the heap in bytes. This size will increase
        // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        //System.out.println("diff="+String.valueOf((Runtime.getRuntime().maxMemory()-heapFreeSize)/1014));
        return heapSize - heapFreeSize;
    }
    
    private Set<OutputFile> realCompile(InputFile input) throws CompilationException {
    	setChanged();
    	notifyObservers("Parsing...");
        long usedMemory = usedMemory();
        ControlFlowGraph graph = codeParser.parseFile(input);
        setChanged();
        
        graph.algebraName = algebraName;
        graph.asRessource = asRessource;
        graph.algebraBaseDirectory = algebraBaseDirectory;
        
        notifyObservers("Setting global settings...");
        globalSettingsStrategy.transform(graph);
        setChanged();

        long graphMemory = usedMemory()-usedMemory;
        System.out.println("AST memory after parsing and global settings = "+
                String.valueOf(graphMemory/1024)+" KBytes");
        
        
        
        notifyObservers("Inserting code for visualization...");
        visualizerStrategy.transform(graph);
        setChanged();

        //System.out.println("after global settings and inserting code for visualization:");
        //System.out.println(GraphLayout.parseInstance(graph).toFootprint());
        long graphMemory2 = usedMemory()-usedMemory;
        System.out.println("AST memory after global settings and code inserting for visualization = "+
                String.valueOf(graphMemory2/1024)+" KBytes");
        
        notifyObservers("Algebra inserting...");  
        algebraStrategy.transform(graph);
        setChanged();
        
        long graphMemory3 = usedMemory()-usedMemory;
        System.out.println("AST memory after algebra inserting algebra = "+
                String.valueOf(graphMemory3/1024/1024)+" MBytes");
        
        //System.out.println("after algebra inserting:");
        //System.out.println(GraphLayout.parseInstance(graph).toFootprint());
        
        
        notifyObservers("Optimizing...");
        optimizationStrategy.transform(graph);
        setChanged();
        
        long graphMemory4 = usedMemory()-usedMemory;
        System.out.println("AST memory after optimization = "+
                String.valueOf(graphMemory4/1024/1024)+" MBytes");
        
        testGraph(graph);
        
        notifyObservers("Generating Code...");
        Set<OutputFile> output = codeGenerator.generate(graph);  
        setChanged();
        notifyObservers("Finished");        
        return output;   	
    }

    /**
     * Do some testing with the Control Flow Graph between Optimization stage and Codegenerator stage
     * @param graph The Control Flow Graph
     */
    protected void testGraph(ControlFlowGraph graph) {
        // This method is empty in the standard CompilerFacade.
        //System.out.println(GraphLayout.parseInstance(graph).toFootprint());
        //long heapSize = Runtime.getRuntime().totalMemory();
        //System.out.println("total memory = "+String.valueOf(heapSize));
    }

}
