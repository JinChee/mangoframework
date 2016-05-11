package org.mangoframework.core.dispatcher;

import org.apache.log4j.Logger;
import org.mangoframework.core.annotation.PathInject;
import org.mangoframework.core.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * controller 映射
 *
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class ControllerMapping {

    private static Logger log = Logger.getLogger(ControllerMapping.class);
    private static Map<String, Controller> mapping = new HashMap<>();

    private ControllerMapping() {
    }

    /**
     * 初始化映射
     *
     * @param classNames 类名
     * @return 映射类
     */
    public static ControllerMapping init(String classNames) {
        String[] names = classNames.split(",");
        for (String className : names) {
            scannerControllerPaths(className);
        }
        return new ControllerMapping();
    }



    public static Controller get(String path) {
        return mapping.get(path);
    }

    /**
     * 扫描controller 和 方法
     *
     * @param className controller类名
     */
    private static void scannerControllerPaths(String className) {
        try {
            Object pathBean = Class.forName(className).newInstance();
            for (Field field : pathBean.getClass().getFields()) {
                PathInject pathInject = field.getAnnotation(PathInject.class);
                if (pathInject != null && pathInject.value().length() > 0) {
                    try {
                        scannerURIAndMethods(pathInject.value(), (String) field.get(null));
                    } catch (IllegalAccessException e) {
                        log.error(e);
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            log.error(e);
        }
    }

    /**
     * 扫描方法与地址
     *
     * @param controllerClass controller 类
     * @param pathValue       字段值
     */
    private static void scannerURIAndMethods(String controllerClass, String pathValue) {
        try {
            Object controller = Class.forName(controllerClass).newInstance();
            for (Method method : controller.getClass().getMethods()) {
                RequestMapping rm = method.getAnnotation(RequestMapping.class);
                if (rm == null)
                    continue;
                if (pathValue.length() == 0) {
                    pathValue = "/";
                }
                if (pathValue.charAt(pathValue.length() - 1) == '/') {
                    pathValue = pathValue.substring(1);
                }
                String[] values = rm.value();
                for (String value : values) {
                    if (value.length() > 0 && value.charAt(0) != '/') {
                        value = "/" + value;
                    }
                    String uri = pathValue.concat(value);

                    mapping.put(uri, new Controller(controller, method, rm));
                    log.debug("scannerURIAndMethods uri:" + uri);
                }
            }
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            log.error(e);
        }
    }

}
