package com.html.helper;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class BzipConcat {

  public static final long MB100 = 100 * 1024 * 1024L;

  public static void concat(final String inputDir, final String outputPath) throws IOException {
    int currentId = 0;
    BufferedWriter out = null;

    final File inputPath = new File(inputDir);
    long numBytesTotal = 0;
    long currentFileBytes = 0;
    final File[] files = inputPath.listFiles();
    int invalidFileCount = 0;
    long numberOfWords = 0;
    for (int i = 0; i < files.length; ++i) {
      if (files[i].getName().contains("redirect")) {
        continue;
      }
      if (out == null) {
        out = getOutputStreamText(outputPath, currentId++);
      }
      final File f = files[i];
      BufferedReader in = null;
      try {
        in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
      } catch (Exception e) {
        try {
          in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        } catch (Exception e1) {
          // Ignore file
          if (++invalidFileCount % 1000 == 0) {
            System.err.println("Ignored invalid files: " + invalidFileCount);
          }
          continue;
        }
      }
      String html = in.readLine();
      if (html == null) {
        if (++invalidFileCount % 1000 == 0) {
          System.err.println("Ignored invalid files: " + invalidFileCount);
        }
        continue;
      }
      String line = Jsoup.parse(html).text();
      if (line.isEmpty()) {
        if (++invalidFileCount % 1000 == 0) {
          System.err.println("Ignored invalid files: " + invalidFileCount);
        }
        continue;
      }
      in.close();
      currentFileBytes += line.length();
      for (String w : line.split("\\s+")) {
        // Filter
        if (w.contains("http") || w.contains("www") || w.contains("@") || w.contains("#")|| w.contains(".com")) {
          continue;
        }
        if (w.contains(".")){
          w = w.replaceAll("\\.", "\n");
        }
        numBytesTotal += w.length();
        numberOfWords++;
        out.write(w.toLowerCase().trim() + " ");
      }
      out.write("\n");

      if (currentFileBytes >= MB100) {
        out.close();
        out = null;
        currentFileBytes = 0;
      }
      if (i % 1000 == 0) {
        System.out.println("Processed " + i + " of " + files.length + " files...");
      }
    }
    if (out != null) {
      out.close();
    }
    System.out.println("Total bytes read: " + numBytesTotal);
  }

//  public static void readBzip(final String dir) throws IOException {
//    File inputPath = new File(dir);
//    final File[] files = inputPath.listFiles();
//    for ( File file : files){
//      BZip2CompressorInputStream br = new BZip2CompressorInputStream(new FileInputStream(file.getName()));
//      final byte[] buffer = new byte[1024];
//      int n = 0;
//      while (-1 != (n = br.read(buffer))) {
//        System.out.println(new String(buffer).toCharArray());
//      }
//      System.out.println("-------");
//    }
//  }

  private static BufferedWriter getOutputStream(final String outputPath, final int currentId) throws IOException {
    return new BufferedWriter(
        new OutputStreamWriter(new BZip2CompressorOutputStream(new FileOutputStream(outputPath + "_" + currentId))));
  }

  private static BufferedWriter getOutputStreamText(final String outputPath, final int currentId) throws IOException {
    return new BufferedWriter(new FileWriter(outputPath + "_" + currentId));
  }

//  public static void main(String[] args) throws IOException {
//    if (args.length != 0) {
//      System.out.println("BzipConcat <input dir> <output_file_name");
//      System.exit(1);
//    }
////    readBzip("../data/concatFiles");
//    concat("/home/vtulsia/data/pagesWithoutTags", "/home/vtulsia/data/concatFiles/dump");
//
//  }
}
