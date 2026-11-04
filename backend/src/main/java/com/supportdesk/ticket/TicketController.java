package com.supportdesk.ticket;

import com.supportdesk.auth.User;
import com.supportdesk.auth.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository tickets;
    private final UserRepository users;

    public TicketController(TicketRepository tickets, UserRepository users) {
        this.tickets = tickets;
        this.users = users;
    }

    private boolean isStaff(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_AGENT") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    private User currentUser(Authentication auth) {
        return users.findByEmail(auth.getName()).orElseThrow();
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest req,
                                                 Authentication auth) {
        Ticket t = new Ticket();
        t.setTitle(req.title());
        t.setDescription(req.description());
        if (req.priority() != null) {
            t.setPriority(req.priority());
        }
        t.setCreatedBy(currentUser(auth));
        return ResponseEntity.status(201).body(TicketResponse.from(tickets.save(t)));
    }

    @GetMapping
    public List<TicketResponse> list(Authentication auth) {
        List<Ticket> result = isStaff(auth)
                ? tickets.findAll()
                : tickets.findByCreatedBy(currentUser(auth));
        return result.stream().map(TicketResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, Authentication auth) {
        var found = tickets.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Ticket not found"));
        }
        Ticket t = found.get();
        if (!isStaff(auth) && !t.getCreatedBy().getEmail().equals(auth.getName())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your ticket"));
        }
        return ResponseEntity.ok(TicketResponse.from(t));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id,
                                          @Valid @RequestBody StatusRequest req,
                                          Authentication auth) {
        if (!isStaff(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only agents can change status"));
        }
        var found = tickets.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Ticket not found"));
        }
        Ticket t = found.get();
        if (!t.getStatus().canMoveTo(req.status())) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "Cannot move ticket from " + t.getStatus() + " to " + req.status()));
        }
        t.setStatus(req.status());
        t.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(TicketResponse.from(tickets.save(t)));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<?> assignToMe(@PathVariable Long id, Authentication auth) {
        if (!isStaff(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only agents can assign tickets"));
        }
        var found = tickets.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Ticket not found"));
        }
        Ticket t = found.get();
        t.setAssignedTo(currentUser(auth));
        t.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(TicketResponse.from(tickets.save(t)));
    }
}
