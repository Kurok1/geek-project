import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.25
 */
class SubscriptionAdapter implements Subscription {

    private final DelegatingSubscriber<?> subscriber;

    public SubscriptionAdapter(Subscriber<?> subscriber) {
        this.subscriber = new DelegatingSubscriber(subscriber);
    }

    @Override
    public void request(long n) {
        if (n < 1) {
            throw new IllegalArgumentException("The number of elements to requests must be more than zero!");
        }
        this.subscriber.setMaxRequest(n);
    }

    @Override
    public void cancel() {
        this.subscriber.cancel();
        this.subscriber.onComplete();
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public Subscriber getSourceSubscriber() {
        return subscriber.getSource();
    }
}