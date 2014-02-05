package com.html.helper;

import java.io.Serializable;
import java.net.URL;
import java.sql.Timestamp;

public class DataStore implements Serializable{
  private static final long serialVersionUID = -5299552157224445593L;
  public DataStore(String htmlData, String redirectUrl) {
    this.timeStamp = new Timestamp(new java.util.Date().getTime());
    this.htmlPage = htmlData;
    this.redirectUrl = redirectUrl;
    try{
      URL url = new URL(redirectUrl);
      this.domain = url.getHost();
    } catch(Exception e){
      System.err.println("redirect url error, url is "+ redirectUrl);
    }
  }

  protected DataStore(){

  }

  public Timestamp getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Timestamp timeStamp) {
    this.timeStamp = timeStamp;
  }

  public void setTimeStamp(){
    this.timeStamp = new Timestamp(new java.util.Date().getTime());
  }

  protected Timestamp timeStamp;

  public String getHtmlPage() {
    return htmlPage;
  }

  public void setHtmlPage(String htmlPage) {
    this.htmlPage = htmlPage;
  }

  protected String htmlPage;

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  protected String domain = "";

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public static int getValidityDays() {
    return validityDays;
  }

  public static void setValidityDays(int validityDays) {
    DataStore.validityDays = validityDays;
  }

  protected static int validityDays = 100;
  protected String redirectUrl;
}