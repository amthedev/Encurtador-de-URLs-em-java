# URL Shortener Service

A high-performance URL shortener service built with Spring Boot, Redis, and SQLite.

## Features

- Shorten long URLs to short, memorable links
- Track click statistics including:
  - Total clicks
  - Clicks by day
  - Clicks by country
  - Clicks by browser
- RESTful API with OpenAPI documentation
- Redis caching for improved performance
- SQLite database for persistent storage
- Custom alias support
- URL expiration
- QR code generation (TODO)

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Redis server (for caching)
- SQLite (embedded)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/url-shortener.git
cd url-shortener
```

### 2. Build the application

```bash
mvn clean package
```

### 3. Run Redis server

Make sure you have Redis server running locally on the default port (6379).

### 4. Run the application

```bash
java -jar target/url-shortener-1.0.0.jar
```

The application will start on `http://localhost:8080`.

## API Documentation

Once the application is running, you can access:

- **OpenAPI Documentation**: http://localhost:8080/swagger-ui.html
- **Actuator Endpoints**: http://localhost:8080/actuator

## API Endpoints

### Shorten a URL

```http
POST /api/urls/shorten
Content-Type: application/json

{
  "originalUrl": "https://example.com/very/long/url",
  "expiresAt": "2025-12-31T23:59:59",
  "customAlias": "my-custom-alias"
}
```

### Redirect to original URL

```http
GET /{shortCode}
```

### Get URL statistics

```http
GET /api/stats/{shortCode}
```

### Deactivate a short URL

```http
DELETE /api/urls/{shortCode}
```

## Configuration

Configuration can be modified in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# SQLite Configuration
spring.datasource.url=jdbc:sqlite:urlshortener.db

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379

# Application Configuration
app.base-url=http://localhost:8080
app.short-url-length=6
```

## Caching

The application uses Redis for caching:
- URL lookups are cached for 7 days
- Statistics are cached for 1 hour

## Contributing

1. Fork the repository
2. Create a new branch for your feature
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
