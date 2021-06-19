package indi.kurok1.ioc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.06.19
 */
public class DatabaseServlet extends HttpServlet {


    private static final String DEFAULT_DATA_SOURCE_NAME = "jdbc/hikari";
    private static final String DATA_SOURCE_HEADER = "X-DATASOURCE";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //http请求头中指定数据源,没有指定使用默认的
            String dataSourceName = req.getHeader(DATA_SOURCE_HEADER);
            if (dataSourceName == null)
                dataSourceName = DEFAULT_DATA_SOURCE_NAME;
            Connection connection = ConnectionProvider.getConnection(dataSourceName);
            if (connection != null) {
                resp.getWriter().println("get Connection for database success!");
            } else resp.getWriter().println("get Connection for database fail!");
        } catch (SQLException throwables) {
            resp.getWriter().println("get Connection for database fail!");
        }
    }



}
