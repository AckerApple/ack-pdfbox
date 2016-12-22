/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ackpdfbox.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

/**
 * An example for singing a PDF with bouncy castle.
 * A keystore can be created with the java keytool, for example:
 *
 * {@code keytool -genkeypair -storepass 123456 -storetype pkcs12 -alias test -validity 365
 *        -v -keyalg RSA -keystore keystore.p12 }
 *
 * @author Thomas Chojecki
 * @author Vakhtang Koroghlishvili
 * @author John Hewson
 */
public class CreateSignature extends CreateSignatureBase
{

    /**
     * Initialize the signature creator with a keystore and certficate password.
     *
     * @param keystore the pkcs12 keystore containing the signing certificate
     * @param pin the password for recovering the key
     * @throws KeyStoreException if the keystore has not been initialized (loaded)
     * @throws NoSuchAlgorithmException if the algorithm for recovering the key cannot be found
     * @throws UnrecoverableKeyException if the given password is wrong
     * @throws CertificateException if the certificate is not valid as signing time
     * @throws IOException if no certificate could be found
     */
    public CreateSignature(KeyStore keystore, char[] pin)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException
    {
        super(keystore, pin);
    }

    /**
     * Signs the given PDF file. Alters the original file on disk.
     * @param file the PDF file to sign
     * @throws IOException if the file could not be read or written
     */
    public void signDetached(File file) throws IOException
    {
        signDetached(file, file, null);
    }

    /**
     * Signs the given PDF file.
     * @param inFile input PDF file
     * @param outFile output PDF file
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile) throws IOException
    {
        signDetached(inFile, outFile, null);
    }

    /**
     * Signs the given PDF file.
     * @param inFile input PDF file
     * @param outFile output PDF file
     * @param tsaClient optional TSA client
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile, TSAClient tsaClient) throws IOException
    {
        if (inFile == null || !inFile.exists())
        {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        FileOutputStream fos = new FileOutputStream(outFile);

        // sign
        PDDocument doc = PDDocument.load(inFile);
        signDetached(doc, fos, tsaClient);
        doc.close();
    }

    public void signDetached(PDDocument document, OutputStream output, TSAClient tsaClient)
            throws IOException
    {
        setTsaClient(tsaClient);

        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Example User");
        signature.setLocation("Los Angeles, CA");
        signature.setReason("Testing");
        // TODO extract the above details from the signing certificate? Reason as a parameter?

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        if (isExternalSigning())
        {
            System.out.println("Sign externally...");
            document.addSignature(signature);
            ExternalSigningSupport externalSigning =
                    document.saveIncrementalForExternalSigning(output);
            // invoke external signature service
            byte[] cmsSignature = sign(externalSigning.getContent());
            // set signature bytes received from the service
            externalSigning.setSignature(cmsSignature);
        }
        else
        {
            // register signature dictionary and sign interface
            document.addSignature(signature, this);

            // write incremental (only for signing purpose)
            document.saveIncremental(output);
        }
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException
    {
        if (args.length < 3)
        {
            usage();
            System.exit(1);
        }

        String outPath = null;
        String tsaUrl = null;
        boolean externalSig = false;
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-tsa"))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                    System.exit(1);
                }
                tsaUrl = args[i];
            }
            if (args[i].equals("-out"))//acker:added
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                    System.exit(1);
                }
                outPath = args[i];
            }
            if (args[i].equals("-e"))
            {
                externalSig = true;
            }
        }

        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        char[] password = args[1].toCharArray(); // TODO use Java 6 java.io.Console.readPassword
        keystore.load(new FileInputStream(args[0]), password);
        // TODO alias command line argument

        // TSA client
        TSAClient tsaClient = null;
        if (tsaUrl != null)
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            tsaClient = new TSAClient(new URL(tsaUrl), null, null, digest);
        }

        // sign PDF
        CreateSignature signing = new CreateSignature(keystore, password);
        signing.setExternalSigning(externalSig);

        File inFile = new File(args[2]);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        /* acker:added */
            File outFile=null;
            if(outPath==null){
                outFile = new File(inFile.getParent(), substring + "_signed.pdf");
            }else{
                outFile = new File(outPath);
            }
        /* end */

        //akcer:removed
        //File outFile = new File(inFile.getParent(), outPath);
        signing.signDetached(inFile, outFile, tsaClient);
    }

    private static void usage()
    {
        System.err.println("usage: java " + CreateSignature.class.getName() + " " +
                           "<pkcs12_keystore> <password> <pdf_to_sign>\n" + "" +
                           "options:\n" +
                           "  -tsa <url>    sign timestamp using the given TSA server\n" +
                           "  -out <path>   pdf output path\n" +//acker:added
                           "  -e            sign using external signature creation scenario");
    }
}