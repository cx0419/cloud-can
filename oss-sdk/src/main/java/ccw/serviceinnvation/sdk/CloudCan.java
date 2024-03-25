package ccw.serviceinnvation.sdk;

import ccw.serviceinnvation.sdk.exception.CloudCanDownLoadException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CloudCan {
    void putObject(String bucketName, String objName, File file) throws IOException;

    void putObject(String bucketName, String objName, InputStream inputStream);

    void createBucket(String bucketName) throws IOException;

    void getObject(String bucketName, String objName, OutputStream outputStream) throws IOException, CloudCanDownLoadException;

    void getObject(String bucketName, String objName, String path) throws IOException, CloudCanDownLoadException;
}
