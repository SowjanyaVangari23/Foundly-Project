package com.foundly.app2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Integer transactionId;
    private String title;
    private String message;
    private String type; // CLAIM or HANDOVER
    private String time; // e.g., "2h ago"
    private String description;
    private String photo;
    private String pickupMessage;
    private String securityId;
    private String securityName;
    private String itemStatus;
    private boolean reporterCompleted;
    private boolean requesterCompleted;
    private String transactionStatus;

    //private String DateUpdated;// FROM: tx.getItem().getStatus()
}