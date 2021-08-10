package sun.net.www.protocol.json;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * JSON协议
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.10
 */
public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new JsonURLConnection(u);
    }
}

class JsonURLConnection extends URLConnection {

    protected JsonURLConnection(URL resource) {
        super(resource);
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public InputStream getInputStream() throws IOException {
        String realPath = "";
        if (url.getPath() == null || url.getPath().isEmpty())
            realPath = "/" + url.getAuthority();
        else realPath = String.format("%s%s", url.getAuthority(), url.getPath());
        return new FileInputStream(realPath);
    }
}
