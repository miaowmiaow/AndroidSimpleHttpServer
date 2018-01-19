package com.z.androidsimplehttpserver;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.z.androidsimplehttpserver.handler.DownloadResUriHandler;
import com.z.androidsimplehttpserver.handler.ReceiveHandler;
import com.z.androidsimplehttpserver.handler.ResourceInAssetsHandler;
import com.z.androidsimplehttpserver.handler.ShareHandler;
import com.z.androidsimplehttpserver.server.SimpleHttpServer;

public class MainActivity extends Activity {

    private TextView tv1;
    private TextView tv2;
    private SimpleHttpServer shs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);

        String addr = "http://" + getlocalip() + ":" + SimpleHttpServer.port;
        String str = "本APP为局域网传输应用，使用和本机处同一网络设备\n\n访问" + addr + "/static/receive.html即可传输文件到本机\n\n访问"
                + addr + "/share/即可查看和下载本机share文件夹下的所有文件（可自行将文件放到share文件夹下实现共享）";
        tv1.setText(str);

        shs = new SimpleHttpServer();
        shs.registerResourceHandler(new ShareHandler(this));
        shs.registerResourceHandler(new ResourceInAssetsHandler(this));
        shs.registerResourceHandler(new DownloadResUriHandler());
        shs.registerResourceHandler(new ReceiveHandler() {
            @Override
            protected void onSuccess(final String str) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv2.setText(str);
                        Toast.makeText(MainActivity.this, "接收到请求参数", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        shs.startAsync();
    }

    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        //  Log.d(Tag, "int ip "+ipAddress);
        if (ipAddress == 0) return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    @Override
    protected void onDestroy() {
        if(shs != null){
            shs.stopAsync();
        }
    }
}
