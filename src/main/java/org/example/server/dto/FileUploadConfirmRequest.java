package org.example.server.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileUploadConfirmRequest {
    private String roomId;
    private String fileName;
    private String objectPath;
    private String contentType;
    private long fileSize;
}
