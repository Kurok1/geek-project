## 参考`com.salesmanager.shop.tags.CommonResponseHeadersTag` 实现一个自定义的 Tag，将 Hard Code 的 Header 名值对，变为属性配置的方式

### 实现`javax.servlet.jsp.tagext.SimpleTagSupport`
[CommonResponseHeadersTag](./src/main/java/indi/kurok1/tags/support/CommonResponseHeadersTag.java)
```java
public class CommonResponseHeadersTag extends SimpleTagSupport {

    private String header;
    private String value;
    //todo getter,setter

    @Override
    public void doTag() throws JspException, IOException {
        HttpServletResponse response = GlobalHttpServletResponseHolder.getHttpServletResponse();
        if (response != null && isValid() )
            response.setHeader(header, value);

        super.doTag();
    }

    /**
     * 校验响应头是否合法
     * @return 是否合法
     */
    private boolean isValid();
}
```

### 全局`HttpServletResponse`容器
用于存储每个`request`对应的`response`，用`ThreadLocal`存储，线程之间互不干扰
```java
public class GlobalHttpServletResponseHolder {

    private static final ThreadLocal<HttpServletResponse> responseHolder = new ThreadLocal<>();

    public static void setHttpServletResponse(HttpServletResponse response) {
        responseHolder.set(response);
    }

    public static HttpServletResponse getHttpServletResponse() {
        return responseHolder.get();
    }

    public static void reset() {
        responseHolder.remove();
    }

}
```

### 注入`HttpServletResponse`
实现`Filter`，过滤请求的同时注入`HttpServletResponse`到`GlobalHttpServletResponseHolder`
```java
public class InjectServletResponseFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletResponse instanceof HttpServletResponse) {
            //注入全局响应
            System.out.println("inject global servlet response");
            GlobalHttpServletResponseHolder.setHttpServletResponse((HttpServletResponse) servletResponse);
        }


        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
```

### 编写tld文件
[customResponseHeader.tld](./src/main/webapp/WEB-INF/customResponseHeader.tld)
```xml
<taglib xmlns="http://java.sun.com/xml/ns/j2ee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0"
        xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd">
    <description><![CDATA["Shopizer tag libs"]]></description>
    <display-name>"ResponseHeaderTags"</display-name>
    <tlib-version>2.3</tlib-version>
    <short-name>rh</short-name>

    <tag>
        <name>ResponseHeaders</name>
        <tag-class>indi.kurok1.tags.support.CommonResponseHeadersTag</tag-class>
        <body-content>empty</body-content>
        <attribute>
            <name>header</name>
            <required>true</required>
            <type>java.lang.String</type>
        </attribute>

        <attribute>
            <name>value</name>
            <required>true</required>
            <type>java.lang.String</type>
        </attribute>
    </tag>
</taglib>
```

### jsp文件中调用
[index.jsp](./src/main/webapp/index.jsp)
```java
<%@ taglib prefix="ex" uri="/WEB-INF/customResponseHeader.tld"%>
<ex:ResponseHeaders header="Cache-Control" value="true" />
<ex:ResponseHeaders header="X-REQUEST-ID" value="123456" />
```