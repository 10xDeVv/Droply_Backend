package org.example.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.server.config.SupabaseProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.MediaType;
import org.example.server.dto.SignedUploadResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseStorageService {

    private final SupabaseProperties properties;
    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init(){
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }


    public SignedUploadResponse createSignedUploadUrl(String roomId, String fileName, String contentType) throws IOException {
        String objectPath = String.format("%s/%s/%s", roomId, UUID.randomUUID().toString().substring(0, 8) ,fileName);


        String url = String.format("%s/storage/v1/object/upload/sign/%s/%s",
                properties.getUrl(),
                properties.getBucketName(),
                objectPath);

        String jsonBody = "{\"expiresIn\": 300}";


        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getServiceKey())
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try(Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                String error = response.body() != null ? response.body().string() : "Unknown";
                throw new IOException("Failed to create signed upload url: " + error);
            }

            String responseBody = response.body().string();
            JsonNode node = objectMapper.readTree(responseBody);
            String signedUrl = node.get("url").asText();
            String token = node.has("token") ? node.get("token").asText() : null;

            String fullSignedUrl = String.format("%s/storage/v1/%s", properties.getUrl(), signedUrl);

            log.info("Created signed upload url for: {} (expires in 5 min)", fileName);

            return SignedUploadResponse.builder()
                    .signedUrl(fullSignedUrl)
                    .objectPath(objectPath)
                    .token(token)
                    .expiresIn(300)
                    .build();
        }
    }

    public String createSignedDownloadUrl(String objectPath, int expiresInSeconds) throws IOException {
        String url = String.format("%s/storage/v1/object/sign/%s/%s",
                properties.getUrl(),
                properties.getBucketName(),
                objectPath);

        String jsonBody = String.format("{\"expiresIn\":%d}", expiresInSeconds);

        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getServiceKey())
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try(Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                String error = response.body() != null ? response.body().string() : "Unknown";
                throw new IOException("Failed to generate signed download URL: " + error);
            }

            String responseBody = response.body().string();
            JsonNode node = objectMapper.readTree(responseBody);
            String signedPath = node.get("signedURL").asText();

            String fileName = objectPath.substring(objectPath.lastIndexOf('/') + 1);
            String downloadParam = "&download=" + java.net.URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8);

            // Build full URL and append &download=<filename> to force attachment
            String fullSignedUrl = properties.getUrl() + "/storage/v1" + signedPath + downloadParam;

            log.info("Created signed download URL (expires in {} sec)", expiresInSeconds);
            return fullSignedUrl;
        }
    }

    public void deleteFile(String objectPath) throws IOException{
        String url = String.format("%s/storage/v1/object/%s/%s",
                properties.getUrl(),
                properties.getBucketName(),
                objectPath);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getServiceKey())
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to delete file: " + objectPath);
            }
            log.info("Deleted: {}", objectPath);
        }
    }

    public void deleteRoomFiles(String roomId) {
        String listUrl = String.format("%s/storage/v1/object/list/%s?prefix=%s/",
                properties.getUrl(),
                properties.getBucketName(),
                roomId);

        try {
            Request request = new Request.Builder()
                    .url(listUrl)
                    .header("Authorization", "Bearer " + properties.getServiceKey())
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonNode files = objectMapper.readTree(responseBody);

                    for (JsonNode file : files) {
                        String filePath = roomId + "/" + file.get("name").asText();
                        deleteFile(filePath);
                    }

                    log.info("Deleted all files for room: {}", roomId);
                }
            }
        } catch (IOException e) {
            log.error("Failed to delete room files: {}", roomId, e);
        }
    }
}
