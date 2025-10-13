package de.gaalop.tba.baseChange;

import de.gaalop.algebra.BladeArrayRoutines;
import de.gaalop.algebra.TCBlade;
import de.gaalop.cfg.AlgebraDefinitionFile;
import de.gaalop.productComputer.AlgebraPC;
import de.gaalop.productComputer.BladeStr;
import de.gaalop.productComputer.BubbleSort;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Implements methods to change one blade of the zero-inf-base to the plus-minus-base
 * @author Christian Steinmetz
 */
public class BladeChanger {
    
    private final AlgebraPC algebraPC;
    private final AlgebraDefinitionFile algebraDefinitionFile;
    private final TCBlade[] blades;

    public BladeChanger(AlgebraPC algebraPC, AlgebraDefinitionFile alFile) {
        this.algebraPC = algebraPC;
        this.algebraDefinitionFile = alFile;
        blades = BladeArrayRoutines.createBlades(algebraPC.base2);
    }
    
    /**
     * Determines, if a given array contains any duplicate element
     * @param array The array
     * @return <value>true</value>, if the array contains at least one duplicate element, <value>false</value> otherwise
     */
    private static boolean duplicates(final String[] array) {
        HashSet<String> set = new HashSet<>();
        for (String i : array) {
            if (set.contains(i)) {
                return true;
            }
            set.add(i);
        }
        return false;
    }
    private static boolean duplicates(final ImmutableList<String> array) {
        HashSet<String> set = new HashSet<>();
        for (String i : array) {
            if (set.contains(i)) {
                return true;
            }
            set.add(i);
        }
        return false;
    }
    
    /**
     * Computes the cartesian product of the elements of a given list.
     * @param list The list of list of blades
     * @return The list of blades as cartesian combination
     */
    private static LinkedList<BladeStr> cartesian(LinkedList<LinkedList<BladeStr>> list) {
        if (list.size() == 1)  //anchor, easy case
            return list.getFirst();
        else {
            //complex case
            LinkedList<BladeStr> result = new LinkedList<>();

            // recursive call on sublist case
            LinkedList<LinkedList<BladeStr>> subList = new LinkedList<>(list);
            LinkedList<BladeStr> thisElement = subList.removeFirst();
            LinkedList<BladeStr> subResult = cartesian(subList);
            
            // merge
            for (BladeStr comp: thisElement) {
                for (BladeStr subResultComp: subResult) {
                    //concatenate arrays
                    //LinkedList<String> both = new LinkedList<>();
                    MutableList<String> both = new FastList<>();
                    //Collections.addAll(both, comp.getBaseVectors());
                    both.addAll(comp.getBaseVectors().castToCollection());
                    //Collections.addAll(both, subResultComp.getBaseVectors());
                    both.addAll(subResultComp.getBaseVectors().castToCollection());
                    
                            
                    BladeStr b = new BladeStr(comp.getPrefactor()*subResultComp.getPrefactor(), 
                            both.toImmutable()/*.toArray(new String[both.size()])*/);
                    
                    //FIXME was für duplicates kann b überhaupt enthalten?
                    // 
                    //Look if the bladeStr is not zero (two equal base components in one blade results in zero)
                    if (!duplicates(b.getBaseVectors()))
                        result.add(b);
                }
            }
           
            return result;
        }
    }
    
