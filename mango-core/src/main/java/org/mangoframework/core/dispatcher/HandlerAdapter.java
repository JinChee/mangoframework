package org.mangoframework.core.dispatcher;

import org.mangoframework.core.view.ResultView;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public interface HandlerAdapter {

    ResultView handle(Parameter parameter) throws Exception;

}
