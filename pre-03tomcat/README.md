## 分析 Tomcat 容器调用 shutdown.sh 脚本最终如何关闭 Tomcat 服务器

### 1.执行`shutdown.sh`命令转发到`catalina.sh`
```shell
PRGDIR=`dirname "$PRG"`
EXECUTABLE=catalina.sh
exec "$PRGDIR"/"$EXECUTABLE" stop "$@"
```

### 2. 向`org.apache.catalina.startup.Bootstrap`发送`stop`命令
```shell
  eval "\"$_RUNJAVA\"" $LOGGING_MANAGER "$JAVA_OPTS" \
    -D$ENDORSED_PROP="\"$JAVA_ENDORSED_DIRS\"" \
    -classpath "\"$CLASSPATH\"" \
    -Dcatalina.base="\"$CATALINA_BASE\"" \
    -Dcatalina.home="\"$CATALINA_HOME\"" \
    -Djava.io.tmpdir="\"$CATALINA_TMPDIR\"" \
    org.apache.catalina.startup.Bootstrap "$@" stop
```

### 3. 初始化`org.apache.catalina.startup.Bootstrap`
实际上这里是另起一个新的进程,所以会重新执行一遍初始化的流程，包括
* 初始化类加载器
* `setParentClassLoader`
* 创建启动并保存实例`org.apache.catalina.startup.Catalina`

然后解析外部传参进来的`stop`命令
```java
else if (command.equals("stop")) {
    daemon.stopServer(args);
}
```
如果外部发送了`stop`命令，则会调用`org.apache.catalina.startup.Bootstrap.stopServer(java.lang.String[])`方法

### 4.执行`org.apache.catalina.startup.Bootstrap.stopServer(java.lang.String[])`
```java
    public void stopServer(String[] arguments) throws Exception {

        Object param[];
        Class<?> paramTypes[];
        if (arguments == null || arguments.length == 0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        Method method =
            catalinaDaemon.getClass().getMethod("stopServer", paramTypes);
        method.invoke(catalinaDaemon, param);
    }
```
可以看到，最终通过反射的形式调用`org.apache.catalina.startup.Catalina.stopServer(java.lang.String[])`

### 5.执行`org.apache.catalina.startup.Catalina.stopServer(java.lang.String[])`
这里就是关闭tomcat服务器的最核心的方法
```java
        Server s = getServer();
        if (s == null) {
            // Create and execute our Digester
            Digester digester = createStopDigester();
            File file = configFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                InputSource is =
                    new InputSource(file.toURI().toURL().toString());
                is.setByteStream(fis);
                digester.push(this);
                digester.parse(is);
            } catch (Exception e) {
                log.error("Catalina.stop: ", e);
                System.exit(1);
            }
        } else {
            // Server object already present. Must be running as a service
            try {
                s.stop();
                s.destroy();
            } catch (LifecycleException e) {
                log.error("Catalina.stop: ", e);
            }
            return;
        }
```
其中，`getServer()`用于获取当前进程已存在的服务实例
* 如果存在不为空，那么直接调用`stop`,`destory`
* 如果不存在，则通过解析`conf/server.xml`查找到已经存在的真实的服务实例，并注入到当前对象中
```java
    protected Digester createStopDigester() {

        // Initialize the digester
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);

        // Configure the rules we need for shutting down
        digester.addObjectCreate("Server",
                                 "org.apache.catalina.core.StandardServer",
                                 "className");
        digester.addSetProperties("Server");
        digester.addSetNext("Server",
                            "setServer",
                            "org.apache.catalina.Server");//这里解析完成后完成set

        return digester;

    }
```
#### 思考为什么要先`getServer`一次呢
猜想：可能用户会利用`Ctrl+C`的形式手动关闭，这时候关闭进程和真实的服务进程是同一进程，无需重复解析

#### 5.1 通过网络通信远程关闭服务
如果关闭进程和真实服务进程不是同一个进程的话。则通过远程`Socket`通信的方式关闭进程
```java
s = getServer();
if (s.getPort()>0) {
    try (Socket socket = new Socket(s.getAddress(), s.getPort());
        OutputStream stream = socket.getOutputStream()) {
        String shutdown = s.getShutdown();
        for (int i = 0; i < shutdown.length(); i++) {
            stream.write(shutdown.charAt(i));
        }
        stream.flush();
    } catch (ConnectException ce) {
        log.error(sm.getString("catalina.stopServer.connectException",
        s.getAddress(),
        String.valueOf(s.getPort())));
        log.error("Catalina.stop: ", ce);
        System.exit(1);
    } catch (IOException e) {
        log.error("Catalina.stop: ", e);
        System.exit(1);
    }
} else {
    log.error(sm.getString("catalina.stopServer"));
    System.exit(1);
}
```
**无论发送成功与否，关闭进程立即销毁**

