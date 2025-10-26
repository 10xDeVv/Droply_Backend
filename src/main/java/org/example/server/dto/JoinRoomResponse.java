package org.example.server.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinRoomResponse  {
    private String roomId;
    private boolean success;
    private String message;
}
