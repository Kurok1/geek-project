package indi.kurok1.servlet.support;

import javax.servlet.http.HttpServletResponse;

/**
 * Http Servlet Response全局响应
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.07
 */
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
