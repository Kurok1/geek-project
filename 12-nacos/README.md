## ⽂字说明 Spring Cloud Alibaba Nacos 服务注册与发现的实现逻辑

依赖Spring Cloud Alibaba Nacos版本为：`2.2.1.RELEASE`

### DiscoveryClient 实现
`NacosDiscoveryClient`中`Services`和`ServiceInstance`的来源均来自于外部注入的`NacosServiceDiscovery`

#### 1.`NamingService`实例化和初始化
`NamingService`用于抽象远程注册中心，提供服务注册，服务发现，注册事件监听器等api
##### 实例化：
`NamingService`在`NacosDiscoveryProperties`是单例实现
```java
//NacosDiscoveryProperties.java
public NamingService namingServiceInstance() {
    if (null != namingService) {
        return namingService;
    }
    try {
        namingService = NacosFactory.createNamingService(getNacosProperties());
    }
    catch (Exception e) {
        log.error("create naming service error!properties={},e=,", this, e);
        return null;
    }
    return namingService;
}
```
在`NacosFactory`中利用反射机制完成实例化
```java
//NacosFactory.java
public static NamingService createNamingService(Properties properties) throws NacosException {
    try {
        Class<?> driverImplClass = Class.forName("com.alibaba.nacos.client.naming.NacosNamingService");
        Constructor constructor = driverImplClass.getConstructor(Properties.class);
        NamingService vendorImpl = (NamingService)constructor.newInstance(properties);
        return vendorImpl;
    } catch (Throwable e) {
        throw new NacosException(NacosException.CLIENT_INVALID_PARAM, e);
    }
}
```

##### 初始化
```java
private void init(Properties properties) {
    namespace = InitUtils.initNamespaceForNaming(properties);
    initServerAddr(properties);
    InitUtils.initWebRootContext();
    initCacheDir();
    initLogName(properties);
    eventDispatcher = new EventDispatcher();
    serverProxy = new NamingProxy(namespace, endpoint, serverList, properties);
    beatReactor = new BeatReactor(serverProxy, initClientBeatThreadCount(properties));
    hostReactor = new HostReactor(eventDispatcher, serverProxy, cacheDir, isLoadCacheAtStart(properties),
        initPollingThreadCount(properties));
}
```
初始化流程：
1. 生成命名空间
2. 初始化已配置的服务列表和设置注册中心地址（一旦设置注册中心地址，手动配置的服务列表失效）
3. 设置本地缓存目录
4. 创建远程服务事件分发器
5. 创建服务管理
6. 创建本地实例信息心跳监听器
7. 创建远程服务实例信息监听器

#### 2.获取服务列表和服务实例列表
服务列表和服务实例列表最终来源都是`NamingService`

获取服务列表
```java
public List<String> getServices() throws NacosException {
    String group = discoveryProperties.getGroup();
    ListView<String> services = discoveryProperties.namingServiceInstance()
        .getServicesOfServer(1, Integer.MAX_VALUE, group);
    return services.getData();
}
```
根据服务获取服务实例列表
```java
@Override
public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy, boolean subscribe) throws NacosException {
    ServiceInfo serviceInfo;
    if (subscribe) {
        serviceInfo = hostReactor.getServiceInfo(NamingUtils.getGroupedName(serviceName, groupName), StringUtils.join(clusters, ","));
    } else {
        serviceInfo = hostReactor.getServiceInfoDirectlyFromServer(NamingUtils.getGroupedName(serviceName, groupName), StringUtils.join(clusters, ","));
    }
    return selectInstances(serviceInfo, healthy);
}
private List<Instance> selectInstances(ServiceInfo serviceInfo, boolean healthy) {
    List<Instance> list;
    if (serviceInfo == null || CollectionUtils.isEmpty(list = serviceInfo.getHosts())) {
        return new ArrayList<Instance>();
    }
    Iterator<Instance> iterator = list.iterator();
    while (iterator.hasNext()) {
        Instance instance = iterator.next();
        if (healthy != instance.isHealthy() || !instance.isEnabled() || instance.getWeight() <= 0) {
            iterator.remove();
        }
    }
    return list;
}
```

### ServiceRegistration 实现
#### NacosRegistration实例化和初始化
```java
public class NacosRegistration implements Registration, ServiceInstance {

    private NacosDiscoveryProperties nacosDiscoveryProperties;

    private ApplicationContext context;

    public NacosRegistration(NacosDiscoveryProperties nacosDiscoveryProperties,
                             ApplicationContext context) {
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
        this.context = context;
    }
    @PostConstruct
    public void init() {
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
        Environment env = context.getEnvironment();
        String endpointBasePath = env.getProperty(MANAGEMENT_ENDPOINT_BASE_PATH);
        if (!StringUtils.isEmpty(endpointBasePath)) {
            metadata.put(MANAGEMENT_ENDPOINT_BASE_PATH, endpointBasePath);
        }
        Integer managementPort = ManagementServerPortUtils.getPort(context);
        if (null != managementPort) {
            metadata.put(MANAGEMENT_PORT, managementPort.toString());
            String contextPath = env
                    .getProperty("management.server.servlet.context-path");
            String address = env.getProperty("management.server.address");
            if (!StringUtils.isEmpty(contextPath)) {
                metadata.put(MANAGEMENT_CONTEXT_PATH, contextPath);
            }
            if (!StringUtils.isEmpty(address)) {
                metadata.put(MANAGEMENT_ADDRESS, address);
            }
        }
        if (null != nacosDiscoveryProperties.getHeartBeatInterval()) {
            metadata.put(PreservedMetadataKeys.HEART_BEAT_INTERVAL,
                    nacosDiscoveryProperties.getHeartBeatInterval().toString());
        }
        if (null != nacosDiscoveryProperties.getHeartBeatTimeout()) {
            metadata.put(PreservedMetadataKeys.HEART_BEAT_TIMEOUT,
                    nacosDiscoveryProperties.getHeartBeatTimeout().toString());
        }
        if (null != nacosDiscoveryProperties.getIpDeleteTimeout()) {
            metadata.put(PreservedMetadataKeys.IP_DELETE_TIMEOUT,
                    nacosDiscoveryProperties.getIpDeleteTimeout().toString());
        }
    }
}
```
外部注入`NacosDiscoveryProperties`和`ApplicationContext`完成实例化

