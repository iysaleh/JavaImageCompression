/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagecompressor;

import java.io.ByteArrayOutputStream;

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
    int counter;
    public VerboseBitSet()
    {
        this.bitString = new StringBuilder();
        this.counter = 0;
    }
    public VerboseBitSet(byte[] bytes)
    {
        this.bitString = new StringBuilder();
        System.out.println("NUM_BYTES_TO_DECODE: "+bytes.length); //DEBUG
        for(int i=0; i < bytes.length;i++){
            //https://stackoverflow.com/questions/12310017/how-to-convert-a-byte-to-its-binary-string-representation
            this.bitString.append(Integer.toBinaryString((bytes[i] & 0xFF) + 0x100).substring(1));
            //System.out.println(Integer.toBinaryString((bytes[i] & 0xFF) + 0x100).substring(1));
        }
        //System.out.println("Encoded BitSet: "+this.bitString.toString());
        this.counter = 0;
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
        //System.out.println("COUNTER:"+counter);
        String subBits = this.bitString.substring(counter,counter+numBits);
        this.counter += numBits;
        //this.bitString.delete(0, numBits);
        return Integer.parseInt(subBits, 2);
    }
    public ByteArrayOutputStream getByteArray()
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for(int i=0;i<this.bitString.length();i+=8){
            bytes.write((byte)Integer.parseInt(this.bitString.substring(i,i+8),2));//Parse each byte from the bitstring
        }
        return bytes;
    }
    public StringBuilder getBitString(){return this.bitString;}
    public String toString(){ return this.bitString.toString(); }
    public int getCounter() { return this.counter; }
    public int length(){ return this.bitString.length(); }
}
