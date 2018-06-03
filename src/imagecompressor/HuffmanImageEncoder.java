/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagecompressor;
import java.util.PriorityQueue;

/**
 *
 * @author Ibraheem Saleh
 */
public class HuffmanImageEncoder implements Comparable<HuffmanImageEncoder> {
    HuffmanImageEncoder left;
    HuffmanImageEncoder right;
    HuffmanImageEncoder parent;
    int pixelValue;
    int frequency;

    private int[] frequencyArray;
    
    
    public HuffmanImageEncoder(){
        frequencyArray = new int[256];//0-255 possible pixel values
        for(int i=0; i<frequencyArray.length;i++){
            frequencyArray[i]=0;//initialize all the values
        }
    }
    public void addIntElement(int pixelValue){
        frequencyArray[pixelValue] = frequencyArray[pixelValue] + 1;
    }

    @Override
    public int compareTo(HuffmanImageEncoder t) {
        
        return 1;
    }
}

/**
 *
 * @author Ibraheem Saleh
 */
public class HuffmanImageFrequencyCalculator{
    private int[] frequencyArray;
    public HuffmanImageFrequencyCalculator()
    {
        
    }
}