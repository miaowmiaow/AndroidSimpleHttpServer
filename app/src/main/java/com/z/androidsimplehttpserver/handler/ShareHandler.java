package com.z.androidsimplehttpserver.handler;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;

import com.z.androidsimplehttpserver.server.HttpContext;
import com.z.androidsimplehttpserver.server.SimpleHttpServer;
import com.z.androidsimplehttpserver.utils.StreamToolkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by 郁垒 on 2017/12/27.
 */

public class ShareHandler implements IResourceUriHandler {

    private Context context;

    public ShareHandler(Context context) {
        this.context = context;
    }

    @Override
    public boolean accept(HttpContext httpContext) {
        return httpContext.getRequestResourceUri().startsWith("/share/");
    }

    @Override
    public void handler(HttpContext httpContext) {
        InputStream fis = null;
        OutputStream nos = null;
        StringBuilder sb = new StringBuilder();
        try {
            nos = httpContext.getUnderlySocket().getOutputStream();
            fis = context.getAssets().open("file_list.html");
            String file_list = StreamToolkit.readRawFromStreamToString(fis);
            fis = context.getAssets().open("file.template");
            String file = StreamToolkit.readRawFromStreamToString(fis);
            File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/share/");
            File[] files = dirFile.listFiles();
            for (File f : files) {
                String str = file;
                str = str.replaceAll("\\{file_name\\}", f.getName());
                str = str.replaceAll("\\{file_path\\}", "http://" + getlocalip() + ":" + SimpleHttpServer.port + "/down/" + f.getName());
                sb.append(str);
            }
            file_list = file_list.replaceAll("\\{file_list\\}", sb.toString());
            PrintStream printer = new PrintStream(nos);
            printer.println("HTTP/1.1 200 OK");
            printer.println("Content-Type:text/html");
            printer.println("Content-Length:" + file_list.length());
            printer.println("Cache-Control:no-cache");
            printer.println("Pragma:no-cache");
            printer.println("Expires:0");
            printer.println();
            byte[] bytes = file_list.getBytes("UTF-8");
            printer.write(bytes);
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

    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        //  Log.d(Tag, "int ip "+ipAddress);
        if (ipAddress == 0) return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }
}
