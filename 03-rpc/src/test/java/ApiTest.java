import indi.kurok1.rpc.api.RemoteShutdownApi;
import indi.kurok1.rpc.api.User;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URL;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.21
 */
public class ApiTest {

    public static void main(String[] args) throws Exception {
        RemoteShutdownApi service = RestClientBuilder.newBuilder()
                .baseUrl(new URL("http://127.0.0.1:8080"))
                .build(RemoteShutdownApi.class);

        User user = new User();
        user.setName("aa");
        System.out.println(service.save(user));

        service.shutdown();
    }

}
