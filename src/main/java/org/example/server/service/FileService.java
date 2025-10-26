package org.example.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.DownloadUrlResponse;
import org.example.server.dto.FileUploadConfirmRequest;
import org.example.server.dto.FileUploadRequest;
import org.example.server.dto.SignedUploadResponse;
import org.example.server.models.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final SupabaseStorageService storageService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;


    public SignedUploadResponse requestUploadUrl(FileUploadRequest request){

        RoomSession room = roomService.getRoom(request.getRoomId())
                .filter(r -> !r.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));


        try{
            SignedUploadResponse response = storageService.createSignedUploadUrl(
                    request.getRoomId(),
                    request.getFileName(),
                    request.getContentType()
            );

            log.info("Issued upload URL for: {} in room: {}", request.getFileName(), request.getRoomId());

            return response;
        }catch (IOException e) {
            log.error("Failed to create signed upload URL", e);
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    public FileMetadata confirmUpload(FileUploadConfirmRequest request){
        RoomSession room = roomService.getRoom(request.getRoomId())
                 .orElseThrow(() -> new IllegalArgumentException("Room not found"));


        FileMetadata metadata = FileMetadata.builder()
                .id(UUID.randomUUID().toString())
                .roomId(request.getRoomId())
                .name(request.getFileName())
                .storagePath(request.getObjectPath())
                .contentType(request.getContentType())
                .size(request.getFileSize())
                .status(FileStatus.UPLOADED)
                .uploadedAt(LocalDateTime.now())
                .downloaded(false)
                .build();

        roomService.addFileToRoom(request.getRoomId(), metadata);

        notifyFileUploaded(request.getRoomId(), metadata);

        log.info("Upload confirmed: {} ({} bytes)",
                metadata.getName(), metadata.getSize());

        return metadata;


    }

    public DownloadUrlResponse getDownloadUrl(String roomId, String fileId){
        RoomSession room = roomService.getRoom(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        FileMetadata data = room.getFiles().stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File not found"));


        try{
            String downloadUrl = storageService.createSignedDownloadUrl(
                    data.getStoragePath(),
                    3600
            );

            roomService.markFileAsDownloaded(roomId, fileId);

            notifyFileDownloaded(roomId, data);

            log.info("Download URL created for: {}", data.getName());

            return DownloadUrlResponse.builder()
                    .downloadUrl(downloadUrl)
                    .expiresIn(3600)
                    .fileName(data.getName())
                    .build();
        }catch (Exception e){
            log.error("Failed to create download URL", e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    private void notifyFileUploaded(String roomId, FileMetadata metadata){
        WebSocketMessage message = WebSocketMessage.builder()
                .type(MessageType.FILE_UPLOADED)
                .roomId(roomId)
                .payload(metadata)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }

    private void notifyFileDownloaded(String roomId, FileMetadata metadata){
        WebSocketMessage message = WebSocketMessage.builder()
                .type(MessageType.FILE_DOWNLOADED)
                .roomId(roomId)
                .payload(metadata)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }
}
