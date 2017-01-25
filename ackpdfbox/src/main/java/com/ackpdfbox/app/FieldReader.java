package com.ackpdfbox.app;

import java.io.IOException;
import java.util.List;
import java.io.File;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;

//choices
import org.apache.pdfbox.pdmodel.interactive.form.PDChoice;

//cords
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;

//page
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;


import java.io.FileOutputStream;


public class FieldReader{
   public PDDocument pdf;

   public FieldReader(){}

  /**
   * This will print all the fields from the document.
   * 
   * @param pdfDocument The PDF to get the fields from.
   * 
   * @throws IOException If there is an error getting the fields.
   */
  public JsonArray getGsonArray(){
    PDDocumentCatalog docCatalog = this.pdf.getDocumentCatalog();
    PDAcroForm acroForm = docCatalog.getAcroForm();
    List<PDField> fields = acroForm.getFields();

    JsonArray jArr = new JsonArray();
    JsonArray rtn = fieldsOntoArray(fields, jArr, acroForm);

    return rtn;
  }

  public String getJsonString(){
    JsonArray jarray = getGsonArray();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(jarray);
  }

  private PDRectangle getFieldArea(PDField field) {
    COSDictionary fieldDict = field.getCOSObject();
    //COSDictionary fieldDict = field.getDictionary();
    COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);

    if(fieldAreaArray==null){
      return null;
    }

    PDRectangle result = new PDRectangle(fieldAreaArray);
    return result;
  }

  private JsonObject getFieldCords(PDField field){
    PDRectangle rect = getFieldArea(field);
    JsonObject jo = new JsonObject();

    if(rect!=null){
      Float lowerLeftX = rect.getLowerLeftX();
      Float upperRightX = rect.getUpperRightX();
      Float lowerLeftY = rect.getLowerLeftY();
      Float upperRightY = rect.getUpperRightY();

      jo.addProperty("x", Float.toString(lowerLeftX));
      //jo.addProperty("lowerLeftX", Float.toString(lowerLeftX));
      
      jo.addProperty("y", Float.toString(lowerLeftY));
      //jo.addProperty("lowerLeftY", Float.toString(lowerLeftY));
      
      //jo.addProperty("upperRightX", Float.toString(upperRightX));
      //jo.addProperty("upperRightY", Float.toString(upperRightY));

      jo.addProperty("width", Float.toString(upperRightX-lowerLeftX));
      jo.addProperty("height", Float.toString(upperRightY-lowerLeftY));
    }
    
    return jo;
  }

  private int getPageByField(PDField field){
    int pageNum = 0;
    for (PDAnnotationWidget widget : field.getWidgets()){
        PDPage page = widget.getPage();
        pageNum = this.pdf.getPages().indexOf(page);
    }
    return pageNum;
  }

  /** loop parent-fields containers and recurse thru to append to an array only the input-fields */
  private JsonArray parentToFieldsArray(PDField field, JsonArray array, PDAcroForm acroForm){
    for (PDField child : ((PDNonTerminalField)field).getChildren()){
      if (child instanceof PDNonTerminalField){//is container of fields
        parentToFieldsArray(child, array, acroForm);
      }else{
        array.add( jsonObByField(child, acroForm) );
      }
    }
    return array;
  }

  /** loop fields and recurse thru children to append to an array only the input-fields */
  private JsonArray fieldsOntoArray(List<PDField> fields, JsonArray array, PDAcroForm acroForm){
    for (PDField loopField : fields){
      if (loopField instanceof PDNonTerminalField){//is container of fields
        parentToFieldsArray(loopField, array, acroForm);
      }else{
        array.add( jsonObByField(loopField, acroForm) );
      }
    }

    return array;
  }
    
  /** get select options of a dropdown */
/*
  private JsonArray getFieldOps(PDChoice field){
    JsonArray ops = new JsonArray();
    for(String option:field.getOptions()){
      ops.add(option);
    }
    return ops;
  }
*/

  private JsonObject jsonObByField(PDField field, PDAcroForm acroForm){
    JsonObject jo = new JsonObject();
    String className = field.getClass().getName();
    String fullname = field.getFullyQualifiedName();
    jo.addProperty("fullyQualifiedName", fullname);

    if( field.isReadOnly() ){
      jo.addProperty("isReadOnly", field.isReadOnly());
    }
    
    jo.addProperty("partialName", field.getPartialName());
    jo.addProperty("type", className);
    jo.addProperty("isRequired", field.isRequired());
    jo.addProperty("page", getPageByField(field));
    jo.add("cords", getFieldCords(field));
    
    String value = field.getValueAsString();
    if(field instanceof PDComboBox && value=="[]"){//is empty choice
      value = "";
    }else if(field instanceof PDCheckBox){//is empty choice
      PDCheckBox pDCheckBox = (PDCheckBox) acroForm.getField(fullname);
      jo.addProperty("onValue", pDCheckBox.getOnValue());
      JsonArray onValueArray = new JsonArray();

      java.util.Set<String> onValues = pDCheckBox.getOnValues();
      for(String checkValue : onValues){
        onValueArray.add( checkValue );
      }


      jo.add("onValues", onValueArray);
      //jo.addProperty("exportValues", pDCheckBox.getExportValues().toString());
    }
    jo.addProperty("value", value);
/*
    if(field instanceof PDChoice){
      String st = getFieldOps(field).toString();
      System.out.println(st);
    }
*/
    
    return jo;
  }
  
  public void loadPdfByPath(String path) throws IOException{
    this.pdf = filePathToPdf(path);
  }

  public void write(String string, String path) throws IOException{
    FileOutputStream out = new FileOutputStream(path);
    byte[] contentInBytes = string.getBytes();
    out.write(contentInBytes);
    out.close();
  }

  public PDDocument filePathToPdf(String path) throws IOException{
    return PDDocument.load(new File(path));
  }

  public String getFields() throws IOException{
    try{
      return getJsonString();
    }finally{
      if (this.pdf != null){
        this.pdf.close();
      }
    }
  }
}
