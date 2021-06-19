package indi.kurok1.ioc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库连接提供,多数据源支持
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.06.19
 */
public class ConnectionProvider {

    private static Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public static void registerDataSource(String dataSourceName, DataSource dataSource) {
        dataSources.put(dataSourceName, dataSource);
    }

    public static Connection getConnection(String dataSourceName) throws SQLException {
        DataSource dataSource = dataSources.get(dataSourceName);
        if (dataSource == null)
            throw new IllegalStateException("no dataSource found!!!");


        if (connectionThreadLocal.get() == null) {
            connectionThreadLocal.set(dataSource.getConnection());
        }

        Connection connection = connectionThreadLocal.get();
        if (connection.isClosed()) {
            //已关闭的重新获取
            connection = dataSource.getConnection();
            connectionThreadLocal.set(connection);
        }

        return connection;
    }
}
