package org.mangoframework.core;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public interface HandlerAdapter {

    Object handle(Parameter parameter) throws MangoException;

}
