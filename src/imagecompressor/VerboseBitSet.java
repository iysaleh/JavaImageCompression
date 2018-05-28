/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagecompressor;

/**
 *
 * @author Ibraheem Saleh
 * 
 * I don't like how java's bit set doesn't include a way to easily see the 0 bits 
 *  This is an alternative that makes working with zeros easy.
 *      An inefficient alternative!
 */
public class VerboseBitSet {
    StringBuilder bitString;
    public VerboseBitSet()
    {
        this.bitString = new StringBuilder();
    }
    public void addByte(int i)
    {
        if (i > 255 || i < 0)
            System.out.println("Overflow! Bits must be in the range from 0-255. Input: "+i);
        bitString.append(String.format("%08d", Integer.valueOf(Integer.toBinaryString(i))));
    }
    public StringBuilder getBitString(){return this.bitString;}
    public String toString(){ return this.bitString.toString(); }
    public int length(){ return this.bitString.length(); }
}
