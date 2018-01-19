package com.z.androidsimplehttpserver.server;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.z.androidsimplehttpserver.utils.StreamToolkit;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * --------------------GET报文格式--------------------
 * <p>
 * GET /post/?name=GET&age=GET HTTP/1.1\r\n
 * Accept: post/html, application/xhtml+xml, image/jxr, /*\r\n
 * Referer: http://192.168.10.62:8088/static/post.html\r\n
 * Accept-Language: zh-CN\r\n
 * User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko\r\n
 * Accept-Encoding: gzip, deflate\r\n
 * Host: 192.168.10.62:8088\r\n
 * Connection: Keep-Alive\r\n
 * \r\n
 * <p>
 * --------------------POST报文格式--------------------
 * <p>
 * POST /post/ HTTP/1.1\r\n
 * Accept: post/html, application/xhtml+xml, image/jxr, /*\r\n
 * Referer: http://192.168.10.62:8088/static/post.html\r\n
 * Accept-Language: zh-CN\r\n
 * User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko\r\n
 * Content-Type: multipart/form-data; boundary=---------------------------7e211a2520680\r\n
 * Accept-Encoding: gzip, deflate\r\n
 * Host: 192.168.10.62:8088\r\n
 * Content-Length: 27566\r\n
 * Connection: Keep-Alive\r\n
 * Cache-Control: no-cache\r\n
 * \r\n
 * -----------------------------7e211a2520680\r\n
 * Content-Disposition: form-data; name="name"\r\n
 * \r\n
 * qwe\r\n
 * -----------------------------7e211a2520680\r\n
 * Content-Disposition: form-data; name="age"\r\n
 * \r\n
 * 123\r\n
 * -----------------------------7e211a2520680\r\n
 * Content-Disposition: form-data; name="file"; filename="qrcode.jpg"\r\n
 * Content-Type: image/jpeg\r\n
 * \r\n
 * ...\r\n 这里是图片的二进制数据略
 * -----------------------------7e211a2520680--\r\n
 */

public class HttpContext {

    private static final String TAG = "HttpContext";

    private Socket underlySocket;
    private final HashMap<String, String> requestHeaders;
    private final HashMap<String, String> requestParams;
    private final HashMap<String, String> requestFileParams;
    private String method;
    private String resourceUri;

    public HttpContext() {
        requestHeaders = new HashMap<>();
        requestParams = new HashMap<>();
        requestFileParams = new HashMap<>();
    }

    public Socket getUnderlySocket() {
        return underlySocket;
    }

