package com.supportdesk.ticket;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
        @NotBlank String title,
        @NotBlank String description,
        TicketPriority priority) {
}
