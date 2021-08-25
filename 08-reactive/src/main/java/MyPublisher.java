import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.25
 */
public class MyPublisher<T> implements Publisher<T> {

    private final List<Subscriber<? super T>> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public void subscribe(Subscriber<? super T> s) {
        subscribers.add(s);
        s.onSubscribe(new SubscriptionAdapter(s));
    }


    public void publish(T data) {
        subscribers.forEach(subscriber -> subscriber.onNext(data));
    }

}
