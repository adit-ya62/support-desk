package com.supportdesk.ticket;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull TicketStatus status) {
}
