package com.z.androidsimplehttpserver.handler;

import com.z.androidsimplehttpserver.server.HttpContext;

/**
 * Created by 郁垒 on 2017/12/27.
 */

public interface IResourceUriHandler {

    boolean accept(HttpContext httpContext);

    void handler(HttpContext httpContext);

    void destroy();

}
