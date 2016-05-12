package org.mangoframework.core.test;

import org.junit.Test;
import org.mangoframework.core.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author: zhoujingjie
 * @Date: 16/5/12
 */
public class TestAnn {


    public void testb(String a,@RequestParam("bbb")String b){

    }
    @Test
    public void test(){
        for(Method method: TestAnn.class.getMethods()){
            Annotation[][] anns =method.getParameterAnnotations();
            System.out.println();
        }
    }
}
