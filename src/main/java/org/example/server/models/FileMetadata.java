package org.example.server.models;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class FileMetadata {
    private String id;
    private String roomId;
    private String name;
    private String storagePath;
    private String contentType;
    private long size;
    private FileStatus status;
    private LocalDateTime uploadedAt;
    private boolean downloaded;
}
