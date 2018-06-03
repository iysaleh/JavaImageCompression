/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagecompressor;

/**
 *
 * @author Ibraheem Saleh
 */
public class HuffmanPixelFrequencyCalculator {
    
    public int[] frequencyArray;
    public int[] intVals;
    public HuffmanPixelFrequencyCalculator(){
        frequencyArray = new int[256];//0-255 possible pixel values
        intVals = new int[256];//0-255 possible pixel values
        for(int i=0; i<frequencyArray.length;i++){
            frequencyArray[i]=0;//initialize all the values
        }
        for(int i=0;i<intVals.length;i++){
            intVals[i]=i;//Populate the int vals array with the 0-255 possible pixelvals.
        }
    }
    public void addIntElement(int pixelValue){
        frequencyArray[pixelValue] = frequencyArray[pixelValue] + 1;
    }
  
    
}
