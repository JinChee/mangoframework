package org.mangoframework.core.utils;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class PropertiesUtils {
    private static Logger log = Logger.getLogger(PropertiesUtils.class);

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

}
