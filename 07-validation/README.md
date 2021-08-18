## 描述 Spring 校验注解 org.springframework.validation.annotation.Validated 的工作原理，它与 Spring Validator 以及 JSR-303 Bean Validation @javax.validation.Valid 之间的关系。

### `@Validated`的工作原理
#### 方法级别参数校验
在每个参数前面声明约束注解，然后通过解析参数注解完成校验，这就是方法级别的参数校验。
这种方式可以用于任何的`Spring Bean`的方法上，一般来说，这种方式一般会采用`AOP`的`Around`增强完成
在Spring中，是通过以下步骤完成
1. `MethodValidationPostProcessor`在Bean的初始化完成之后，判断是否要进行AOP代理（类是否被`@Validated`标记）
2. `MethodValidationInterceptor`拦截所有方法，执行校验逻辑
3. 委派`Validator`执行参数校验和返回值校验，得到`ConstraintViolation`
4. 处理`ConstraintViolation`

先看看`MethodValidationPostProcessor`,`MethodValidationPostProcessor`继承自`AbstractAdvisingBeanPostProcessor`
```java
public class MethodValidationPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor
		implements InitializingBean {

	private Class<? extends Annotation> validatedAnnotationType = Validated.class;
	
    //todo 设置Validator

	@Override
	public void afterPropertiesSet() {
		Pointcut pointcut = new AnnotationMatchingPointcut(this.validatedAnnotationType, true);
		this.advisor = new DefaultPointcutAdvisor(pointcut, createMethodValidationAdvice(this.validator));
	}
	
	protected Advice createMethodValidationAdvice(@Nullable Validator validator) {
		return (validator != null ? new MethodValidationInterceptor(validator) : new MethodValidationInterceptor());
	}

}
```
在`afterPropertiesSer()`方法中，根据`@Validated`创建`AnnotationMatchingPointcut`，并根据该切入点，匹配需要完成aop代理的bean
```java
public abstract class AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport implements BeanPostProcessor {

	@Nullable
	protected Advisor advisor;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (this.advisor == null || bean instanceof AopInfrastructureBean) {
			// Ignore AOP infrastructure such as scoped proxies.
			return bean;
		}
		if (isEligible(bean, beanName)) {
		    //创建代理...
		}
		// No proxy needed.
		return bean;
	}
	
	protected boolean isEligible(Object bean, String beanName) {
		return isEligible(bean.getClass());
	}
	
	protected boolean isEligible(Class<?> targetClass) {
		//空校验.
		eligible = AopUtils.canApply(this.advisor, targetClass);
		this.eligibleBeans.put(targetClass, eligible);
		return eligible;
	}
}
```
接着看一下MethodValidationInterceptor：
```java
public class MethodValidationInterceptor implements MethodInterceptor {

	private final Validator validator;

	@Override
	@Nullable
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// Avoid Validator invocation on FactoryBean.getObjectType/isSingleton
		if (isFactoryBeanMetadataMethod(invocation.getMethod())) {
			return invocation.proceed();
		}

		Class<?>[] groups = determineValidationGroups(invocation);

		// Standard Bean Validation 1.1 API
		ExecutableValidator execVal = this.validator.forExecutables();
		Method methodToValidate = invocation.getMethod();
		Set<ConstraintViolation<Object>> result;

		Object target = invocation.getThis();
		Assert.state(target != null, "Target must not be null");

		try {
			result = execVal.validateParameters(target, methodToValidate, invocation.getArguments(), groups);
		}
		catch (IllegalArgumentException ex) {
			// Probably a generic type mismatch between interface and impl as reported in SPR-12237 / HV-1011
			// Let's try to find the bridged method on the implementation class...
			methodToValidate = BridgeMethodResolver.findBridgedMethod(
					ClassUtils.getMostSpecificMethod(invocation.getMethod(), target.getClass()));
			result = execVal.validateParameters(target, methodToValidate, invocation.getArguments(), groups);
		}
		if (!result.isEmpty()) {
			throw new ConstraintViolationException(result);
		}

		Object returnValue = invocation.proceed();

		result = execVal.validateReturnValue(target, methodToValidate, returnValue, groups);
		if (!result.isEmpty()) {
			throw new ConstraintViolationException(result);
		}

		return returnValue;
	}

	private boolean isFactoryBeanMetadataMethod(Method method) {
		//是否是FactoryBean，这类bean往往不需要执行方法校验
	}
	
	protected Class<?>[] determineValidationGroups(MethodInvocation invocation) {
		//判断分组
	}

}
```
实际上，可以明显的看到，在`invoke`方法内部，真正完成校验的操作是由外部传入的`Validator`实现的，`Spring Validation`**只是做了一层再封装**

