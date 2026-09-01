package com.paulo.independentschooldata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrevoEventDto {
    private String event;
    private String email;
    private String subject;
    private String reason;
    private String uuid;

    // Webhook ID (stays the same for every call from this webhook)
    private Long id;

    private Long ts;
    @JsonProperty("sender_email")
    private String senderEmail;

    @JsonProperty("message-id")
    private String messageId;

    @JsonProperty("ts_event")
    private Long tsEvent;

    @JsonProperty("sending_ip")
    private String sendingIp;

    @JsonProperty("template_id")
    private Integer templateId;

    // Unix timestamp in milliseconds
    @JsonProperty("ts_epoch")
    private Long tsEpoch;
}