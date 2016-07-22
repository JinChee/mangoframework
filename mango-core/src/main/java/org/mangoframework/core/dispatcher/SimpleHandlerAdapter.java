package org.mangoframework.core.dispatcher;

import org.mangoframework.core.annotation.RequestParam;
import org.mangoframework.core.exception.MangoException;
import org.mangoframework.core.exception.UnauthorizedException;
import org.mangoframework.core.utils.ConfigUtils;
import org.mangoframework.core.utils.ResultviewUtils;
import org.mangoframework.core.view.HeadView;
import org.mangoframework.core.view.ResultView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;


/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class SimpleHandlerAdapter implements HandlerAdapter {

    //
    private Map<MangoFilter, String> filterMap;

    public SimpleHandlerAdapter() {
        filterMap = ConfigUtils.getFilters();
    }



    @Override
    public ResultView handle(Parameter parameter) throws Exception {
        if(parameter.getMethod().equals("HEAD")){
            return new HeadView();
        }
        Controller controller = ControllerMapping.get(parameter.getPath(),parameter.getMethod());
        if(controller == null){
            //throw new ControllerNotFoundException(String.format("%s not found ",path));
            return null;
        }
        RequestMapping rm = controller.getRequestMapping();
        Object instance = null;
        if(rm.isSingleton()){
            instance = controller.getInstance();
        }else{
            try {
                instance = controller.getInstance().getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new MangoException("InstantiationException or IllegalAccessException",e);
            }
        }
        Method requestMethod = controller.getMethod();
        doFilter(parameter,requestMethod);

        Class<?>[] argTypes = requestMethod.getParameterTypes();
        Annotation[][] paramAnns = requestMethod.getParameterAnnotations();
        Object data = null;
        if(argTypes.length == 0){
            data = requestMethod.invoke(instance);
        }else{
            Object[] args = new Object[argTypes.length];
            for (int i = 0; i < argTypes.length; i++) {
                if(Parameter.class.isAssignableFrom(argTypes[i])){
                    args[i] = parameter;
                }else if(String.class.isAssignableFrom(argTypes[i]) && paramAnns[i]!=null && paramAnns[i].length>0){
                    for(Annotation ann:paramAnns[i]){
                        if(ann.annotationType().equals(RequestParam.class)){
                            RequestParam rp = (RequestParam) ann;
                            args[i] = controller.getPathMap().get(rp.value());
                            break;
                        }
                    }
                }else{
                    args[i] = null;
                }
            }
            data = requestMethod.invoke(instance,args);
        }
        if(data == null){
            if(requestMethod.getReturnType().getName().equals("void")){
                return null;
            }
        }
        if(data instanceof ResultView){
            return (ResultView) data;
        }
        ResultView view = ResultviewUtils.getResultView(parameter.getExtension());
        view.setData(data);
        view.setTemplate(rm.getTemplate());
        return view;
    }


    private void doFilter(Parameter parameter,Method method) throws Exception {
        for (MangoFilter mf : filterMap.keySet()) {
            if (!mf.doFilter(parameter,method)) {
                throw new UnauthorizedException();
            }
        }
    }
}
