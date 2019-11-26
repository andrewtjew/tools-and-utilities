/*******************************************************************************
 * Copyright (C) 2017-2019 Kat Fung Tjew, All Rights Reserved
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.nova.utils.FileUtils;
import org.nova.utils.Utils;

public class KeyPair
{
    static class StringKeyPair
    {
        public StringKeyPair(String publicKey,String privateKey)
        {
            this.publicKey=publicKey;
            this.privateKey=privateKey;
        }
        public String publicKey;
        public String privateKey;
    }
    public static StringKeyPair generateKeyPair(String algorithm,int keylength) throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keylength);
    
        java.security.KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();
        
        String privateKeyString=Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicKeyString=Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return new StringKeyPair(publicKeyString,privateKeyString);
    }

    public static void encrypt(String privateKeyString,String inputFileName,String outputFileName) throws Throwable
    {
        Cipher cipher=Cipher.getInstance("RSA");
        byte[] keyBytes=Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey key=KeyFactory.getInstance("RSA").generatePrivate(spec);        
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] bytes=FileUtils.readFile(inputFileName);
        
        try (FileOutputStream writer=new FileOutputStream(outputFileName))
        {
            writer.write(cipher.doFinal(bytes));
        }
    }
    
    public static void decrypt(String publicKeyString,String inputFileName,String outputFileName) throws Throwable
    {
        
        Cipher cipher=Cipher.getInstance("RSA");
        byte[] keyBytes=Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        PublicKey key=KeyFactory.getInstance("RSA").generatePublic(spec);        
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] cipherBytes=FileUtils.readFile(inputFileName);
        try (FileOutputStream writer=new FileOutputStream(outputFileName))
        {
            writer.write(cipher.doFinal(cipherBytes));
        }
    }

    static enum Mode
    {
        Generate,
        Encypt,
        Decrypt
    }
    
    public static void main(String[] args) throws IOException, Throwable
    {
//        args=new String[]{"-h"};
//        args=new String[]{"-g","public=public.txt","private=private.txt"};
//        args=new String[]{"-e","private=private.txt","in=in.txt","out=encrypted"};
//        args=new String[]{"-d","public=public.txt","in=encrypted","out=out.txt"};
        if (args.length==0)
        {
            System.out.println("-h or -help for help");
            return;
        }
        String algorithm="RSA";
        int keylength=1024;
        String publicFile="public.key";
        String privateFile="private.key";
        String in=null;
        String out=null;
        Mode mode=null;
                
        for (String arg:args)
        {
            String[] parts=Utils.split(arg, '=');
            if (parts.length==2)
            {
                switch (parts[0])
                {
                    case "keylength":
                        keylength=Integer.parseInt(parts[1]);
                        break;
                        
                    case "public":
                        publicFile=parts[1];
                        break;
                        
                    case "private":
                        privateFile=parts[1];
                        break;
                        
                    case "out":
                        out=parts[1];
                        break;
                        
                    case "in":
                        in=parts[1];
                        break;
                        
                }
            }
            else
            {
                switch (arg)
                {
                    case "-g":
                        mode=Mode.Generate;
                        break;
                        
                    case "-e":
                        mode=Mode.Encypt;
                        break;

                    case "-d":
                        mode=Mode.Decrypt;
                        break;

                    case "-h":
                    case "-help":
                        System.out.println("Generate encryped keypairs: -g");
                        System.out.println("[algorithm=supported algorithms,default="+algorithm+"] [keylength=keypair length,default="+keylength+"]");
                        System.out.println("[public-file=public key file,default="+publicFile+"] [private-file=private key file,default="+privateFile+"]");
                        System.out.println("\nEncode: -e");
                        System.out.println("<private-file=<private key file,default="+privateFile+"]");
                        System.out.println("<in=plain text file> <out=encrypted file>");
                        System.out.println("\nDecode: -d");
                        System.out.println("<public-file=<ppublic key file,default="+publicFile+"]");
                        System.out.println("<in=encrpted file> <out=decrypted file>");
                        return;
                }
            }
        }
        switch (mode)
        {
            case Decrypt:
                String publicKey=FileUtils.readTextFile(publicFile);
                decrypt(publicKey,in,out);
                break;
            case Encypt:
                String privateKey=FileUtils.readTextFile(privateFile);
                encrypt(privateKey,in,out);
                break;
            case Generate:
                {
                    StringKeyPair pair=generateKeyPair(algorithm, keylength);
                    try (FileOutputStream writer=new FileOutputStream(publicFile))
                    {
                        writer.write(pair.publicKey.getBytes());
                    }
                    try (FileOutputStream writer=new FileOutputStream(privateFile))
                    {
                        writer.write(pair.privateKey.getBytes());
                    }
                    return;
                }
            default:
                System.out.println("mode required");
                break;
            
        }
        
    }
}
