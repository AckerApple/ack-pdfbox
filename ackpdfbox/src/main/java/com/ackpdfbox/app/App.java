package com.ackpdfbox.app;

import java.io.IOException;

import com.ackpdfbox.app.AddImage;
import com.ackpdfbox.app.FieldReader;
import com.ackpdfbox.app.FieldFiller;

import com.ackpdfbox.app.Decrypt;
import com.ackpdfbox.app.Encrypt;
import com.ackpdfbox.app.CreateSignature;
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
            FieldFiller fieldFiller = new FieldFiller(args[1], args[2], args[3]);

            //add options
            for( int i=0; i<args.length; i++ ){
              String key = args[i];
              if( key.equals( "-flatten" ) ){
                fieldFiller.flatten = Boolean.parseBoolean( args[++i] );
              }
            }

            fieldFiller.execute();
          }
          break;

        case "add-image":
          com.ackpdfbox.app.AddImage.main(args);
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
        
        case "sign":
          try{
            com.ackpdfbox.app.CreateSignature.main(arguments);
          }catch(Exception e){
            System.out.println(arguments[0]);
            e.printStackTrace();
          }
          break;

        case "pdftoimage":
          try{
            String result = com.ackpdfbox.app.PDFToImage.main(arguments);
            System.out.println(result);
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