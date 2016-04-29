package org.mangoframework.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mangoframework.core.view.ResultView;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class ConfigUtils {
    private static Logger log = Logger.getLogger(ConfigUtils.class);

    private static Properties properties;

    //单文件最大值 2M
    private static long maxFileSize = 2 << 20;
    //文件上传最大值 20M
    private static long maxSize =(2 << 20)*10;

    public static void init(String file){
        properties = new Properties();
        try {
            properties.setProperty("mango.exception.handler","org.mangoframework.core.exception.SimpleExceptionHandler");
            properties.setProperty("mango.view.default","org.mangoframework.core.view.JsonView");
            properties.setProperty("mango.safe.http","disabled");
            properties.setProperty("mango.filesize.max",String.valueOf(maxFileSize));
            properties.setProperty("mango.size.max",String.valueOf(maxSize));
            properties.setProperty("mango.view.json","org.mangoframework.core.view.JsonView");
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(file));
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static String getControllerClassNames(){
        return properties.getProperty("mango.controller.class");
    }

    public static String getExceptionHandlerClass(){
        String clazz = properties.getProperty("mango.exception.handler");
        if(StringUtils.isEmpty(clazz)){
            clazz = "";
        }
        return clazz;
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

    public static Map<String,ResultView> getViewsMap(){
        Map<String,ResultView> viewsMap = new HashMap<>();
        String prefix = "mango.view.";
        for(Map.Entry<Object,Object> entry:properties.entrySet()){
            String key = (String)entry.getKey();
            if(key.startsWith(prefix)){
                String value =(String)entry.getValue();
                viewsMap.put(key.substring(prefix.length()).toUpperCase(),getResultView(value));
            }
        }
        return viewsMap;
    }
    public static ResultView getResultView(String viewClass){
        try {
            int sIndex = viewClass.indexOf("(");
            int eIndex = viewClass.indexOf(")");
            if (sIndex != -1 && eIndex != -1) {
                String arg = viewClass.substring(sIndex + 1, eIndex);
                String[] args = arg.split(",");
                viewClass = viewClass.substring(0, sIndex);
                Class<?>[] classes = new Class[args.length];
                for (int i = 0; i < classes.length; i++) {
                    classes[i] = String.class;
                }
                Constructor<?> constructor = Class.forName(viewClass).getConstructor(classes);
                return (ResultView)constructor.newInstance(args);
            }
            return (ResultView) Class.forName(viewClass).newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | ClassNotFoundException |InstantiationException |IllegalAccessException e) {
            log.error(e.getMessage(),e);
            return null;
        }
    }

    public static String getDefaultController() {
        return properties.getProperty("mango.controller.default");
    }
}
