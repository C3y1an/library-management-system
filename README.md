# Library Management System

This repository contains a library management system with a Spring Boot backend and a Vue/Vite frontend.

## Project Structure

- `library-management/` - Spring Boot backend API.
- `library-management-web/` - Vue 3 frontend.

The `word/` directory is local documentation/material and is intentionally excluded from Git.

## Development

Backend:

```bash
cd library-management
./mvnw spring-boot:run
```

Frontend:

```bash
cd library-management-web
npm install
npm run dev
```

## Build

Backend:

```bash
cd library-management
./mvnw test
```

Frontend:

```bash
cd library-management-web
npm run build
```
