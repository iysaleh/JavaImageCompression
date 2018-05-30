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
    public VerboseBitSet(byte[] bytes)
    {
        this.bitString = new StringBuilder();
        for(int i=0; i < bytes.length;i++){
            //https://stackoverflow.com/questions/12310017/how-to-convert-a-byte-to-its-binary-string-representation
            this.bitString.append(Integer.toBinaryString((bytes[i] & 0xFF) + 0x100).substring(1));
        }
    }
    public void addByteSizedInt(int i)
    {
        if (i > 255 || i < 0)
            System.out.println("Overflow! Bits must be in the range from 0-255. Input: "+i);
        bitString.append(String.format("%08d", Integer.valueOf(Integer.toBinaryString(i))));
    }
    public void addInt(int i,int numBits)
    {
        if(numBits==8)
            bitString.append(String.format("%08d", Integer.valueOf(Integer.toBinaryString(i))));
        else if(numBits==4)
            bitString.append(String.format("%04d", Integer.valueOf(Integer.toBinaryString(i))));
        else if(numBits==2)
            bitString.append(String.format("%04d", Integer.valueOf(Integer.toBinaryString(i))));
    }
    public void addOne(){this.bitString.append('1');}
    public void addZero(){this.bitString.append('0');}
    public int popInt(int numBits)
    {
        String subBits = this.bitString.substring(0,numBits);
        this.bitString.delete(0, numBits);
        return Integer.parseInt(subBits, 2);
    }
    public StringBuilder getBitString(){return this.bitString;}
    public String toString(){ return this.bitString.toString(); }
    public int length(){ return this.bitString.length(); }
}
