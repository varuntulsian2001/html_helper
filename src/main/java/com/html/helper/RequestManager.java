package com.html.helper;


import com.google.common.net.InternetDomainName;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * RequestManger honours Query per seconds values in the rateLimiterObject (initiated in the constructor) and fetches pages in the doRequest method. Thread Safe
 */
public class RequestManager {
  private static final double DEFAULT_QPS = 2;
  private final Map<String, RateLimiter> rateLimiterMap = new HashMap<String, RateLimiter>();
  private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36";
  private MultiThreadedHttpConnectionManager manager;
  private HttpClient httpClient;

  /**
   * PageUrl - innerClass of RequestManager. DataStructure that doRequest method returns
   */
  public class PageUrl {
    public String htmlPage;
    public String redirectUrl;

    public PageUrl(String htmlText, String url) {
      this.htmlPage = htmlText;
      this.redirectUrl = url;
    }
  }

  /**
   * Request manager constructor sets the properties for the requestManager object and loads the (domain, qps) hash map
   *
   * @param qpsMap --- url-qps map.
   */
  public RequestManager(Map<String, Double> qpsMap) {
    manager = new MultiThreadedHttpConnectionManager();
    HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
    httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(100);
    httpConnectionManagerParams.setMaxTotalConnections(100);
    manager.setParams(httpConnectionManagerParams);
    httpClient = new HttpClient(manager);
    httpClient.getParams().setParameter(
        HttpMethodParams.USER_AGENT,
        USER_AGENT
    );
    httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
    for (String domain : qpsMap.keySet()) {
      rateLimiterMap.put(domain, RateLimiter.create(qpsMap.get(domain)));
    }
  }

  public RequestManager(Map<String, Double> qpsMap, String loc) {
    manager = new MultiThreadedHttpConnectionManager();
    HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
    httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(100);
    httpConnectionManagerParams.setMaxTotalConnections(100);
    manager.setParams(httpConnectionManagerParams);
    httpClient = new HttpClient(manager);
    httpClient.getParams().setParameter(
        HttpMethodParams.USER_AGENT,
        USER_AGENT
    );
    httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
    for (String domain : qpsMap.keySet()) {
      rateLimiterMap.put(domain, RateLimiter.create(qpsMap.get(domain)));
    }
  }

  public PageUrl doRequest(String url) throws IOException {
    return doRequest(url, null);
  }

  /**
   * @param url        - url to be fetched
   * @param stopDomain - if stopDomain is not null the the function doRequest returns when the url is redirected to a url from the stopDomain domain
   */
  public PageUrl doRequest(String url, String stopDomain) throws IOException {
    if (url == null) {
      return this.new PageUrl(null, null);
    }
    final Set<String> redirectUrls = new HashSet<String>();
    redirectUrls.add(url);
    url = removeInvalidCharacters(url);
    InternetDomainName topPrivateDomain = InternetDomainName.from(new URL(url).getHost()).topPrivateDomain();
    final String urlTopDomain = topPrivateDomain.toString();
    String host = new URL(url).getHost();
    if (!rateLimiterMap.containsKey(urlTopDomain)) {
      rateLimiterMap.put(urlTopDomain, RateLimiter.create(DEFAULT_QPS));
    }
    this.rateLimiterMap.get(urlTopDomain).acquire();
    String htmlText = null;
    String redirectLocation;
    while (true) {
      //System.out.println("entering the request manger while loop");
      url = addDomain(url, host);
      host = new URL(url).getHost();
      if (stopDomain != null && url.contains(stopDomain)) {
//         if domain == stopDomain then I don't need the html page return a blank html page
        return this.new PageUrl("<html> </html>", url);
      }
      HttpMethod getMethod = new GetMethod(url);
      getMethod.setFollowRedirects(false);
      try {
        //System.out.println("executing");
        httpClient.executeMethod(getMethod);

        //System.out.println("done executing");
        StringWriter writer = new StringWriter();
        InputStream inputStream = getMethod.getResponseBodyAsStream();
        IOUtils.copy(inputStream, writer);
        // get redirect location
        Header locationHeader = getMethod.getResponseHeader("location");
        // default case is that no redirect happens
        if (locationHeader != null) {
          redirectLocation = locationHeader.getValue();
          // if url is present in the set of already fetched urls in the doRequest method then a cycle of redirects exists
          if (redirectUrls.contains(redirectLocation)) {
            System.err.println("cyclic redirects");
          } else {
            redirectUrls.add(redirectLocation);
            redirectLocation = removeInvalidCharacters(redirectLocation);
            url = redirectLocation;
            // continue and fetch the new redirected url
            continue;
          }
        }
        htmlText = writer.toString();
      } finally {
        getMethod.releaseConnection();
      }
      // default case is that no redirect happens, in that case break from the loop
      break;
    }
    //System.out.println("TP2");
    return this.new PageUrl(htmlText, url);
  }

  /**
   * Replace the illegal characters from the url with ascii values
   */
  private String removeInvalidCharacters(String url) {
    url = url.replaceAll("<", "%3c");
    url = url.replaceAll(">", "%3e");
    url = url.replaceAll("\\[", "%5b");
    url = url.replaceAll("\\]", "%5d");
    url = url.replaceAll("\\|", "%7c");
    url = url.replaceAll("\\{", "%7b");
    url = url.replaceAll("}", "%7d");
    url = url.replaceAll(" ", "%20");
    url = url.replaceAll("\"", "%22");
    return url;
  }

  private String addDomain(String url, String domain) {
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      if (!url.startsWith("/")) {
        return "http://www." + url;
      }
      return "http://" + domain + url;
    } else {
      return url;
    }
  }
}