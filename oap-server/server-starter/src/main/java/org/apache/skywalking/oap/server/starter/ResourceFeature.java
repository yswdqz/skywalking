//package org.apache.skywalking.oap.server.starter;
//
//import com.oracle.svm.core.jdk.Resources;
//import org.apache.skywalking.oap.server.starter.config.Main;
//import org.graalvm.nativeimage.hosted.Feature;
//
//import java.io.InputStream;
//
//
//public class ResourceFeature implements Feature {
//
//    @Override
//    public void beforeAnalysis(BeforeAnalysisAccess access) {
//        try {
//            // 你的资源路径
//            String resourcePath = "/application.yml";
//            InputStream resourceStream = Main.class.getResourceAsStream(resourcePath);
//
//
//            // 注册资源
//            Resources.registerResource("application.yml",resourceStream);
//            resourceStream.close();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to register resource", e);
//        }
//    }
//}
