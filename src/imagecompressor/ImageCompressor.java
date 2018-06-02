/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagecompressor;


import java.util.BitSet;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import javax.imageio.ImageIO;
import java.io.File;

import argparser.ArgParser;
import argparser.StringHolder;
import argparser.BooleanHolder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.StringTokenizer;


/*
    Third Party Software Libraries included:
        Argparser from: http://www.cs.ubc.ca/~lloyd/java/argparser.html
            *This is a command line argument parser library.
*/

/**
 *
 * @author Ibraheem Saleh
 */
public class ImageCompressor {
    
    public static BufferedImage loadGrayscaleImage(File file) throws Exception
    {
        BufferedImage src = ImageIO.read(file);
        //Create a placedholder BufferedImage object which operates in the grayscale domain.
        BufferedImage destGray = new BufferedImage(src.getWidth(),src.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        
        WritableRaster wrSrc = src.getRaster();
        WritableRaster wrDestGray = destGray.getRaster();

        System.out.println("FIRST 3 ENC IM VALS: "+ wrSrc.getSample(0, 0, 0) +"," +wrSrc.getSample(0, 1, 0) + ","+wrSrc.getSample(0, 2, 0));
        
        //Copy each sample from the original image into the grayscale domain BufferedImage.
        for(int i=0;i<wrSrc.getWidth();i++){
            for(int j=0;j<wrSrc.getHeight();j++){
                wrDestGray.setSample(i, j, 0, wrSrc.getSample(i, j, 0));
            }
        }
        destGray.setData(wrDestGray);
        return destGray;
    }
    
    public static String getExtension(String fileString)
    {
        String[] tokens = fileString.split("\\.(?=[^\\.]+$)");
        return tokens[1];
    }
    public static char swapCurrentlyEncoding(char encoding)
    {
        if(encoding=='1')
            return '0';
        else
            return '1';
    }
    
    public static byte[] bufferedImageToByteArray(BufferedImage img,String srcFormat)throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, srcFormat, baos);
        return baos.toByteArray();
    }
    public static void addIntToBitSet(BitSet bits,int val)
    {
        //BitSet valBits = BitSet.valueOf()
    }
 
    public static void runLengthGrayEncode(String inputPath, String outputPath) throws Exception
    {
        BufferedImage srcImg = loadGrayscaleImage(new File(inputPath));
        WritableRaster wrSrc = srcImg.getRaster();
        
        PrintStream printStream = new PrintStream(new FileOutputStream(outputPath));
        
        //Add the imageWidth and imageHeight parameters to the compressed file.
        printStream.print(srcImg.getWidth()+","+srcImg.getHeight()+",");
        
        VerboseBitSet bits = new VerboseBitSet();

        //Start by encoding the runlength of 0
        ///This algorithm will always start with zero (decoding/encoding)
        char currentlyEncoding = '0';
        int runLength = 0;
        
        for(int i=0;i<wrSrc.getWidth();i++){
            for(int j=0;j<wrSrc.getHeight();j++){
                bits.addByteSizedInt(wrSrc.getSample(i, j, 0));
            }
        }
        
        //System.out.println(bits.toString());
        StringBuilder encodedString = new StringBuilder();


        System.out.println("UncompressedBitStringEncode: "+bits.getBitString().substring(0,64));//debug
        System.out.println("UncompressedBitStringEncodeLength: "+bits.getBitString().length());//debug
       
        //ByteArrayOutputStream bytesEncoded = new ByteArrayOutputStream();
        String bitString = bits.toString();        
        for(int i=0; i < bits.length(); i++){
            if(bitString.charAt(i)==currentlyEncoding && runLength <= 254)
                runLength++;
            else if(bitString.charAt(i)!=currentlyEncoding){
                encodedString.append(runLength+",");
                currentlyEncoding = swapCurrentlyEncoding(currentlyEncoding);
                runLength=1;
            }
            else{ //runLength >= 254 but still encoding char!
                //bytesEncoded.write(runLength);
                encodedString.append(runLength+",");
                encodedString.append("0,");
                runLength=1;
            }
        }
        System.out.println("BITS_IN_ENCODED_STRING: "+encodedString.length()); //debug
        System.out.println("RunLengthBitString: "+encodedString.substring(0,64));//debug
        
        //Write encoded bytes to file
        //bytesEncoded.writeTo(new FileOutputStream(outputPath));

        ByteArrayOutputStream bytesEncoded = new ByteArrayOutputStream();
        String[] runLengthTokens = encodedString.toString().split(",");
        System.out.println(runLengthTokens.length); //DEBUG
        
        
        VerboseBitSet compressedBitstring = new VerboseBitSet();
        for(String token: runLengthTokens){
            compressedBitstring.addInt(Integer.parseInt(token), 8);
        }
        System.out.println("Encoded Compressed Bitstring: "+ compressedBitstring.getBitString().substring(0, 64));
        System.out.println("Encoded Compressed Bitstring Length: "+ compressedBitstring.getBitString().length());
        
        compressedBitstring.getByteArray().writeTo(new FileOutputStream(outputPath));
        
        /*//NO IDEA IF THIS WORKS
        for(String token: runLengthTokens){
            bytesEncoded.write((byte)Integer.parseInt(token));
        }
        bytesEncoded.writeTo(new FileOutputStream(outputPath));
        */
        
        
        //BitSet bits = BitSet.valueOf(imageBytes);
        //System.out.println(bits.toString());
       
        //Write encodedBits byte[] to file.
        //Files.write(Paths.get(outputPath), encodedBits.toByteArray(),StandardOpenOption.CREATE_NEW);
    }
    //encodedBitSize is the number of bits used to encode each runlength.
    public static void runLengthGrayDecode(String inputPath, String outputPath,int encodedBitSize) throws Exception
    {
        //Read the compressed bytes.
        byte[] inputByteArray = Files.readAllBytes(Paths.get(inputPath));
        VerboseBitSet compressedBits = new VerboseBitSet(inputByteArray);
        VerboseBitSet uncompressedBits = new VerboseBitSet();
        
        //TODO: FIX Hardcoding dimensions since they are not in the encoded output yet.
        BufferedImage dest = new BufferedImage(512,512,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wrDest = dest.getRaster();

        int imWidth = wrDest.getWidth();
        int imHeight = wrDest.getHeight();
        
        
        char currentlyDecoding = '0';
        int runLength = 0;
        System.out.println("BIT LENGTH: "+compressedBits.length());
        String compressedBitString = compressedBits.getBitString().toString();
        System.out.println("CompressedBitStringLength: "+compressedBitString.length());
        System.out.println("CompressedBitstring: "+compressedBitString.substring(0, 64));//DEBUG
        /*
        while(compressedBits.getCounter()+encodedBitSize < compressedBits.length()){
            runLength = compressedBits.popInt(encodedBitSize);
            for(int i=0; i <runLength; i++)
            {
                if(currentlyDecoding=='0')
                    uncompressedBits.addZero();
                else
                    uncompressedBits.addOne();
            }
            if(currentlyDecoding=='0')
                currentlyDecoding='1';
            else
                currentlyDecoding='0';
        }
        */
        for(int i=0;i<compressedBitString.length();i+=encodedBitSize){
            runLength = Integer.parseInt(compressedBitString.substring(i,i+encodedBitSize),2);
            for(int j=0; j<runLength;j++){
                if(currentlyDecoding=='0')
                    uncompressedBits.addZero();
                else
                    uncompressedBits.addOne();
            }
            currentlyDecoding = swapCurrentlyEncoding(currentlyDecoding);
        }
        //FOR SOME REASON THAT I DON'T UNDERSTAND, WE LOSE A SINGLE BIT HERE (WE ARE ONE BIT SHORT)... Add in a random 0.
        uncompressedBits.addZero();
        
        
        System.out.println("UncompressedBitstringDecode: "+uncompressedBits.getBitString().toString().substring(0,64));//DEBUG
        System.out.println("UncompressedBitstringDecodeLength: "+uncompressedBits.getBitString().toString().length());
        
        for(int i=0;i<wrDest.getWidth();i++){ //
            for(int j=0;j<wrDest.getHeight();j++){
                wrDest.setSample(i, j, 0, uncompressedBits.popInt(8));
            }
        }
        dest.setData(wrDest);
        ImageIO.write(dest, getExtension(outputPath), new File(outputPath));
    }
    public static void runLengthBitPlaneEncode(String inputPath, String outputPath) throws Exception
    {
    }
    public static void runLengthBitPlaneDecode(String inputPath, String outputPath) throws Exception
    {
    }
    public static void huffmanEncode(String inputPath, String outputPath) throws Exception
    {
    }
    public static void huffmanDecode(String inputPath, String outputPath) throws Exception
    {
    }
    public static void lzwEncode(String inputPath, String outputPath) throws Exception
    {
    }
    public static void lzwDecode(String inputPath, String outputPath) throws Exception
    {
    }
    

    public static void main(String[] args) throws Exception{
        StringHolder input = new StringHolder();
        StringHolder output = new StringHolder();
        StringHolder compressionType = new StringHolder();
        BooleanHolder decode = new BooleanHolder();
        
        //DELETE ME BEFORE SUBMISSION!
        /////THIS IS JUST SO I DON'T NEED TO TYPE IN COMMAND LINE ARGUMENTS FROM NETBEANS
        args = new String[] {"-input","lena.gif","-output","lenaRLGCompressed.gif","-compressionType","runLengthGray"};
        args = new String[] {"-input","lenaRLGCompressed.gif","-output","lenaRLGUncompressed.gif","-compressionType","runLengthGray", "-decode"};

        //Create argument parser for command line arguments
        ArgParser parser = new ArgParser("Java ImageCompressor CLI Application");
        parser.addOption("-input %s #Relative path to input file to be processed", input);
        parser.addOption("-output %s #Relative path to output file to be written", output);
        parser.addOption("-compressionType %s #Compression type to utilize for encoding/decoding (runLengthGray,runLengthBitPlane,huffman,lzw)", compressionType);
        parser.addOption("-decode %v #If specified, decode action will be performed. Otherwise the program will encode the source input.", decode);
        parser.matchAllArgs (args);
        
        //Catch null required commandline arguments and exit program
        try{
            if(input.value.equals("null") || output.value.equals("null") || compressionType.value.equals("null")){
                //NullPointerException will happen if command line args not input, go to catch.
            }
        }
        catch(NullPointerException e){
            System.out.println("You must specify the input, output and compressionType arguments!");
            System.out.println("Run -help for argument usage printout.");
            System.exit(1);            
        }
        
        //System.out.println(String.format("%08d", Integer.valueOf(Integer.toBinaryString(0))));
        
        //Perform encoding/decoding compressionType for input stream based on user supplied inputs.
        switch(compressionType.value){
            case "runLengthGray":
                if(decode.value)
                    runLengthGrayDecode(input.value,output.value,8);
                else //Decode not set so Encode
                    //runLengthGrayEncodeVerboseBitset(input.value,output.value);
                    runLengthGrayEncode(input.value,output.value);
            case "runLengthBitPlane":
                if(decode.value)
                    runLengthBitPlaneDecode(input.value,output.value);
                else //Decode not set so Encode
                    runLengthBitPlaneEncode(input.value,output.value);
            case "huffman":
                if(decode.value)
                    huffmanDecode(input.value,output.value);
                else //Decode not set so Encode
                    huffmanEncode(input.value,output.value);
            case "lzw":
                if(decode.value)
                    lzwDecode(input.value,output.value);
                else //Decode not set so Encode
                    lzwEncode(input.value,output.value);
        }
        
    }
    
}
