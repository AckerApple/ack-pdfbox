package com.ackpdfbox.app;

import java.io.IOException;

import com.ackpdfbox.app.ImageAdder;
import com.ackpdfbox.app.FieldReader;
import com.ackpdfbox.app.FieldFiller;

import com.ackpdfbox.app.Decrypt;
import com.ackpdfbox.app.Encrypt;
import org.apache.pdfbox.util.Version;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class App{
  /**
   * This will read a PDF file and print out the form elements. <br>
   * see usage() for commandline
   * 
   * @param args command line arguments
   * 
   * @throws IOException If there is an error importing the FDF document.
   */
  public static void main(String[] args) throws IOException{
    if (args.length<1){
      usage();
    }else{
      String command = args[0];
      String[] arguments = new String[args.length - 1];
      System.arraycopy(args, 1, arguments, 0, arguments.length);

      switch( command.toLowerCase() ){
        case "read":
          FieldReader fieldReader = new FieldReader();
          fieldReader.loadPdfByPath( args[1] );
          String json = fieldReader.getFields();
          if(args.length>2){
            fieldReader.write(json, args[2]);
          }else{
            System.out.println( json );
          }
          break;

        case "fill":
          if(args.length < 4){
            fillUsage();
          }else{
            new FieldFiller(args[1], args[2], args[3]).execute();
          }
          break;

        case "add-image":
          if(args.length < 7){
            addImageUsage();
          }else{
            ImageAdder imageAdder = new ImageAdder();
            imageAdder.loadPdfByPath( args[1] );
            imageAdder.setOutPath( args[6] );
            imageAdder.setImagePath( args[2] );
            imageAdder.setPageNumber( Integer.parseInt(args[3]) );
            imageAdder.setCords(Float.parseFloat( args[4] ), Float.parseFloat( args[5] ));
            imageAdder.execute();
            imageAdder.save();
          }
          break;
        
        case "encrypt":
          addBouncyCastle();
          try{
            com.ackpdfbox.app.Encrypt.main(arguments);
          }catch(Exception e){
            e.printStackTrace();
          }
          break;
        
        case "decrypt":
          addBouncyCastle();
          try{
            com.ackpdfbox.app.Decrypt.main(arguments);
          }catch(Exception e){
            System.out.println(arguments[0]);
            e.printStackTrace();
          }
          break;
        
        case "pdftoimage":
          try{
            com.ackpdfbox.app.PDFToImage.main(arguments);
          }catch(Exception e){
            System.out.println(arguments[0]);
            e.printStackTrace();
          }
          break;

        case "-version":
          String version = org.apache.pdfbox.util.Version.getVersion();
          if (version != null){
              System.out.println("PDFBox version: \""+version+"\"");
          }else{
            System.out.println("unknown");
          }
          break;

        default:usage();
      }
    }
  }

  public static void addBouncyCastle(){
    System.out.println("added bouncy");
    Security.addProvider(new BouncyCastleProvider());
  }

  private static void usage(){
    System.err.println("usage: <read|fill|pdftoimage|add-image|encrypt|decrypt|-version>");
  }

  private static void fillUsage(){
    System.err.println("usage: fill <pdf-path> <json-path> <out-path>");
  }

  private static void addImageUsage(){
    System.err.println("usage: add-image <pdf-path> <image-path> <page> <x> <y> <out-path>");
  }
}