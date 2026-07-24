package de.gaalop.algebra;

import de.gaalop.cfg.AlgebraDefinitionFile;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class TestAlgebraGenerator {
    
    public TestAlgebraGenerator() {
    }

    @Test
    public void testQCA1() {
        AlgebraDefinitionFile algebraDefinitionFile = new AlgebraDefinitionFile();
        //algebraDefinitionFile.setUsePrecalculatedTable(true);
        algebraDefinitionFile.setUseAsRessource(false);
        algebraDefinitionFile.create("qca", 1);
        System.out.println(algebraDefinitionFile.toString());
        System.out.println(algebraDefinitionFile.getSignatureString());
    }
    @Test
    public void testQCA2() {
        AlgebraDefinitionFile algebraDefinitionFile = new AlgebraDefinitionFile();
        //algebraDefinitionFile.setUsePrecalculatedTable(true);
        algebraDefinitionFile.setUseAsRessource(false);
        algebraDefinitionFile.create("qca", 2);
        System.out.println(algebraDefinitionFile.toString());
        System.out.println(algebraDefinitionFile.getSignatureString());
    }
    
}
