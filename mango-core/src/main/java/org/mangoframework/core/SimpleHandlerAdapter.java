package org.mangoframework.core;

import org.mangoframework.core.annotation.RequestMapping;
import org.mangoframework.core.exception.ControllerNotFoundException;
import org.mangoframework.core.exception.UnsupportedMethodException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class SimpleHandlerAdapter implements HandlerAdapter {


    @Override
    public Object handle(Parameter parameter) throws MangoException {
        String path = parameter.getPath();
        String method = parameter.getMethod();
        Controller controller = ControllerMapping.get(path);
        if(controller == null)
            throw new ControllerNotFoundException(String.format("%s not found ",path));
        RequestMapping rm = controller.getRequestMapping();
        if(method.equals("GET") && rm.get()
                ||(method.equals("POST") && rm.post())
                || (method.equals("DELETE") && rm.delete())
                || (method.equals("PUT") && rm.put())
                ){
            Object instance = null;
            if(rm.singleton()){
                instance = controller.getInstance();
            }else{
                try {
                    instance = controller.getInstance().getClass().newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new MangoException("InstantiationException or IllegalAccessException",e);
                }
            }
            Method requestMethod = controller.getMethod();
            Class<?>[] argTypes = requestMethod.getParameterTypes();
            try {
                Object data = null;
                if(argTypes.length == 0){
                    data = requestMethod.invoke(instance,null);
                }else{
                    Object[] args = new Object[argTypes.length];
                    for (int i = 0; i < argTypes.length; i++) {
                        if(Parameter.class.isAssignableFrom(argTypes[i])){
                            args[i] = parameter;
                        }else{
                            args[i] = null;
                        }
                    }
                    data = requestMethod.invoke(instance,args);
                }
                 return data;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MangoException("IllegalAccessException or InvocationTargetException ",e);
            }
        }else{
            throw new UnsupportedMethodException(String.format("%s not support %s",path,method));
        }
    }
}
