package org.example.server.models;

public enum MessageType {
    FILE_UPLOAD_STARTED,
    FILE_UPLOADED,

    FILE_DOWNLOAD_STARTED,
    FILE_DOWNLOADED,

    FILE_DELETED,

    ROOM_CREATED,
    ROOM_EXPIRED,
    ROOM_CLOSED,
    ROOM_JOINED,
    PEER_JOINED,
    PEER_LEFT,
}
