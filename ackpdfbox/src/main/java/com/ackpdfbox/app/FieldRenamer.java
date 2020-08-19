package com.ackpdfbox.app;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.fdf.FDFField;
import org.apache.pdfbox.pdmodel.fdf.FDFDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;


public class FieldRenamer {
  public PDDocument pdf;

  public String sourcePdfPath;
  public String outputPdfPath;
  public String sourceString;
  public String targetString;

  public FieldRenamer(String sourcePdfPath, String outputPdfPath, String sourceString, String targetString){
    this.sourcePdfPath = sourcePdfPath;
    this.sourceString = sourceString;
    this.targetString = targetString;
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
    }

    field.setPartialName(partialName.replaceAll(this.sourceString, this.targetString));
  }
}