### 6. ```org.apache.catalina.startup.Catalina```是如何关闭服务的
通过分析我们可以知道，tomcat关闭服务器会去寻找对应的`Server`，同时注意，如果关闭进程和服务进程是不同进程，那么关闭进程会通过网络通信的方式远程关闭，这点我们可以在`Catalina`的启动过程中找到痕迹
#### 6.1 启动时监听端口，等待关闭
```java
    public void start() {
        //todo...
        if (await) {
            await();
            stop();
        }
    }
```
其中`await()`是调用`org.apache.catalina.Server.await()`
我们查看下默认实现
```java
//org.apache.catalina.core.StandardServer.await
public void await() {
    //todo...
    try {
        awaitSocket = new ServerSocket(port, 1,
        InetAddress.getByName(address));
    } catch (IOException e) {
        log.error("StandardServer.await: create[" + address
        + ":" + port
        + "]: ", e);
        return;
    }
    //do something...
    // Loop waiting for a connection and a valid command
    while (!stopAwait) {
        socket = serverSocket.accept();
        //do something...
        boolean match = command.toString().equals(shutdown);
        if (match) {
            log.info(sm.getString("standardServer.shutdownViaPort"));
            break;
        } else
            log.warn(sm.getString("standardServer.invalidShutdownCommand", command.toString()));
    }
}
```
可以看到，一旦监听到`shutdown`命令，则立刻停止阻塞。紧接着调用`org.apache.catalina.startup.Catalina.stop()`

#### 6.2 `org.apache.catalina.startup.Catalina.stop()`
这个方法实际上就是远程关闭服务进程的核心方法，检查查看下逻辑
```java

 public void stop() {
     try {
         // Remove the ShutdownHook first so that server.stop()
         // doesn't get invoked twice
         if (useShutdownHook) {
             Runtime.getRuntime().removeShutdownHook(shutdownHook);
             // If JULI is being used, re-enable JULI's shutdown to ensure
             // log messages are not lost
             LogManager logManager = LogManager.getLogManager();
             if (logManager instanceof ClassLoaderLogManager) {
                 ((ClassLoaderLogManager) logManager).setUseShutdownHook(
                         true);
             }
         }
     } catch (Throwable t) {
         ExceptionUtils.handleThrowable(t);
         // This will fail on JDK 1.2. Ignoring, as Tomcat can run
         // fine without the shutdown hook.
     }
     // Shut down the server
     try {
         Server s = getServer();
         LifecycleState state = s.getState();
         if (LifecycleState.STOPPING_PREP.compareTo(state) <= 0
                 && LifecycleState.DESTROYED.compareTo(state) >= 0) {
             // Nothing to do. stop() was already called
         } else {
             s.stop();
             s.destroy();
         }
     } catch (LifecycleException e) {
         log.error("Catalina.stop", e);
     }
 }
```
可以看到大致分为两步
1. 移除`ShutdownHook`
2. 执行`org.apache.catalina.Server.Server.stop()`和`org.apache.catalina.Server.destory()`
其中第二步，和上面所提及的**如果关闭进程和服务进程是同一个，则直接关闭**的步骤是一致的。所以核心代码在于如下两个方法:
   1. `org.apache.catalina.Server.Server#stop`
   1. `org.apache.catalina.Server.Server#destory`
    
### 7.`org.apache.catalina.Server.Server`的关闭和销毁
`org.apache.catalina.Server.Server`的默认实现为`org.apache.catalina.core.StandardServer`
#### 7.1 org.apache.catalina.Server.Server#stop
这个方法的实现继承自`org.apache.catalina.util.LifecycleBase.stop`
核心步骤如下：
1. 执行`stopInternal`
2. 生命周期的状态变更`state = LifecycleState.STOPPED`
3. 发送生命周期变更事件`fireLifecycleEvent(lifecycleEvent, data)`

其中`stopInternal`已经由`org.apache.catalina.core.StandardServer`实现
```java
@Override
protected void stopInternal() throws LifecycleException {
    setState(LifecycleState.STOPPING);
    fireLifecycleEvent(CONFIGURE_STOP_EVENT, null);
    // Stop our defined Services
    for (Service service : services) {
        service.stop();
    }
    globalNamingResources.stop();
    stopAwait();
}
```
可以看到，主要关闭 **各个`Service`，全局的JDNI资源和阻塞的`Socket`**

#### 7.1 org.apache.catalina.Server.Server#destory
`destory`方法同理，除了生命周期的变更外，主要还是调用`org.apache.catalina.core.StandardServer.destroyInternal`
```java
@Override
protected void destroyInternal() throws LifecycleException {
    // Destroy our defined Services
    for (Service service : services) {
        service.destroy();
    }
    globalNamingResources.destroy();
    unregister(onameMBeanFactory);
    unregister(onameStringCache);
    super.destroyInternal();
}
```
除了各自`Service`，全局JNDI资源的销毁外，还取消注册了`MBean`
