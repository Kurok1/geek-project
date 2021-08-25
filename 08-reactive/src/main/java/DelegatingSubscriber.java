import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.25
 */
public class DelegatingSubscriber<T> implements Subscriber<T> {

    private final Subscriber<T> source;

    private long maxRequest = -1;

    private boolean canceled = false;

    private boolean completed = false;

    private long requestCount = 0;

    public DelegatingSubscriber(Subscriber<T> source) {
        this.source = source;
    }

    @Override
    public void onSubscribe(Subscription s) {
        source.onSubscribe(s);
    }

    @Override
    public void onNext(T t) {

        assertRequest();

        if (isCompleted()) {
            System.out.println("The data subscription was completed, This method should not be invoked again!");
            return;
        }

        if (isCanceled()) { // Indicates that the Subscriber invokes Subscription#cancel() method.
            System.out.println(String.format("The Subscriber has canceled the data subscription," +
                    " current data[%s] will be ignored!", t));
            return;
        }

        if (requestCount == maxRequest && maxRequest < Long.MAX_VALUE) {
            onComplete();
            System.out.println(String.format("The number of requests is up to the max threshold[%d]," +
                    " the data subscription is completed!", maxRequest));
            return;
        }

        next(t);

    }

    private void assertRequest() {
        if (maxRequest < 1) {
            throw new IllegalStateException("the number of request must be initialized before " +
                    "Subscriber#onNext(Object) method, please set the positive number to " +
                    "Subscription#request(int) method on Publisher#subscribe(Subscriber) phase.");
        }
    }

    private void next(T t) {
        try {
            source.onNext(t);
        } catch (Throwable e) {
            onError(e);
        } finally {
            requestCount++;
        }
    }

    @Override
    public void onError(Throwable t) {
        source.onError(t);
    }

    @Override
    public void onComplete() {
        source.onComplete();
        completed = true;
    }

    public void setMaxRequest(long maxRequest) {
        this.maxRequest = maxRequest;
    }

    public void cancel() {
        canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Subscriber<T> getSource() {
        return source;
    }
}
