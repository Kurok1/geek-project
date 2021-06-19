package indi.kurok1.ioc;

import javax.naming.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

/**
 * 当容器启动时，获取数据源
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.06.19
 */
public class DataSourceContextListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * <p>All ServletContextListeners are notified of context
     * initialization before any filters or servlets in the web
     * application are initialized.
     *
     * @param sce the ServletContextEvent containing the ServletContext
     *            that is being initialized
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Context context = null;
        try {
            context = new InitialContext();
            Name envName = new CompositeName("java:comp/env");
            Context envContext = (Context) context.lookup(envName);
            Name dbcpJdbcName=  new CompositeName("jdbc/dbcp");
            Name hikariJdbcName=  new CompositeName("jdbc/hikari");
            DataSource dbcpDataSource = (DataSource) envContext.lookup(dbcpJdbcName);
            DataSource hikariDataSource = (DataSource) envContext.lookup(hikariJdbcName);
            ConnectionProvider.registerDataSource("jdbc/dbcp", dbcpDataSource);
            ConnectionProvider.registerDataSource("jdbc/hikari", hikariDataSource);
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    /**
     * Receives notification that the ServletContext is about to be
     * shut down.
     *
     * <p>All servlets and filters will have been destroyed before any
     * ServletContextListeners are notified of context
     * destruction.
     *
     * @param sce the ServletContextEvent containing the ServletContext
     *            that is being destroyed
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
