# mangoframework
简单的javaweb框架。


# 项目迁移至：http://git.oschina.net/zhoujingjie/mangoframework

## 使用说明
需要classpath下创建mongo.properties

web.xml中配置
````
    <servlet>
       <servlet-name>mangoservlet</servlet-name>
        <servlet-class>org.mangoframework.core.MangoDispatcher</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>mangoservlet</servlet-name>
        <!--<url-pattern>/test/*</url-pattern>-->
        <url-pattern>/</url-pattern>
    </servlet-mapping>
```` 
## mongo.properties配置说明

* 基本配置
mango.controller.package ：controller所属包名，如com.xxx.xx.controller


* 文件大小
mango.filesize.max ：上传单个文件最大值  默认1024000（1M）
mango.size.max ：多个文件总共最大值 默认：40960000（默认4M）

* 其他 
mango.errorpage ：全局错误页面地址
mango.safe.http : 是否启用安全http。选项：disabled/yes；默认disabled
mango.escape：是否过滤xss。选项true/false

* 过滤器
mango.filter.com.xxx.xx.filter.AuthorityFilter=true  需要实现 org.mangoframework.core.dispatcher.MangoFilter


* 处理器
mongo.view.xxx=完整包名.类名。如：
 * json后缀处理器 mango.view.json=cn.com.xxx.xx.view.JsonView
 * html后缀处理器 mango.view.html=org.mangoframework.core.view.JspView(/WEB-INF/web,.jsp) 
 * 无后缀处理器 mango.view.default=org.mangoframework.core.view.JspView(/WEB-INF/jsp,.jsp)
