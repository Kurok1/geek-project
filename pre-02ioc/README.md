## 通过 JNDI 获 取 JDBC DataSource，能够获取到正常的 java.sql.Connection 对象即可

#### 1. [Context.xml](./src/main/webapp/META-INF/context.xml)
这里定义了两个`DataSource`
```xml
    <!--使用apache dbcp  默认-->
    <Resource
            auth="Container"
            driverClassName="com.mysql.cj.jdbc.Driver"
            maxIdle="30"
            maxTotal="50"
            maxWaitMillis="-1"
            name="jdbc/dbcp"
            username="root"
            password="123qwertyA"
            type="javax.sql.DataSource"
            url="jdbc:mysql://localhost:3306/test_jdbc?serverTimezone=GMT%2B8"/>

    <!--使用Hikari数据源-->
    <Resource
            auth="Container"
            driverClassName="com.mysql.cj.jdbc.Driver"
            maximumPoolSize="50"
            name="jdbc/hikari"
            username="root"
            password="123qwertyA"
            type="javax.sql.DataSource"
            factory="com.zaxxer.hikari.HikariJNDIFactory"
            jdbcUrl="jdbc:mysql://localhost:3306/test_jdbc?serverTimezone=GMT%2B8"/>
```



#### 2. [ConnectionProvider](./src/main/java/indi/kurok1/ioc/ConnectionProvider.java)

定义多数据源管理
```java
public class ConnectionProvider {

    private static Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public static void registerDataSource(String dataSourceName, DataSource dataSource);

    public static Connection getConnection(String dataSourceName) throws SQLException;
}    
```

#### 3.数据源注册
利用`ServletContextListener`注册，实现代码 [DataSourceContextListener](./src/main/java/indi/kurok1/ioc/DataSourceContextListener.java)
```java
public class DataSourceContextListener implements ServletContextListener {
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
}
```

#### 4. servlet实现
[DatabaseServlet](./src/main/java/indi/kurok1/ioc/DatabaseServlet.java)

利用HTTP请求头，指定数据源
```java
public class DatabaseServlet extends HttpServlet {
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //http请求头中指定数据源,没有指定使用默认的
        String dataSourceName = req.getHeader(DATA_SOURCE_HEADER);
        if (dataSourceName == null)
            dataSourceName = DEFAULT_DATA_SOURCE_NAME;
        Connection connection = ConnectionProvider.getConnection(dataSourceName);
        //todo ...
    }
}
```
