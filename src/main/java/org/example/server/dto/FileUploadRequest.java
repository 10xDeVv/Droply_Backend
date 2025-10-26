package org.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileUploadRequest {

    @NotBlank
    private String roomId;

    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;

    private long fileSize;
}
