package com.html.helper;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.WriteBatch;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UrlHtmlMapping {
    private DB db;
    private String DB_FILE = null;
    private DBIterator dbi;

    public UrlHtmlMapping(String dbFileName) throws IOException {
        DB_FILE = dbFileName;
        try {
            this.initRO(this.DB_FILE);
        } catch (Exception e) {
            LeveldbHelper.create(this.DB_FILE);
            this.initRO(this.DB_FILE);
            System.err.println("Could not read database from : " + this.DB_FILE);
        }
    }

    public void createDB(String dataFileName, String dbLocation) throws IOException {
        db = LeveldbHelper.create(dbLocation);
        FileReaderHelper reader = new FileReaderHelper(dataFileName, false);
        int batchSize = 0;

        WriteBatch batch = db.createWriteBatch();
        int badlineCount = 0;
        int recordWritten = 0;
        while (reader.advance()) {
            final String[] parts = reader.splitCurrentLine("\\t");
            if (parts.length != 2) {
                ++badlineCount;
                continue;
            }
            batch.put(parts[1].getBytes(), parts[0].getBytes());
            ++batchSize;
            if (batchSize >= 10000) {
                db.write(batch);
                batch.close();
                batch = db.createWriteBatch();
                batchSize = 0;
            }
            if (++recordWritten % 1000000 == 0) {
                System.out.println("Lines read: " + reader.getNumLinesRead());
                System.out.println("Records written: " + recordWritten);
            }
        }
        if (batchSize > 0) {
            db.write(batch);
        }
        batch.close();
        db.close();
        System.err.println("Bad lines: " + badlineCount);
    }

    public void initRO(String dbLocation) throws IOException {
        db = LeveldbHelper.openRO(dbLocation);
    }

    public String get(final String key) throws UnsupportedEncodingException {
        byte[] bytes = db.get(key.getBytes());
        if (bytes == null) {
            return "";
        }
        return new String(bytes);
    }

    public void put(final Object key, final Object value) {
        byte[] bytesKey;
        byte[] bytesValue;
        try {
            bytesKey = objectToByte(key);
            bytesValue = objectToByte(value);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        db.put(bytesKey, bytesValue);
    }

    public Object get(final Object key) {
        byte[] bytesKey;
        try {
            bytesKey = objectToByte(key);
            byte[] b = db.get(bytesKey);
            return byteToObject(b);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() throws IOException {
        db.close();
    }

    public byte[] objectToByte(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] yourBytes;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            yourBytes = bos.toByteArray();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return yourBytes;
    }

    public Object byteToObject(byte[] yourBytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
        ObjectInput in = null;
        Object o;
        try {
            in = new ObjectInputStream(bis);
            o = in.readObject();
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return o;
    }

    public void seekToFirst() {
        this.dbi = db.iterator();
        dbi.seekToFirst();
    }

    public Map.Entry<Object, Object> next() throws IOException, ClassNotFoundException {
        if (!dbi.hasNext()) {
            return null;
        }
        final Map.Entry<byte[], byte[]> nextVal = dbi.next();
        return new HashMap.SimpleEntry<Object, Object>(byteToObject(nextVal.getKey()),
                                                       byteToObject(nextVal.getValue()));
    }


    public static void main(String[] args) throws IOException {

        UrlHtmlMapping mapping = new UrlHtmlMapping("leveldb/inlinkToOutlinks");
//    mapping.db.put("b".getBytes(), "c".getBytes());
//    mapping.db.put("d".getBytes(), "c".getBytes());
//    mapping.db.put("e".getBytes(), "c".getBytes());
//    mapping.db.put("f".getBytes(), "c".getBytes());
//    mapping.db.put("g".getBytes(), "c".getBytes());
//    for( Integer i =0 ; i<10000; i++){
//      mapping.db.put(String.valueOf(i).getBytes(), "c".getBytes());
//    }
        test(mapping);
//    System.out.println(new String(mapping.db.get("e".getBytes())));
        mapping.close();

//    ItemMapping mapping = new ItemMapping("mappingdb_Amazon");
//    String s = mapping.get("921928");
//    System.out.println(s);
    }

    private static void test(UrlHtmlMapping mapping) throws UnsupportedEncodingException {
        String s = " lol ";
        mapping.put(s, 100);
        Integer i = (Integer) mapping.get((Object) s);
        System.out.println(i);
    }
}


