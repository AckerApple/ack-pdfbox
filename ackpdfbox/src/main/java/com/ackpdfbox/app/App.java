package com.ackpdfbox.app;

import java.io.IOException;

import com.ackpdfbox.app.ImageAdder;
import com.ackpdfbox.app.FieldReader;
import com.ackpdfbox.app.FieldFiller;

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
      switch(args[0]){
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

        default:usage();
      }
    }
  }

  private static void usage(){
    System.err.println("usage: FieldManager.jar <read|fill> <pdf-path>");
  }

  private static void fillUsage(){
    System.err.println("usage: FieldManager.jar fill <pdf-path> <json-path> <out-path>");
  }

  private static void addImageUsage(){
    System.err.println("usage: FieldManager.jar add-image <pdf-path> <image-path> <page> <x> <y> <out-path>");
  }
}