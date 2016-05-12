package org.mangoframework.core.dispatcher;

import org.apache.log4j.Logger;
import org.mangoframework.core.annotation.PathInject;
import org.mangoframework.core.annotation.RequestMapping;
import org.mangoframework.core.utils.ConfigUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    public static void init(String classNames) {
        if(classNames==null)
            return;
        String[] names = classNames.split(",");
        for (String className : names) {
            scannerControllerPaths(className);
        }
    }

    public static void initPackages(String packages){
        String[] packageNames = packages.split(",");
        for(String packageName:packageNames){
            scannerControllersFromPackage(packageName);
        }
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
            if(pathValue.length()>0 && pathValue.charAt(pathValue.length()-1)=='/'){
                pathValue = pathValue.substring(0,pathValue.length()-1);
            }
            Object controller = Class.forName(controllerClass).newInstance();
            RequestMapping rm = controller.getClass().getAnnotation(RequestMapping.class);
            if(rm!=null){
                String value = rm.value()[0];
                if(value.charAt(0)!='/'){
                    value = "/"+value;
                }
                pathValue = pathValue + value;
            }
            if(pathValue.length()>0) {
                if (pathValue.charAt(0) != '/') {
                    pathValue = "/" + pathValue;
                }
                if (pathValue.charAt(pathValue.length() - 1) == '/') {
                    pathValue = pathValue.substring(1);
                }
            }
            for (Method method : controller.getClass().getMethods()) {
                rm = method.getAnnotation(RequestMapping.class);
                if (rm == null)
                    continue;
                String[] values = rm.value();
                if(values.length == 0){
                    mapping.put(pathValue, new Controller(controller, method, rm));
                    log.debug("scannerURIAndMethods uri:" + pathValue);
                }else {
                    for (String value : values) {
                        if (value.length() > 0 && value.charAt(0) != '/') {
                            value = "/" + value;
                        }
                        String uri = pathValue.concat(value);
                        if (uri.length() > 0 && uri.charAt(uri.length() - 1) == '/') {
                            uri = uri.substring(0, uri.length() - 1);
                        }
                        mapping.put(uri, new Controller(controller, method, rm));
                        log.debug("scannerURIAndMethods uri:" + uri);
                    }
                }
            }
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            log.error(e);
        }
    }

    /**
     * 扫描包
     * @param packageName
     */
    private static void scannerControllersFromPackage(String packageName){
        List<String> classNames = getClassName(packageName);
        String prefix ="";
        if("enable".equals(ConfigUtils.getControllerPrefix())){
            prefix = packageName.substring(packageName.lastIndexOf(".")+1);
            prefix = ConfigUtils.getControllerPrefix(prefix);
            if(prefix==null){
                prefix = "";
            }
        }
        for(String className:classNames){
            scannerURIAndMethods(className,prefix);
        }
    }


    /**
     * 获取某包下（包括该包的所有子包）所有类
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName) {
        return getClassName(packageName, true);
    }

    /**
     * 获取某包下所有类
     * @param packageName 包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, boolean childPackage) {
        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            if (type.equals("file")) {
                fileNames = getClassNameByFile(url.getPath(), null, childPackage);
            } else if (type.equals("jar")) {
                fileNames = getClassNameByJar(url.getPath(), childPackage);
            }
        } else {
            fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     * @param filePath 文件路径
     * @param className 类名集合
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath, List<String> className, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));
                }
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("classes") + 8, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("\\", ".");
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     * @param jarPath jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    if (childPackage) {
                        if (entryName.startsWith(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    } else {
                        int index = entryName.lastIndexOf("/");
                        String myPackagePath;
                        if (index != -1) {
                            myPackagePath = entryName.substring(0, index);
                        } else {
                            myPackagePath = entryName;
                        }
                        if (myPackagePath.equals(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myClassName;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     * @param urls URL集合
     * @param packagePath 包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith("classes/")) {
                    continue;
                }
                String jarPath = urlPath + "!/" + packagePath;
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }
}
