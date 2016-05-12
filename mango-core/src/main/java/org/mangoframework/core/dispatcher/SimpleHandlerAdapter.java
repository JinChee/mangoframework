package org.mangoframework.core.dispatcher;

import org.mangoframework.core.exception.MangoException;
import org.mangoframework.core.annotation.RequestMapping;
import org.mangoframework.core.exception.ControllerNotFoundException;
import org.mangoframework.core.exception.UnsupportedMethodException;
import org.mangoframework.core.utils.ConfigUtils;
import org.mangoframework.core.view.JsonView;
import org.mangoframework.core.view.ResultView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class SimpleHandlerAdapter implements HandlerAdapter {

    private Map<String,ResultView> resultViewMap;

    public SimpleHandlerAdapter() {
        initializeResultViews();
    }

    /**
     * 初始化result view
     */
    private void initializeResultViews() {
        resultViewMap = ConfigUtils.getViewsMap();
    }


    @Override
    public ResultView handle(Parameter parameter) throws MangoException {
        String path = parameter.getPath();
        String method = parameter.getMethod();
        Controller controller = ControllerMapping.get(path);
        if(controller == null){
            //throw new ControllerNotFoundException(String.format("%s not found ",path));
            return null;
        }
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
                    data = requestMethod.invoke(instance);
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
                if(data instanceof ResultView){
                    return (ResultView) data;
                }
                ResultView view = getResultView(parameter.getExtension());
                view.setData(data);
                view.setTemplate(rm.template());
                return view;
            } catch (IllegalAccessException |ClassNotFoundException | InstantiationException e) {
                throw new MangoException("IllegalAccessException or InvocationTargetException ",e);
            }catch (InvocationTargetException e){
                throw new MangoException("IllegalAccessException or InvocationTargetException ",e.getTargetException());
            }
        }else{
            throw new UnsupportedMethodException(String.format("%s not support %s",path,method));
        }
    }


    /**
     * 获取resultView
     * @param extension 请求类型
     * @return ResultView
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private ResultView getResultView(String extension) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ResultView view = resultViewMap.get(extension);
        if(view == null){
            view = ConfigUtils.getResultView(ConfigUtils.getDefaultResultView());
        }
        return view;
    }
}
