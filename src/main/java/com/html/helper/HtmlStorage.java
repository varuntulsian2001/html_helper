package com.html.helper;

import org.jsoup.Jsoup;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Stored page format
 * mp contains html page and redirect link
 */
public class HtmlStorage implements Serializable{
  private static long counter = 0L;
  private static String content = "";
  private static int currentId = 0;
  public String htmlPage;
  public String redirectUrl;
  protected static volatile UrlHtmlMapping URL_HTML_MAPPING = null;
  private static final long serialVersionUID = -5299552157224445592L;
  private final static String LOCK = "lock";

  public static String getLevelDbLocation() {
    return levelDbLocation;
  }

  public static void setLevelDbLocation(String levelDbLocation) {
    HtmlStorage.levelDbLocation = levelDbLocation;
  }

  private static String levelDbLocation = "/Users/vtulsia/data/walmart/leveldb";

  public HtmlStorage(String htmlPage, String url) {
    this.htmlPage = htmlPage;
    this.redirectUrl = url;
  }

  /**
   * looks for the html page in the disk, if the page is available then the page is returned
   */
  private static HtmlStorage returnPageIfAvailable(String origUrl) throws IOException {
    String htmlFile = "../data/pages/" + origUrl.replace("/", "_").hashCode() + ".out";
    File newFile = new File(htmlFile);
    String redirectHtmlFile = "../data/pages/" + origUrl.replace("/", "_").hashCode() + "_redirect.out";
    File newFile1 = new File(redirectHtmlFile);
    if (newFile.exists() && newFile1.exists()) {
      StringBuilder htmlText = Utility.readFile(htmlFile);
      String url = Utility.readFile(redirectHtmlFile).toString();
      return new HtmlStorage(htmlText.toString(), url);
    } else {
      return new HtmlStorage(null, null);
    }
  }

  /**
   * Stores the page in the disk
   */
  private static void storePage(String origUrl, HtmlStorage htmlStorage) throws IOException {
    String fileName = "../data/pages/" + origUrl.replace("/", "_").hashCode() + ".out";
    Utility.writeFile(htmlStorage.htmlPage, fileName);
    fileName = "../data/pages/" + origUrl.replace("/", "_").hashCode() + "_redirect.out";
    Utility.writeFile(htmlStorage.redirectUrl, fileName);
  }

  public static final long MB100 = 100*1024*1024L;

  private static BufferedWriter getOutputStream(final String outputPath, final int currentId) throws IOException {
    return new BufferedWriter(
        new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputPath + "_" + currentId))));
  }

  private static BufferedWriter getOutputStreamText(final String outputPath, final int currentId) throws IOException {
    return new BufferedWriter(new FileWriter(outputPath + "_" + currentId));
  }

  public static synchronized void concat(final String content, final String outputPath) throws IOException {
    BufferedWriter out = null;

    long numBytesTotal = 0;
    long currentFileBytes = 0;
    long numberOfWords = 0;
    out = getOutputStreamText(outputPath, currentId++);

    String html = content;
    if (html == null) {
      return;
    }
    String line = Jsoup.parse(html).text();
    if (line.isEmpty()) {
      return;
    }
    currentFileBytes += line.length();
    for (String w : line.split("\\s+")) {
      // Filter
      if (w.contains("http") || w.contains("www") || w.contains("@") || w.contains("#") || w.contains(".com")) {
        continue;
      }
      if (w.contains(".")){
        w = w.replaceAll("\\.", "\n");
      }
      numBytesTotal += w.length();
      numberOfWords++;
      out.write(w.toLowerCase().trim() + " ");
    }
    if (out != null) {
      out.close();
    }
    System.out.println("Total bytes read: " + numBytesTotal);
  }

  /**
   * Stores the page in the disk
   */
  private static void storePageText(String origUrl, HtmlStorage htmlStorage) throws IOException {
    String fileName = "/Users/vtulsia/data/walmart/concatFiles/polaris";
    if ( counter > MB100){
      concat(HtmlStorage.content, fileName);
      counter = 0;
      HtmlStorage.content = "";
    }
    else{
      synchronized (URL_HTML_MAPPING){
        String cleanedPage = Jsoup.parse(htmlStorage.htmlPage).text();
        counter += cleanedPage.length();
//        System.out.println("the counter size is "+ counter );
        HtmlStorage.content = HtmlStorage.content.concat("\n").concat(cleanedPage);
      }
    }
  }


  /**
   * get the page from the local repository or using the doRequest method
   *
   * @param forced - it true the force fetch the page using the doRequest method
   * @return - htmlStorage object
   */
  public static HtmlStorage getPage(RequestManager rm, String origUrl, String stopDomain, Boolean forced) throws IOException {
    synchronized (LOCK){
      if (URL_HTML_MAPPING == null) {
        URL_HTML_MAPPING = new UrlHtmlMapping(levelDbLocation);
      }
    }
//        If data is available and force is false then fetch data from disk
    if (!forced) {
      HtmlStorage localData;
      try {
        localData = (HtmlStorage) URL_HTML_MAPPING.get((Object) origUrl);
      } catch (Exception e) {
        localData = new HtmlStorage(null, null);
      }
      if (localData.htmlPage != null && localData.htmlPage.length() > 100 && localData.redirectUrl != null) {
        synchronized (URL_HTML_MAPPING){
//          HtmlStorage.storePageText(origUrl,localData);
        }
        return localData;
      }
    }
    RequestManager.PageUrl pageUrl;
    try {
      pageUrl = rm.doRequest(origUrl, stopDomain);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("some error fetching url " + origUrl);
      return new HtmlStorage("<html> </html>", "");
    }
    HtmlStorage htmlStorage = new HtmlStorage(pageUrl.htmlPage, pageUrl.redirectUrl);
    URL_HTML_MAPPING.put(origUrl, htmlStorage);
    HtmlStorage.storePageText(origUrl, htmlStorage);
    return htmlStorage;
  }

  public static HtmlStorage getPage(RequestManager rm, String origUrl) throws IOException {
    return getPage(rm, origUrl, null, false);
  }

  public static HtmlStorage getPage(RequestManager rm, String origUrl, String stopDomain) throws IOException {
    return getPage(rm, origUrl, stopDomain, false);
  }

  public static HtmlStorage getPage(RequestManager rm, String origUrl, Boolean forced) throws IOException {
    return getPage(rm, origUrl, null, forced);
  }

  public static void close() throws IOException {
    URL_HTML_MAPPING.close();
    concat(HtmlStorage.content,"/Users/vtulsia/data/walmart/concatFiles/polarisEnd");
  }
}