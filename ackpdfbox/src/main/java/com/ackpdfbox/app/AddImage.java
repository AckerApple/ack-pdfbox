package com.ackpdfbox.app;

import java.util.*;
import java.io.File;
import com.ackpdfbox.app.ImageAdder;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public final class AddImage{
  private AddImage(){
  }

  public static void process(ImageAdder imageAdder, PDDocument doc, String img, Float x, Float y, String width, String height, Integer page) throws IOException{
    if(page==null || page==-1){
      int count = doc.getNumberOfPages();
      PDPage newPage = new PDPage();
      doc.addPage(newPage);
      page = count;
    }

    imageAdder.pdf = doc;
    imageAdder.setImagePath( img );

    PDImageXObject ximg = imageAdder.paramImage();
    PDPage samplePage = doc.getPage(0);
    
    Float imgHeight = new Float( ximg.getHeight() );
    Boolean isPercent = (width!=null && width.indexOf("%")>=0);

    if(isPercent){
      Integer percent = Integer.parseInt( width.substring(0, width.length()-1) );
      Float pageWidth = samplePage.getMediaBox().getWidth();
      Float newWidth = new Float( pageWidth * (percent*.01) );
      imageAdder.width = newWidth;
      imgHeight = ximg.getHeight() * (newWidth / ximg.getWidth());
      imageAdder.height = imgHeight;
    
    }else{
      if(width==null){
        imageAdder.width = null;
      }else{
        imageAdder.width = Float.parseFloat(width);
      }

      if(height==null){
        imageAdder.height = null;
      }else{
        imageAdder.height = Float.parseFloat(height);
        imgHeight = imageAdder.height;
      }
    }

    if(y==null || y==-1){
      Float sampleHeight = samplePage.getMediaBox().getHeight();
      y = sampleHeight - imgHeight;
    }

    if(x==null){
      x = Float.parseFloat("0");
    }


    imageAdder.setPageNumber( page );
    imageAdder.setCords(x, y);

    imageAdder.execute();
  }

  public static void main( String[] args ) throws IOException{
    String width = null;
    String height = null;
    Float x = null;
    Float y = null;
    Integer page = null;
    String out = null;
    ArrayList<String> images = new ArrayList<String>();
    
    for( int i=0; i<args.length; i++ ){
      String key = args[i];

      if(i>1 && key.charAt(0)!='-'){
        images.add( key );
        continue;
      }

      if( key.equals( "-x" ) ){
        x = Float.parseFloat( args[++i] );
      }else if(key.equals( "-y" )){
        y = Float.parseFloat( args[++i] );
      }else if(key.equals( "-width" )){
        width = args[++i];
      }else if(key.equals( "-height" )){
        height = args[++i];
      }else if(key.equals( "-page" )){
        page = Integer.parseInt( args[++i] );
      }else if(key.equals( "-out" )){
        out = args[++i];
      }
    }

    PDDocument doc = PDDocument.load(new File(args[1]));
    ImageAdder imageAdder = new ImageAdder();

    if(out==null){
      out = args[1];
    }
    imageAdder.setOutPath( out );
    
    for( int i=0; i<images.size(); i++ ){
      process(imageAdder, doc, images.get(i), x, y, width, height, page);
    }

    imageAdder.save();
  }
}
