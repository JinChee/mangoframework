package org.mangoframework.core.test;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.lf5.util.ResourceUtils;
import org.junit.Test;
import org.mangoframework.core.dispatcher.ControllerMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class TestCore {

    @Test
    public void testName() {
        System.out.println(TestCore.class.getSimpleName());
    }

    @Test
    public void testPath(){
        ControllerMapping.initPackages("org.mangoframework.core.test");
    }

    @Test
    public void test3() throws ClassNotFoundException {
         Class<?> clazz = Class.forName(TestCore.class.getName());
        System.out.println(Modifier.isStatic(clazz.getModifiers()));
        System.out.println(Modifier.isAbstract(clazz.getModifiers()));
        System.out.println(Modifier.isFinal(clazz.getModifiers()));
        System.out.println(Modifier.isNative(clazz.getModifiers()));
        System.out.println(Modifier.isStrict(clazz.getModifiers()));
        System.out.println(Modifier.isTransient(clazz.getModifiers()));
        System.out.println(Modifier.isInterface(clazz.getModifiers()));
        System.out.println(Modifier.isPrivate(clazz.getModifiers()));
        System.out.println(Modifier.isPublic(clazz.getModifiers()));
        System.out.println(Modifier.isProtected(clazz.getModifiers()));
        System.out.println(Modifier.isVolatile(clazz.getModifiers()));
        System.out.println(Modifier.isSynchronized(clazz.getModifiers()));

    }




}
