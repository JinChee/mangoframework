package org.mangoframework.core.utils;

import org.mangoframework.core.view.ResultView;

import java.util.Map;

/**
 * @author zhoujingjie
 * @date 2016-05-27
 */
public class ResultviewUtils {

    private static Map<String,ResultView> resultViewMap;
    static {
        resultViewMap = ConfigUtils.getViewsMap();
    }
    /**
     * 获取resultView
     * @param extension 请求类型
     * @return ResultView
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static ResultView getResultView(String extension) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ResultView view = resultViewMap.get(extension);
        if(view == null){
            view = ConfigUtils.getResultView(ConfigUtils.getDefaultResultView());
        }
        return view;
    }
}
