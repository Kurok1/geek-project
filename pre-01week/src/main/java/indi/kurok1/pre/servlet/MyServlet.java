package indi.kurok1.pre.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义Servlet
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.06.12
 */
public class MyServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        RequestDispatcher dispatcher = req.getRequestDispatcher("/index.jsp");
        //添加传参
        req.setAttribute("key", "value");
        //服务端转发
        //  dispatcher.forward(req, resp);
        //客户端重定向
        dispatcher.include(req, resp);
        resp.getWriter().println("after jsp!!!");
    }
}
