package org.apache.skywalking.oap.server.starter.config;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        String resourcePath = "/application.yml";
        InputStream resourceStream = Main.class.getResourceAsStream(resourcePath);
        System.out.println(resourceStream);
    }
}
