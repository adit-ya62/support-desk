package com.supportdesk.comment;

import com.supportdesk.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}