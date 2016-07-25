package org.mangoframework.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mangoframework.core.dispatcher.MangoFilter;
import org.mangoframework.core.view.ResultView;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class ConfigUtils {
    private static Logger log = Logger.getLogger(ConfigUtils.class);

    private static Properties properties;


    static {
        properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mango.default.properties"));
            //properties.setProperty("mango.view.json","org.mangoframework.core.view.JsonView");
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static void init(String file){
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(file));
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static String getControllerClassNames(){
        return properties.getProperty("mango.controller.class");
    }

    public static String getControllerPackage(){
        return properties.getProperty("mango.controller.package");
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
                if(!"".equals(arg.trim())) {
                    String[] args = arg.split(",");
                    viewClass = viewClass.substring(0, sIndex);
                    Class<?>[] classes = new Class[args.length];
                    for (int i = 0; i < classes.length; i++) {
                        classes[i] = String.class;
                    }
                    Constructor<?> constructor = Class.forName(viewClass).getConstructor(classes);
                    return (ResultView) constructor.newInstance(args);
                }else{
                    viewClass = viewClass.replace("(","").replace(")","");
                }
            }
            return (ResultView) Class.forName(viewClass).newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | ClassNotFoundException |InstantiationException |IllegalAccessException e) {
            log.error(e.getMessage(),e);
            return null;
        }
    }

    public static String getWelcomeFile() {
        return properties.getProperty("mango.welcome");
    }

    public static String getControllerPrefix(){
        return properties.getProperty("mango.controller.prefix");
    }
    public static String getControllerPrefix(String name){
        return properties.getProperty("mango.controller.prefix."+name);
    }

    public static String getDefaultExtension() {
        return properties.getProperty("mango.controller.defaultextension");
    }

    public static String getErrorPage(){
        return properties.getProperty("mango.errorpage");
    }

    public static Map<MangoFilter,String> getFilters(){
        Map<MangoFilter,String> viewsMap = new HashMap<>();
        String prefix = "mango.filter.";
        for(Map.Entry<Object,Object> entry:properties.entrySet()){
            String key = (String)entry.getKey();
            if(key.startsWith(prefix)){
                String value =(String)entry.getValue();
                String realKey  =key.substring(prefix.length());
                try {
                    Object instance = Class.forName(realKey).newInstance();
                    if(instance instanceof MangoFilter) {
                        viewsMap.put((MangoFilter) instance, value);
                    }
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    log.error(e.getMessage(),e);
                }
            }
        }
        return viewsMap;
    }

    public static boolean getRequestEscape(){
        return Boolean.parseBoolean(properties.getProperty("mango.escape","true"));
    }
}
