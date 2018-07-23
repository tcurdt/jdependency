package org.vafer.jdependency.test;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class Main {

    public static void main(String[] argv) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        IOUtils.copy(is, os);
    }
}