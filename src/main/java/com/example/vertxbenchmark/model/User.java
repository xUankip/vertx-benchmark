package com.example.vertxbenchmark.model;

import io.vertx.sqlclient.Row;
import java.time.LocalDateTime;

public class User {
    private Integer id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Factory method to create User from database Row
    public static User fromRow(Row row) {
        User user = new User();
        user.setId(row.getInteger("id"));
        user.setName(row.getString("name"));
        user.setEmail(row.getString("email"));
        user.setCreatedAt(row.getLocalDateTime("created_at"));
        return user;
    }
}