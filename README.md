# Support Desk

A support ticket system backend. Customers open tickets and comment on them. Agents pick tickets up and move them through a status workflow.

## Tech stack

- Java 21, Spring Boot
- Spring Security with JWT login and BCrypt password hashing
- Spring Data JPA with MySQL 8.4
- Docker and Docker Compose

## Run it

You only need Docker Desktop.

    docker compose up -d --build

The API is then available at http://localhost:8080. The first build downloads dependencies and takes a few minutes.

Stop it with `docker compose down`. Your data stays in a Docker volume. Use `docker compose down -v` to wipe it.

Before using it for real, change `JWT_SECRET` and the database password in `docker-compose.yml`.

## Roles

- CUSTOMER (default): can create tickets, see only their own tickets, and comment on them.
- AGENT and ADMIN: can see all tickets, assign them, change their status, and comment.

New users are customers. To promote one to agent:

    docker compose exec db mysql -uroot -proot supportdesk -e "UPDATE users SET role='AGENT' WHERE email='agent@mail.com';"

Log in again afterwards, because the role is stored inside the token.

## Ticket status rules

- OPEN can go to IN_PROGRESS or CLOSED
- IN_PROGRESS can go to RESOLVED or OPEN
- RESOLVED can go to CLOSED or IN_PROGRESS
- CLOSED is final

Any other move returns a 400 error.

## API

All endpoints except register and login need the header `Authorization: Bearer <token>`.

| Method | Path | Who | What |
|--------|------|-----|------|
| POST | /api/auth/register | anyone | create an account |
| POST | /api/auth/login | anyone | get a token |
| GET | /api/me | logged in | show the current user |
| POST | /api/tickets | logged in | create a ticket |
| GET | /api/tickets | logged in | list tickets (own for customers, all for staff) |
| GET | /api/tickets/{id} | owner or staff | get one ticket |
| PATCH | /api/tickets/{id}/assign | staff | assign the ticket to yourself |
| PATCH | /api/tickets/{id}/status | staff | change status, body: {"status":"IN_PROGRESS"} |
| POST | /api/tickets/{id}/comments | owner or staff | add a comment, body: {"body":"text"} |
| GET | /api/tickets/{id}/comments | owner or staff | list comments, oldest first |

## Quick example

    curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"name":"Test","email":"test@mail.com","password":"secret123"}'

    curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"test@mail.com","password":"secret123"}'

## Project layout

    backend/src/main/java/com/supportdesk/
        auth/      users, login, JWT, security config
        ticket/    tickets, status workflow
        comment/   ticket comments
    docker-compose.yml   MySQL and backend containers
    backend/Dockerfile   builds the Spring Boot jar
