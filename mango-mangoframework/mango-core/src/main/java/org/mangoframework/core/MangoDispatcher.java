package org.mangoframework.core;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.mangoframework.core.exception.MangoException;
import org.mangoframework.core.utils.PropertiesUtils;
import org.mangoframework.core.view.ResultView;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author zhoujingjie
 * @date 2016/4/22.
 */
public class MangoDispatcher extends HttpServlet{

    private static String DEFAULT_CHARSET = "UTF-8";

    private static String DEFAULT_CONFIG="mango.properties";

    private static Logger log = Logger.getLogger(MangoDispatcher.class);

    private HandlerAdapter ha;

    private ControllerMapping hm;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ha = initializeHandlerAdapter();
        hm = ControllerMapping.init(PropertiesUtils.getControllerClassNames());
        PropertiesUtils.init(DEFAULT_CONFIG);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Parameter parameter = initializeParameter(request);
        ResultView view = ha.handle(parameter);
        view.represent();

    }

    /**
     * 初始化参数
     * @param request   请求
     * @return Parameter
     * @throws UnsupportedEncodingException
     */
    private Parameter initializeParameter(HttpServletRequest request) throws UnsupportedEncodingException {
        Parameter parameter = new Parameter();
        parameter.setMethod(request.getMethod().toUpperCase());

        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            ServletContext ctx = request.getServletContext();
            upload.setFileSizeMax(Long.parseLong(ctx.getInitParameter("fileSizeMax")));
            upload.setSizeMax(Long.parseLong(ctx.getInitParameter("sizeMax")));
            try {
                Map<String, List<FileItem>> map = upload.parseParameterMap(request);
                for (Map.Entry<String, List<FileItem>> entry : map.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().size() > 0) {
                        if (entry.getValue().get(0).isFormField()) {
                            parameter.getParamString().put(entry.getKey().toUpperCase(), join(entry.getValue()));
                            parameter.getParamString().put(entry.getKey(), join(entry.getValue()));
                        } else {
                            parameter.getParamFile().put(entry.getKey().toUpperCase(), entry.getValue());
                            parameter.getParamFile().put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                if (request.getParameterMap() != null) {
                    for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                        parameter.getParamString().put(entry.getKey(), join(entry.getValue(), ","));
                        parameter.getParamString().put(entry.getKey().toUpperCase(), join(entry.getValue(), ","));
                    }
                }
            } catch (FileUploadException e) {
                throw new MangoException(e.getCause());
            }
        } else {
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                parameter.getParamString().put(entry.getKey().toUpperCase(), join(entry.getValue(), ","));
                parameter.getParamString().put(entry.getKey(), join(entry.getValue(), ","));
            }
        }

        String requestUrl = request.getRequestURI();
        if (requestUrl.endsWith("/")) {
            requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
        }
        String contextPath = request.getServletContext().getContextPath() ;
        parameter.setRequestURL(requestUrl);
        int index = requestUrl.indexOf(contextPath);
        String path = requestUrl.substring(index + contextPath.length());
        path = URLDecoder.decode(new String(path.getBytes("ISO-8859-1"), DEFAULT_CHARSET), DEFAULT_CHARSET);
        if (path.contains(".")) {
            int temp = path.lastIndexOf(".");
            parameter.setExtension(path.substring(temp + 1));
            parameter.setPath(path.substring(0, temp));
        } else {
            parameter.setExtension("");
            parameter.setPath(path);
        }
        parameter.setRequest(request);

        return parameter;
    }

    private HandlerAdapter initializeHandlerAdapter(){
        final Object adapter = new SimpleHandlerAdapter();
        return (HandlerAdapter) Proxy.newProxyInstance(getClass().getClassLoader(), HandlerAdapter.class.getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(adapter,args);
            }
        });
    }

}
