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
public class HuffmanTree implements Comparable<HuffmanTree> {
    HuffmanTree left;
    HuffmanTree right;
    HuffmanTree parent;
    int pixelValue;
    int frequency;    
    
    public HuffmanTree(int pixelValue,int frequency){
        this.pixelValue = pixelValue;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
        this.parent = null;
    }
    public HuffmanTree(int frequency){
        this.pixelValue=-1;//sentinel value for combined nodes.
        this.frequency=frequency;
        this.left = null;
        this.right = null;
        this.parent = null;
    }
            
    @Override
    public int compareTo(HuffmanTree huffComp) {
        if(this.frequency < huffComp.frequency)
            return -1;//values are less
        else if(this.frequency > huffComp.frequency){
            return 1; //values are greater--go right
        }
        else{
            return 0; //values are equal
        }
    }

    //https://stackoverflow.com/questions/15734922/huffman-tree-with-given-frequency-confuse-as-how-to-start-java
    public static HuffmanTree makeHuffmanTree(int frequencies[], int pixelVals[]) {
        PriorityQueue<HuffmanTree> queue = new PriorityQueue<HuffmanTree>();
        for(int i=0; i<pixelVals.length;i++){
            HuffmanTree ht = new HuffmanTree(pixelVals[i],frequencies[i]);
            queue.add(ht);
        }
        HuffmanTree root = null;
        while(queue.size() > 1){
            HuffmanTree lowest = queue.poll();
            HuffmanTree lowest2 = queue.poll();
            HuffmanTree combined = new HuffmanTree(lowest.frequency+lowest2.frequency);
            combined.right = lowest;
            combined.left = lowest2;
            lowest.parent = combined;
            lowest2.parent = combined;
            queue.add(combined);
            root = combined;
        }
        return root;
    }
    
    public HuffmanTree findTreeNode(int pixelValue){
        HuffmanTree leftSearch = null;
        HuffmanTree rightSearch = null;
        if(this.left!=null)
            leftSearch = this.left.findTreeNode(pixelValue);
        if(this.right!=null)
            rightSearch = this.right.findTreeNode(pixelValue);
        
        if(this.pixelValue==pixelValue)
            return this;
        if(leftSearch!=null)
            return leftSearch;
        if(rightSearch!=null)
            return rightSearch;
        return null; //node not found;
    }
    //recursively generate the huffman encoding for a HuffmanTree node.
    public String generateHuffmanCode(){
        if(this.parent==null)
            return "";
        else if(this.parent.right==this)
            return this.parent.generateHuffmanCode()+"1";
        else //This is a left child node!
            return this.parent.generateHuffmanCode()+"0";
    }
    public String[] generateHuffmanCodes(){
        String[] codeArray = new String[256];//0-255 String[] pertaining to each pixel value
        
        for(int i=0;i<256;i++){
            HuffmanTree pixelValueNode = this.findTreeNode(i);
            codeArray[i] = pixelValueNode.generateHuffmanCode();
        }
        return codeArray;
    }
    
}