package com.walmart.labs.search.commons.htmlhelper;

import org.apache.tools.bzip2.CBZip2OutputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utility {
    static StringBuilder readFile(String string) throws IOException {
        FileReader fr = null;
        BufferedReader br = null;
        StringBuilder htmlText = new StringBuilder();
        try {
            fr = new FileReader(string);
            br = new BufferedReader(fr);
            String temp = br.readLine();
            while (temp != null) {
                htmlText = htmlText.append(temp);
                temp = br.readLine();
            }
        } finally {
            if (br != null) {
                br.close();
            }
            if (fr != null) {
                fr.close();
            }
        }
        return htmlText;
    }

    static void writeFile(String string, String fileName) throws IOException {
        FileWriter newFile = null;
        BufferedWriter bw = null;
        try {
            newFile = new FileWriter(fileName);
            bw = new BufferedWriter(newFile);
            bw.write(string);
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (newFile != null) {
                newFile.close();
            }
        }
    }

    static StringBuilder readGzipFile(String string) throws IOException {
        StringBuilder htmlText = new StringBuilder();
        InputStream fileStream = null;
        InputStream gzipStream = null;
        BufferedReader br = null;
        try {
            fileStream = new FileInputStream(string);
            gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
            br = new BufferedReader(decoder);
            String temp = br.readLine();
            while (temp != null) {
                htmlText = htmlText.append(temp);
                temp = br.readLine();
            }
        } finally {
            if (br != null) { br.close(); }
            if (gzipStream != null) { gzipStream.close(); }
            if (fileStream != null) { fileStream.close(); }
        }
        return htmlText;
    }

    static void writeGzipFile(String string, String fileName) throws IOException {
        FileOutputStream fos = null;
        GZIPOutputStream gos = null;
        try {
            fos = new FileOutputStream(fileName);
            gos = new GZIPOutputStream(fos);
            gos.write(string.getBytes(Charset.forName("UTF-8")));
        } finally {
            if (gos != null) { gos.close(); }
            if (fos != null) { fos.close(); }
        }
    }

    static void writeBzip2File(String fileName, StringBuilder string) throws IOException {
        FileOutputStream fos = null;
        CBZip2OutputStream bzip = null;
        try {
            fos = new FileOutputStream(fileName);
            bzip = new CBZip2OutputStream(fos);
            bzip.write(string.toString().getBytes(Charset.forName("UTF-8")));
        } finally {
            if (bzip != null) {
                bzip.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
}