    public void setUnderlySocket(Socket underlySocket) {
        try {
            this.underlySocket = underlySocket;
            InputStream is = this.underlySocket.getInputStream();
            String headerLine = StreamToolkit.readLine(is);// 获取请求方式和地址
            if (!TextUtils.isEmpty(headerLine)) {
//                Log.d(TAG, headerLine);
                String[] headerLines = headerLine.split(" ");
                if (headerLines.length > 1) {
                    setRequestMethod(headerLines[0]);
                    setRequestResourceUri(headerLines[1]);
                }
                boolean headerEnd = false;
                while (!headerEnd) {
                    headerLine = StreamToolkit.readLine(is);
//                    Log.d(TAG + "_h", headerLine);
                    if (!TextUtils.isEmpty(headerLine)) {
                        if ("\r\n".equals(headerLine)) { // 当遇到第一个\r\n时，获取请求头结束
                            headerEnd = true;
                        }
                        String[] pair = headerLine.split(": ");// 获取请求头参数
                        if (pair.length > 1) {
                            addRequestHeader(pair[0], pair[1].replace("\r\n", ""));
                        }
                    }
                }
                if ("GET".equals(getRequestMethod())) {
                    String url = getRequestResourceUri();
                    String params = url.substring(url.indexOf("?") + 1, getRequestResourceUri().length());
                    String[] params1 = params.split("&");
                    if (params1.length > 1) {
                        for (String params2 : params1) {
                            String[] params3 = params2.split("=");
                            if (params3.length > 1) {
                                addRequestParams(params3[0], params3[1].replace("\r\n", ""));
                            }
                        }
                    }
                } else if ("POST".equals(getRequestMethod())) {
                    if ("multipart/form-data".equals(getContentTypeValue())) {
                        boolean postBoundaryEnd = false;
                        String name = null;
                        String filename = null;
                        while (!postBoundaryEnd) {
                            String line = StreamToolkit.readLine(is);
                            Log.d(TAG + "_p", line);
                            if (!"\r\n".equals(line)) {// 获取提交上来的参数名
                                if (line.contains("Content-Disposition")) {
                                    String[] pair = line.replaceAll("\r\n", "").split("; ");
                                    if (pair[1].length() > 7 && "name".equals(pair[1].substring(0, 4))) {
                                        name = pair[1].substring(6, pair[1].length() - 1);
                                        if (pair.length > 2 && pair[2].length() > 11 && "filename".equals(pair[2].substring(0, 8))) {
                                            filename = pair[2].substring(10, pair[2].length() - 1);
                                        }
                                    }
                                }
                            } else {
                                boolean postParams = false;
                                if (TextUtils.isEmpty(filename)) {// 获取到filename代表是文件流，否则是字符流
                                    StringBuilder sb = new StringBuilder();
                                    while (!postParams) {
                                        line = StreamToolkit.readLine(is);
//                                        Log.d(TAG + "_p", line);
                                        if (getBoundary().equals(line) || getBoundaryEnd().equals(line)) {
                                            addRequestParams(name, sb.toString().replaceAll("\r\n", ""));
                                            postParams = true;
                                        } else {
                                            sb.append(line);
                                        }
                                    }
                                } else {
                                    String filePath = Environment.getExternalStorageDirectory().getPath() + "/share/" + System.currentTimeMillis() + "_" + filename;
                                    FileOutputStream fos = new FileOutputStream(filePath);
                                    while (!postParams) {
                                        byte[] data = StreamToolkit.readLineToByte(is);
                                        line = new String(data);
//                                        Log.d(TAG + "_p", line);
                                        if (getBoundary().equals(line) || getBoundaryEnd().equals(line)) {
                                            addRequestFileParams(name, filePath);
                                            postParams = true;
                                        } else {
                                            fos.write(data);
                                        }
                                    }
                                    fos.close();
                                    filename = "";
                                }
                            }
                            if (getBoundaryEnd().equals(line)) {// 当遇到结束分隔符退出循环
                                postBoundaryEnd = true;
                            }
                        }
                    } else if ("application/x-www-form-urlencoded".equals(getContentTypeValue())) {// 根据自己的实际需求进行解析
                        Log.d(TAG + "_x", StreamToolkit.readRawFromStreamToString(is));
                    } else if ("text/plain".equals(getContentTypeValue())) {
                        Log.d(TAG + "_t", StreamToolkit.readRawFromStreamToString(is));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRequestHeader(String headerName, String headerValue) {
        requestHeaders.put(headerName, headerValue);
    }

    private void addRequestParams(String paramsName, String paramsValue) {
        requestParams.put(paramsName, paramsValue);
    }

    private void addRequestFileParams(String paramsName, String paramsValue) {
        requestFileParams.put(paramsName, paramsValue);
    }

    public String getRequestHeaderValue(String name) {
        return requestHeaders.containsKey(name) ? requestHeaders.get(name) : "";
    }

    public HashMap<String, String> getRequestParams() {
        return requestParams;
    }

    public String getRequestParams(String name) {
        return requestParams.containsKey(name) ? requestParams.get(name) : "";
    }

    public HashMap<String, String> getRequestFileParams() {
        return requestFileParams;
    }

    public String getRequestFileParams(String name) {
        return requestFileParams.containsKey(name) ? requestFileParams.get(name) : "";
    }

    public String getRequestMethod() {
        return method;
    }

    private void setRequestMethod(String method) {
        this.method = method;
    }

    private void setRequestResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getRequestResourceUri() {
        return resourceUri;
    }

    public Long getContentLength() {
        String length = getRequestHeaderValue("Content-Length");
        if (TextUtils.isEmpty(length)) {
            length = getRequestHeaderValue("content-length");
        }
        return Long.parseLong(length);
    }

    public String getContentType() {
        return getRequestHeaderValue("Content-Type");
    }

    public String getContentTypeValue() {
        return getContentType().split("; ")[0];
    }

    public String getBoundary() {
        return "--" + getContentType().split("; ")[1].substring(9) + "\r\n";
    }

    public String getBoundaryEnd() {
        return "--" + getContentType().split("; ")[1].substring(9) + "--\r\n";
    }

}
