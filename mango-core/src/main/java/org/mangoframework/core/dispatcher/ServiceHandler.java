package org.mangoframework.core.dispatcher;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mangoframework.core.exception.UnauthorizedException;
import org.mangoframework.core.utils.ConfigUtils;
import org.mangoframework.core.view.ResultView;

/**
 * @author: zhoujingjie
 * @Date: 16/4/23
 */
public class ServiceHandler {

    private static Logger log = Logger.getLogger(ServiceHandler.class);

    private static String DEFAULT_CHARSET = "UTF-8";

    private HandlerAdapter ha;

    private static ServiceHandler instance;

    private ServiceHandler() {
        ha = initializeHandlerAdapter();
    }

    /**
     * 初始化
     *
     * @return #ServiceHandler
     */
    public static ServiceHandler initialize() {
        instance = new ServiceHandler();
        return instance;
    }

    /**
     * 获取实例
     *
     * @return #ServiceHandler
     */
    public static ServiceHandler getInstance() {
        if (instance == null) {
            synchronized (ServiceHandler.class) {
                if (instance == null) {
                    instance = initialize();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化参数
     *
     * @param request 请求
     * @return Parameter
     * @throws java.io.UnsupportedEncodingException
     *
     */
    public Parameter initializeParameter(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Parameter parameter = new Parameter();
        parameter.setMethod(request.getMethod().toUpperCase());

        String requestUrl = request.getRequestURI();
        if (requestUrl.endsWith("/")) {
            requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
        }

        String contextPath = request.getServletContext().getContextPath();
        parameter.setRequestURL(requestUrl);
        int index = requestUrl.indexOf(contextPath);
        String path = requestUrl.substring(index + contextPath.length());
        String servletPath = request.getServletPath();
        if(!servletPath.equals(path)) {
            path = path.substring(servletPath.length());
        }
        path = URLDecoder.decode(new String(path.getBytes("ISO-8859-1"), DEFAULT_CHARSET), DEFAULT_CHARSET);
        if (path.contains(".")) {
            int temp = path.lastIndexOf(".");
            parameter.setExtension(path.substring(temp + 1).toUpperCase());
            parameter.setPath(path.substring(0, temp));
        } else {
            parameter.setExtension(ConfigUtils.getDefaultExtension());
            parameter.setPath(path);
        }
        parameter.setRequest(request);
        parameter.setResponse(response);

        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(ConfigUtils.getMaxFileSize());
            upload.setSizeMax(ConfigUtils.getMaxSize());
            Map<String, List<FileItem>> map = upload.parseParameterMap(request);
            for (Map.Entry<String, List<FileItem>> entry : map.entrySet()) {
                if (entry.getValue() != null && entry.getValue().size() > 0) {
                    if (entry.getValue().get(0).isFormField()) {
                        parameter.getParamString().put(entry.getKey(), join(entry.getValue()));
                    } else {
                        parameter.getParamFile().put(entry.getKey(), entry.getValue());
                    }
                }
            }
            if (request.getParameterMap() != null) {
                for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                    parameter.getParamString().put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
                }
            }
        } else {
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                parameter.getParamString().put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
            }
        }

        return parameter;
    }

    private String join(List<FileItem> items) {
        StringBuilder sb = new StringBuilder();
        if (items == null || items.size() == 0)
            return null;
        for (FileItem item : items) {
            try {
                sb.append(item.getString(DEFAULT_CHARSET));
                sb.append(",");
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }
        sb = sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    /**
     * 初始化处理适配器
     *
     * @return handlerAdapter
     */
    private HandlerAdapter initializeHandlerAdapter() {
        final Object adapter = new SimpleHandlerAdapter();
        return (HandlerAdapter) Proxy.newProxyInstance(getClass().getClassLoader(), SimpleHandlerAdapter.class.getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(adapter, args);
            }
        });
    }

    /**
     * 处理请求 返回结果
     *
     * @param parameter 参数
     * @return object
     */
    public ResultView handleRequest(Parameter parameter) throws Exception {
        return ha.handle(parameter);
    }

}