### Bean级别的校验
Bean级别的校验在`Spring`中主要完成在`Bean`的初始化阶段，可以指定在初始化前还是初始化后（默认为初始化后）
核心处理类为`BeanValidationPostProcessor`
```java
public class BeanValidationPostProcessor implements BeanPostProcessor, InitializingBean {

	@Nullable
	private Validator validator;
	private boolean afterInitialization = false;
	
	@Override
	public void afterPropertiesSet() {
		if (this.validator == null) {
			this.validator = Validation.buildDefaultValidatorFactory().getValidator();
		}
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (!this.afterInitialization) {
			doValidate(bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (this.afterInitialization) {
			doValidate(bean);
		}
		return bean;
	}
	
	protected void doValidate(Object bean) {
		Assert.state(this.validator != null, "No Validator set");
		Object objectToValidate = AopProxyUtils.getSingletonTarget(bean);
		if (objectToValidate == null) {
			objectToValidate = bean;
		}
		Set<ConstraintViolation<Object>> result = this.validator.validate(objectToValidate);

		if (!result.isEmpty()) {
			StringBuilder sb = new StringBuilder("Bean state is invalid: ");
			for (Iterator<ConstraintViolation<Object>> it = result.iterator(); it.hasNext();) {
				ConstraintViolation<Object> violation = it.next();
				sb.append(violation.getPropertyPath()).append(" - ").append(violation.getMessage());
				if (it.hasNext()) {
					sb.append("; ");
				}
			}
			throw new BeanInitializationException(sb.toString());
		}
	}
}
```
可以看到，跟*方法校验*一样，这里也是依赖外部`Validator`来完成校验

### `Spring Validator`
`Spring Validator`准确来讲应该是`Spring Framework`自身对`hibernate-validation`的整合，主要体现在以下方面
1. 消息文本替换，改由Spring委派
2. `ConstraintValidator`的创建，从反射形式改为`BeanFactory`创建
3. `Bean Validation XML`映射添加

至于其核心的校验api,`validate`，我们在`SpringValidatorAdapter`中能明显看到还是利用外部`Validator`进行委派操作
```java
public class SpringValidatorAdapter implements SmartValidator, javax.validation.Validator {
    @Nullable
    private javax.validation.Validator targetValidator;

    public SpringValidatorAdapter(javax.validation.Validator targetValidator) {
        Assert.notNull(targetValidator, "Target Validator must not be null");
        this.targetValidator = targetValidator;
    }

    SpringValidatorAdapter() {
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return (this.targetValidator != null);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (this.targetValidator != null) {
            processConstraintViolations(this.targetValidator.validate(target), errors);
        }
    }

    @Override
    public void validate(Object target, Errors errors, Object... validationHints) {
        if (this.targetValidator != null) {
            processConstraintViolations(
                    this.targetValidator.validate(target, asValidationGroups(validationHints)), errors);
        }
    }
    //其余判断
}
```

### 总结
* `@Validated`的原理本质还是`AOP`。在方法校验上，利用AOP动态拦截方法，利用`JSR303 Validator`实现完成校验。在Bean的属性校验上，则是基于Bean的生命周期，在其初始化前后完成校验
* `Spring Validator`本质实现还是`JSR303 Validaotr`，只是能让其更好的适配`Spring Context`
* `@javax.validation.Valid`是`JSR303`的核心标记注解，但是在`Spring Framework`中被`@Validated`取代，但是`Spring Validator`的实现可以支持兼容`@javax.validation.Valid`

例如,在`MethodValidationPostProcessor`提供了`setValidatedAnnotationType`方法，替换默认的`@Validated`

在`Spring MVC`中，`RequestResponseBodyMethodProcessor`对`@RequestBody`和`@ResponseBody`的校验处理，就兼容了`@javax.validation.Valid`和`@Validated`
```java
public class RequestResponseBodyMethodProcessor extends AbstractMessageConverterMethodProcessor {
    @Override
    protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        Annotation[] annotations = parameter.getParameterAnnotations();
        for (Annotation ann : annotations) {
            Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
            if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
                Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[] {hints});
                binder.validate(validationHints);
                break;
            }
        }
    }
}
```
