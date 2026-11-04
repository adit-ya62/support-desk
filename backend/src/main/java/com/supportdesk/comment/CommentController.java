package com.supportdesk.comment;

import com.supportdesk.auth.User;
import com.supportdesk.auth.UserRepository;
import com.supportdesk.ticket.Ticket;
import com.supportdesk.ticket.TicketRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentRepository comments;
    private final TicketRepository tickets;
    private final UserRepository users;

    public CommentController(CommentRepository comments, TicketRepository tickets, UserRepository users) {
        this.comments = comments;
        this.tickets = tickets;
        this.users = users;
    }

    private boolean isStaff(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_AGENT") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    @PostMapping
    public ResponseEntity<?> add(@PathVariable Long ticketId,
                                 @Valid @RequestBody CommentRequest req,
                                 Authentication auth) {
        var found = tickets.findById(ticketId);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Ticket not found"));
        }
        Ticket ticket = found.get();
        if (!isStaff(auth) && !ticket.getCreatedBy().getEmail().equals(auth.getName())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your ticket"));
        }
        User author = users.findByEmail(auth.getName()).orElseThrow();
        Comment c = new Comment();
        c.setTicket(ticket);
        c.setAuthor(author);
        c.setBody(req.body());
        return ResponseEntity.status(201).body(CommentResponse.from(comments.save(c)));
    }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long ticketId, Authentication auth) {
        var found = tickets.findById(ticketId);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Ticket not found"));
        }
        Ticket ticket = found.get();
        if (!isStaff(auth) && !ticket.getCreatedBy().getEmail().equals(auth.getName())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your ticket"));
        }
        return ResponseEntity.ok(comments.findByTicketOrderByCreatedAtAsc(ticket)
                .stream().map(CommentResponse::from).toList());
    }
}