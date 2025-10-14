package de.gaalop.productComputer;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;

/**
 * This class provides the BubbleSort algorithm using a comparator
 * @author Christian Steinmetz
 */
public class BubbleSort {
   
    /**
     * BubbleSort Algorithm - from
     * http://de.wikipedia.org/wiki/Bubblesort#Formaler%20Algorithmus
     * @param arr The array to sort in place
     * @return The count of exchanges
     */
    /*public static int doBubbleSort(Integer[] arr) {

        boolean swapped = true;
        int count = 0;
        for(int i = arr.length - 1; i > 0 && swapped; i--) {
            swapped = false;
            for (int j = 0; j < i; j++) {
                if (arr[j] > arr[j+1]) {
                    Integer temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                    swapped = true;
                    count++;
                }
            }
        }
        return count;

    }*/

    public static int doBubbleSort(int[] arr) {
        boolean swapped = true;
        int count = 0;
        for(int i = arr.length - 1; i > 0 && swapped; i--) {
            swapped = false;
            for (int j = 0; j < i; j++) {
                if (arr[j] > arr[j+1]) {
                    Integer temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                    swapped = true;
                    count++;
                }
            }
        }
        return count;
    }
    //FIXME test schreiben
    // immutable does not allow to change/swap values
    /*public static int doBubbleSort(MutableIntList arr){
        boolean swapped = true;
        int count = 0;
        for(int i = arr.size() - 1; i > 0 && swapped; i--) {
            swapped = false;
            for (int j = 0; j < i; j++) {
                if (arr.get(j) > arr.get(j+1)) {
                    int temp = arr.get(j);
                    arr.set(j, arr.get(j+1));
                    arr.set(j+1,temp);
                    swapped = true;
                    count++;
                }
            }
        }
        return count;
    }*/
}
