package org.mangoframework.core.dispatcher;

import org.apache.log4j.Logger;
import org.mangoframework.core.annotation.*;
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
    private static Map<String, Map<String,Controller>> mapping = new HashMap<>();

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


    public static Controller get(String path,String method) {
        Map<String,Controller> controllerMap = mapping.get(path);
        if(controllerMap == null){
            for(String key:mapping.keySet()){
                if(key.contains("{") && key.contains("}")){
                    //先看是否有对方方法
                    controllerMap = mapping.get(key);
                    Controller controller = controllerMap.get(method);
                    if(controller==null) {
                        continue;
                    }
                    String[] keys = key.split("/");
                    String[] paths = path.split("/");
                    if(keys.length != paths.length){
                        continue;
                    }
                    boolean isMatching = true;
                    Map<String,String> pathValueMap = new HashMap<>();
                    for(int i=0;i<keys.length;i++){
                        if(!keys[i].equals(paths[i])){
                            if(keys[i].contains("{") && keys[i].contains("}")){
                                String name = keys[i].substring(1,keys[i].length()-1);
                                pathValueMap.put(name,paths[i]);
                            }else{
                                isMatching = false;
                                break;
                            }
                        }
                    }
                    if(isMatching){
                        controller.setPathMap(pathValueMap);
                        return controller;
                    }
                }
            }
            return null;
        }else{
            return controllerMap.get(method);
        }
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
                String[] values = null;
                RequestMethod[] methods = null;
                org.mangoframework.core.dispatcher.RequestMapping requestMapping = new org.mangoframework.core.dispatcher.RequestMapping();
                if (rm == null) {
                    Get get = method.getAnnotation(Get.class);
                    Post post = method.getAnnotation(Post.class);
                    Delete delete = method.getAnnotation(Delete.class);
                    Put put = method.getAnnotation(Put.class);

                    if(get!=null){
                        values = get.value();
                        methods = new RequestMethod[]{RequestMethod.GET};
                        requestMapping.setSingleton(get.singleton());
                        requestMapping.setTemplate(get.template());
                    }else if(post !=null){
                        values = post.value();
                        methods = new RequestMethod[]{RequestMethod.POST};
                        requestMapping.setSingleton(post.singleton());
                        requestMapping.setTemplate(post.template());
                    }
                    else if(put !=null){
                        values = put.value();
                        methods = new RequestMethod[]{RequestMethod.PUT};
                        requestMapping.setSingleton(put.singleton());
                        requestMapping.setTemplate(put.template());
                    }
                    else if(delete !=null){
                        values = delete.value();
                        methods = new RequestMethod[]{RequestMethod.DELETE};
                        requestMapping.setSingleton(delete.singleton());
                        requestMapping.setTemplate(delete.template());
                    }

                    if(values == null) {
                        continue;
                    }
                }else {
                    values = rm.value();
                    methods = rm.method();
                    requestMapping.setTemplate(rm.template());
                    requestMapping.setSingleton(rm.singleton());
                }


                if(values.length == 0){
                    putController(pathValue, new Controller(controller, method, requestMapping),methods);
                }else {
                    for (String value : values) {
                        if (value.length() > 0 && value.charAt(0) != '/') {
                            value = "/" + value;
                        }
                        String uri = pathValue.concat(value);
                        if (uri.length() > 0 && uri.charAt(uri.length() - 1) == '/') {
                            uri = uri.substring(0, uri.length() - 1);
                        }
                        putController(uri, new Controller(controller, method, requestMapping),methods);
                    }
                }
            }
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            log.error(e);
        }
    }

    private static void putController(String path,Controller controller,RequestMethod[] methods){
        Map<String,Controller> controllerMap = mapping.get(path);
        if(controllerMap == null){
            controllerMap = new HashMap<>();
            mapping.put(path,controllerMap);
        }

        if(methods.length==0){
            controllerMap.put("GET",controller);
            log.debug("scanned uri: "+path+" method:GET");
        }else{
            for(RequestMethod method:methods){
                if(RequestMethod.GET == method){
                    controllerMap.put("GET",controller);
                    log.debug("scanned uri: "+path+" method:GET");
                }else if(RequestMethod.POST == method){
                    controllerMap.put("POST",controller);
                    log.debug("scanned uri: "+path+" method:POST");
                }else if(RequestMethod.PUT == method){
                    controllerMap.put("PUT",controller);
                    log.debug("scanned uri: "+path+" method:PUT");
                }else if(RequestMethod.DELETE == method){
                    controllerMap.put("DELETE",controller);
                    log.debug("scanned uri: "+path+" method:DELETE");
                }
            }
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
        if(childFiles==null || childFiles.length==0)
            return myClassName;
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));
                }
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("classes") + 8, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace(File.separator, ".");
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
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf(File.separator));
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
                            entryName = entryName.replace(File.separator, ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    } else {
                        int index = entryName.lastIndexOf(File.separator);
                        String myPackagePath;
                        if (index != -1) {
                            myPackagePath = entryName.substring(0, index);
                        } else {
                            myPackagePath = entryName;
                        }
                        if (myPackagePath.equals(packagePath)) {
                            entryName = entryName.replace(File.separator, ".").substring(0, entryName.lastIndexOf("."));
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
