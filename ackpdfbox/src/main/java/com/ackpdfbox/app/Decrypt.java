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
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

/**
 * This will read a document from the filesystem, decrypt it and and then write
 * the result to the filesystem.
 *
 * @author  Ben Litchfield
 */
public final class Decrypt
{
    private static final String ALIAS = "-alias";
    private static final String PASSWORD = "-password";
    private static final String KEYSTORE = "-keyStore";
    
    private String password;
    private String infile;
    private String outfile;
    private String alias;
    private String keyStore;


    private Decrypt()
    {
    }
    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws IOException If there is an error decrypting the document.
     */
    public static void main(String[] args) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        Decrypt decrypt = new Decrypt();
        decrypt.parseCommandLineArgs(args);
        decrypt.decrypt();
    }
    
    private void parseCommandLineArgs(String[] args)
    {
        if( args.length < 1 || args.length > 8 )
        {
            usage();
        }
        else
        {
            for( int i=0; i<args.length; i++ )
            {
                if( args[i].equals( ALIAS ) )
                {
                    i++;
                    if( i >= args.length )
                    {
                        usage();
                    }
                    alias = args[i];
                }
                else if( args[i].equals( KEYSTORE ) )
                {
                    i++;
                    if( i >= args.length )
                    {
                        usage();
                    }
                    keyStore = args[i];
                }
                else if( args[i].equals( PASSWORD ) )
                {
                    i++;
                    if( i >= args.length )
                    {
                        usage();
                    }
                    password = args[i];
                }
                else if( infile == null )
                {
                    infile = args[i];
                }
                else if( outfile == null )
                {
                    outfile = args[i];
                }
                else
                {
                    usage();
                }
            }
            if( infile == null )
            {
                usage();
            }
            if( outfile == null )
            {
                outfile = infile;
            }
            if( password == null )
            {
                password = "";
            }
        }
    }

    private void decrypt() throws IOException
    {
        PDDocument document = null;
        try
        {
            InputStream keyStoreStream = null;
            if( keyStore != null )
            {
                keyStoreStream = new FileInputStream(keyStore);
            }

            document = PDDocument.load(new File(infile), password, keyStoreStream, alias);
            
            if (document.isEncrypted())
            {
                AccessPermission ap = document.getCurrentAccessPermission();
                if(ap.isOwnerPermission())
                {
                    document.setAllSecurityToBeRemoved(true);
                    document.save( outfile );
                }
                else
                {
                    throw new IOException(
                            "Error: You are only allowed to decrypt a document with the owner password." );
                }
            }
            else
            {
                System.err.println( "Error: Document is not encrypted." );
            }
        }
        finally
        {
            if( document != null )
            {
                document.close();
            }
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage()
    {
        
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar Decrypt [options] <inputfile> [outputfile]\n"
                + "\nOptions:\n"
                + "  -alias    : The alias of the key in the certificate file (mandatory if several keys are available\n"
                + "  -password : The password to open the certificate and extract the private key from it.\n"
                + "  -keyStore : The KeyStore that holds the certificate.";
        
        System.err.println(message);
        System.exit(1);
    }

}
