package com.html.helper;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ReadLevelDb {
  private static String levelDbLocation = "/Users/vtulsia/data/walmart/leveldb";
  private static List<String> urlSet = Lists.newArrayList();

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    UrlHtmlMapping urlHtmlMapping = new UrlHtmlMapping(levelDbLocation);
    DataStore h = (DataStore)urlHtmlMapping.get((Object)"http://www.yahoo.com");
    System.out.println(h.htmlPage);
    urlHtmlMapping.seekToFirst();

    Map.Entry<Object, Object> mp = urlHtmlMapping.next();
    int i=0;
    int numberOfDomains = 0;
    while (mp != null){
      mp = urlHtmlMapping.next();
      String key = (String)mp.getKey();
      try{
        HtmlStorage value = (HtmlStorage)mp.getValue();
      } catch (Exception e){
        System.err.println("error");
        continue;
      }
      URL u = null;
      try{
        u = new URL(key);
      } catch (Exception e){
        continue;
      }
      if ( !urlSet.contains(u.getHost())) {
        urlSet.add(u.getHost());
        System.out.println(u.getHost());
        numberOfDomains++;
      }
      i++;
    }
    System.out.println(numberOfDomains);
  }
}
