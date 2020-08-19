package com.ackpdfbox.app;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.fdf.FDFField;
import org.apache.pdfbox.pdmodel.fdf.FDFDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;


public class FieldTranslator {
  public PDDocument pdf;

  public String sourcePdfPath;
  public String outputPdfPath;
  public String translateX;
  public String translateY;

  public FieldTranslator(String sourcePdfPath, String outputPdfPath, String translateX, String translateY){
    this.sourcePdfPath = sourcePdfPath;
    this.translateX = translateX;
    this.translateY = translateY;
    this.outputPdfPath = outputPdfPath;
  }

  public void execute() throws IOException {
    PDDocument sourcePdf = PDDocument.load(new File(this.sourcePdfPath));
    PDAcroForm sourcePdfAcroForm = sourcePdf.getDocumentCatalog().getAcroForm();

    List<PDField> fields = sourcePdfAcroForm.getFields();

    //inspect field values
    for (PDField field : fields) {
      processField(field);
    }

    sourcePdf.save(this.outputPdfPath);
    sourcePdf.close();
  }


  private void processField(PDField field) throws IOException {
    String partialName = field.getPartialName();

    if (field instanceof PDNonTerminalField) {
      // this field has children
      for (PDField child : ((PDNonTerminalField)field).getChildren()) {
        processField(child);
      }
    } else {
      // this field has no children
      PDRectangle rectangle = field.getWidgets().get(0).getRectangle();
      rectangle.setLowerLeftX(rectangle.getLowerLeftX() - Float.parseFloat(this.translateX));
      rectangle.setLowerLeftY(rectangle.getLowerLeftY() - Float.parseFloat(this.translateY));
      rectangle.setUpperRightX(rectangle.getUpperRightX() - Float.parseFloat(this.translateX));
      rectangle.setUpperRightY(rectangle.getUpperRightY() - Float.parseFloat(this.translateY));
      field.getWidgets().get(0).setRectangle(rectangle);
    }

    field.setPartialName(partialName.replaceAll(this.translateX, this.translateY));
  }
}
