
import reactor.core.publisher.Mono;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.25
 */
public class ReactiveApplication {

    public static void main(String[] args) {
        MyPublisher<String> publisher = new MyPublisher<>();
        Mono.from(publisher).subscribe(new BusinessSubscriber(2));
        for (int i = 0 ; i < 10 ; i++)
            publisher.publish("hello, "+ i);

    }

}
