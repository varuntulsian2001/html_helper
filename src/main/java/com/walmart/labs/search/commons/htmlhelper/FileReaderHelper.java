package com.walmart.labs.search.commons.htmlhelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileReaderHelper {
    private BufferedReader reader;
    private String line;
    private int numLinesRead = 0;

    public FileReaderHelper(final String file, final boolean skipHeader) {
        try {
            reader = new BufferedReader(new FileReader(file));
            if (skipHeader) {
                reader.readLine();
                ++numLinesRead;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean advance() {
        try {
            line = reader.readLine();
            boolean b = line != null;
            if (b) {
                ++numLinesRead;
            }
            return b;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] splitCurrentLine(final String regex) {
        return line.split(regex);
    }

    public String getCurrentLine() {
        return line;
    }

    public int getNumLinesRead() {
        return numLinesRead;
    }
}
