package org.mangoframework.core;

import org.apache.log4j.Logger;
import org.mangoframework.core.dispatcher.ControllerMapping;
import org.mangoframework.core.dispatcher.Parameter;
import org.mangoframework.core.dispatcher.ServiceHandler;
import org.mangoframework.core.utils.ConfigUtils;
import org.mangoframework.core.view.ResultView;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

        ControllerMapping.init(ConfigUtils.getControllerClassNames());

        ControllerMapping.initPackages(ConfigUtils.getControllerPackage());

        sh = ServiceHandler.initialize();
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Parameter parameter = sh.initializeParameter(request, response);
        if (parameter.getMethod().equals("OPTIONS")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Request-Method", "GET,POST,DELETE,PUT,OPTIONS");
            response.setHeader("Access-Control-Request-Method", "X-PINGOTHER");
            return;
        }
        if("".equals(parameter.getPath())){
            String defaultController = ConfigUtils.getDefaultController();
            if(defaultController!=null){
                parameter.setPath(defaultController);
            }
        }
        //sh.doDispatcher(parameter,response);
        try {
            ResultView view = sh.handleRequest(parameter);
            if (view != null) {
                doRepresent(view, parameter);
            }else{
                log.info("no result view to be returned "+parameter.getRequestURL());
                super.service(request, response);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            sh.handleException(parameter, e);
        }
    }

    public void doRepresent(ResultView view, Parameter parameter) throws IOException, ServletException {
        if ("enable".equals(ConfigUtils.getSafeHttp())) {
            parameter.getResponse().setHeader("X-Frame-Options", "SAMEORIGIN");
            //parameter.getResponse().setHeader("Content-Security-Policy", "");
        }
        view.doRepresent(parameter);
    }


}
