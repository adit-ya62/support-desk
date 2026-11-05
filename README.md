# Support Desk

Backend for a support ticket system. Customers open tickets and comment on them. Agents take tickets, reply, and move them through a status workflow.

## Features

- Register and login with JWT tokens (valid for 24 hours). Passwords are stored as BCrypt hashes.
- Three roles: customer, agent and admin.
- Tickets with a title, description, priority, status, owner and assigned agent.
- A status workflow that rejects invalid moves.
- Comments on tickets, listed oldest first.
- Customers only see their own tickets and comments. Agents and admins see everything.
- Starts with one command using Docker Compose.

## Tech stack

Java 21, Spring Boot 4, Spring Security with JWT, Spring Data JPA, MySQL 8.4, Maven, Docker and Docker Compose.

## Quick start with Docker

You only need Docker Desktop.

    git clone https://github.com/adit-ya62/support-desk.git
    cd support-desk
    docker compose up -d --build

The API runs at http://localhost:8080. The first build downloads dependencies and takes a few minutes. Follow the log with `docker compose logs -f backend`.

Stop it with `docker compose down`. Your data stays in a Docker volume. Use `docker compose down -v` to delete the data too.

## Run without Docker

You need Java 21, Maven and a local MySQL server.

    mysql -u root -e "CREATE DATABASE supportdesk;"
    cd backend
    mvn spring-boot:run

By default it connects to MySQL on localhost:3306 as `root` with an empty password. Change this in `backend/src/main/resources/application.properties`.

## Configuration

Docker Compose passes these environment variables to the backend:

| Variable | Purpose |
|----------|---------|
| SPRING_DATASOURCE_URL | JDBC address of the database |
| SPRING_DATASOURCE_USERNAME | database user |
| SPRING_DATASOURCE_PASSWORD | database password |
| JWT_SECRET | secret used to sign tokens, at least 32 characters |

The values in `docker-compose.yml` are examples for local use. Change `JWT_SECRET` and the database password before running this anywhere public.

## Roles

New accounts are customers.

- CUSTOMER: create tickets, see and comment on their own tickets.
- AGENT and ADMIN: see all tickets, assign them, change their status and comment.

To make a user an agent when running with Docker:

    docker compose exec db mysql -uroot -proot supportdesk -e "UPDATE users SET role='AGENT' WHERE email='agent@mail.com';"

Log in again afterwards, because the role is stored inside the token.

## Ticket status rules

- OPEN can go to IN_PROGRESS or CLOSED
- IN_PROGRESS can go to RESOLVED or OPEN
- RESOLVED can go to CLOSED or IN_PROGRESS
- CLOSED is final

Any other move is refused with a 400 error. Priority is LOW, MEDIUM or HIGH (default MEDIUM).

## API

Every endpoint except register and login needs the header `Authorization: Bearer <token>`.

| Method | Path | Who | What |
|--------|------|-----|------|
| POST | /api/auth/register | anyone | create an account (name, email, password of 6+ characters) |
| POST | /api/auth/login | anyone | returns a token and the role |
| GET | /api/me | logged in | the current user |
| POST | /api/tickets | logged in | create a ticket (title, description, priority) |
| GET | /api/tickets | logged in | list tickets (own for customers, all for staff) |
| GET | /api/tickets/{id} | owner or staff | get one ticket |
| PATCH | /api/tickets/{id}/assign | staff | assign the ticket to yourself |
| PATCH | /api/tickets/{id}/status | staff | change status, body {"status":"IN_PROGRESS"} |
| POST | /api/tickets/{id}/comments | owner or staff | add a comment, body {"body":"text"} |
| GET | /api/tickets/{id}/comments | owner or staff | list comments |

Error codes: 400 invalid input or status move, 401 missing or wrong login, 403 not allowed, 404 not found, 409 email already registered.

## Try it

    curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"name":"Test","email":"test@mail.com","password":"secret123"}'

    curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"test@mail.com","password":"secret123"}'

Copy the token from the login response, then:

    curl -X POST http://localhost:8080/api/tickets -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN" -d '{"title":"Cannot login","description":"Reset email never arrives","priority":"HIGH"}'

    curl http://localhost:8080/api/tickets -H "Authorization: Bearer TOKEN"

## Project layout

    backend/src/main/java/com/supportdesk/
        auth/       users, login, JWT, security config
        ticket/     tickets and the status workflow
        comment/    ticket comments
    backend/Dockerfile   builds the Spring Boot jar
    docker-compose.yml   MySQL and backend containers

## Possible improvements

- Automated tests with JUnit
- API docs with Swagger/OpenAPI
- Email notifications when a ticket changes
- Reply suggestions using an AI API

## License

MIT, see the LICENSE file.
