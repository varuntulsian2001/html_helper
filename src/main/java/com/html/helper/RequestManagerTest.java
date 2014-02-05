package com.html.helper;

import com.google.common.collect.Lists;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestManagerTest {
  private static List<String> files = Lists.newArrayList();
  private static List<String> urlSet = Lists.newArrayList();

  public static void main(String[] args) throws IOException {
    Map<String, Double> map = new HashMap<String, Double>();
    RequestManager requestManager = new RequestManager(map);
    HtmlStorage_new.setDataStore(new DataStoreImpl());
//    HtmlStorage_new.getPage(requestManager,"http://www.yahoo.com",null, true);
    DataStore h = HtmlStorage_new.getPage(requestManager,"http://www.yahoo.com",null, false);

    HtmlStorage_new.close();
    int numberOfDomains =0;
    filesForFolder(new File("/Users/vtulsia/data/pages"));
    for ( String f : files){
      if (f.contains("redirect")){
        String url = null;
        try{
          url = Utility.readGzipFile("/Users/vtulsia/data/pages/" + f).toString();
        } catch (Exception e){
          try{
            BufferedReader bw = new BufferedReader(new FileReader("/Users/vtulsia/data/pages/" + f));
            url = bw.readLine();
          } catch( Exception e1){

          }
        }
        if ( url == null){
          continue;
        }
        URL u = null;
        try{
          u = new URL(url);
        } catch (Exception e){
          continue;
        }
        if ( !urlSet.contains(u.getHost())) {
          urlSet.add(u.getHost());
          System.out.println(u.getHost());
          numberOfDomains++;
        }
      }
    }
    System.out.println(numberOfDomains);
  }

  public static void filesForFolder(final File folder) {
    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isDirectory()) {
        continue;
      } else {
        files.addAll(Arrays.asList(fileEntry.getName().split("\n")));
      }
    }
  }
}

class DataStoreImpl extends DataStore implements Serializable {
  private static final long serialVersionUID = -5299552157224445593L;

  public Integer getAbcDE() {
    return abcDE;
  }

  public void setAbcDE(Integer abcDE) {
    this.abcDE = abcDE;
  }

  public void setAbcDE(String abcDE) {
    this.abcDE = 1;
  }

  Integer abcDE;
}
