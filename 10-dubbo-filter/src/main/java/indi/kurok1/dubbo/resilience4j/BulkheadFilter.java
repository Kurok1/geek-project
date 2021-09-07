package indi.kurok1.dubbo.resilience4j;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.vavr.control.Try;
import org.apache.dubbo.rpc.*;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * BulkheadFilter implements for resilience4j-bulkhead {@see https://resilience4j.readme.io/docs/bulkhead}
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.09.07
 */
public class BulkheadFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // Create a custom configuration for a Bulkhead
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();

        // Create a BulkheadRegistry with a custom global configuration
        BulkheadRegistry bulkheadRegistry =
                BulkheadRegistry.of(config);

        Bulkhead bulkhead = bulkheadRegistry
                .bulkhead(String.format("%s:%s", invocation.getServiceName(), invocation.getMethodName()));

        Supplier<Result> decoratedSupplier = Bulkhead
                .decorateSupplier(bulkhead, ()->invoker.invoke(invocation) );

        Try<Result> result = Try.ofSupplier(decoratedSupplier);

        if (result.isEmpty())
            throw new RpcException(result.getCause());

        return result.get();
    }
}
