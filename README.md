# Redis Simple Chat Application

## About
This project was created by [Tymofii Skrypko](https://github.com/Ka1zerov) as part of the Non-Relational Databases (NDB) course. It demonstrates the practical application of Redis as a non-relational database in building a real-time chat system.

## Overview
This is a real-time chat application built with Spring Boot (Backend) and React (Frontend), using Redis as the primary database and message broker. The application supports private messaging, user presence detection, and persistent chat history.

## Features
- Real-time messaging using Server-Sent Events (SSE)
- Private chat rooms between users
- User presence detection (online/offline status)
- Persistent chat history
- User authentication
- General chat room for all users

## Technology Stack
### Backend
- Java 21
- Spring Boot 3.2.1
- Redis
- Spring Security
- Lombok
- Server-Sent Events (SSE)

### Frontend
- React
- Node.js 16
- Yarn
- TypeScript

## Prerequisites
- Java 21
- Redis Server
- Node.js 16
- Yarn package manager
- Maven

## Installation & Setup

### Backend Setup
1. Clone the repository
2. Make sure Redis server is running locally on port 6379 (default)
3. Configure Redis connection in `application.properties` if needed
4. Build and run the application:
```bash
./mvnw clean install
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup
1. Navigate to the frontend directory
2. Set Node.js version:
```bash
nvm use 16
```

3. Install dependencies:
```bash
yarn install
```

4. Build the application:
```bash
yarn build
```

5. Start the development server:
```bash
yarn start
```

The frontend will be available at `http://localhost:3000`

## Environment Variables

### Backend
- `PORT` - Server port (default: 8080)
- `REDIS_ENDPOINT_URL` - Redis connection URL (default: localhost:6379)
- `REDIS_PASSWORD` - Redis password (optional)

## Architecture

### Backend Components
1. **Authentication Controller**: Handles user login/logout
2. **Chat Controller**: Manages real-time messaging using SSE
3. **Redis Configuration**: Sets up Redis connection and message listeners

### Data Flow
1. Users authenticate through the `/auth/login` endpoint
2. Authenticated users connect to SSE stream through `/chat/stream`
3. Messages are published to Redis channels
4. Redis publishes messages to all subscribed clients
5. Messages are persisted in Redis for chat history

## Demo Users
The application creates demo users on startup with the following credentials:
- Usernames: Pablo, Joe, Mary, Alex
- Password for all users: password123

## Security
The application implements basic security measures:
- Password encryption using BCrypt
- Session-based authentication
- CORS configuration for development

## Author
**Tymofii Skrypko** - [GitHub Profile](https://github.com/Ka1zerov)

## Academic Context
This project was developed as part of the Non-Relational Databases (NDB) course curriculum, showcasing the practical implementation of:
- Redis as a primary database
- Real-time message broadcasting
- User session management
- Data persistence in a non-relational context

## Contributing
While this is primarily an academic project, issues and pull requests are welcome. Please feel free to suggest improvements or report bugs.

## License
This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
