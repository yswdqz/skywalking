package org.apache.skywalking.oap.meter.analyzer.dsl.graal;

public class NativeImageUtils {
    public static boolean isNativeImage () {
        return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }
}
