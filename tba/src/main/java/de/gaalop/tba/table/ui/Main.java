package de.gaalop.tba.table.ui;

import de.gaalop.tba.Multivector;
import de.gaalop.tba.Products;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class Main extends JFrame {
      
    /*private static File determineAlgebraFolder(String name){
        File gaalopSRCFolder = (new File(Main.class.getProtectionDomain().getCodeSource().
                getLocation().getPath()));
        return new File(gaalopSRCFolder.getAbsolutePath(), "/de/gaalop/algebra/algebra/"+name);
    }*/
    private static File determineAlgebraFolder(String name){
        File gaalopSRCFolder = (new File(de.gaalop.tba.table.Main.class.getProtectionDomain().getCodeSource().
                getLocation().getPath())).getParentFile().getParentFile().getParentFile();
        return new File(gaalopSRCFolder.getAbsolutePath(), "/algebra/src/main/resources/de/gaalop/algebra/algebra/"+name);
    }
    public static void main(String[] args){
        try {
            String algebraName = "cga"; //cga";//d41";//cga";
            Products product = Products.GEO;
            
            JFrame frame = new JFrame("Cayley table for "+product.toString()+"-product for algebra \""+ algebraName+"\""); // Fenster mit Titel erstellen
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Beenden der Anwendung beim Schließen des Fensters
            frame.setSize(400, 300); // Fenstergröße festlegen

            JPanel panel = new JPanel(); // Ein neues JPanel erstellen
            panel.setLayout(new BorderLayout()); // Beispiel: Layout-Manager für das Panel setzen

            GAProductTblJTable table = new GAProductTblJTable(determineAlgebraFolder(algebraName), product);
            //TODO in den Konstruktor von GAProductTblJTable verschieben
            // scheint trotz setzen nicht aufgerufen zu werden
            //FIXME
            table.setDefaultRenderer(MultivectorCell.class, new MultivectorCellRenderer());
            table.setDefaultRenderer(Multivector.class, new MultivectorCellRenderer());
            
            
            panel.add(table, BorderLayout.CENTER); // Label zum Panel hinzufügen
            frame.getContentPane().add(new JScrollPane(panel)); // Das Panel zum Inhaltsbereich des Frames hinzufügen

            //frame.pack();
            frame.setVisible(true); // Das Fenster anzeigen
        
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        //  System.in.read();
        //  System.in.read();
    }
}
