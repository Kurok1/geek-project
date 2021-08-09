package org.geektimes.configuration.demo;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.09
 */
public class ClassPathTest {

    @Test
    public void test() throws Exception {
        String packageName = "org.geektimes.configuration.microprofile.config";
        packageName = packageName.replace(".", "/");
        resolveClass(packageName);

    }


    private void resolveClass(String basePackage) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(basePackage);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            System.out.println(resource.getFile());
            File file = new File(resource.getFile());
            if (file.exists() && file.isDirectory()) {
                String[] fileList = file.list();
                for (String fileName : fileList) {
                    if (fileName.endsWith(".class")) {
                        fileName = fileName.substring(0, fileName.length() - 6);
                        System.out.println("--"+fileName);
                        continue;
                    }
                    resolveClass(basePackage + "/" + fileName);
                }
            }

        }
    }

}
