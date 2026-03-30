# AI Assistant - Ktor Web Application

A full-stack web application built with **Ktor** (Kotlin) and **HTMX** for managing RAG (Retrieval-Augmented Generation) documents and interacting with LLMs through a Q&A interface with an admin approval workflow.

## Features

- **Authentication & Authorization**: Role-based access control (Admin / User)
- **RAG Document Management**: CRUD operations for RAG documents (Admin only)
- **Q&A Interface**: Ask questions to LLMs with configurable model name and API key
- **AI Proposal Workflow**: AI proposes steps → Admin approves → Execute → Logged
- **Execution Logs**: Full audit trail of all executed proposals
- **HTMX Frontend**: Dynamic, server-rendered UI with HTMX for seamless interactions

## Tech Stack

- **Backend**: Ktor 2.3.7 (Kotlin)
- **Frontend**: HTMX 1.9.10 + kotlinx.html (server-side rendering)
- **Database**: H2 (development) / PostgreSQL (production)
- **ORM**: Exposed (JetBrains)
- **Authentication**: Session-based with BCrypt password hashing

## Database Schema

| Table | Description |
|-------|-------------|
| `users` | User accounts with roles (ADMIN/USER) |
| `rag_documents` | RAG documents with domain, text, vector, metadata |
| `ai_proposals` | AI-generated proposals with status workflow |
| `execution_logs` | Audit log of all executed proposals |

## Getting Started

### Prerequisites
- JDK 17+
- (Optional) PostgreSQL for production

### Run
```bash
./gradlew run
```

The application starts at **http://localhost:8080**

### Default Accounts
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

### Configuration

Edit `src/main/resources/application.conf` to configure:
- **Port**: Default 8080
- **Database**: Default H2 in-memory; set `JDBC_URL` env var for PostgreSQL

### PostgreSQL Setup
```sql
-- For production, use PostgreSQL with pgvector extension for vector search
CREATE EXTENSION IF NOT EXISTS vector;
```

Set environment variable:
```bash
export JDBC_URL="jdbc:postgresql://localhost:5432/demo"
```

## User Roles

### Admin
- CRUD RAG documents
- Approve/reject AI proposals
- Execute approved proposals
- View all logs and proposals

### User
- Ask questions via Q&A interface
- View AI proposals and their status
- View execution logs
