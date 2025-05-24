package org.example.facerec02.Entity;

import jakarta.persistence.*;


@Entity
public class FaceRecognitionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // 外键关联到User表
    private User user;  // 关联的用户

    private String imageUrl;
    private boolean isGenuine;
    private double confidence;

    private String createTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;  // 设置关联的User对象
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isGenuine() {
        return isGenuine;
    }

    public void setGenuine(boolean genuine) {
        isGenuine = genuine;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}

