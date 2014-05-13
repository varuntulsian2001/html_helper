package com.html.helper;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LeveldbHelper {
    private static ByteBuffer byteBuffer = ByteBuffer.allocate(4);

    public static DB create(final String loc) throws IOException {
        Options options = new Options();
        JniDBFactory.factory.destroy(new File(loc), options);
        options.createIfMissing(true);
        options.maxOpenFiles(25);
//    options.verifyChecksums(false);
//    options.blockSize(4 * 1024 * 1024);
        return JniDBFactory.factory.open(new File(loc), options);
    }

    public synchronized static DB openRO(final String loc) throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        options.maxOpenFiles(100);
        return JniDBFactory.factory.open(new File(loc), options);
    }

    public synchronized static byte[] intToBytes(final Integer i) {
        byteBuffer.clear();
        return byteBuffer.order(ByteOrder.LITTLE_ENDIAN).putInt(i).array();
    }

    public synchronized static int byteArrToInt(final byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
