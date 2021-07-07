package indi.kurok1.filter;

import indi.kurok1.servlet.support.GlobalHttpServletResponseHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 将当前请求的servlet response注册到holder中
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.07
 * @see indi.kurok1.servlet.support.GlobalHttpServletResponseHolder
 */
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
