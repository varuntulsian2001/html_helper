package com.html.helper;

import org.apache.log4j.Logger;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Timestamp;
import java.util.zip.GZIPOutputStream;

/**
 * Stored page format
 * mp contains html page and redirect link
 */
public class HtmlStorageNew implements Serializable {
    static final Logger LOGGER = Logger.getLogger(HtmlStorageNew.class);
    public String htmlPage;
    public String redirectUrl;
    protected static volatile UrlHtmlMapping URL_HTML_MAPPING = null;
    private static final long serialVersionUID = -5299552157224445592L;
    private final static String LOCK = "lock";

    public static void setDataStore(DataStore dataStore) {
        HtmlStorageNew.dataStore = dataStore;
    }

    static DataStore dataStore = null;

    public static String getLevelDbLocation() {
        return levelDbLocation;
    }

    public static void setLevelDbLocation(String levelDbLocation) {
        HtmlStorageNew.levelDbLocation = levelDbLocation;
    }

    private static String levelDbLocation = null;

    public HtmlStorageNew(String htmlPage, String url) {
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
            return new DataStore(htmlText.toString(), url,0);
        } else {
            return new DataStore(null, null, 0);
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

    private static BufferedWriter getOutputStream(final String outputPath, final int currentId) throws IOException {
        return new BufferedWriter(
                new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputPath + "_" + currentId))));
    }

    /**
     * get the page from the local repository or using the doRequest method
     *
     * @param forced - it true the force fetch the page using the doRequest method
     * @return - DataStore object
     */
    public static DataStore getPage(RequestManager rm, String origUrl, String stopDomain, Boolean forced, int validity) throws IOException {
        initLeveldb();
//        If data is available and force is false then fetch data from disk
        if (!forced) {
            DataStore localData;
            try {
                localData = (DataStore) URL_HTML_MAPPING.get((Object) origUrl);
            } catch (Exception e) {
                localData = null;
            }
            if (localData != null && localData.getHtmlPage().length() > 100 && localData.getHtmlPage() != null) {
                if ((new Timestamp(new java.util.Date().getTime()).getTime() -
                        localData.getTimeStamp().getTime()) > (long)(localData.getValidityDays()) * 24 * 60 * 60 * 1000) {
                    LOGGER.info("page is in-validated so fetching it afresh");
                } else{
                    return localData;
                }
            }
        }
        RequestManager.PageUrl pageUrl;
        try {
            pageUrl = rm.doRequest(origUrl, stopDomain);
        } catch (Exception e) {
            LOGGER.error("Some error fetching url " + origUrl, e);
            return new DataStore("<html> </html>", "",0);
        }
        DataStore dataStore;
        if (HtmlStorageNew.dataStore == null) {
            dataStore = new DataStore(pageUrl.htmlPage, pageUrl.redirectUrl, validity);
        } else {
            dataStore = HtmlStorageNew.dataStore;
            dataStore.setHtmlPage(pageUrl.htmlPage);
            dataStore.setRedirectUrl(pageUrl.redirectUrl);
            dataStore.setValidityDays(validity);
            dataStore.setTimeStamp();
            URL u = new URL(pageUrl.redirectUrl);
            dataStore.setDomain(u.getHost());
            setFields(dataStore, dataStore.htmlPage);
        }
        URL_HTML_MAPPING.put(origUrl, dataStore);
        return dataStore;
    }

    private static void initLeveldb() throws IOException {
        if (URL_HTML_MAPPING == null) {
            synchronized (LOCK) {
                if (URL_HTML_MAPPING == null) {
                    if (levelDbLocation == null) {
                        LOGGER.error("call setLevelDbLocation before calling get page");
                        System.exit(1);
                    }
                    URL_HTML_MAPPING = new UrlHtmlMapping(levelDbLocation);
                }
            }
        }
    }

    private static void setFields(DataStore dataStore, String html) {
        Field[] fields = dataStore.getClass().getDeclaredFields();
        LOGGER.debug(dataStore.getClass().getName());
        for (Field field : fields) {
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), dataStore.getClass());
//        pd.getWriteMethod().invoke(dataStore, 100 );
                String methodName = "set" + (field.getName().substring(0, 1)).toUpperCase() + field.getName().substring(
                        1);
                Class<?> params[] = new Class[1];
                for (int i = 0; i < params.length; i++) {
                    params[i] = String.class;
                }
                String[] param = new String[]{html};
                // calling the desired method
                dataStore.getClass().getMethod(methodName, params).invoke(dataStore, param);
                LOGGER.debug(pd.getReadMethod().invoke(dataStore));
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
    }

    public static DataStore getPage(RequestManager rm, String origUrl) throws IOException {
        return getPage(rm, origUrl, null, false, 10);
    }

    public static DataStore getPage(RequestManager rm, String origUrl, String stopDomain) throws IOException {
        return getPage(rm, origUrl, stopDomain, false, 10);
    }

    public static DataStore getPage(RequestManager rm, String origUrl, Boolean forced) throws IOException {
        return getPage(rm, origUrl, null, forced, 10);
    }

    public static DataStore getPage(RequestManager rm, String origUrl, int validity) throws IOException {
        return getPage(rm, origUrl, null, false, validity);
    }

    public static DataStore getPage(RequestManager rm, String origUrl, String stopDomain, int validity) throws IOException {
        return getPage(rm, origUrl, stopDomain, false, validity);
    }

    public static DataStore getPage(RequestManager rm, String origUrl, Boolean forced, int validity) throws IOException {
        return getPage(rm, origUrl, null, forced, validity);
    }

    public static void close() throws IOException {
        URL_HTML_MAPPING.close();
    }
}
