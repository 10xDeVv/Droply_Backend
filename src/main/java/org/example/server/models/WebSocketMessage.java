package org.example.server.models;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessage {
    private MessageType type;
    private String roomId;
    private Object payload;
}
