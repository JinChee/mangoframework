package org.mangoframework.core.view;

import org.mangoframework.core.Parameter;

import java.util.Map;

/**
 * @author: zhoujingjie
 * @Date: 16/4/22
 */
public class RequestContextView implements ResultView {
    @Override
    public void doRepresent(Parameter parameter, Object data) {
        if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                parameter.getRequest().setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }
}
