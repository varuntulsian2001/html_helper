package com.html.helper;

import org.jsoup.Jsoup;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

/**
 * Stored page format
 * mp contains html page and redirect link
 */
public class HtmlStorage_new implements Serializable{
  private static long counter = 0L;
  private static String content = "";
  private static int currentId = 0;
  public String htmlPage;
  public String redirectUrl;
  protected static volatile UrlHtmlMapping URL_HTML_MAPPING = null;
  private static final long serialVersionUID = -5299552157224445592L;
  private final static String LOCK = "lock";

  public static void setDataStore(DataStore dataStore) {
    HtmlStorage_new.dataStore = dataStore;
  }

  static DataStore dataStore = null;

  public static String getLevelDbLocation() {
    return levelDbLocation;
  }

  public static void setLevelDbLocation(String levelDbLocation) {
    HtmlStorage_new.levelDbLocation = levelDbLocation;
  }

  private static String levelDbLocation = "/Users/vtulsia/data/walmart/leveldb";

  public HtmlStorage_new(String htmlPage, String url) {
    this.htmlPage = htmlPage;
    this.redirectUrl = url;
  }

  /**
   * looks for the html page in the disk, if the page is available then the page is returned
   */
  private static DataStore returnPageIfAvailable(String origUrl) throws IOException {
    String htmlFile = "../data/pages/" + origUrl.replace("/", "_").hashCode() + ".out";
    File newFile = new File(htmlFile);
    String redirectHtmlFile = "../data/pages/" + origUrl.replace("/", "_").hashCode() + "_redirect.out";
    File newFile1 = new File(redirectHtmlFile);
    if (newFile.exists() && newFile1.exists()) {
      StringBuilder htmlText = Utility.readFile(htmlFile);
      String url = Utility.readFile(redirectHtmlFile).toString();
      return new DataStore(htmlText.toString(), url);
    } else {
      return new DataStore(null, null);
    }
  }

  /**
   * Stores the page in the disk
   */
  private static void storePage(String origUrl, DataStore dataStorage) throws IOException {
    String fileName = "../data/pages/" + origUrl.replace("/", "_").hashCode() + ".out";
    Utility.writeFile(dataStorage.htmlPage, fileName);
    fileName = "../data/pages/" + origUrl.replace("/", "_").hashCode() + "_redirect.out";
    Utility.writeFile(dataStorage.redirectUrl, fileName);
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
  private static void storePageText(String origUrl, DataStore dataStore) throws IOException {
    String fileName = "/Users/vtulsia/data/walmart/concatFiles/polaris";
    if ( counter > MB100){
      concat(HtmlStorage_new.content, fileName);
      counter = 0;
      HtmlStorage_new.content = "";
    }
    else{
      synchronized (URL_HTML_MAPPING){
        String cleanedPage = Jsoup.parse(dataStore.htmlPage).text();
        counter += cleanedPage.length();
//        System.out.println("the counter size is "+ counter );
        HtmlStorage_new.content = HtmlStorage_new.content.concat("\n").concat(cleanedPage);
      }
    }
  }


  /**
   * get the page from the local repository or using the doRequest method
   *
   * @param forced - it true the force fetch the page using the doRequest method
   * @return - DataStore object
   */
  public static DataStore getPage(RequestManager rm, String origUrl, String stopDomain, Boolean forced) throws IOException {
    if (URL_HTML_MAPPING == null){
      synchronized (LOCK){
        if (URL_HTML_MAPPING == null) {
          URL_HTML_MAPPING = new UrlHtmlMapping(levelDbLocation);
        }
      }
    }
//        If data is available and force is false then fetch data from disk
    if (!forced) {
      DataStore localData;
      try {
        localData = (DataStore) URL_HTML_MAPPING.get((Object) origUrl);
      } catch (Exception e) {
        localData = new DataStore(null, null);
      }
      if (localData.getHtmlPage() != null && localData.getHtmlPage().length() > 100 && localData.getHtmlPage() != null) {
        return localData;
      }
    }
    RequestManager.PageUrl pageUrl;
    try {
      pageUrl = rm.doRequest(origUrl, stopDomain);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("some error fetching url " + origUrl);
      return new DataStore("<html> </html>", "");
    }
    DataStore dataStore = null;
    if (HtmlStorage_new.dataStore == null){
      dataStore = new DataStore(pageUrl.htmlPage, pageUrl.redirectUrl);
    } else{
      dataStore = HtmlStorage_new.dataStore;
      dataStore.setHtmlPage(pageUrl.htmlPage);
      dataStore.setRedirectUrl(pageUrl.redirectUrl);
      dataStore.setTimeStamp();
      URL u = new URL(pageUrl.redirectUrl);
      dataStore.setDomain(u.getHost());
      setFields(dataStore, dataStore.htmlPage);
    }
    URL_HTML_MAPPING.put(origUrl, dataStore);
    return dataStore;
  }

  private static void setFields(DataStore dataStore, String html) {
    Field[] fields = dataStore.getClass().getDeclaredFields();
    System.out.println(dataStore.getClass().getName());
    for (Field field : fields){
      try{
        PropertyDescriptor pd = new PropertyDescriptor(field.getName(),dataStore.getClass());
//        pd.getWriteMethod().invoke(dataStore, 100 );
        String methodName = "set"+(field.getName().substring(0,1)).toUpperCase()+field.getName().substring(1);
        Class<?> params[] = new Class[1];
        for (int i = 0; i < params.length; i++) {
          params[i] = String.class;
        }
        String[] param = new String[]{html};
          // calling the desired method
          dataStore.getClass().getMethod(methodName, params).invoke(dataStore, param);
        System.out.println(pd.getReadMethod().invoke(dataStore));
      } catch(Exception e){
        e.printStackTrace();
      }
    }
  }

  public static DataStore getPage(RequestManager rm, String origUrl) throws IOException {
    return getPage(rm, origUrl, null, false);
  }

  public static DataStore getPage(RequestManager rm, String origUrl, String stopDomain) throws IOException {
    return getPage(rm, origUrl, stopDomain, false);
  }

  public static DataStore getPage(RequestManager rm, String origUrl, Boolean forced) throws IOException {
    return getPage(rm, origUrl, null, forced);
  }

  public static void close() throws IOException {
    URL_HTML_MAPPING.close();
    concat(HtmlStorage_new.content,"/Users/vtulsia/data/walmart/concatFiles/polarisEnd");
  }
}
