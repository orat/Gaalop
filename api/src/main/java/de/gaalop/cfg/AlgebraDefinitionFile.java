package de.gaalop.cfg;

import de.gaalop.dfg.Expression;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * Defines an algebra
 * @author Christian Steinmetz
 */
public class AlgebraDefinitionFile {

    /**
     * The zeroinf base
     */
    public String[] base;
    /**
     * The line contating the map to transform from the plusminus base to the zeroinf base
     */
    public String lineMapPlusMinusToZeroInf;
    /**
     * The plusminus base
     */
    public String[] base2;
    /**
     * The squares of the plusminus base
     */
    public HashMap<String, Byte> baseSquares = new HashMap<>();
    /**
     * The line contating the map to transform from the zeroinf base to the 
     * plusminus base
     */
    public String lineMapZeroInfToPlusMinus;
    
    public AlgebraSignature getSignature() {
        int p = 0;
        int q = 0;
        int r = 0;
        
        for (Byte b: baseSquares.values()) {
            switch (b) {
                case 1:
                    p++;
                    break;
                case -1:
                    q++;
                    break;
                case 0:
                    r++;
                    break;
            }
        }
        
        return new AlgebraSignature(p, q, r);
    }

    //generated attributes
    public Expression[] blades;
    public Expression[] blades2;

    private boolean usePrecalculatedTable;
    private String productsFilePath;
    private boolean useAsRessource;

    /**
     * The indices of the base vectors
     */
    public HashMap<String, Integer> indices = new HashMap<>();

    /**
     * Returns the string of blade with a given index
     * @param index The index
     * @return The string representing the blade
     */
    public String getBladeString(int index) {
        return blades[index].toString();
    }
    
    /**
     * Returns the string of blade with a given index for the normal base
     * @param index The index
     * @return The string representing the blade
     */
    public String getBladeStringNormalBase(int index) {
        return blades2[index].toString();
    }

    /**
     * Retruns the number of blades
     * @return The number of blades
     */
    public int getBladeCount() {
        return blades.length;
    }

    /**
     * Retruns the expression of a blade with a given index
     * @param index The index
     * @return The expression
     */
    public Expression getBladeExpression(int index) {
        return blades[index];
    }

    /**
     * Returns the index of a base vector
     * @param baseVectorString The string, representing the base vector
     * @return The index
     */
    public int getIndex(String baseVectorString) {
        return getIndexInArray(baseVectorString, base);
    }

    /**
     * Determines the index of a given string in a given string array
     * @param str The string to search
     * @param arr The array to search
     * @return The index, -1 if the array does not contain the string
     */
    private int getIndexInArray(String str, String[] arr) {
        for (int i=0;i<arr.length;i++)
            if (arr[i].equals(str))
                return i;
        return -1;
    }

    public boolean isUsePrecalculatedTable() {
        return usePrecalculatedTable;
    }

    public void setUsePrecalculatedTable(boolean usePrecalculatedTable) {
        this.usePrecalculatedTable = usePrecalculatedTable;
    }

    public boolean isUseAsRessource() {
        return useAsRessource;
    }

    public void setUseAsRessource(boolean useAsRessource) {
        this.useAsRessource = useAsRessource;
    }

