package de.gaalop.tba.table.ui;

import de.gaalop.tba.Products;
import java.io.File;
import java.io.IOException;
import javax.swing.JTable;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class GAProductTblJTable extends JTable {
    
    public GAProductTblJTable(File algebraDir, Products product) throws IOException {
        super(new MultivectorJTableModel(algebraDir, product)); 
        setTableHeader(null);
    }
}