package ru.arklual.telegramparser.service;

import org.springframework.stereotype.Service;
import ru.arklual.telegramparser.config.S3Properties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Paths;
import java.util.UUID;

@Service
public class MediaUploader {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public MediaUploader(S3Client s3Client, S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
        ensureBucketExists();
    }

    public String uploadToS3(String localFilePath) {
        String key = UUID.randomUUID() + "-" + Paths.get(localFilePath).getFileName();
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(putReq, RequestBody.fromFile(Paths.get(localFilePath)));

        return s3Properties.getEndpoint() + "/" + s3Properties.getBucketName() + "/" + key;
    }

    private void ensureBucketExists() {
        try {
            HeadBucketRequest head = HeadBucketRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .build();
            s3Client.headBucket(head);
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .build());
        }
    }
}
