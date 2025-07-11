package com.aashdit.digiverifier.utils;

import com.aashdit.digiverifier.common.enums.ContentViewType;
import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.vendorcheck.dto.ConventionalCandidateReportDto;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AwsUtils {

    @Autowired
    AmazonS3 s3Client;
    public static final String DIGIVERIFIER_DOC_BUCKET_NAME = "digiverifier-new";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public String uploadFile(String bucketName, String path, File file) {
        PutObjectResult putObjectResult = s3Client.putObject(bucketName, path, file);
        URL url = s3Client.getUrl(bucketName, path);
        return Objects.nonNull(url) ? String.valueOf(url) : "";
    }

    public String uploadFileAndGetPresignedUrl(String bucketName, String path, File file) {
        s3Client.putObject(bucketName, path, file);

        Date expiration = getExpirationDate();

        // Generate the presigned URL.
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, path)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        return Objects.nonNull(url) ? String.valueOf(url) : "";
    }


    public String uploadEmptyFolderAndGeneratePrecisedUrl(String bucketName, String path) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path, emptyContent, metadata);
        Date expiration = getExpirationDate();
        // Generate the presigned URL.
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, path).withMethod(HttpMethod.GET).withExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return Objects.nonNull(url) ? String.valueOf(url) : "";
    }

    public String uploadFileAndGetPresignedUrl_bytes(String bucketName, String path, byte[] file) {
        byte[] bytes = file;
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(bytes.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path, byteArrayInputStream, metaData);
        s3Client.putObject(putObjectRequest);
        Date expiration = getExpirationDate();


        // Generate the presigned URL.
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, path).withMethod(HttpMethod.GET).withExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return Objects.nonNull(url) ? String.valueOf(url) : "";
    }

    public String uploadFileAndGetPresignedUrl_bytes(String bucketName, String path, byte[] file, ObjectMetadata metadata) {
        byte[] bytes = file;

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path, byteArrayInputStream, metadata);
        s3Client.putObject(putObjectRequest);
        Date expiration = getExpirationDate();


        // Generate the presigned URL.
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, path).withMethod(HttpMethod.GET).withExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return Objects.nonNull(url) ? String.valueOf(url) : "";
    }

    private Date getExpirationDate() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 10000 * 60 * 10;
        expiration.setTime(expTimeMillis);
        return expiration;
    }

    public String getPresignedUrl(String bucketName, String path) {
        return getPresignedUrl(bucketName, path, ContentViewType.VIEW);
    }


    public static void toFile(byte[] data, File destination) {
        try (FileOutputStream fos = new FileOutputStream(destination)) {
            fos.write(data);
            fos.close();
        } catch (Exception e) {

        }
    }

    public S3Object getZipdata(String bucketname, String filename) {
        {

            S3Object object = s3Client.getObject(bucketname, filename);
            return object;
        }
    }


    public String getPresignedUrl(String bucketName, String path, ContentViewType type) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, path).withMethod(HttpMethod.GET).withExpiration(getExpirationDate());
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        String dispositionValue = (type.equals(ContentViewType.VIEW)) ? "inline" : "attachment";

        return Objects.nonNull(url) ? String.valueOf(url) : "";
    }

    public void getFileFromS3(String bucketName, String path, File file) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, path);
        S3Object s3Object = s3Client.getObject(getObjectRequest);
        S3ObjectInputStream stream = s3Object.getObjectContent();
        try {
            FileUtils.copyInputStreamToFile(stream, file);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public File getFileFromS3(String bucketName, String path) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, path);
        S3Object s3Object = s3Client.getObject(getObjectRequest);

        // Get the content type of the object
        ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
        String contentType = objectMetadata.getContentType();

        String fileExtension = getFileExtension(contentType);
        // Create a temporary file to save the content
