package com.z.androidsimplehttpserver.server;

import android.os.Environment;
import android.util.Log;

import com.z.androidsimplehttpserver.handler.IResourceUriHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 郁垒 on 2017/12/27.
 */

public class SimpleHttpServer {

    private static final String TAG = "SimpleHttpServer";


    public static final int port = 8088;
    private final ExecutorService threadPool;
    private boolean isEnable;
    private ServerSocket serverSocket;
    private Set<IResourceUriHandler> resourceHandler;

    public SimpleHttpServer() {
        threadPool = Executors.newCachedThreadPool();
        resourceHandler = new HashSet<>();
        File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/share/");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    public void startAsync() {
        isEnable = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                doProcSync();
            }
        }).start();
    }

    public void stopAsync() {
        if (!isEnable) {
            return;
        }
        isEnable = false;
        unregisterResUriHandlerList();
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }
    }

    private void doProcSync() {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(port);
            serverSocket = new ServerSocket();
            serverSocket.bind(socketAddress);
            while (isEnable) {
                final Socket remotePeer = serverSocket.accept();
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
//                        Log.d("spy", "a remote peer accepted..." + remotePeer.getRemoteSocketAddress().toString());
                        doAcceptRemotePeer(remotePeer);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

    public void registerResourceHandler(IResourceUriHandler handler) {
        resourceHandler.add(handler);
    }

    private void doAcceptRemotePeer(Socket remotePeer) {
        HttpContext httpContext = new HttpContext();
        httpContext.setUnderlySocket(remotePeer);
        for (IResourceUriHandler handler : resourceHandler) {
            if (!handler.accept(httpContext)) {
                continue;
            }
            handler.handler(httpContext);
        }
    }

    public void unregisterResUriHandlerList() {
        for (IResourceUriHandler handler : resourceHandler) {
            handler.destroy();
        }
    }
}
