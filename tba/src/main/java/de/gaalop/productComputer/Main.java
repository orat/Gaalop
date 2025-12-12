package de.gaalop.productComputer;

import de.gaalop.algebra.AlStrategy;
import de.gaalop.algebra.DefinedAlgebra;
import de.gaalop.cfg.AlgebraDefinitionFile;
import de.gaalop.tba.IMultTable;
import de.gaalop.tba.MultTableAbsDirectComputer;
import de.gaalop.tba.MultTableImpl;
import de.gaalop.tba.table.TableFormat;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Creates the multiplication table.
 * 
 * The created files are saved in the tba plugins main folder.
 * 
 * Only the algebra definitions defined in the file "definedAlgebras.txt" are processed.
 * 
 * @author Christian Steinmetz
 */
public class Main {

    public static void main(String[] args) throws IOException {
        for (DefinedAlgebra definedAlgebra: de.gaalop.algebra.Plugin.getDefinedAlgebras()) {
            System.out.println("Create table for algebra \""+definedAlgebra.id+":\" "+definedAlgebra.name+" ...");
            ProductComputer productComputer = new ProductComputer();
            AlgebraDefinitionFile alFile = new AlgebraDefinitionFile();
            InputStream in = AlStrategy.class.getResourceAsStream("algebra/"+definedAlgebra.id+"/definition.csv");
            if (in != null){
                alFile.loadFromFile(new InputStreamReader(in));
                AlgebraPC algebraPC = new AlgebraPC(alFile);
                productComputer.initialize(algebraPC);


                //precalculate tables because of double use in TableCompressed
                int bladeCount = (int) Math.pow(2, algebraPC.base.length);

                MultTableImpl inner = new MultTableImpl();
                inner.createTable(bladeCount);
                MultTableImpl outer = new MultTableImpl();
                outer.createTable(bladeCount);
                MultTableImpl geo = new MultTableImpl();
                geo.createTable(bladeCount);

                IMultTable innerLive = new MultTableAbsDirectComputer(alFile, new InnerProductCalculator());
                IMultTable outerLive = new MultTableAbsDirectComputer(alFile, new OuterProductCalculator());
                IMultTable geoLive = new MultTableAbsDirectComputer(alFile, new GeoProductCalculator());

                for (int i=0;i<bladeCount;i++)
                    for (int j=0;j<bladeCount;j++) {
                        inner.setProduct(i, j, innerLive.getProduct(i, j));
                        outer.setProduct(i, j, outerLive.getProduct(i, j));
                        geo.setProduct(i, j, geoLive.getProduct(i, j));
                    }

                TableFormat.writeToFile(bladeCount,algebraPC.base.length,inner,outer,
                        geo,new FileOutputStream("products_"+definedAlgebra.id+".csv"),TableFormat./*TABLE_HUMAN_READABLE/*/TABLE_COMPRESSED_MAX);
            } else {
                System.out.println("Algebra definition file: \""+"algebra/"+definedAlgebra.id+"/definition.csv"+"\" not found!");
            }
        }

    }

}