初始化过程中读取系统参数配置，写入元信息

#### NacosAutoServiceRegistration实例化和初始化
```java
public class NacosAutoServiceRegistration
		extends AbstractAutoServiceRegistration<Registration> {

	private static final Logger log = LoggerFactory
			.getLogger(NacosAutoServiceRegistration.class);

	private NacosRegistration registration;

	public NacosAutoServiceRegistration(ServiceRegistry<Registration> serviceRegistry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			NacosRegistration registration) {
		super(serviceRegistry, autoServiceRegistrationProperties);
		this.registration = registration;
	}

	@Override
	protected void register() {
		if (!this.registration.getNacosDiscoveryProperties().isRegisterEnabled()) {
			log.debug("Registration disabled.");
			return;
		}
		if (this.registration.getPort() < 0) {
			this.registration.setPort(getPort().get());
		}
		super.register();
	}
}
```
外部注入ServiceRegistry用于自动注册服务，同时引入`NacosRegistration`

在`AbstractAutoServiceRegistration`中，会监听`WebServerInitializedEvent`触发`register`方法完成自动注册
```java
public abstract class AbstractAutoServiceRegistration<R extends Registration> {
	private boolean autoStartup = true;
	private AtomicBoolean running = new AtomicBoolean(false);

	@Override
	@SuppressWarnings("deprecation")
	public void onApplicationEvent(WebServerInitializedEvent event) {
		bind(event);
	}

	@Deprecated
	public void bind(WebServerInitializedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		if (context instanceof ConfigurableWebServerApplicationContext) {
			if ("management".equals(((ConfigurableWebServerApplicationContext) context)
					.getServerNamespace())) {
				return;
			}
		}
		this.port.compareAndSet(0, event.getWebServer().getPort());
		this.start();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
		this.environment = this.context.getEnvironment();
	}

	public void start() {
		if (!isEnabled()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Discovery Lifecycle disabled. Not starting");
			}
			return;
		}
		// only initialize if nonSecurePort is greater than 0 and it isn't already running
		// because of containerPortInitializer below
		if (!this.running.get()) {
			this.context.publishEvent(
					new InstancePreRegisteredEvent(this, getRegistration()));
			register();
			if (shouldRegisterManagement()) {
				registerManagement();
			}
			this.context.publishEvent(
					new InstanceRegisteredEvent<>(this, getConfiguration()));
			this.running.compareAndSet(false, true);
		}

	}

	protected void register() {
		this.serviceRegistry.register(getRegistration());
	}
}
```
通过原子变量`running`确保只会注册一次，在执行`register`前后分别发布`InstancePreRegisteredEvent`和`InstanceRegisteredEvent`

### ⾃动装配
`spring.factories`
```text
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration,\
  com.alibaba.cloud.nacos.ribbon.RibbonNacosAutoConfiguration,\
  com.alibaba.cloud.nacos.endpoint.NacosDiscoveryEndpointAutoConfiguration,\
  com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration,\
  com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration,\
  com.alibaba.cloud.nacos.discovery.reactive.NacosReactiveDiscoveryClientConfiguration,\
  com.alibaba.cloud.nacos.discovery.configclient.NacosConfigServerAutoConfiguration
org.springframework.cloud.bootstrap.BootstrapConfiguration=\
  com.alibaba.cloud.nacos.discovery.configclient.NacosDiscoveryClientConfigServiceBootstrapConfiguration
```
其中`NacosServiceRegistryAutoConfiguration`依赖`spring.cloud.service-registry.auto-registration.enabled=true`,并且需要`AutoServiceRegistrationConfiguration`生效

而`AutoServiceRegistrationConfiguration`生效需要依赖`@EnableDiscoveryClient#autoRegister()=true`

1. `NacosDiscoveryAutoConfiguration` 提供`NacosServiceDiscovery`
2. `RibbonNacosAutoConfiguration`用于激活`RibbonClients`
3. `NacosDiscoveryEndpointAutoConfiguration` 提供相关actuator endpoint实现
4. `NacosServiceRegistryAutoConfiguration`提供`NacosRegistration`和`NacosAutoServiceRegistration`
5. `NacosDiscoveryClientConfiguration`提供`DiscoveryClient`的nacos实现
6. `NacosReactiveDiscoveryClientConfiguration`提供`NacosReactiveDiscoveryClient`

