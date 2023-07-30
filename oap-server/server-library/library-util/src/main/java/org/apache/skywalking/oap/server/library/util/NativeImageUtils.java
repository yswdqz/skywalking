package org.apache.skywalking.oap.server.library.util;

public class NativeImageUtils {
    public static boolean isNativeImage () {
        return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }
}
