package com.z.androidsimplehttpserver.handler;

import android.os.Environment;

import com.z.androidsimplehttpserver.server.HttpContext;
import com.z.androidsimplehttpserver.utils.StreamToolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by 郁垒 on 2018/1/18.
 */

public class DownloadResUriHandler implements IResourceUriHandler {

    private String acceptPrefix = "/down/";

    @Override
    public boolean accept(HttpContext httpContext) {
        return httpContext.getRequestResourceUri().startsWith(acceptPrefix);
    }

    @Override
    public void handler(final HttpContext httpContext) {

        int startIndex = acceptPrefix.length();
        final String resPath = httpContext.getRequestResourceUri().substring(startIndex);

        OutputStream nos = null;
        FileInputStream fis = null;
        byte[] raw;
        try {
            nos = httpContext.getUnderlySocket().getOutputStream();
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/share/" + resPath);
            fis = new FileInputStream(file);
            raw = StreamToolkit.readRawFromStream(fis);
            PrintStream printer = new PrintStream(nos);
            printer.println("HTTP/1.1 200 OK");
            printer.println("Content-Length:" + raw.length);
            if (acceptPrefix.endsWith(".html")) {
                printer.println("Content-Type:text/html");
            } else if (acceptPrefix.endsWith(".js")) {
                printer.println("Content-Type:text/js");
            } else if (acceptPrefix.endsWith(".css")) {
                printer.println("Content-Type:text/css");
            } else if (acceptPrefix.endsWith(".jpg")) {
                printer.println("Content-Type:text/jpg");
            } else if (acceptPrefix.endsWith(".png")) {
                printer.println("Content-Type:text/png");
            } else if (acceptPrefix.endsWith(".mp3")) {
                printer.println("Content-Type:audio/mp3");
            } else if (acceptPrefix.endsWith(".mp4")) {
                printer.println("Content-Type:video/mpeg4");
            } else {
                printer.println("Content-Type:application/octet-stream");
            }
            printer.println();
            printer.write(raw);
            printer.flush();
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (nos != null) {
                    nos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {

    }
}
