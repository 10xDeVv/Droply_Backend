package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.DownloadUrlResponse;
import org.example.server.dto.FileUploadConfirmRequest;
import org.example.server.dto.FileUploadRequest;
import org.example.server.dto.SignedUploadResponse;
import org.example.server.models.FileMetadata;
import org.example.server.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;



    @PostMapping("/request-upload")
    public ResponseEntity<SignedUploadResponse> requestUploadUrl(
            @Valid @RequestBody FileUploadRequest request
    ){
        try {
            SignedUploadResponse response = fileService.requestUploadUrl(request);
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }catch (Exception e){
            log.error("Failed to request upload url", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/confirm-upload")
    public ResponseEntity<FileMetadata> confirmUpload(
            @Valid @RequestBody FileUploadConfirmRequest request
    ){
        try {
            FileMetadata metadata = fileService.confirmUpload(request);
            return ResponseEntity.ok(metadata);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }catch (Exception e){
            log.error("Failed to confirm upload", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{roomId}/{fileId}")
    public ResponseEntity<DownloadUrlResponse> getDownloadUrl (
            @PathVariable String roomId,
            @PathVariable String fileId
    ) {
        try {
            DownloadUrlResponse response = fileService.getDownloadUrl(roomId, fileId);
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }catch (Exception e){
            log.error("Failed to get download url", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}





