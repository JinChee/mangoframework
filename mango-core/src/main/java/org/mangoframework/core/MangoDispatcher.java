package org.mangoframework.core;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.mangoframework.core.exception.ExceptionHandler;
import org.mangoframework.core.utils.ConfigUtils;
import org.mangoframework.core.view.JsonView;
import org.mangoframework.core.view.RequestContextView;
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
import java.util.HashMap;
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

    private Map<String,ResultView> resultViewMap;

    private ExceptionHandler exceptionHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ha = initializeHandlerAdapter();

        exceptionHandler = initializeExceptionHandler();

        ControllerMapping.init(ConfigUtils.getControllerClassNames());

        ConfigUtils.init(DEFAULT_CONFIG);

        initializeResultViews();
    }

    /**
     *  初始化异常处理器
     * @return ExceptionHandler
     */
    private ExceptionHandler initializeExceptionHandler() {
        String clazz = ConfigUtils.getExceptionHandlerClass();
        try {
            Object meh = Class.forName(clazz).newInstance();
            if(meh instanceof ExceptionHandler){
                return (ExceptionHandler) meh;
            }
        } catch (InstantiationException |IllegalAccessException |ClassNotFoundException e) {
            log.error(e);
        }
        throw new ClassCastException(String.format("%s can not cast to ExceptionHandler",clazz));
    }

    /**
     * 初始化result view
     */
    private void initializeResultViews() {
        resultViewMap = new HashMap<>();
        resultViewMap.put("json",new JsonView());
        resultViewMap.put("_requestContext_",new RequestContextView());
    }


    /**
     * 获取resultView
     * @param extension 请求类型
     * @return ResultView
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private ResultView getResultView(String extension) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ResultView view = resultViewMap.get(extension);
        if(view == null){
            view = (ResultView) Class.forName(ConfigUtils.getDefaultResultView()).newInstance();
        }
        return view;
    }

    /**
     * 初始化参数
     * @param request   请求
     * @return Parameter
     * @throws UnsupportedEncodingException
     */
    public Parameter initializeParameter(HttpServletRequest request,HttpServletResponse response) throws UnsupportedEncodingException {
        Parameter parameter = new Parameter();
        parameter.setMethod(request.getMethod().toUpperCase());

        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(ConfigUtils.getMaxFileSize());
            upload.setSizeMax(ConfigUtils.getMaxSize());
            try {
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
                        parameter.getParamString().put(entry.getKey(), join(entry.getValue(), ","));
                    }
                }
            } catch (FileUploadException e) {
                throw new MangoException(e.getCause());
            }
        } else {
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
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
        parameter.setResponse(response);
        return parameter;
    }

    /**
     * 初始化处理适配器
     * @return  handlerAdapter
     */
    private HandlerAdapter initializeHandlerAdapter(){
        final Object adapter = new SimpleHandlerAdapter();
        return (HandlerAdapter) Proxy.newProxyInstance(getClass().getClassLoader(), HandlerAdapter.class.getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(adapter,args);
            }
        });
    }




    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Parameter parameter = initializeParameter(request,response);
        doDispatcher(parameter,response);
    }

    public void doDispatcher(Parameter parameter,HttpServletResponse response){
        if(parameter.getMethod().equals("OPTIONS")){
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setHeader("Access-Control-Request-Method","GET,POST,DELETE,PUT,OPTIONS");
            response.setHeader("Access-Control-Request-Method","X-PINGOTHER");
            return;
        }
        try {
            Object data = ha.handle(parameter);
            ResultView view = null;
            if(data instanceof ResultView){
                view = (ResultView) data;
            }else{
                view = getResultView(parameter.getExtension());
            }
            doRepresent(view,parameter,data);
        } catch (Exception e) {
            exceptionHandler.process(parameter,e);
        }
    }

    protected void doRepresent(ResultView view,Parameter parameter,Object data){
        if("enable".equals(ConfigUtils.getSafeHttp())){
            parameter.getResponse().setHeader("X-Frame-Options", "SAMEORIGIN");
            //parameter.getResponse().setHeader("Content-Security-Policy", "");
        }
        view.doRepresent(parameter,data);
    }

}
