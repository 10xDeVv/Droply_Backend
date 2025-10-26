package org.example.server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRoomResponse {
    private String roomId;
    private String code;
    private String qrCodeData;
    private Long expiresIn;
}
