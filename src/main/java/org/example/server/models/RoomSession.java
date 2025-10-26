package org.example.server.models;

import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSession {
    private String id;
    private String code;
    private String secret;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean connected;
    private String receiverSessionId;
    private String senderSessionId;
    private boolean autoDownload;

    @Builder.Default
    private List<FileMetadata> files = new ArrayList<>();

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void addFile(FileMetadata file){
        this.files.add(file);
    }

    public long getRemainingSeconds(){
        return Duration.between(
                LocalDateTime.now(),
                expiresAt
        ).getSeconds();
    }
}
