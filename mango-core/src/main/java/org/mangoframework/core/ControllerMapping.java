package org.mangoframework.core;

import org.apache.log4j.Logger;
import org.mangoframework.core.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class ControllerMapping {

    private static Logger log  = Logger.getLogger(ControllerMapping.class);
    private static Map<String,Controller> mapping = new HashMap<>();

    private ControllerMapping(){}

    public static ControllerMapping init(String classNames){
        String[] names = classNames.split(",");
        for(String className:names){
            scannerControllerAndMethods(className);
        }
        return new ControllerMapping();
    }

    private static void scannerControllerAndMethods(String className) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Object pathBean = Class.forName(className).newInstance();
        for(Field field:pathBean.getClass().getFields()){
            PathInject pathInject = field.getAnnotation(PathInject.class);
            if(pathInject != null && pathInject.value().length()>0){
                scannerURIAndMethods(pathInject.value(), (String) field.get(null));
            }
        }
    }

    private static void scannerURIAndMethods(String controllerClass,String pathValue){
        try {
            Object controller = Class.forName(controllerClass).newInstance();
            for(Method method:controller.getClass().getMethods()){
                RequestMapping rm = method.getAnnotation(RequestMapping.class);
                if(rm == null)
                    continue;
                if(pathValue.charAt(pathValue.length()-1) != '/') {
                    pathValue = pathValue+"/";
                }
                String uri = pathValue.concat(rm.value());
                mapping.put(uri,new Controller(controller,method,rm));
            }
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
