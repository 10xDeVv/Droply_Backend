package org.example.server.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DownloadUrlResponse  {
    private String downloadUrl;
    private String fileName;
    private long expiresIn;
}
