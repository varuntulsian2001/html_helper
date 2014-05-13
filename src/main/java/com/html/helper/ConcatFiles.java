package com.html.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConcatFiles {
    List<String> files = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        final File folder = new File("../data/pagesWithoutTags");
        ConcatFiles c = new ConcatFiles();
        c.filesForFolder(folder);
        c.concatFiles();
    }

    public void concatFiles() throws IOException {
        File dir = new File("../data/concatFiles");
        dir.mkdir();
        StringBuilder sbFull = new StringBuilder();
        int size = 0;
        int numberOfFiles = 0;
        for (String file : files) {
            StringBuilder sb = Utility.readGzipFile("../data/pagesWithoutTags/" + file);
            sbFull.append(sb);
            size += sb.length();
            if (numberOfFiles < size / 50000000) {
                Utility.writeBzip2File("../data/concatFiles/concatDump" + numberOfFiles, sbFull);
                sbFull = new StringBuilder();
                numberOfFiles++;
            }
        }
        if (sbFull.length() > 0) {
            Utility.writeBzip2File("../data/concatFiles/concatDump" + numberOfFiles, sbFull);
        }
    }

    public void filesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                continue;
            } else {
                files.addAll(Arrays.asList(fileEntry.getName().split("\n")));
            }
        }
    }
}
