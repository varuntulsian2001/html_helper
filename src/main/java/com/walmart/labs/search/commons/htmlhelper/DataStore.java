package com.walmart.labs.search.commons.htmlhelper;

import java.io.Serializable;
import java.net.URL;
import java.sql.Timestamp;

public class DataStore implements Serializable {
    private static final long serialVersionUID = -5299552157224445593L;

    public DataStore(String htmlData, String redirectUrl, int validityDays) {
        this.timeStamp = new Timestamp(new java.util.Date().getTime());
        this.htmlPage = htmlData;
        this.redirectUrl = redirectUrl;
        try {
            URL url = new URL(redirectUrl);
            this.domain = url.getHost();
        } catch (Exception e) {
            HtmlStorageNew.LOGGER.error("redirect url error, url is " + redirectUrl);
        }
        this.validityDays = validityDays;
    }

    protected DataStore() {

    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setTimeStamp() {
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

    public int getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(int validityDays) {
        this.validityDays = validityDays;
    }

    protected int validityDays = 100;
    protected String redirectUrl;
}
