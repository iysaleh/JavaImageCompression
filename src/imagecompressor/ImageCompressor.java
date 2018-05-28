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
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


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
    
    public static void runLengthGrayEncodeBitset(String inputPath, String outputPath) throws Exception
    {
        BufferedImage srcImg = loadGrayscaleImage(new File(inputPath));
        WritableRaster wrSrc = srcImg.getRaster();
        
        
        
        BitSet encodedBits = new BitSet();
        //First 24bits encode width of image
        //Second 24bits encode height of image
        //This algorihtm always starts with 0 for RL encoding.
        for(int i=0;i<wrSrc.getWidth();i++){
            for(int j=0;j<wrSrc.getHeight();j++){
                //TODO
            }
        }
        
        //Write encodedBits byte[] to file.
        Files.write(Paths.get(outputPath), encodedBits.toByteArray(),StandardOpenOption.CREATE_NEW);
    }
    public static void runLengthGrayEncode(String inputPath, String outputPath) throws Exception
    {
        BufferedImage srcImg = loadGrayscaleImage(new File(inputPath));
        WritableRaster wrSrc = srcImg.getRaster();

        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ByteArrayOutputStream bytesEncoded = new ByteArrayOutputStream();
        
        //TODO: Add image height and width to bytes encoded
        
        for(int i=0;i<wrSrc.getWidth();i++){
            for(int j=0;j<wrSrc.getHeight();j++){
                bytes.write(wrSrc.getSample(i, j, 0)); //Add pixel to bytes array
            }
        }
        BitSet bits = BitSet.valueOf(bytes.toByteArray());
        
        
        System.out.println(bits.toString());
        /*
        for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i + 1)) {
            indexes.add(i);
        }*/

        
    }    
    public static void runLengthGrayEncodeVerboseBitset(String inputPath, String outputPath) throws Exception
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
                bits.addByte(wrSrc.getSample(i, j, 0));
            }
        }
        
        System.out.println(bits.toString());
        StringBuilder encodedString = new StringBuilder();
        
        ByteArrayOutputStream bytesEncoded = new ByteArrayOutputStream();
        String bitString = bits.toString();
        for(int i=0; i < bits.length(); i++){
            if(bitString.charAt(i)==currentlyEncoding && runLength <= 254)
                runLength++;
            else{
                bytesEncoded.write(runLength);
                encodedString.append(runLength+",");
                if(currentlyEncoding=='0')
                    currentlyEncoding='1';
                else
                    currentlyEncoding='0';
                runLength=0;
            }
        }
        //Write encoded bytes to file
        bytesEncoded.writeTo(new FileOutputStream(outputPath));

        
        //BitSet bits = BitSet.valueOf(imageBytes);
        //System.out.println(bits.toString());
       
        //Write encodedBits byte[] to file.
        //Files.write(Paths.get(outputPath), encodedBits.toByteArray(),StandardOpenOption.CREATE_NEW);
    }
    public static void runLengthGrayDecode(String inputPath, String outputPath) throws Exception
    {
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
                    runLengthGrayDecode(input.value,output.value);
                else //Decode not set so Encode
                    runLengthGrayEncodeVerboseBitset(input.value,output.value);
                    //runLengthGrayEncode(input.value,output.value);
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
