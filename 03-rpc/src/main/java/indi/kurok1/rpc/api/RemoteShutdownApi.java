package indi.kurok1.rpc.api;

import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * 远程调用接口，远程调用 http://localhost:8080/actuator/shutdown
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.20
 */
public interface RemoteShutdownApi {

    @POST
    @Path("/actuator/shutdown")
    public void shutdown();

    @POST
    @Path(("/api/user/save"))
    @Produces("application/json")
    public User save(@BeanParam User user);

}
