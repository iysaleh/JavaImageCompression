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
 
    public static void runLengthGrayEncode(String inputPath, String outputPath,int encodedBitSize) throws Exception
    {
        BufferedImage srcImg = loadGrayscaleImage(new File(inputPath));
        WritableRaster wrSrc = srcImg.getRaster();
        
      
        VerboseBitSet bits = new VerboseBitSet();

        //Add the image height and image width parameters to the bits (16 bits-2bytes each).
        bits.addInt(srcImg.getWidth(), 16);
        bits.addInt(srcImg.getHeight(), 16);
        
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
            if(bitString.charAt(i)==currentlyEncoding && runLength < (int)Math.pow(2, encodedBitSize)-1)
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
            compressedBitstring.addInt(Integer.parseInt(token), encodedBitSize);
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
        //FOR SOME REASON THAT I DON'T UNDERSTAND, WE LOSE A SINGLE BIT HERE (WE ARE ONE BIT SHORT)... Add in 0 padding.
        uncompressedBits.padZeros();
        
        int imWidth = uncompressedBits.popInt(16);
        int imHeight = uncompressedBits.popInt(16);
        BufferedImage dest = new BufferedImage(imWidth,imHeight,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wrDest = dest.getRaster();
        
        System.out.println("UncompressedBitstringDecode: "+uncompressedBits.getBitString().toString().substring(0,64));//DEBUG
        System.out.println("UncompressedBitstringDecodeLength: "+uncompressedBits.getBitString().toString().length());
        
        for(int i=0;i<imWidth;i++){ //
            for(int j=0;j<imHeight;j++){
                wrDest.setSample(i, j, 0, uncompressedBits.popInt(8));
            }
        }
        dest.setData(wrDest);
        ImageIO.write(dest, getExtension(outputPath), new File(outputPath));
    }
    public static void runLengthBitPlaneEncode(String inputPath, String outputPath,int encodedBitSize) throws Exception
    {
        BufferedImage srcImg = loadGrayscaleImage(new File(inputPath));
        WritableRaster wrSrc = srcImg.getRaster();
        
      
        VerboseBitSet bits = new VerboseBitSet();
        VerboseBitSet bitplaneBits = new VerboseBitSet();

        //Add the image height and image width parameters to the bits (16 bits-2bytes each).
        bitplaneBits.addInt(srcImg.getWidth(), 16);
        bitplaneBits.addInt(srcImg.getHeight(), 16);
        
        //Start by encoding the runlength of 0
        ///This algorithm will always start with zero (decoding/encoding)
        char currentlyEncoding = '0';
        int runLength = 0;
        
        for(int i=0;i<wrSrc.getWidth();i++){
            for(int j=0;j<wrSrc.getHeight();j++){
                bits.addByteSizedInt(wrSrc.getSample(i, j, 0));
            }
        }
        System.out.println("UncompressedBitStringEncode: "+bits.getBitString().substring(0,128));//debug
        System.out.println("UncompressedBitStringEncodeLength: "+bits.getBitString().length());//debug
        
        //Go through all of the bits and reorganize them so that they are structured by bitplanes.
        for(int i=0; i<8;i++){//0-7 bitplanes
            for(int j=i;j<bits.length();j+=8)
            {
                bitplaneBits.addChar(bits.getBitString().charAt(j));
            }
        }
        //Use same logic from run-length-encode from this point on.
        bits = bitplaneBits;
            
        
        //System.out.println(bits.toString());
        StringBuilder encodedString = new StringBuilder();


        System.out.println("UncompressedBitPlaneBitStringEncode: "+bits.getBitString().substring(0,128));//debug
        System.out.println("UncompressedBitPlaneBitStringEncodeLength: "+bits.getBitString().length());//debug
       
        //ByteArrayOutputStream bytesEncoded = new ByteArrayOutputStream();
        String bitString = bits.toString();        
        for(int i=0; i < bits.length(); i++){
            if(bitString.charAt(i)==currentlyEncoding && runLength < (int)Math.pow(2,encodedBitSize)-1)
                runLength++;
            else if(bitString.charAt(i)!=currentlyEncoding){
                encodedString.append(runLength+",");
                currentlyEncoding = swapCurrentlyEncoding(currentlyEncoding);
                runLength=1;
            }
            else{ //runLength >= 254(or encodedBitSizeBits) but still encoding char!
                //bytesEncoded.write(runLength);
                encodedString.append(runLength+",");
                encodedString.append("0,");
                runLength=1;
            }
        }
        System.out.println("BITS_IN_ENCODED_STRING: "+encodedString.length()); //debug
        System.out.println("RunLengthBitString: "+encodedString.substring(0,128));//debug
        
        //Write encoded bytes to file
        //bytesEncoded.writeTo(new FileOutputStream(outputPath));

        ByteArrayOutputStream bytesEncoded = new ByteArrayOutputStream();
        String[] runLengthTokens = encodedString.toString().split(",");
        System.out.println(runLengthTokens.length); //DEBUG
        
        
        VerboseBitSet compressedBitstring = new VerboseBitSet();
        for(String token: runLengthTokens){
            compressedBitstring.addInt(Integer.parseInt(token), encodedBitSize);
        }
        System.out.println("Encoded Compressed Bitstring: "+ compressedBitstring.getBitString().substring(0, 128));
        System.out.println("Encoded Compressed Bitstring Length: "+ compressedBitstring.getBitString().length());
        
        compressedBitstring.getByteArray().writeTo(new FileOutputStream(outputPath));
    }
    public static void runLengthBitPlaneDecode(String inputPath, String outputPath,int encodedBitSize) throws Exception
    {
        //Read the compressed bytes.
        byte[] inputByteArray = Files.readAllBytes(Paths.get(inputPath));
        VerboseBitSet compressedBits = new VerboseBitSet(inputByteArray);
        VerboseBitSet uncompressedBitsBitplane = new VerboseBitSet();
        VerboseBitSet uncompressedBits = new VerboseBitSet();

        
        char currentlyDecoding = '0';
        int runLength = 0;
        System.out.println("BIT LENGTH: "+compressedBits.length());
        String compressedBitString = compressedBits.getBitString().toString();
        System.out.println("CompressedBitStringLength: "+compressedBitString.length());
        System.out.println("CompressedBitstring: "+compressedBitString.substring(0, 128));//DEBUG

        
        for(int i=0;i<compressedBitString.length();i+=encodedBitSize){
            runLength = Integer.parseInt(compressedBitString.substring(i,i+encodedBitSize),2);
            for(int j=0; j<runLength;j++){
                if(currentlyDecoding=='0')
                    uncompressedBitsBitplane.addZero();
                else
                    uncompressedBitsBitplane.addOne();
            }
            currentlyDecoding = swapCurrentlyEncoding(currentlyDecoding);
        }
        //FOR SOME REASON THAT I DON'T UNDERSTAND, WE LOSE BITS HERE... Add in some 0-padding to make it byte-sized.
        uncompressedBitsBitplane.padZeros();

        System.out.println("UncompressedBitPlaneBitstringDecode: "+uncompressedBitsBitplane.getBitString().toString().substring(0,128));//DEBUG
        System.out.println("UncompressedBitPlaneBitstringDecodeLength: "+uncompressedBitsBitplane.getBitString().toString().length());
        
        //Go through all of the bits and reorganize them so that they are structured by bitplanes.
        int uncompressedBitStringLength = uncompressedBitsBitplane.length();
        int bitPlaneScalar = (uncompressedBitStringLength-32)/8;
        for(int j=32;j<bitPlaneScalar+32;j++)//First 32 bits are reserved for imWidth && imHeight!
        {
            uncompressedBits.addChar(uncompressedBitsBitplane.getBitString().charAt((bitPlaneScalar*0)+j));
            uncompressedBits.addChar(uncompressedBitsBitplane.getBitString().charAt((bitPlaneScalar*1)+j));
            uncompressedBits.addChar(uncompressedBitsBitplane.getBitString().charAt((bitPlaneScalar*2)+j));
            uncompressedBits.addChar(uncompressedBitsBitplane.getBitString().charAt((bitPlaneScalar*3)+j));
            uncompressedBits.addChar(uncompressedBitsBitplane.getBitString().charAt((bitPlaneScalar*4)+j));
            uncompressedBits.addChar(uncompressedBitsBitplane.getBitString().charAt((bitPlaneScalar*5)+j));
            uncompressedBits.addChar(uncompressedBitsBitplane.getBitString().charAt((bitPlaneScalar*6)+j));
            uncompressedBits.addChar(uncompressedBitsBitplane.getBitString().charAt((bitPlaneScalar*7)+j));
        }
        
        int imWidth = uncompressedBitsBitplane.popInt(16);
        int imHeight = uncompressedBitsBitplane.popInt(16);
        BufferedImage dest = new BufferedImage(imWidth,imHeight,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wrDest = dest.getRaster();
        
        System.out.println("UncompressedBitStringDecode: "+uncompressedBits.getBitString().toString().substring(0,128));//DEBUG
        System.out.println("UncompressedBitStringDecodeLength: "+uncompressedBits.getBitString().toString().length());
        
        for(int i=0;i<imWidth;i++){ //
            for(int j=0;j<imHeight;j++){
                wrDest.setSample(i, j, 0, uncompressedBits.popInt(8));
            }
        }
        dest.setData(wrDest);
        ImageIO.write(dest, getExtension(outputPath), new File(outputPath));
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
        StringHolder encodingBitSize = new StringHolder();
        StringHolder compressionType = new StringHolder();
        BooleanHolder decode = new BooleanHolder();
        
        /////THIS IS JUST SO I DON'T NEED TO TYPE IN COMMAND LINE ARGUMENTS FROM NETBEANS
        args = new String[] {"-input","lena.gif","-output","lenaRLGCompressed.gif","-compressionType","runLengthGray","-encodingBitSize","4"};
        args = new String[] {"-input","lenaRLGCompressed.gif","-output","lenaRLGUncompressed.gif","-compressionType","runLengthGray","-encodingBitSize","4","-decode"};
        args = new String[] {"-input","lena.gif","-output","lenaRLGBitplaneCompressed.gif","-compressionType","runLengthBitPlane","-encodingBitSize","4"};
        args = new String[] {"-input","lenaRLGBitplaneCompressed.gif","-output","lenaRLGBitplaneUncompressed.gif","-compressionType","runLengthBitPlane","-encodingBitSize","4","-decode"};

        //Create argument parser for command line arguments
        ArgParser parser = new ArgParser("Java ImageCompressor CLI Application");
        parser.addOption("-input %s #Relative path to input file to be processed", input);
        parser.addOption("-output %s #Relative path to output file to be written", output);
        parser.addOption("-encodingBitSize %s #Amount of bits to use for each encoded value--Use same bit size for compression & decompression!!!!", encodingBitSize);
        parser.addOption("-compressionType %s #Compression type to utilize for encoding/decoding (runLengthGray,runLengthBitPlane,huffman,lzw)", compressionType);
        parser.addOption("-decode %v #If specified, decode action will be performed. Otherwise the program will encode the source input.", decode);
        parser.matchAllArgs (args);
        
        //Catch null required commandline arguments and exit program
        try{
            if(input.value.equals("null") || output.value.equals("null") || compressionType.value.equals("null") || encodingBitSize.value.equals("null")){
                //NullPointerException will happen if command line args not input, go to catch.
            }
        }
        catch(NullPointerException e){
            System.out.println("You must specify the input, output and compressionType arguments!");
            System.out.println("Run -help for argument usage printout.");
            System.exit(1);            
        }
        
        int encodingBitSizeInt = Integer.parseInt(encodingBitSize.value);
        if(!(encodingBitSizeInt!=2 || encodingBitSizeInt!=4 || encodingBitSizeInt!=8 || encodingBitSizeInt!=16)){
            System.out.println("Encoding Bit Size (the number of bits used per each compressed run-length value) must be 2,4,8 or 16.");
            System.out.println("Be sure to use the same value for compression && decompression!");
            System.exit(1);
        }
        
        //System.out.println(String.format("%08d", Integer.valueOf(Integer.toBinaryString(0))));
        
        //Perform encoding/decoding compressionType for input stream based on user supplied inputs.
        switch(compressionType.value.toString()){
            case "runLengthGray":
                if(decode.value){
                    System.out.println("RUN LENGTH GREY DECODE!");
                    runLengthGrayDecode(input.value,output.value,encodingBitSizeInt);
                    System.exit(0);
                }
                else{ //Decode not set so Encode
                    //runLengthGrayEncodeVerboseBitset(input.value,output.value);
                    runLengthGrayEncode(input.value,output.value,encodingBitSizeInt);
                    System.exit(0);
                }
            case "runLengthBitPlane":
                if(decode.value){
                    runLengthBitPlaneDecode(input.value,output.value,encodingBitSizeInt);
                    System.exit(0);
                }
                else{ //Decode not set so Encode
                    runLengthBitPlaneEncode(input.value,output.value,encodingBitSizeInt);
                    System.exit(0);
                }
            case "huffman":
                if(decode.value){
                    huffmanDecode(input.value,output.value);
                    System.exit(0);
                }
                else{ //Decode not set so Encode
                    huffmanEncode(input.value,output.value);
                    System.exit(0);
                }
            case "lzw":
                if(decode.value){
                    lzwDecode(input.value,output.value);
                    System.exit(0);
                }
                else{ //Decode not set so Encode
                    lzwEncode(input.value,output.value);
                    System.exit(0);
                }
        }
        
    }
    
}
