# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **mini-Tomcat implementation** that demonstrates the evolution of a web server through 5 progressive chapters. Each chapter builds upon the previous one, adding new features and architectural improvements.

## Architecture

### Chapter Progression

The codebase demonstrates a step-by-step evolution from a simple HTTP server to a multi-threaded servlet container with connection pooling:

- **Chapter 2** (`lab.minitomcat.chapter2`): Basic single-threaded HTTP server that serves static files from `webroot/`
- **Chapter 3** (`lab.minitomcat.chapter3`): Adds servlet support with `ServletProcessor` and `StaticResourceProcessor` - routes `/servlet/*` URLs to dynamic servlets
- **Chapter 4** (`lab.minitomcat.chapter4`): Separates concerns with `HttpConnector` (accepts connections) and `HttpProcessor` (processes requests) running in separate threads
- **Chapter 5** (`lab.minitomcat.chapter5`): Implements HttpProcessor pooling with thread synchronization for better performance and resource management

### Key Components

- **HttpServer** (Ch2-3): Main server that accepts connections and processes requests
- **HttpConnector** (Ch4-5): Runnable that accepts incoming socket connections
- **HttpProcessor** (Ch4-5): Runnable that processes HTTP requests (Ch5 includes object pooling)
- **Request/Response**: HTTP request/response parsers and handlers
- **ServletProcessor**: Routes and executes servlet requests
- **StaticResourceProcessor**: Serves static files from `webroot/`
- **WEB_ROOT**: Points to `{user.dir}/webroot` - static files and compiled servlets are served from here

### Threading Model

- **Ch2-3**: Single-threaded, handles one request at a time
- **Ch4**: Multi-threaded - Connector accepts connections, each Processor runs in its own thread
- **Ch5**: Thread pool pattern - HttpProcessors are pooled and recycled using synchronized wait/notify

## Common Commands

### Build the Project
```bash
mvn clean compile
```

### Compile Servlets for Testing
Servlets in `src/test/java/chapterX/` must be compiled to `webroot/chapterX/` to be accessible:

```bash
# Compile servlet for chapter 3
javac -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
  src/test/java/chapter3/HelloServlet.java -d webroot/chapter3/

# Compile servlet for chapter 4
javac -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
  src/test/java/chapter4/HelloServlet.java -d webroot/chapter4/

# Compile servlet for chapter 5
javac -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
  src/test/java/chapter5/HelloServlet.java -d webroot/chapter5/
```

### Run Different Chapters

**Chapter 2** (Static files only):
```bash
mvn exec:java -Dexec.mainClass="lab.minitomcat.chapter2.HttpServer"
```

**Chapter 3** (Static + Servlets):
```bash
mvn exec:java -Dexec.mainClass="lab.minitomcat.chapter3.HttpServer"
```
Then visit: `http://localhost:8080/hello.txt` or `http://localhost:8080/servlet/chapter3.HelloServlet`

**Chapter 4** (Multi-threaded):
```bash
mvn exec:java -Dexec.mainClass="lab.minitomcat.chapter4.HttpServer"
```

**Chapter 5** (Thread pool):
```bash
mvn exec:java -Dexec.mainClass="lab.minitomcat.chapter5.HttpServer"
```
Then visit: `http://localhost:8080/servlet/chapter5.HelloServlet`

### Run Test Programs
```bash
# Run UnsafeLockDemo (demonstrates synchronization issues)
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" chapter5.UnsafeLockDemo
```

### Clean Build
```bash
mvn clean compile
rm -rf webroot/chapter3/*.class webroot/chapter4/*.class webroot/chapter5/*.class
```

## Development Notes

- The project uses Java 17
- Servlets follow the `javax.servlet.Servlet` interface (Servlet API 4.0.1)
- All servers listen on `127.0.0.1:8080`
- Static resources are served from `webroot/` directory
- Servlet URLs follow pattern: `/servlet/{className}` (without package)
- Chapter 5 demonstrates HttpProcessor pooling with minProcessors=3, maxProcessors=10
- The `UnsafeLockDemo` in chapter5 shows pitfalls of non-final locks and unsynchronized operations