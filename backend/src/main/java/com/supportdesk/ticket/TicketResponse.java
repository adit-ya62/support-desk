package com.supportdesk.ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        String createdBy,
        String assignedTo,
        LocalDateTime createdAt) {

    public static TicketResponse from(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getCreatedBy().getEmail(),
                t.getAssignedTo() == null ? null : t.getAssignedTo().getEmail(),
                t.getCreatedAt());
    }
}
