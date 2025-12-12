package de.gaalop.tba.table;

import de.gaalop.tba.BladeRef;
import de.gaalop.tba.IMultTable;
import de.gaalop.tba.Multivector;
import de.gaalop.tba.table.BitIO.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores the product tables in a compressed format.
 * 
 * @author Christian Steinmetz, Oliver Rettig
 */
public class TableCompressed implements TableReaderIO {

    private AbsBitReader reader;
    private AbsBitWriter writer;

    public TableCompressed(AbsBitReader reader, AbsBitWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * Read the tbl entries from the given DataInputStream into the given tbl objects.
     * 
     * @param in
     * @param innerTable
     * @param outerTable
     * @param geoTable 
     */
    @Override
    public void readFromInputStream(DataInputStream in, IMultTable innerTable, 
                                    IMultTable outerTable, IMultTable geoTable) {
        try {
            int dimension = in.readByte(); // dimension of the algebra
            int bladeCount = (int) Math.pow(2,dimension); // number of basis blades of the algebra
            int bladesBitCount = in.readByte(); // numer of bits to save the number of blades the MV consists of 

            // Liste von prefactors !=0 laden und eine List aufbauen, die von
            // readMultivector() verwendet werden kann, um den prefactor zu bestimmen
            List<Float> prefactors = new ArrayList<>();
            int prefactorsCount = in.readInt();
            for (int i=0;i<prefactorsCount;i++){
                prefactors.add(in.readFloat());
            }
            
            reader.setDataInputStream(in);

            for (int i=0;i<bladeCount;i++)
                for (int j=0;j<bladeCount;j++) {
                    innerTable.setProduct(i, j, readMultivector(reader, dimension, bladesBitCount, prefactors));
                    outerTable.setProduct(i, j, readMultivector(reader, dimension, bladesBitCount, prefactors));
                    geoTable.setProduct(i, j, readMultivector(reader, dimension, bladesBitCount, prefactors));
                }

            in.close();
        } catch (IOException ex) {
            Logger.getLogger(TableCompressed.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads a non-0 multivector.
     * 
     * @param in The reader to be used
     * @param dimension The dimension of the algebra
     * @param bladesBitCount The bit count to save the the number of summed blades the MV consists of
     * @param prefactors if is empty, the prefactors are read directly as a 
     *                   float else read the index and use the List to get the corresponding prefactor in the list
     * @return The read multivector
     * @throws IOException
     */
    private Multivector readMultivector(AbsBitReader in, int dimension, int bladesBitCount, 
                                        List<Float> prefactors) throws IOException {
        Multivector result = new Multivector();
        int bladesCount = in.read(bladesBitCount);
        int prefactorsBitCount = neededBits(prefactors.size());
        for (int i=0;i<bladesCount;i++) {
            //int prefactor = in.read(1);
            float prefactor;
            if (prefactors.isEmpty()){
                prefactor = in.readFloat();
            } else {
                int prefactorIndex = in.read(prefactorsBitCount);
                prefactor = prefactors.get(prefactorIndex);
            }
            int index = in.read(dimension);
            //result.addBlade(new BladeRef((prefactor == 1) ? (byte) -1: (byte) 1, index));
            result.addBlade(new BladeRef(prefactor, index));
        }
        return result;
    }

    /**
     * Writes a non-0 multivector.
     * 
     * @param productTblEntry The multivector to be written (can be a sum of several blades)
     * @param dimension The dimension of the algebra
     * @param out The writer to be used
     * @param bladesBitCount The bit count used to save the number of blades
     * @param prefactors if is empty each prefactor is saved as a float, 
     *                   instead only save the the index in the list
     * @throws IOException
     */
    private void writeMultivector(Multivector productTblEntry, int dimension, 
            AbsBitWriter out, int bladesBitCount, List<Float> prefactors) throws IOException {
        
        // save the number of summed basis blades per multivector
        int size = productTblEntry.getBlades().size();
        out.write(size, bladesBitCount);
        int prefactorsBitCount = neededBits(prefactors.size());
        // save the prefactor and the index of each blade
        // a multivector can be a sum of more than one blades
        for (BladeRef bR: productTblEntry.getBlades()) {
            //out.write((bR.getPrefactor() < 0) ? 1 : 0, 1);
            if (!prefactors.isEmpty()){
                int prefactorIndex = prefactors.indexOf(bR.getPrefactor());
                out.write(prefactorIndex, prefactorsBitCount);
            } else {
                out.writeFloat(bR.getPrefactor());
            }
            out.write(bR.getIndex(), dimension);
        }
    }

    /**
     * Multivectors are written based on fixed bitCount of 32 to store the blades
     * structure in a temporary file and the with a minimized bitCount in the 
     * final file.
     * 
     * @param bladeCount
     * @param dimension
     * @param innerTable
     * @param outerTable
     * @param geoTable
     * @param out 
     */
    @Override
    public void writeFromInputStream(int bladeCount, int dimension, 
            IMultTable innerTable, IMultTable outerTable, IMultTable geoTable, 
            DataOutputStream out) {
        
        try {
            // Calculate the tble and write tbl temporary, non optimized, 
            // do not save parameters e.g. count of blades of each tbl-entry
            
            AbsBitWriter w = new SimpleBitWriter();
            int bladesBitCount = 32; // number of bits to save temporary the count of blades
            File tempFile = File.createTempFile("TableCreator", "txt");
            DataOutputStream out1 = new DataOutputStream(new FileOutputStream(tempFile));
            w.setDataOutputStream(out1);

            // typically 1, this is not the number of basis-vectors spanning a blade!
            int maxNumberOfBlades = 0; // max number of blades in the the entries of the cayley-table

            // List of different prefactors in the table entries
            List<Float> prefactors = new ArrayList<>();
            
            for (int i=0;i<bladeCount;i++)
                for (int j=0;j<bladeCount;j++) {
                    Multivector innerM = innerTable.getProduct(i, j);
                    Multivector outerM = outerTable.getProduct(i, j);
                    Multivector geoM = geoTable.getProduct(i, j);

                    maxNumberOfBlades = Math.max(maxNumberOfBlades, innerM.getBlades().size());
                    maxNumberOfBlades = Math.max(maxNumberOfBlades, outerM.getBlades().size());
                    maxNumberOfBlades = Math.max(maxNumberOfBlades, geoM.getBlades().size());

                    addPrefactors(prefactors, innerM);
                    addPrefactors(prefactors, outerM);
                    addPrefactors(prefactors, geoM);
                    
                    writeMultivector(innerM, dimension, w, bladesBitCount, prefactors);
                    writeMultivector(outerM, dimension, w, bladesBitCount, prefactors);
                    writeMultivector(geoM, dimension, w, bladesBitCount, prefactors);
                }

            w.finish();
            out1.close();

            printPrefactorsList(prefactors);
            
                    
            // ===== 

            DataInputStream in = new DataInputStream(new FileInputStream(tempFile));

            AbsBitReader r = new SimpleBitReader();
            r.setDataInputStream(in);

            // Determine the maximal count of bits needed to store for all entries
            // corresponding to the maximium number of blades of each table entry
            // in the cayley-table : bitCount2
            /*int number = 2;
            int bitCount2 = 1;
            while (number < maxNumberOfBlades+1) {
                bitCount2++;
                number *= 2;
            }*/
            int bladesBitCount2 = neededBits(maxNumberOfBlades);
            writer.setDataOutputStream(out);

            out.writeByte(dimension); // dimension
            out.writeByte(bladesBitCount2); // count of bits needed to save compressed the blade structure

            // save prefactors
            out.writeInt(prefactors.size());
            for (Float prefactor: prefactors){
                out.writeFloat(prefactor);
            }
            
            for (int i=0;i<bladeCount;i++)
                for (int j=0;j<bladeCount;j++) {
                    writeMultivector(readMultivector(r, dimension, 
                            bladesBitCount, prefactors), dimension, writer, bladesBitCount2, prefactors);
                    writeMultivector(readMultivector(r, dimension, 
                            bladesBitCount, prefactors), dimension, writer, bladesBitCount2, prefactors);
                    writeMultivector(readMultivector(r, dimension,
                            bladesBitCount, prefactors), dimension, writer, bladesBitCount2, prefactors);
                }

            writer.finish();
            in.close();
            tempFile.delete();

        } catch (IOException ex) {
            Logger.getLogger(TableCompressed.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public AbsBitReader getReader() {
        return reader;
    }
    
    
    /**
     * Add prefactors.
     * 
     * @param prefactors the list of prefactors the prefactors are added to
     * @param mv multivector as a sum of one or more basis blades, with prefactors to add into the list of prefactors
     * @return true if prefactors are added or false if not
     */
    private static boolean addPrefactors(List<Float> prefactors, Multivector mv){
        boolean added = false;
        for (BladeRef blade: mv.getBlades()){
            float prefactor = blade.getPrefactor();
            if (!prefactors.contains(prefactor)){
                added = true;
                prefactors.add(prefactor);
            }
        }
        return added;
    }
    private static void printPrefactorsList(List<Float> prefactors){
        StringBuilder sb = new StringBuilder();
        sb.append("prefactors = {");
        for (Float value: prefactors){
            sb.append(value.toString());
            sb.append(", ");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.append("}");
        System.out.println(sb.toString());
    }
    
    private static int neededBits(int value){
        int number = 2;
        int bitCount = 1;
        while (number < value+1) {
            bitCount++;
            number *= 2;
        }
        return bitCount;
    }
}
