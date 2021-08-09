package org.geektimes.configuration.microprofile.config.fs;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.09
 */
public class FileSystemTest {

    public static void main(String[] args) throws IOException {
        URL url = new URL("fs:///Speed.log");
        FileInputStream inputStream = (FileInputStream) url.openStream();
        System.out.println(inputStream.available());
    }

}