    /**
     * Loads a algebra definition from a Reader
     * @param reader The reader to be used
     * @throws java.io.IOException
     */
    public void loadFromFile(Reader reader) throws IOException {
        BufferedReader d = new BufferedReader(reader);
        base = parseStrArray(d.readLine());
        lineMapPlusMinusToZeroInf = d.readLine();
        base2 = parseStrArray(d.readLine());
        baseSquares = parseMapStrByte(d.readLine());
        lineMapZeroInfToPlusMinus = d.readLine();
        d.close();
        createIndices();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        //The zeroinf base
        for (int i=0;i<base.length;i++){
            sb.append(base[i]);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("\n");
        // The line contating the map to transform from the plusminus base to the zeroinf base
        sb.append(lineMapPlusMinusToZeroInf);
        sb.append("\n");
        // The plusminus base
        for (int i=0;i<base2.length;i++){
            sb.append(base2[i]);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("\n");
        // The squares of the plusminus base
        for (String key: baseSquares.keySet()){
            sb.append(key);
            sb.append("=");
            sb.append(String.valueOf(baseSquares.get(key)));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("\n");
        // The line contating the map to transform from the zeroinf base to the 
        // plusminus base
        sb.append(lineMapZeroInfToPlusMinus);
        sb.append("\n");
        return sb.toString();
    }
    // return true, if the algebra with the given name and dimension was sucessfully created
    public boolean create(String name, int dimension){
        switch (name){
            case "qca":
                createQCA(dimension);
                break;
            case "qra":  
                 createQRA(dimension);
                break;
            default:
                return false;
        }
        return true;
    }
    
    public String getSignatureString(){
        AlgebraSignature signature = getSignature();
        return "Cl("+signature.toString()+")";
    }
    
    public static String getSignatureString(String algebraName, int dimension){
         switch (algebraName){
            case "qca":
                return getSignatureQCA(dimension);
            case "qra":   
                return getSignatureQRA(dimension);
            default:
                return null;
        }
    }
    private static String getSignatureQCA(int dimension){
        return "Cl("+String.valueOf(dimension*2+1) + "," + String.valueOf(dimension*2+1) + ",0)";
    }
    private static String getSignatureQRA(int dimension){
        return "Cl("+String.valueOf(dimension+2) + "," + String.valueOf(dimension) + ",0)";
    }
    
    //TODO createQGA?
    
    //TODO
    private void createQRA(int dimension){
        // example 1-dim
        // 1,e1,e2,f1,f1T
        // example 2-dim
        // 1,e1,e2,e3,e4,f1,f1T,f2,f2T
        base = new String[1+4*dimension];
        base[0] = "1"; 
        for (int i=1;i<=2*dimension;i++){
            base[i] = "e"+String.valueOf(i);
        }
        for (int i=1;i<=dimension;i++){
            base[2*dimension+(2*i-1)] = "f"+String.valueOf(i); // Bug index 7 out of bounds für dim = 2
            base[2*dimension+(2*i)] = "f"+String.valueOf(i)+"T"; 
        }
        
        // example 1-dim
        //e3=1.0*f1+1.0*f1T,e4=1.0*f1-1.0*f1T
        // example 2-dim
        //e3=1.0*f1+1.0*f1T,e4=1.0*f1-1.0*f1T,
        // e5=1.0*f2+1.0*f2T,e6=1.0*f2-1.0*f2T
        StringBuilder sb = new StringBuilder();
        for (int i=1;i<=dimension;i++){
            sb.append("e"); sb.append(String.valueOf(2+i)); sb.append("=1.0*f");
            sb.append(String.valueOf(i)); sb.append("+1.0*f"); sb.append(String.valueOf(i));
            sb.append("T, ");
            sb.append("e"); sb.append(String.valueOf(3+i)); sb.append("=1.0*f");
            sb.append(String.valueOf(i)); sb.append("-1.0*f"); sb.append(String.valueOf(i));
            sb.append("T,");
        }
        sb.deleteCharAt(sb.length()-1);
        lineMapPlusMinusToZeroInf=sb.toString();
        
        //1,e1,e2,e3,e4
        //1,e1,e2,e3,e4,e5,e6
        base2 = new String[2+2*dimension+1];
        base2[0] = "1"; base2[1] = "e1"; base2[2] = "e2"; 
        for (int i=1;i<=dimension;i++){
            base2[2+(2*i-1)] = "e"+String.valueOf(2+i);
            base2[2+(2*i)] = "e"+String.valueOf(2+i+1);
        }
        
        //e1=1,e2=1,e3=1,e4=-1
        //e1=1,e2=1,e3=1,e4=-1,e5=1,e6=-1
        // HashMap<String, Byte> baseSquares
        baseSquares.put("e1", (byte) 1);
        baseSquares.put("e2", (byte) 1);
        for (int i=1;i<=dimension;i++){
            baseSquares.put("e"+String.valueOf((2*i)+1),(byte) 1);
            baseSquares.put("e"+String.valueOf((2*i)+2),(byte) -1);
        } 
        
        //f1=0.5*e3+0.5*e4,f1T=0.5*e3-0.5*e4
        //      f1=0.5*e3+0.5*e4, f1T=0.5*e3-0.5*e4,f2=0.5*e5+0.5*e6, f2T=0.5*e5-0.5*e6
        sb = new StringBuilder();
        for (int i=1;i<=dimension;i++){
            sb.append("f"); sb.append(String.valueOf(i)); sb.append("=0.5*e");
            sb.append(String.valueOf(2*i+1)); sb.append("+0.5*e"); sb.append(String.valueOf(2*i+2));
            sb.append(", f"); sb.append(String.valueOf(i)); sb.append("T=0.5*e");
            sb.append(String.valueOf(2*i+1)); sb.append("-0.5*e"); sb.append(String.valueOf(2*i+2));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        lineMapZeroInfToPlusMinus = sb.toString();
    }
    
    private void createQCA(int dimension){
        // 1, e0p, e0m, f1, f1T
        base = new String[2+2*dimension+1];
        base[0] = "1"; base[1] = "e0p"; base[2] = "e0m";
        for (int i=1;i<=dimension;i++){
            base[1+2*i] = "f"+String.valueOf(i);
            base[2+2*i] = "f"+String.valueOf(i)+"T";
        }

        // e1p=1.0*f1+1.0*f1T,e1m=1.0*f1-1.0*f1T
        StringBuilder sb = new StringBuilder();
        for (int i=1;i<=dimension;i++){
            sb.append("e"); sb.append(String.valueOf(i)); sb.append("p=1.0*f");
            sb.append(String.valueOf(i)); sb.append("+1.0*f"); sb.append(String.valueOf(i));
            sb.append("T,e"); sb.append(String.valueOf(i)); sb.append("m=1.0*f");
            sb.append(String.valueOf(i)); sb.append("-1.0*f"); sb.append(String.valueOf(i));
            sb.append("T,");
        }
        sb.deleteCharAt(sb.length()-1);
        lineMapPlusMinusToZeroInf = sb.toString();

        // 1, e0p, e0m, e1p, e1m
        base2 = new String[2+2*dimension+1];
        base2[0] = "1"; base2[1] = "e0p"; base2[2] = "e0m";
        for (int i=1;i<=dimension;i++){
            base2[1+2*i] = "e"+String.valueOf(i) + "p";
            base2[2+2*i] = "e"+String.valueOf(i) + "m";
        }

        // e0p=1, eom=-1, eqp=1, e1m=-1
        // HashMap<String, Byte> baseSquares
        baseSquares.put("e0p", (byte) 1);
        baseSquares.put("e0m", (byte) -1);
        for (int i=1;i<=dimension;i++){
            baseSquares.put("e"+String.valueOf(i)+"p",(byte) 1);
            baseSquares.put("e"+String.valueOf(i)+"m",(byte) -1);
        }

        // f1=0.5*e1p+0.5*e1m, f1T=0.5*e1p-0.5*e1m
        sb = new StringBuilder();
        for (int i=1;i<=dimension;i++){
            sb.append("f"); sb.append(String.valueOf(i)); sb.append("=0.5*e");
            sb.append(String.valueOf(i)); sb.append("p+0.5*e"); sb.append(String.valueOf(i));
            sb.append("m,f"); sb.append(String.valueOf(i)); sb.append("T=0.5*e");
            sb.append(String.valueOf(i)); sb.append("p-0.5*e"); sb.append(String.valueOf(i));
            sb.append("m,");

        }
        sb.deleteCharAt(sb.length()-1);
        lineMapZeroInfToPlusMinus = sb.toString();
    }
    
    /**
     * Parses a String array, splitet with commas
     * @param The string to be parsed
     */
    private String[] parseStrArray(String str) {
        return str.replaceAll(" ", "").split(",");
    }

    /**
     * Parses a map<String, Byte>
     * @param The string to be parsed
     */
    private HashMap<String, Byte> parseMapStrByte(String str) {
        str = str.replaceAll(" ", "");
        HashMap<String, Byte> result = new HashMap<String, Byte>();
        if (str.isEmpty()) {
            return result;
        }
        String[] parts = str.split(",");
        for (String part : parts) {
            String[] parts2 = part.split("=");
            result.put(parts2[0], (byte) Integer.parseInt(parts2[1]));
        }
        return result;
    }

    /**
     * Creates indices from the two bases
     */
    public void createIndices() {
        indices.clear();
        for (int i = 1; i < base.length; i++) {
            indices.put(base[i], i);
        }
        for (int i = 1; i < base2.length; i++) {
            indices.put(base2[i], i);
        }
    }

    public String getProductsFilePath() {
        return productsFilePath;
    }

    public void setProductsFilePath(String productsFilePath) {
        this.productsFilePath = productsFilePath;
    }
}
