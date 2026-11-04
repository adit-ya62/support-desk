package com.supportdesk.comment;

import java.time.LocalDateTime;

public record CommentResponse(Long id, String author, String role, String body, LocalDateTime createdAt) {

    public static CommentResponse from(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getAuthor().getEmail(),
                c.getAuthor().getRole().name(),
                c.getBody(),
                c.getCreatedAt());
    }
}