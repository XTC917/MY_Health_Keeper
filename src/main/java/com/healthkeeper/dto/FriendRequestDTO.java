package com.healthkeeper.dto;

import lombok.Data;

@Data
public class FriendRequestDTO {
    private Long id;
    private String senderUsername;
    private String senderAvatar;
    private String receiverUsername;
    private String receiverAvatar;
    private String status;

    public FriendRequestDTO(Long id, String senderUsername, String senderAvatar,
                            String receiverUsername, String receiverAvatar, String status) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.senderAvatar = senderAvatar;
        this.receiverUsername = receiverUsername;
        this.receiverAvatar = receiverAvatar;
        this.status = status;
    }
}
