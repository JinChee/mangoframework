package org.mangoframework.core;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: zhoujingjie
 * @Date: 16/4/22
 */
public class MangoDispatcherHelper {
    private static ThreadLocal<MangoDispatcher> threadLocal = new ThreadLocal<>();

    public static void register(MangoDispatcher mangoDispatcher){
        threadLocal.set(mangoDispatcher);
    }

    public static void post(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        MangoDispatcher md = threadLocal.get();
        Parameter parameter = md.initializeParameter(request,response);
        parameter.setExtension("_requestContext_");
        md.doDispatcher(parameter,response);
    }

}
