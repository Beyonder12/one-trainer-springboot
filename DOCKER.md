# Dockerfile Instructions Reference

A comprehensive guide to understanding Dockerfile commands and best practices.

---

## Table of Contents

- [WORKDIR](#workdir)
- [RUN](#run)
- [CMD](#cmd)
- [ENTRYPOINT](#entrypoint)
- [RUN vs CMD vs ENTRYPOINT](#run-vs-cmd-vs-entrypoint)
- [Other Important Instructions](#other-important-instructions)
    - [COPY](#copy)
    - [ADD](#add)
    - [EXPOSE](#expose)
    - [ENV](#env)
    - [ARG](#arg)
- [Common Patterns](#common-patterns)
- [Best Practices](#best-practices)

---

## WORKDIR

Sets the working directory for subsequent instructions in the Dockerfile.

### Syntax
```dockerfile
WORKDIR /path/to/directory
```

### Behavior
- Creates the directory if it doesn't exist
- All subsequent `RUN`, `CMD`, `COPY`, `ADD`, and `ENTRYPOINT` commands execute from this location
- Multiple `WORKDIR` commands are cumulative

### Examples
```dockerfile
WORKDIR /app
# Now at /app

WORKDIR backend
# Now at /app/backend

RUN pwd  # Output: /app/backend
```

### Best Practices
- Use absolute paths for clarity
- Set early in Dockerfile to establish context
- Avoid using `RUN cd /path` - use `WORKDIR` instead

---

## RUN

Executes commands **during image build time** and creates a new layer in the image.

### Syntax
```dockerfile
# Shell form
RUN <command>

# Exec form (preferred)
RUN ["executable", "param1", "param2"]
```

### When It Executes
- **Build time** - when you run `docker build`
- Results are committed to the image as a new layer

### Examples
```dockerfile
# Installing packages
RUN apt-get update

# Multiple commands (creates multiple layers - not optimal)
RUN apt-get update
RUN apt-get install -y curl
RUN apt-get clean

# Better: combine to reduce layers
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Building application
RUN ./mvnw clean package -DskipTests

# Creating files
RUN echo "Hello World" > /app/hello.txt
```

### Common Uses
- Installing system packages
- Building/compiling applications
- Creating directories and files
- Downloading dependencies
- Setting up environment

### Best Practices
- Chain commands with `&&` to reduce layers
- Clean up in the same `RUN` command
- Use `\` for multi-line commands for readability

---

## CMD

Provides the **default command** to execute when a container starts (runtime).

### Syntax
```dockerfile
# Exec form (preferred) - no shell processing
CMD ["executable", "param1", "param2"]

# Shell form - runs in /bin/sh -c
CMD command param1 param2

# As default parameters to ENTRYPOINT
CMD ["param1", "param2"]
```

### When It Executes
- **Runtime** - when you run `docker run`
- Only the **last** `CMD` in a Dockerfile takes effect

### Can Be Overridden
```bash
# Dockerfile has: CMD ["java", "-jar", "app.jar"]

# Use default CMD
docker run myimage
# → Runs: java -jar app.jar

# Override CMD completely
docker run myimage echo "Hello World"
# → Runs: echo "Hello World"
```

### Examples
```dockerfile
# Running a Java application
CMD ["java", "-jar", "app.jar"]

# Starting a web server
CMD ["nginx", "-g", "daemon off;"]

# Running with environment variable
CMD ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "app.jar"]

# Shell form (not recommended for production)
CMD java -jar app.jar
```

### Best Practices
- Use exec form `["cmd", "arg"]` to avoid shell processing
- Provide sensible defaults that users can easily override
- Only one `CMD` per Dockerfile

---

## ENTRYPOINT

Sets the **main executable** that will always run when the container starts.

### Syntax
```dockerfile
# Exec form (preferred)
ENTRYPOINT ["executable", "param1", "param2"]

# Shell form
ENTRYPOINT command param1 param2
```

### When It Executes
- **Runtime** - when you run `docker run`
- More difficult to override than `CMD` (requires `--entrypoint` flag)

### Key Difference from CMD
- `ENTRYPOINT` defines the **fixed executable**
- `CMD` provides **default arguments** to `ENTRYPOINT`
- They work together!

### Examples
```dockerfile
# Basic usage
ENTRYPOINT ["java", "-jar", "app.jar"]

# Combined with CMD for default arguments
ENTRYPOINT ["java", "-jar"]
CMD ["app.jar"]

# Docker run examples:
# docker run myimage          → java -jar app.jar
# docker run myimage test.jar → java -jar test.jar

# With JVM options
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]

# Script as entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["app.jar"]
```

### Overriding ENTRYPOINT
```bash
# Override with --entrypoint flag
docker run --entrypoint /bin/bash myimage
```

### Best Practices
- Use for the main application executable
- Combine with `CMD` for flexible default arguments
- Use exec form to properly handle signals (SIGTERM, etc.)

---

## RUN vs CMD vs ENTRYPOINT

| Instruction | Execution Time | Can Override | Primary Use Case | Example |
|-------------|----------------|--------------|------------------|---------|
| **RUN** | **Build time** | No (part of image) | Install packages, build code, setup environment | `RUN apt-get install curl` |
| **CMD** | **Runtime** | Yes (easy) | Default command/arguments that users might change | `CMD ["app.jar"]` |
| **ENTRYPOINT** | **Runtime** | Hard (needs `--entrypoint`) | Main application executable | `ENTRYPOINT ["java", "-jar"]` |

### Comparison Example
```dockerfile
FROM ubuntu:22.04

# RUN - executed during build
RUN apt-get update && apt-get install -y python3
RUN pip3 install flask

# ENTRYPOINT - main executable at runtime
ENTRYPOINT ["python3"]

# CMD - default arguments at runtime
CMD ["app.py"]

# Result when running:
# docker run myimage           → python3 app.py
# docker run myimage test.py   → python3 test.py
```

### Decision Guide
- **Installing software?** → Use `RUN`
- **Need users to easily change behavior?** → Use `CMD`
- **Want to lock the main executable?** → Use `ENTRYPOINT`
- **Both ENTRYPOINT + CMD?** → Maximum flexibility!

---

## Other Important Instructions

### COPY

Copies files and directories from the host to the image.

#### Syntax
```dockerfile
COPY <src>... <dest>
COPY ["<src>",... "<dest>"]  # Use for paths with whitespace
```

#### Examples
```dockerfile
# Copy single file
COPY package.json /app/

# Copy directory
COPY src/ /app/src/

# Copy multiple files
COPY file1.txt file2.txt /app/

# Copy with pattern
COPY *.jar /app/

# Copy everything from current directory
COPY . /app/

# Copy from build stage (multi-stage builds)
COPY --from=builder /app/target/*.jar app.jar

# Copy with ownership
COPY --chown=user:group files* /app/
```

#### Best Practices
- Copy only what you need to reduce image size
- Copy dependencies before source code for better layer caching
- Use `.dockerignore` to exclude unnecessary files

---

### ADD

Similar to `COPY` but with additional features.

#### Syntax
```dockerfile
ADD <src>... <dest>
```

#### Extra Features
1. **Auto-extracts** tar archives
2. Can download files from **URLs**

#### Examples
```dockerfile
# Auto-extract tar file
ADD archive.tar.gz /app/
# Extracts contents to /app/

# Download from URL
ADD https://example.com/file.zip /app/

# Copy like COPY (not recommended)
ADD myfile.txt /app/
```

#### COPY vs ADD
| Feature | COPY | ADD |
|---------|------|-----|
| Copy files | ✅ | ✅ |
| Auto-extract tar | ❌ | ✅ |
| Download URLs | ❌ | ✅ |
| Recommended | ✅ (preferred) | ⚠️ (only when needed) |

#### Best Practice
**Use `COPY` unless you specifically need `ADD`'s special features.**

---

### EXPOSE

Documents which ports the container listens on at runtime.

#### Syntax
```dockerfile
EXPOSE <port> [<port>/<protocol>...]
```

#### Important
- **Metadata only** - doesn't actually publish the port
- Must use `-p` or `-P` flag with `docker run` to publish ports

#### Examples
```dockerfile
# Single port
EXPOSE 8080

# Multiple ports
EXPOSE 8080 8443

# With protocol
EXPOSE 80/tcp
EXPOSE 53/udp

# Port range
EXPOSE 8000-8010
```

#### Publishing Ports at Runtime
```bash
# Publish specific port
docker run -p 8080:8080 myimage

# Publish all EXPOSE'd ports to random host ports
docker run -P myimage

# Bind to specific interface
docker run -p 127.0.0.1:8080:8080 myimage
```

---

### ENV

Sets environment variables available during build and at runtime.

#### Syntax
```dockerfile
ENV <key>=<value> ...
ENV <key> <value>
```

#### Examples
```dockerfile
# Single variable
ENV NODE_ENV=production

# Multiple variables (one line)
ENV DATABASE_HOST=localhost \
    DATABASE_PORT=5432 \
    DATABASE_NAME=myapp

# Used in subsequent instructions
ENV APP_HOME=/app
WORKDIR $APP_HOME

# Runtime usage
ENV SPRING_PROFILES_ACTIVE=prod
```

#### Accessing in Application
```bash
# In container
echo $DATABASE_HOST
# Output: localhost
```

#### Override at Runtime
```bash
docker run -e DATABASE_HOST=prod-db myimage
docker run --env-file .env myimage
```

#### Best Practices
- Use for configuration that might change between environments
- Avoid hardcoding secrets (use Docker secrets or external config)
- Group related variables together

---

### ARG

Defines build-time variables (not available at runtime).

#### Syntax
```dockerfile
ARG <name>[=<default value>]
```

#### Examples
```dockerfile
# With default value
ARG VERSION=1.0.0
ARG BUILD_DATE

# Using ARG
RUN echo "Building version ${VERSION}"

# ARG before FROM (special case)
ARG BASE_IMAGE=eclipse-temurin:21-jdk
FROM ${BASE_IMAGE}

# ARG in multi-stage builds
FROM builder AS build
ARG MAVEN_OPTS="-Xmx1024m"
RUN ./mvnw package ${MAVEN_OPTS}
```

#### Providing ARG at Build Time
```bash
docker build --build-arg VERSION=2.0.0 .
docker build --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') .
```

#### ARG vs ENV
| Feature | ARG | ENV |
|---------|-----|-----|
| Available during build | ✅ | ✅ |
| Available at runtime | ❌ | ✅ |
| Can set at build time | ✅ | ❌ (only runtime) |
| Use case | Build configuration | Runtime configuration |

#### Example: ARG → ENV Pattern
```dockerfile
# Accept build arg
ARG APP_VERSION=1.0.0

# Make it available at runtime
ENV VERSION=${APP_VERSION}
```

---

## Common Patterns

### Pattern 1: Basic Spring Boot Application
```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Pattern 2: Optimized with Layer Caching
```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Dependencies layer (cached unless pom.xml changes)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Build layer (only runs when source changes)
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Pattern 3: Production-Ready with Security
```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Security: Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy artifact
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# JVM optimization flags
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

### Pattern 4: Multi-Service with Arguments
```dockerfile
FROM node:18 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:18-slim
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
EXPOSE 3000

# Flexible entrypoint
ENTRYPOINT ["node"]
CMD ["dist/index.js"]

# Run examples:
# docker run myapp                    → node dist/index.js
# docker run myapp dist/worker.js     → node dist/worker.js
```

---

## Best Practices

### 1. Layer Optimization
```dockerfile
# ❌ Bad - Multiple layers
RUN apt-get update
RUN apt-get install -y curl
RUN apt-get install -y git
RUN apt-get clean

# ✅ Good - Single layer with cleanup
RUN apt-get update && \
    apt-get install -y curl git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
```

### 2. Leverage Build Cache
```dockerfile
# ✅ Good - Dependencies first (changes less frequently)
COPY package.json package-lock.json ./
RUN npm install
COPY . .

# ❌ Bad - Source first (changes frequently, invalidates cache)
COPY . .
RUN npm install
```

### 3. Multi-Stage Builds
```dockerfile
# ✅ Smaller final image - only runtime dependencies
FROM maven:3.9-jdk-21 AS builder
RUN mvn package

FROM eclipse-temurin:21-jre
COPY --from=builder /app/target/*.jar app.jar
```

### 4. Use Specific Tags
```dockerfile
# ❌ Bad - unpredictable
FROM node:latest

# ✅ Good - predictable and reproducible
FROM node:18.17.0-alpine
```

### 5. Security
```dockerfile
# ✅ Run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# ✅ Use minimal base images
FROM alpine:3.18
FROM distroless/java21

# ✅ Scan for vulnerabilities
# docker scout quickview
# trivy image myimage:latest
```

### 6. Use .dockerignore
```
# .dockerignore file
node_modules
.git
.env
*.md
target/
.idea/
```

### 7. One Process Per Container
```dockerfile
# ✅ Good - single responsibility
ENTRYPOINT ["java", "-jar", "app.jar"]

# ❌ Bad - multiple processes
CMD service nginx start && java -jar app.jar
```

### 8. Health Checks
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1
```

---

## Quick Reference Cheat Sheet

| Instruction | Time | Purpose | Override |
|-------------|------|---------|----------|
| `FROM` | Build | Base image | No |
| `WORKDIR` | Build | Set working directory | No |
| `COPY` | Build | Copy files | No |
| `ADD` | Build | Copy + extract/download | No |
| `RUN` | Build | Execute commands | No |
| `ENV` | Build + Runtime | Set environment variables | Yes (runtime) |
| `ARG` | Build | Build-time variables | Yes (build) |
| `EXPOSE` | Metadata | Document ports | N/A |
| `USER` | Build + Runtime | Set user | No |
| `ENTRYPOINT` | Runtime | Main executable | Hard |
| `CMD` | Runtime | Default arguments | Easy |
| `HEALTHCHECK` | Runtime | Container health | No |

---

## Additional Resources

- [Docker Official Documentation](https://docs.docker.com/engine/reference/builder/)
- [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)

---

## License

This documentation is provided as-is for educational purposes.

---

**Last Updated:** December 2025