package com.ptithcm.smartshop.review.dto;

import java.time.Instant;
import java.util.UUID;

public class ReviewResponse {
    private UUID id;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private String userName;
    private UUID orderId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
}
