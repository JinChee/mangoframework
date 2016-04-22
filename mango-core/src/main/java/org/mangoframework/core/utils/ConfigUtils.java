package org.mangoframework.core.utils;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class ConfigUtils {
    private static Logger log = Logger.getLogger(ConfigUtils.class);

    private static Properties properties;

    public static void init(String file){
        properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(file));
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static String getControllerClassNames(){
        return properties.getProperty("mango.controller.class");
    }

    public static String getExceptionHandlerClass(){
        return properties.getProperty("mango.exception.handler");
    }

    public static String getDefaultResultView(){
        return properties.getProperty("mango.view.default");
    }
    public static String getSafeHttp(){
        return properties.getProperty("mango.safe.http");
    }

    public static long getMaxFileSize(){
        return Long.parseLong(properties.getProperty("mango.filesize.max"));
    }

    public static long getMaxSize(){
        return Long.parseLong(properties.getProperty("mango.size.max"));
    }

}
