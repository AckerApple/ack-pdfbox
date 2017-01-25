package com.ackpdfbox.app;

import com.ackpdfbox.app.ImageAdder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import org.apache.pdfbox.pdmodel.interactive.form.PDField;
//import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
//import org.apache.pdfbox.pdmodel.interactive.form.PDRadioCollection;

public class FieldFiller{
  public String pdfPath;
  public String jsonPath;
  public String outPath;
  public Boolean flatten;

  public FieldFiller(String pdfPath, String jsonPath, String outPath){
    this.pdfPath = pdfPath;
    this.jsonPath = jsonPath;
    this.outPath = outPath;
    this.flatten = false;
  }

  public String getJsonFileString() throws IOException{
    byte[] encoded = Files.readAllBytes( Paths.get(this.jsonPath) );
    return new String(encoded, Charset.forName("UTF-8"));
  }

  private JsonArray getJsonArray() throws IOException{
    String string = getJsonFileString();
    return new Gson().fromJson(string, JsonArray.class);
  }

  private PDDocument loopJsonArray(JsonArray jarr) throws IOException{
    PDDocument pdf = PDDocument.load( new File(this.pdfPath) );
    PDDocumentCatalog docCatalog = pdf.getDocumentCatalog();
    PDAcroForm acroForm = docCatalog.getAcroForm();
    acroForm.setXFA(null);//XFA must be turned off for Acrobat XI and other advanced PDF form readers

    for(JsonElement field : jarr){
      JsonObject fObject = field.getAsJsonObject();
     
      String fullname = fObject.get("fullyQualifiedName").getAsString();
      JsonElement value = fObject.get("value");
      JsonElement remove = fObject.get("remove");

      //System.out.println(fullname+"="+value);
      
      PDField pdField = acroForm.getField(fullname);
      Boolean isReadOnly = pdField.isReadOnly();
      if(isReadOnly==true){
        pdField.setReadOnly(false);
      }

      if(pdField instanceof PDCheckBox){
        PDCheckBox pDCheckBox = (PDCheckBox) acroForm.getField(fullname);
        fillCheckbox(pDCheckBox, value);
      }else if(value!=null){
        pdField.setValue( value.getAsString() );
      }

      if(isReadOnly==true){
        pdField.setReadOnly(true);
      }

      if(fObject.get("base64Overlay")!=null){
        JsonObject base64Overlay = fObject.get("base64Overlay").getAsJsonObject();
        String base64String = base64Overlay.get("uri").getAsString();

        ImageAdder imageAdder = new ImageAdder();
        imageAdder.pdf = pdf;
        imageAdder.setImageByBase64String( base64String );
        imageAdder.setPageNumber( fObject.get("page").getAsInt() );
        
        JsonObject cords = fObject.get("cords").getAsJsonObject();
        float x = Float.parseFloat( cords.get("x").getAsString() );
        float y = Float.parseFloat( cords.get("y").getAsString() );
        
        JsonElement forceWidthHeight = base64Overlay.get("forceWidthHeight");
        if(forceWidthHeight!=null && forceWidthHeight.getAsBoolean()==true){
          float width = Float.parseFloat( cords.get("width").getAsString() );
          float height = Float.parseFloat( cords.get("height").getAsString() );
          imageAdder.setCords(x, y, width, height);
        }else{
          imageAdder.setCords(x, y);
        }

        imageAdder.execute();
      }

      if(remove!=null && remove.getAsBoolean()==true){
        pdField.getCOSObject().clear();
      }
    }

    if(this.flatten==true){
      acroForm.flatten();//Makes uneditable. Makes prettier by removing input highlights
    }

    return pdf;
  }

  public void execute() throws IOException{
    JsonArray jarr = getJsonArray();
    PDDocument pdf = loopJsonArray(jarr);
    pdf.setAllSecurityToBeRemoved(true);
    pdf.save(this.outPath);
    pdf.close();
  }

  private void fillCheckbox(PDCheckBox pDCheckBox, JsonElement value) throws IOException{
    if(value==null){
      return;
    }

    String valueString = value.getAsString();
    
    Boolean checkedByValue = false;
    java.util.Set<String> onValues = pDCheckBox.getOnValues();
    for(String checkValue : onValues){
      //System.out.println( checkValue+" - "+ );
      if(checkValue.equals(valueString)){
        pDCheckBox.setValue( valueString );
        checkedByValue = true;
      }
    }

    if(!checkedByValue){
      Boolean checkOff = valueString.toLowerCase().equals("off");
      Boolean checkOn = valueString.length()>0 && !checkOff;
      
      if(checkOff){
        pDCheckBox.unCheck();
      }else if(checkOn){
        pDCheckBox.check();
      }
    }
  }
}
