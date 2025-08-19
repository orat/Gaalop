package de.gaalop.tba.table.ui;

import de.gaalop.algebra.AlStrategy;
import de.gaalop.cfg.AlgebraDefinitionFile;
import de.gaalop.productComputer.AlgebraPC;
import de.gaalop.productComputer.GeoProductCalculator;
import de.gaalop.productComputer.InnerProductCalculator;
import de.gaalop.productComputer.OuterProductCalculator;
import de.gaalop.tba.Algebra;
import de.gaalop.tba.IMultTable;
import de.gaalop.tba.MultTableAbsDirectComputer;
import de.gaalop.tba.Products;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.table.AbstractTableModel;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class MultivectorJTableModel extends AbstractTableModel {

    private final int dim;
    private final IMultTable tbl;
    private AlgebraPC algebraPC;
    private Algebra algebra;
    private final int n;
    
    public MultivectorJTableModel(File algebraDir, Products product){
    
        AlgebraDefinitionFile alFile = loadAlgebraDefinitionFile(algebraDir);
        if (null == product){
            throw new IllegalArgumentException("product==null not allowed!");
        } else switch (product) {
            case GEO:
                tbl = new MultTableAbsDirectComputer(alFile, new GeoProductCalculator());
                break;
            case INNER:
                tbl = new MultTableAbsDirectComputer(alFile, new InnerProductCalculator());
                break;
            default:
                tbl = new MultTableAbsDirectComputer(alFile, new OuterProductCalculator());
                break;
        }
        
        algebraPC = new AlgebraPC(alFile); //loadAlgebra(algebraDir);
       
        AlStrategy.createBlades(alFile);
        algebra = new Algebra(alFile);
        
        n = alFile.getSignature().getDimension(); // alFile.base2.length-1;;
        
        dim = (int) Math.pow(2, algebraPC.base.length/*-1*/);
    }
    
    private static AlgebraDefinitionFile loadAlgebraDefinitionFile(File algebraDir){
        AlgebraDefinitionFile alFile = getAlgebraDefinition(algebraDir);
        //AlgebraPC algebraPC = null;
        try (FileReader reader = new FileReader(getAlgebraDefinitionFile(algebraDir))) {
            alFile.loadFromFile(reader);
            //algebraPC = new AlgebraPC(alFile);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        } 
        return alFile;
    }
    private static File getAlgebraDefinitionFile(File algebraDir){
        return new File(algebraDir, "definition.csv");
    }
    private static AlgebraDefinitionFile getAlgebraDefinition(File algebraDir){
        if (!algebraDir.exists()) {
            System.out.println("The given first parameter, is not the path of an existing directory!");
            return null;
        }
        File definitionFile = getAlgebraDefinitionFile(algebraDir);
        if (!definitionFile.exists()) {
            System.out.println("There is no file named 'definition.csv' in the directory!");
            return null;
        }
        return new AlgebraDefinitionFile();
    }
                    
    @Override
    public int getRowCount() {
        return dim; 
    }

    @Override
    public int getColumnCount() {
        return dim; 
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return new MultivectorCell(algebra, tbl.getProduct(rowIndex,columnIndex));
    }
    
    public Class<?> getColumnClass(int columnIndex) {
        return MultivectorCell.class;
    }
    
    public String[] getBasisBladeNames(){
        return algebraPC.base;
    }
}