    /**
     * Transforms an bladeIndex of the zero-inf-base into a list of prefactors with blade indices in the plus-minus base
     * @param fromIndex The bladeIndex of the zero-inf-base
     * @return A list of prefactors with blade indices in the plus-minus base
     */
    public LinkedList<PrefactoredBladeIndex> transform(int fromIndex) {
        if (fromIndex == 0) {
            LinkedList<PrefactoredBladeIndex> result = new LinkedList<>();
            result.add(new PrefactoredBladeIndex(1, 0));
            return result;
        }
            
        String bladeString = algebraDefinitionFile.getBladeString(fromIndex);
        bladeString = bladeString.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(" ", "");
        
        // transform blade expression, e.g. e1^einf^e0 to e1^(em+ep)^(0.5*em-0.5*ep)
        LinkedList<LinkedList<BladeStr>> bladeExpressionTransformed = new LinkedList<>();
        String[] parts = bladeString.split("\\^");
        for (String part: parts) {
            LinkedList<BladeStr> e = new LinkedList<>();
            if (algebraPC.mapToPlusMinus.containsKey(part)) 
                e.addAll(algebraPC.mapToPlusMinus.get(part));
            else 
                e.add(new BladeStr(part));
            bladeExpressionTransformed.add(e);
        }
        
        LinkedList<BladeStr> cartesianResult = cartesian(bladeExpressionTransformed);

        
        // Transform each blade into its canonical order
        for (BladeStr b: cartesianResult) {
            //Integer[] arr = baseVectorArrToIntArr(b.getBaseVectors());
            MutableIntList arr = baseVectorToMutableIntList(b.getBaseVectors());
            // numExchanges is not available with arr.sortThis(), thats why own implementation is needed
            int numExchanges = BubbleSort.doBubbleSort(arr);
            if (numExchanges % 2 == 1)
                b.setPrefactor(-b.getPrefactor());  // Change sign because of odd number of exchanges
            //FIXME warum wird hier erneut sortiert?
            //Arrays.sort(arr);
            arr.sortThis();
            b.setBaseVectors(intArrToBaseVectorArr(arr));
        }
        
        
        // Transform
        //HashMap<Integer, PrefactoredBladeIndex> result = new HashMap<Integer, PrefactoredBladeIndex>();
        MutableIntObjectMap<PrefactoredBladeIndex> result = new IntObjectHashMap<>();
        for (BladeStr b: cartesianResult) {
            // null is possible --> -1 is possible
            // [ERROR] de/gaalop/tba/baseChange/BladeChanger.java:[159,74] 
            // Inkompatible Typen: org.eclipse.collections.api.list.ImmutableList<java.lang.String> 
            // kann nicht in java.lang.String[] konvertiert werden
            int index = baseVectorArrToIndex(new TCBlade(b.getBaseVectors()));
            
            PrefactoredBladeIndex p;
            if (result.containsKey(index)) {
                p = result.get(index);
            } else {
                //FIXME
                // was passiert bei Integer=null --> int
                p = new PrefactoredBladeIndex(0, index);
                //FIXME
                // wass passiert bei put(null, also bei einer HashMap
                result.put(index, p);
            }
            
            p.prefactor += b.getPrefactor();
        }
        
        return new LinkedList<>(result.values());
    }
    
    /**
     * Converts an array of base vectors into an array of indices according to algebraPC.base2
     * @param baseVectorArr The array of base vectors
     * @return The array of indices
     */
    /*private Integer[] baseVectorArrToIntArr(String[] baseVectorArr) {
        //LinkedList<Integer> result = new LinkedList<>();
        MutableIntList result = new IntArrayList();
        for (String baseVector: baseVectorArr)
            result.add(getIndexInArray(baseVector, algebraPC.base2));
        return result.toArray(new Integer[0]);
    }*/
    /*private MutableIntList baseVectorArrToMutableIntList(String[] baseVectorArr) {
        MutableIntList result = new IntArrayList();
        for (String baseVector: baseVectorArr)
            result.add(getIndexInArray(baseVector, algebraPC.base2));
        return result;
    }*/
    private MutableIntList baseVectorToMutableIntList(/*String[]*/ ImmutableList<String> baseVectorArr) {
        MutableIntList result = new IntArrayList();
        //for (String baseVector: baseVectorArr)
        //    result.add(getIndexInArray(baseVector, algebraPC.base2));
        baseVectorArr.forEach(baseVector -> result.add(getIndexInArray(baseVector, algebraPC.base2)));
        return result;
    }
    
    /**
     * Converts an array of indices into an array of base vectors according to algebraPC.base2
     * @param intArr The array of indices
     * @return The array of base vectors
     */
    /*private String[] intArrToBaseVectorArr(Integer[] intArr) {
        LinkedList<String> result = new LinkedList<>();
        for (Integer i: intArr)
            result.add(algebraPC.base2[i]);
        return result.toArray(new String[0]);
    }*/
    private ImmutableList<String> intArrToBaseVectorArr(MutableIntList intArr) {
        MutableList<String> result = new FastList<>();
        intArr.forEach(index -> result.add(algebraPC.base2[index]));
        return result.toImmutableList();
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
    
    /**
     * Returns the index of a TCBlade in the blades array -> the bladeIndex
     * @param blade The TCBlade to search for
     * @return The index. Null, if the blades array does not contain such an element
     */
    /*private Integer baseVectorArrToIndex(TCBlade blade) {
        for (int i=0;i<blades.length;i++)
            //if (Arrays.equals(blade.getBase(), blades[i].getBase())) 
            if (blade.getBase().equals(blades[i].getBase())) 
                return i;
        return null;
    }*/
    /**
     * Returns the index of a TCBlade in the blades array -> the bladeIndex
     * @param blade The TCBlade to search for
     * @return The index == -1, if the blades array does not contain such an element
     */
    private int baseVectorArrToIndex(TCBlade blade) {
        for (int i=0;i<blades.length;i++)
            //if (Arrays.equals(blade.getBase(), blades[i].getBase())) 
            if (blade.getBase().equals(blades[i].getBase())) 
                return i;
        return -1;
    }

}
