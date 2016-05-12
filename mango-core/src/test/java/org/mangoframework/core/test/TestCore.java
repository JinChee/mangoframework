package org.mangoframework.core.test;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.lf5.util.ResourceUtils;
import org.junit.Test;
import org.mangoframework.core.dispatcher.ControllerMapping;

import java.io.File;
import java.io.IOException;
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





}
