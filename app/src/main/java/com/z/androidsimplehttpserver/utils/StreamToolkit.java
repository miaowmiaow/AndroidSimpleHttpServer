package com.z.androidsimplehttpserver.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 郁垒 on 2017/12/27.
 */

public class StreamToolkit {

    public static String readLine(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1];
        int c1 = 0;
        int c2 = 0;
        int len = 0;
        while (c2 != -1 && !(c1 == '\r' && c2 == '\n')) {
            len = is.read(bytes);
            if (len > 0) {
                c1 = c2;
                c2 = bytes[0];
                bos.write(bytes, 0, len);
            }
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    public static byte[] readLineToByte(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1];
        int c1 = 0;
        int c2 = 0;
        int len = 0;
        while (c2 != -1 && !(c1 == '\r' && c2 == '\n')) {
            len = is.read(bytes);
            if (len > 0) {
                c1 = c2;
                c2 = bytes[0];
                bos.write(bytes, 0, len);
            }
        }
        return bos.toByteArray();
    }

    public static byte[] readRawFromStream(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int nReaded;
        while ((nReaded = is.read(buffer)) != -1) {
            bos.write(buffer, 0, nReaded);
        }
        return bos.toByteArray();
    }

    public static String readRawFromStreamToString(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = 0;
        byte[] bytes = new byte[2048];
        while ((len = is.read(bytes)) != -1) {
            baos.write(bytes, 0, len);
        }
        return baos.toString("UTF-8");
    }


}
