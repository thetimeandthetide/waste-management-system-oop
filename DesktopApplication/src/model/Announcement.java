package model;

import java.time.LocalDateTime;

public class Announcement {
    private final int id;
    private final String title;
    private final String message;
    private final String author;
    private final LocalDateTime createdAt;
    private final boolean read;

    public Announcement(int id, String title, String message, String author,
                        LocalDateTime createdAt, boolean read) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.author = author;
        this.createdAt = createdAt;
        this.read = read;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
}
