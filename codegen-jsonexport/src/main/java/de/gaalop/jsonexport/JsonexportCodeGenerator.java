package de.gaalop.jsonexport;

import de.gaalop.DefaultCodeGenerator;
import de.gaalop.cfg.ControlFlowGraph;

/**
 * This class facilitates json code generation.
 */
public class JsonexportCodeGenerator extends DefaultCodeGenerator {

    private final Plugin plugin;

    public JsonexportCodeGenerator(Plugin plugin) {
        super("json");
        this.plugin = plugin;
    }

    @Override
    protected String generateCode(ControlFlowGraph graph) {
        JsonexportVisitor visitor = new JsonexportVisitor();
        try {
            graph.accept(visitor);
        } catch (Throwable error) {
            plugin.notifyError(error);
        }
        return visitor.getCode();
    }
}
