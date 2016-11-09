package com.ackpdfbox.app;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
//import org.apache.pdfbox.pdmodel.graphics.image.PDPixelMap;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPage;

public class ImageAdder{
  public PDDocument pdf;
  public String outPath;
  public Integer pageNumber;
  public Float height;
  public Float width;
  public Float x;
  public Float y;
  public Boolean isPngStream;
  //public String imgPath;
  public InputStream imageStream;

  public ImageAdder(){
  }

  public void setOutPath(String path){
    this.outPath = path;
  }
 
  public void setImagePath(String path) throws IOException{
    this.imageStream = new FileInputStream(path);
  }

  public void setImageByBase64String(String data){
    int commaPos = data.indexOf(",");
    String imgData = data;
  
    if(commaPos>0){
      imgData = data.substring(commaPos+1);
      this.isPngStream = data.substring(0, commaPos).indexOf("png")>0;
    }

    this.imageStream = new ByteArrayInputStream( Base64.getDecoder().decode( imgData ) );
  }

  public void setPageNumber(Integer num){
    this.pageNumber = num;
  }

  public void setCords(Float x, Float y, Float width, Float height){
    this.x = x;
    this.y = y;

    if(width!=null){
      this.width = width;
    }

    if(height!=null){
      this.height = height;
    }
  }

  public void setCords(Float x, Float y){
    setCords(x,y,null,null);
  }

  public PDDocument filePathToPdf(String path) throws IOException{
    return PDDocument.load(new File(path));
  }

  public void loadPdfByPath(String path) throws IOException{
    this.pdf = filePathToPdf(path);
  }

  public void execute() throws IOException{
    PDPage pdfPage = this.pdf.getPages().get( this.pageNumber );
    PDPageContentStream contentStream = new PDPageContentStream(this.pdf, pdfPage, true, true, true);
    
    PDImageXObject ximage = null;
    if(this.isPngStream!=null && this.isPngStream==true){
      BufferedImage bImageFromConvert = ImageIO.read(this.imageStream);
      ximage = LosslessFactory.createFromImage(this.pdf, bImageFromConvert);
    }else{
      ximage = JPEGFactory.createFromStream(this.pdf, this.imageStream);
    }


    if(this.width!=null && this.height!=null){
      contentStream.drawImage(ximage, this.x, this.y, this.width, this.height);
    }else{
      contentStream.drawImage(ximage, this.x, this.y);
    }
    contentStream.close();
  }

  public void save() throws IOException{
    this.pdf.setAllSecurityToBeRemoved(true);
    this.pdf.save( this.outPath );
    this.pdf.close();
  }
}
