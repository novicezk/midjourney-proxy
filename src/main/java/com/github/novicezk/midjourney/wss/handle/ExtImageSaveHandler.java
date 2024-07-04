package com.github.novicezk.midjourney.wss.handle;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.github.novicezk.midjourney.OssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Component
public class ExtImageSaveHandler {

    private final OssProperties ossProperties;
    private final OSS ossClient;

    @Autowired
    public ExtImageSaveHandler(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
        this.ossClient = createOssClient();
    }

    public String uploadToOSSAndGetUrl(String imageUrl) {
        String bucketName = ossProperties.getBucketName();
        if (!ossProperties.getOssSave() || bucketName == null){
            return null;
        }
        File file = downloadImage(imageUrl);
        if (file != null && file.exists()) {
            String objectKey = ossProperties.getPrefixPath() + "/" + UUID.randomUUID() + getFileExtension(imageUrl);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, file);
            ossClient.putObject(putObjectRequest);

            if (ossProperties.getToSign()) {
                GeneratePresignedUrlRequest generatePresignedUrlRequest =
                        new GeneratePresignedUrlRequest(bucketName, objectKey);
                generatePresignedUrlRequest.setExpiration(new Date(System.currentTimeMillis() + ossProperties.getExpirationSeconds() * 1000));
                URL url = ossClient.generatePresignedUrl(generatePresignedUrlRequest);
                return url.toString();
            } else {
                String endpoint = ossProperties.getEndpoint().split("https://")[1];
                return "https://" + bucketName + "." + endpoint + "/" + objectKey;
            }
        }
        return null;
    }

    private OSS createOssClient() {
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
    }

    private static File downloadImage(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                File file = File.createTempFile("tempImage", "tmp");
                file.deleteOnExit();

                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static String getFileExtension(String imageUrl) {
        String fileName = new File(imageUrl.split("\\?")[0]).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }
}
