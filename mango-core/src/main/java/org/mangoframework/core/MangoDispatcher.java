package org.mangoframework.core;

import org.apache.log4j.Logger;
import org.mangoframework.core.dispatcher.ControllerMapping;
import org.mangoframework.core.dispatcher.Parameter;
import org.mangoframework.core.dispatcher.ServiceHandler;
import org.mangoframework.core.utils.ConfigUtils;
import org.mangoframework.core.utils.ResultviewUtils;
import org.mangoframework.core.view.ResultView;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author zhoujingjie
 * @date 2016/4/22.
 */
public class MangoDispatcher extends HttpServlet {

    private static Logger log = Logger.getLogger(MangoDispatcher.class);

    private static String MANGO_CONFIG = "mango.properties";

    private ServiceHandler sh;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ConfigUtils.init(MANGO_CONFIG);

        //ControllerMapping.init(ConfigUtils.getControllerClassNames());

        ControllerMapping.initPackages(ConfigUtils.getControllerPackage());

        sh = ServiceHandler.initialize();
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Parameter parameter = null;
        try {
            parameter = sh.initializeParameter(request, response);
            if (parameter.getMethod().equals("OPTIONS")) {
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "X-PINGOTHER");
                return;
            }
            ResultView view = sh.handleRequest(parameter);
            if (view != null) {
                doRepresent(view, parameter);
            }else{
                if("".equals(parameter.getPath())){
                    String welcomeFile = ConfigUtils.getWelcomeFile();
                    if(welcomeFile!=null){
                        request.getRequestDispatcher(welcomeFile).forward(request,response);
                        return;
                    }
                }
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                log.info("no result view ; path:"+parameter.getPath());
            }
        }catch (Exception e) {
            try {
                handleException(e,parameter);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    protected void handleException(Throwable e,Parameter parameter) throws Exception {
        if(e instanceof InvocationTargetException){
            handleException(((InvocationTargetException) e).getTargetException(),parameter);
        }else if(e instanceof UndeclaredThrowableException){
            handleException(((UndeclaredThrowableException) e).getUndeclaredThrowable(),parameter);
        }else if(e.getCause()!=null){
            handleException(e.getCause(),parameter);
        }else{
            ResultviewUtils.getResultView(parameter.getExtension()).handleException(parameter,e);
        }
    }

    public void doRepresent(ResultView view, Parameter parameter) throws Exception{
        if ("enable".equals(ConfigUtils.getSafeHttp())) {
            parameter.getResponse().setHeader("X-Frame-Options", "SAMEORIGIN");
            //parameter.getResponse().setHeader("Content-Security-Policy", "");
        }
        view.doRepresent(parameter);
    }


}
