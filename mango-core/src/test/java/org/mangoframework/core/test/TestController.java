package org.mangoframework.core.test;

import org.mangoframework.core.annotation.RequestMapping;

/**
 * @author zhoujingjie
 * @date 2016-05-12
 */
@RequestMapping("/test")
public class TestController {

    @RequestMapping("")
    public void test1(){}

    @RequestMapping("test2")
    public void test2(){}

    @RequestMapping("/test3")
    public void test3(){}

    @RequestMapping("test4/")
    public void test4(){}

    @RequestMapping("/test5/")
    public void test5(){}


}
