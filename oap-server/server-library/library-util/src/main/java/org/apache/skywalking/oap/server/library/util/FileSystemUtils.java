//package org.apache.skywalking.oap.server.library.util;
//
//import java.net.URI;
//import java.nio.file.FileSystem;
//
//public class FileSystemUtils {
//
//    public static FileSystem getOrCreateFileSystem() {
//        URI resource = resourceNameToURI(RESOURCE_FILE_1);
//
//        Map<String, String> env = new HashMap<>();
//        env.put("create", "true");
//
//        FileSystem fileSystem = null;
//        boolean exceptionThrown = false;
//        try {
//            // Try to get file system. This should raise exception.
//            fileSystem = FileSystems.getFileSystem(resource);
//        } catch (FileSystemNotFoundException e) {
//            // File system not found. Create new one.
//            exceptionThrown = true;
//            try {
//                fileSystem = FileSystems.newFileSystem(resource, env);
//            } catch (IOException ioException) {
//                Assert.fail("Error during creating a new file system!");
//            }
//        }
//
//        Assert.assertTrue("File system is already created!", exceptionThrown);
//        Assert.assertNotNull("File system is not created!", fileSystem);
//
//        return fileSystem;
//    }
//}
