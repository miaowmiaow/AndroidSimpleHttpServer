package com.z.androidsimplehttpserver.handler;

import com.z.androidsimplehttpserver.server.HttpContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 郁垒 on 2017/12/27.
 */

public class ReceiveHandler implements IResourceUriHandler {

    private String acceptPrefix = "/receive/";

    @Override
    public boolean accept(HttpContext httpContext) {
        return httpContext.getRequestResourceUri().startsWith(acceptPrefix);
    }

    @Override
    public void handler(HttpContext httpContext) {
        OutputStream nos = null;
        try {
            StringBuilder sb = new StringBuilder();
            HashMap<String, String> ps = httpContext.getRequestParams();
            for (Map.Entry<String, String> entry : ps.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(";\n");
            }
            HashMap<String, String> pfs = httpContext.getRequestFileParams();
            sb.append("\n\n\n");
            for (Map.Entry<String, String> entry : pfs.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(";\n");
            }
            onSuccess(sb.toString());
            nos = httpContext.getUnderlySocket().getOutputStream();
            PrintStream printer = new PrintStream(nos);
            printer.println("HTTP/1.1 200 OK");
            printer.println();
            printer.write("success".getBytes());
            printer.flush();
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (nos != null) {
                    nos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected void onSuccess(String str) {

    }

    @Override
    public void destroy() {

    }

}
