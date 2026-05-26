package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReviewView {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private String reviewerName;

    public ReviewView() {
    }

    public ReviewView(String id, int rating, String comment, LocalDateTime createdAt, String reviewerName) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.reviewerName = reviewerName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getFormattedCreatedAt() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_FORMAT);
    }
}
