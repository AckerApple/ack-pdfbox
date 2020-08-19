package com.ackpdfbox.app;

import java.io.IOException;
import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.PDPage;

public class FieldCopier {
  public PDDocument pdf;

  public String sourcePdfPath;
  public String targetPdfPath;
  public String outputPdfPath;

  public FieldCopier(String sourcePdfPath, String targetPdfPath, String outputPdfPath){
    this.sourcePdfPath = sourcePdfPath;
    this.targetPdfPath = targetPdfPath;
    this.outputPdfPath = outputPdfPath;
  }

  public void execute() throws IOException {
    PDDocument sourcePdf = PDDocument.load(new File(this.sourcePdfPath));
    PDAcroForm acroFormSource = sourcePdf.getDocumentCatalog().getAcroForm();

    PDDocument targetPdf = PDDocument.load(new File(this.targetPdfPath));
    PDAcroForm acroFormTarget = new PDAcroForm(targetPdf);

    acroFormTarget.setCacheFields(true);
    acroFormTarget.setFields(acroFormSource.getFields());
    targetPdf.getDocumentCatalog().setAcroForm(acroFormTarget);

    int pageIndex = 0;
    for(PDPage page: sourcePdf.getPages()){
        targetPdf.getPage(pageIndex).setAnnotations(page.getAnnotations());
        pageIndex++;
    }

    targetPdf.save(this.outputPdfPath);
    targetPdf.close();
    sourcePdf.close();
  }
}
