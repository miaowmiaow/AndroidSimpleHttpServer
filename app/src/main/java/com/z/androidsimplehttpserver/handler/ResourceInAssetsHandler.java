package com.z.androidsimplehttpserver.handler;

import android.content.Context;

import com.z.androidsimplehttpserver.server.HttpContext;
import com.z.androidsimplehttpserver.utils.StreamToolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by 郁垒 on 2017/12/27.
 */

public class ResourceInAssetsHandler implements IResourceUriHandler {

    private Context context;
    private String acceptPrefix = "/static/";

    public ResourceInAssetsHandler(Context context) {
        this.context = context;
    }

    @Override
    public boolean accept(HttpContext httpContext) {
        return httpContext.getRequestResourceUri().startsWith(acceptPrefix);
    }

    @Override
    public void handler(HttpContext httpContext) {
        int startIndex = acceptPrefix.length();
        String assetsPath = httpContext.getRequestResourceUri().substring(startIndex);
        InputStream fis = null;
        OutputStream nos = null;
        byte[] raw;
        try {
            nos = httpContext.getUnderlySocket().getOutputStream();
            fis = context.getAssets().open(assetsPath);
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
            }
            printer.println("from resource in assets handler");
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
        this.context = null;
    }
}
