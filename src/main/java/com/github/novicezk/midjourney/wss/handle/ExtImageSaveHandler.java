package com.github.novicezk.midjourney.wss.handle;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.github.novicezk.midjourney.OssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        if (!ossProperties.getOssSave() || bucketName == null) {
            return null;
        }
        File file = null;
        int maxRetries = 3;
        int retries = 0;
        String url = null;

        while (retries < maxRetries) {
            try {
                file = downloadImage(imageUrl, maxRetries);
                if (file != null && file.exists()) {
                    log.debug("Image downloaded successfully. Start upload file to oss");
                    String objectKey = ossProperties.getPrefixPath() + "/" + UUID.randomUUID() + getFileExtension(imageUrl);
                    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, file);
                    ossClient.putObject(putObjectRequest);

                    if (ossProperties.getToSign()) {
                        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                                new GeneratePresignedUrlRequest(bucketName, objectKey);
                        generatePresignedUrlRequest.setExpiration(new Date(System.currentTimeMillis() + ossProperties.getExpirationSeconds() * 1000));
                        URL generatedUrl = ossClient.generatePresignedUrl(generatePresignedUrlRequest);
                        url = generatedUrl.toString();
                    } else {
                        String protocol = "https://";
                        String endpoint = ossProperties.getEndpoint();
                        if (endpoint.contains("://")) {
                            String[] parts = endpoint.split("://", 2);
                            protocol = parts[0] + "://";
                            endpoint = parts[1];
                        }
                        String fileUrl;
                        if (ossProperties.getIsCname()) {
                            fileUrl = protocol + endpoint + "/" + objectKey;
                        } else {
                            fileUrl = protocol + bucketName + "." + endpoint + "/" + objectKey;
                        }
                        url = fileUrl;
                    }
                    break;
                }
            } catch (Exception e) {
                log.error("Failed to upload image to aliyun oss. Retrying... ", e);
                retries++;
                if (retries >= maxRetries) {
                    log.error("Failed to upload image to aliyun oss after {} attempts.", maxRetries);
                }
            } finally {
                if (file != null) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        log.error("Failed to delete the temporary file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        return url;
    }

    private OSS createOssClient() {
        ClientBuilderConfiguration config = new ClientBuilderConfiguration();
        config.setSupportCname(ossProperties.getIsCname());
        config.setConnectionTimeout(10000);
        config.setSocketTimeout(30000);
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret(),
                config
        );
    }

    private static File downloadImage(String imageUrl, int maxRetries) {
        int retries = 0;
        while (retries < maxRetries) {
            try {
                URI uri = new URI(imageUrl);
                URL url = uri.toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
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
                } else {
                    log.warn("Failed to download image, HTTP response code: " + responseCode);
                }
            } catch (IOException e) {
                log.warn("IOException occurred, retrying... " + e.getMessage());
                e.printStackTrace();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (retries < maxRetries - 1) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Thread was interrupted");
                }
            }
            retries++;
        }
        log.error("Failed to download image after several attempts.");
        return null;
    }

    private static String getFileExtension(String imageUrl) {
        String fileName = new File(imageUrl.split("\\?")[0]).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }
}
