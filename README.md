# ack-pdfbox
Java code for specific pdf manipulations

> This package is Java code and not Node code. This Java code has been put on NPM for easy inclusion in other NPM packages such as [pdfbox-cli-wrap](https://www.npmjs.com/package/pdfbox-cli-wrap)

### Table of Contents

- [Purpose](#purpose)
- [CLI Test Commands](#cli-test-commands)
    - [PDFBox Version](#pdfbox-version)
    - [PDFToImage Test](#pdftoimage)
    - [Embed Timestamp Signature](#embed-timestamp-signature)
    - [Read Acroform Output Json File](#read-acroform-output-json-file)
    - [Fill Acroform From Json File](#fill-acroform-from-json-file)
- [Sample Code](sample-code)
    - [Sample Read Acroform Json File Result](#sample-read-acroform-json-file-result)
    - [Generate Certificates and KeyStore](#generate-certificates-and-keystore)
- [API](api)
    - [PDFToImage](#pdftoimage)
    - [Timestamp Signature](#timestamp-signature)
    - [Encrypt](#encrypt)
    - [Decrypt](#decrypt)
    - [add-image](#add-image)
    - [read](#read)
    - [fill](#fill)
- [Resources](#resources)

## Purpose
To have a uniform and standardized method for populating PDF Acroforms. A JSON file is produced when reading a PDF Acroform and that same JSON file can be used to turn around and populate the source PDF.

## CLI Test Commands
Open ack-pdfbox in a terminal command prompt and then run the following commands

### PDFBox Version
```
java -jar dist/ackpdfbox-1.0-SNAPSHOT-jar-with-dependencies.jar -version
```

### PDFToImage Test
```
java -jar dist/ackpdfbox-1.0-SNAPSHOT-jar-with-dependencies.jar pdftoimage -endPage 1
```

### Embed Timestamp Signature
```
java -jar dist/ackpdfbox-1.0-SNAPSHOT-jar-with-dependencies.jar sign  test/pdfbox-test.p12 pdfbox-test-password test/unencrypted.pdf -tsa http://tsa.safecreative.org -out test/unencrypted22.pdf
```

### Read Acroform Output Json File
```
java -jar dist/ackpdfbox-1.0-SNAPSHOT-jar-with-dependencies.jar read test/i-9.pdf test/i-9.pdf.json
```

### Fill Acroform From Json File
```
java -jar dist/ackpdfbox-1.0-SNAPSHOT-jar-with-dependencies.jar fill test/i-9.pdf test/i-9-with-sig.pdf.json test/i-9-with-sig.pdf -flatten true
```

### Encrypt PDF
```
java -jar dist/ackpdfbox-1.0-SNAPSHOT-jar-with-dependencies.jar Encrypt <pdf-path> <pdf-out-path> <options>
```

### Decrypt PDF
```
java -jar dist/ackpdfbox-1.0-SNAPSHOT-jar-with-dependencies.jar Encrypt <pdf-path> <pdf-out-path> <options>
```

## Sample Code

### Sample Read Acroform Json File Result
```
[{
  "fullyQualifiedName": "form1[0].#subform[6].FamilyName[0]",
  "isReadOnly": false,
  "partialName": "FamilyName[0]",
  "type": "org.apache.pdfbox.pdmodel.interactive.form.PDTextField",
  "isRequired": false,
  "page": 6,
  "cords": {
    "x": "39.484",
    "y": "597.929",
    "width": "174.00198",
    "height": "15.119995"
  },
  "value": "Apple"
}]
```

### Sample Json File Used For Acroform Fill

The JSON file below will fill two fields.

- First field is a plain text field
- Second field will be replaced by a base64 image of a hand-signature
    - **remove** was added to delete field from pdf
    - **base64Overlay** was added to insert hand-signature image where field was
        - **uri** specifies jpg or png image data
        - **forceWidthHeight** forces image to fit with-in field coordinates

```
[{
  "fullyQualifiedName": "form1[0].#subform[6].FamilyName[0]",
  "isReadOnly": false,
  "partialName": "FamilyName[0]",
  "type": "org.apache.pdfbox.pdmodel.interactive.form.PDTextField",
  "isRequired": false,
  "page": 6,
  "cords": {
    "x": "39.484",
    "y": "597.929",
    "width": "174.00198",
    "height": "15.119995"
  },
  "value": "Apple"
},{
  "fullyQualifiedName": "form1[0].#subform[6].EmployeeSignature[0]",
  "isReadOnly": true,
  "partialName": "EmployeeSignature[0]",
  "type": "org.apache.pdfbox.pdmodel.interactive.form.PDTextField",
  "isRequired": false,
  "page": 6,
  "cords": {
    "x": "126.964",
    "y": "227.523",
    "width": "283.394",
    "height": "15.12001"
  },
  "remove": true,
  "base64Overlay": {
    "uri": "data:image/png;base64,iVBORw0KGgoAAAA...",
    "forceWidthHeight": true
  }
}]
```

### Generate Certificates and KeyStore
If you will be running encrypt/decrypt functionality WITH certificate based security, get ready to run some terminal commands against Java's keytool.

> In a terminal command prompt window, run the following in a folder where certificate files can live

Step #1 Create keyStore
```
keytool -genkey -keyalg RSA -alias pdfbox-test-alias -keystore pdfbox-test-keystore.jks -storepass pdfbox-test-password -validity 360 -keysize 2048
```

Step #2 Create a selfsigned certificate
```
keytool -export -alias pdfbox-test-alias -file pdfbox-test.crt -keystore pdfbox-test-keystore.jks
```

Step #3 Marry the certificate and keyStore together as a .p12 file
```
keytool -importkeystore -srckeystore pdfbox-test-keystore.jks -destkeystore pdfbox-test.p12 -srcstoretype JKS -deststoretype PKCS12 -deststorepass pdfbox-test-password -srcalias pdfbox-test-alias -destalias pdfbox-test-p12
```

You should now have the following files in targeted folder:

- pdfbox-test.crt
    - used to encrypt
- pdfbox-test-keystore.jks
    - used to create p12 file below
- pdfbox-test.p12
    - used to decrypt


### MAY Need Java Cryptography
Depending on your level of advanced encryption needs, you (may) need to install [Java Cryptography](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)


## API

### PDFToImage
Will create an image for any or every page in a PDF document.

- **password** - The password to the PDF document.
- **imageType**=jpg - The image type to write to. Currently only jpg or png.
- **outputPrefix** - Name of PDF document  The prefix to the image file.
- **startPage**=1 - The first page to convert, one based.
- **endPage** - The last page to convert, one based.
- **nonSeq** - false Use the new non sequential parser.

> See [pdfbox.apache.org#pdftoimage](https://pdfbox.apache.org/2.0/commandline.html#pdftoimage)

### Timestamp Signature
Will embed timestamp signature with optional TSA option

- **pkcs12_keystore** - keystore the pkcs12 keystore containing the signing certificate (typically a .p12 file)
- **password** - the password for recovering the key
- **pdf_to_sign** - the PDF file to sign
- **options**
  - **tsa** - url - sign timestamp using the given TSA server
  - **out** - path - pdf output path
  - **e** - sign using external signature creation scenario


### Encrypt
Will encrypt a PDF document

- **pdfPath** - The PDF file to encrypt
- **outPath** - The file to save the decrypted document to. If left blank then it will be the same as the input file || options
- **options**
  - **O**:                          The owner password to the PDF, ignored if -certFile is specified.
  - **U**:                          The user password to the PDF, ignored if -certFile is specified.
  - **certFile**:                   Path to X.509 cert file.
  - **canAssemble**:                true  Set the assemble permission.
  - **canExtractContent**:          true  Set the extraction permission.
  - **canExtractForAccessibility**: true  Set the extraction permission.
  - **canFillInForm**:              true  Set the fill in form permission.
  - **canModify**:                  true  Set the modify permission.
  - **canModifyAnnotations**:       true  Set the modify annots permission.
  - **canPrint**:                   true  Set the print permission.
  - **canPrintDegraded**:           true  Set the print degraded permission.
  - **keyLength**:                  40, 128 or 256  The number of bits for the encryption key. For 128 and above bits Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files must be installed.

> See [pdfbox.apache.org#encrypt](https://pdfbox.apache.org/2.0/commandline.html#encrypt)

### Decrypt
Will decrypt a PDF document

- **pdfPath** - The PDF file to decrypt
- **outPath** - The file to save the decrypted document to. If left blank then it will be the same as the input file || options
- **options** - {}
    - **password**: Password to the PDF or certificate in keystore.
    - **keyStore**: Path to keystore that holds certificate to decrypt the document (typically a .p12 file). This is only required if the document is encrypted with a certificate, otherwise only the password is required.
    - **alias**:    The alias to the certificate in the keystore.

> See [pdfbox.apache.org#decrypt](https://pdfbox.apache.org/2.0/commandline.html#decrypt)

### read
Read Acroform fields from a PDF

- **pdfPath** - The PDF file to read
- **jsonPath** - optional, path to write JSON file otherwise output to console

### fill
Fill Acroform fields from a PDF

- **pdfPath** - The PDF file to fill
- **jsonPath** - The json file to fill pdf with
- **outPath** - The file to save the decrypted document to. If left blank then it will be the same as the input file || options
- **options**
  - **flatten** - Boolean - The form will become uneditable

### add-image
Insert a single image into a PDF or append multi images as individual pages

- **pdfPath** - The PDF file to encrypt
- **imagesPath** - The file image(s) to append to document. Allows multiple image arguments, which is great for appending photos as pages.
- **options**
  - **out** - The file to save the decrypted document to. If left blank then it will be the same as the input file || options
  - **page** - The page number where to drop image. Use -1 to append on a new page
  - **x** - The x cord where to drop image
  - **y** - The y cord where to drop image. Use -1 for top
  - **width** - default is image width. Accepts percent width
  - **height** - default is image height

Example Add Images as Pages
```
java -jar dist/ackpdfbox-1.0-SNAPSHOT-jar-with-dependencies.jar add-image test/unencrypted.pdf test/testImage.png test/testImage2.JPG test/testImage.JPG -y -1 -width 100% -page -1 -out test/unencrypted2.pdf
```


## Resources

- This Java code is bundled using [Maven](https://maven.apache.org/)
- [PDFBox](https://pdfbox.apache.org/)
- Cryptography is provided by [BouncyCastle](http://www.bouncycastle.org/)
  - [latest releases](https://www.bouncycastle.org/latest_releases.html)