//	    File tempFile = File.createTempFile("temp", ".pdf");
        File tempFile = File.createTempFile("temp", fileExtension);

        try (InputStream inputStream = s3Object.getObjectContent();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {

            // Copy content from input stream to the temporary file
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    private String getFileExtension(String contentType) {
        switch (contentType) {
            case "application/pdf":
                return ".pdf";
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            // Add more cases for other content types as needed
            default:
                return ".dat"; // Default to .dat if content type not recognized
        }
    }

    public byte[] getbyteArrayFromS3(String bucketName, String path) throws IOException {

        S3Object s3Object = s3Client.getObject(bucketName, path);
        InputStream inputStream = s3Object.getObjectContent();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(s3Object.getObjectContent(), outputStream);

        byte[] pdfBytes = outputStream.toByteArray();
        s3Object.close();
        outputStream.close();
        return pdfBytes;
//        byte[] buffer = new byte[4096];
//        int bytesRead;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//
//        byte[] data = outputStream.toByteArray();
//        outputStream.close();
//        inputStream.close();

    }

    public ConventionalCandidateReportDto uploadDtoAsJsonToS3AndReturnDto(String bucketName, String path, ConventionalCandidateReportDto dto) {
        try {
            // Convert DTO to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(dto);

            // Calculate the file size in bytes
            byte[] jsonBytes = jsonString.getBytes();
            long fileSizeInBytes = jsonBytes.length;

            // Print the file size in the console
            System.out.println("Uploading JSON to S3...");
            System.out.println("File Size: " + fileSizeInBytes + " bytes (" + fileSizeInBytes / 1024 + " KB)");

            // Prepare for upload
            InputStream jsonInputStream = new ByteArrayInputStream(jsonBytes);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSizeInBytes);
            metadata.setContentType("application/json");
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path, jsonInputStream, metadata);
            s3Client.putObject(putObjectRequest);

            // Generate pre-signed URL for the uploaded JSON
            Date expiration = getExpirationDate();
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, path)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            // Print confirmation
            System.out.println("File uploaded successfully to S3. Pre-signed URL: " + url);

            // Return the same DTO
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload JSON to S3", e);
        }
    }

    public String uploadDtosAsJsonToS3AndReturnPresignedUrl(
            Map<String, ConventionalCandidateReportDto> checkNameToDtoMap,
            String path,
            String bucketName) {
        try {
            // Convert the map to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(checkNameToDtoMap);

            // Calculate the file size in bytes
            byte[] jsonBytes = jsonString.getBytes();
            long fileSizeInBytes = jsonBytes.length;

            // Print the total file size in the console
            System.out.println("Uploading JSON to S3...");
            System.out.println("Total File Size: " + fileSizeInBytes + " bytes (" + fileSizeInBytes / 1024 + " KB)");

            // Prepare for upload
            InputStream jsonInputStream = new ByteArrayInputStream(jsonBytes);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSizeInBytes);
            metadata.setContentType("application/json");

            // Upload to S3
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path, jsonInputStream, metadata);
            s3Client.putObject(putObjectRequest);

            // Generate pre-signed URL for the uploaded JSON
            Date expiration = getExpirationDate();
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, path)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            URL preSignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            // Print confirmation
            System.out.println("File uploaded successfully to S3. Pre-signed URL: " + preSignedUrl);

            // Return the pre-signed URL
            return preSignedUrl.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload JSON to S3", e);
        }
    }

    // it should  be deleted in 5mins after uploaded,just used for temporary purpose url generation
    public String uploadFileAndGetPresignedUrlTemp(String bucketName, String path, File file) {
        // Print the file size in bytes
        System.out.println("Uploading file: " + file.getName());
        System.out.println("File size: " + file.length() + " bytes");

        // Upload the file to S3
        s3Client.putObject(bucketName, path, file);

        // Get the expiration date for the presigned URL (5 minutes from now)
        Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);

        // Generate the presigned URL
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, path)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        // Schedule deletion of the file after 5 minutes
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            try {
                log.info("Attempting to delete file from S3: {}", path);
                s3Client.deleteObject(bucketName, path);
                log.info("File successfully deleted from S3!");
            } catch (Exception e) {
                log.error("Error deleting file from S3: {}", e.getMessage(), e);
            }
        }, 5, TimeUnit.MINUTES);
        scheduler.shutdown();

        return Objects.nonNull(url) ? String.valueOf(url) : "";
    }

    public ServiceOutcome<List<String>> deletePathsFromS3(List<String> pathKeys, String bucketName, String requestId) {
        ServiceOutcome<List<String>> serviceOutcome = new ServiceOutcome<>();
        List<String> deletedPaths = new ArrayList<>();

        if (pathKeys.isEmpty()) {
            log.warn("No S3 paths provided for deletion.");
            serviceOutcome.setOutcome(true);
            serviceOutcome.setMessage("No S3 paths to delete.");
            return serviceOutcome;
        }

        for (String pathKey : pathKeys) {
            if (pathKey != null) {
                try {
                    s3Client.deleteObject(bucketName, pathKey);
                    deletedPaths.add(pathKey);
                    log.info("Deleted from S3: " + pathKey);
                } catch (Exception e) {
                    log.error("S3 deletion failed for path: " + pathKey + " | Error: " + e.getMessage());
                    serviceOutcome.setOutcome(false);
                    serviceOutcome.setMessage("Failed to delete some S3 files.");
                    return serviceOutcome;
                }
            } else {
                log.warn("Skipping null pathKey during S3 deletion.");
            }
        }

        // Attempt to delete the folder after deleting files
        String folderPath = "Candidate/Conventional/" + requestId + "/";
        log.info("Calling deleteFolder recursively from deletePathsFromS3 for folder: " + folderPath);

        ServiceOutcome<List<String>> folderDeletionOutcome = deleteFolder(bucketName, folderPath);

        if (folderDeletionOutcome.getOutcome()) {
            log.info("Deleted folder from S3: " + folderPath);
            deletedPaths.add(folderPath);
        } else {
            log.error("Failed to delete folder from S3: " + folderPath);
            serviceOutcome.setOutcome(false);
            serviceOutcome.setMessage("Failed to delete folder from S3.");
            return serviceOutcome;
        }

        serviceOutcome.setOutcome(true);
        serviceOutcome.setData(deletedPaths);
        serviceOutcome.setMessage("All S3 files deleted successfully.");
        return serviceOutcome;
    }

    public ServiceOutcome<List<String>> deleteFolder(String bucketName, String folderPath) {
        ServiceOutcome<List<String>> serviceOutcome = new ServiceOutcome<>();
        List<String> objectKeys = new ArrayList<>();

        try {
            // Ensure the folder path ends with '/'
            if (!folderPath.endsWith("/")) {
                folderPath += "/";
            }

            // List all objects under the folder prefix
            ObjectListing objectListing = s3Client.listObjects(bucketName, folderPath);
            while (true) {
                for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                    objectKeys.add(summary.getKey());
                }

                // If there are more objects, continue listing
                if (objectListing.isTruncated()) {
                    objectListing = s3Client.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }

            if (objectKeys.isEmpty()) {
                log.warn("No objects found in folder: " + folderPath);
                serviceOutcome.setOutcome(true);
                serviceOutcome.setMessage("Folder  Deleted ");
                return serviceOutcome;
            }

            // Explicitly logging recursive call
            log.info("Calling deletePathsFromS3 recursively from deleteFolder for objects inside folder: " + folderPath);

            // Recursively delete all objects inside the folder
            ServiceOutcome<List<String>> deleteOutcome = deletePathsFromS3(objectKeys, bucketName, "");

            if (!deleteOutcome.getOutcome()) {
                log.error("Failed to delete some objects in folder: " + folderPath);
                return deleteOutcome; // Return failure if deletion failed
            }

            serviceOutcome.setOutcome(true);
            serviceOutcome.setData(objectKeys);
            serviceOutcome.setMessage("Folder deleted successfully: " + folderPath);

        } catch (Exception e) {
            log.error("Failed to delete folder: " + folderPath + " | Error: " + e.getMessage());
            serviceOutcome.setOutcome(false);
            serviceOutcome.setMessage("Failed to delete folder.");
        }

        return serviceOutcome;
    }


}
