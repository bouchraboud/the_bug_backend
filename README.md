# The Bug 🐛

A modern Q&A platform inspired by Stack Overflow, built with Spring Boot. Ask questions, get answers, and build your developer reputation in the community.

## ✨ Features

### 🔐 Authentication & User Management
- User registration and email confirmation
- JWT-based authentication
- OAuth2 integration (Google, GitHub)
- Password reset functionality
- User profiles and reputation system

### 📝 Question & Answer System
- Create, edit, and search questions
- Rich text content support (Lexical JSON format)
- Answer questions with detailed responses
- Accept/unaccept answers
- Tag-based categorization

### 🗳️ Voting System
- Upvote/downvote questions and answers
- Reputation-based voting privileges

### 🏷️ Tagging System
- Create and manage tags
- Tag-based question filtering
- Follow specific tags

### 👥 Social Features
- Follow other users
- Follow questions, answers, and tags
- User profiles 
- Follower/following system

### 🔔 Notifications
- Real-time notifications for followed content
- Mark notifications as read
- Unread notification counts

### 🏆 Reputation System
- Earn reputation through quality contributions
- Reputation-based privileges
- Reputation history tracking
- Daily earning limits

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.x
- **Security**: Spring Security with JWT
- **Database**: JPA/Hibernate
- **Authentication**: OAuth2 (Google, GitHub)
- **Email**: Spring Mail
- **Build Tool**: Maven/Gradle

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Database (MySQL/PostgreSQL)
- SMTP server for email notifications

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/bouchraboud/the_bug_backend.git
   cd backend
   ```

2. **Configure application properties**
   ```properties
   # Database configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/theBug
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   # JWT configuration
   jwt.secret=your-secret-key
      
   # Email configuration
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   
   # OAuth2 configuration
   spring.security.oauth2.client.registration.google.client-id=your-google-client-id
   spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
   spring.security.oauth2.client.registration.google.scope=profile,email
   spring.security.oauth2.client.registration.github.client-id=your-github-client-secret
   spring.security.oauth2.client.registration.github.client-secret=your-github-client-secret
   spring.security.oauth2.client.registration.github.scope=user:email,read:user
   spring.security.oauth2.client.registration.github.client-name=GitHub
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the API**
   - Base URL: `http://localhost:8080/api`

## 📚 API Endpoints

### Authentication
- `POST /auth/login/user` - User login

### Users
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/register/users` - Register new user
- `PUT /api/users` - Update user profile

### Questions
- `GET /api/questions` - Get all questions
- `POST /api/questions` - Create new question
- `GET /api/questions/{id}` - Get question by ID
- `PUT /api/questions/{id}` - Update question
- `GET /api/questions/search` - Search questions

### Answers
- `POST /api/answers` - Create new answer
- `PUT /api/answers/{id}` - Update answer
- `POST /api/answers/{id}/accept` - Accept answer
- `POST /api/answers/{id}/disaccept` - Unaccept answer

### Voting
- `POST /api/votes/questions/{id}/upvote` - Upvote question
- `POST /api/votes/questions/{id}/downvote` - Downvote question
- `POST /api/votes/answers/{id}/upvote` - Upvote answer
- `POST /api/votes/answers/{id}/downvote` - Downvote answer

### Comments
- `POST /api/comments/questions/{id}` - Add comment to question
- `POST /api/comments/answers/{id}` - Add comment to answer
- `GET /api/comments/questions/{id}` - Get question comments

### Tags
- `GET /api/tags` - Get all tags
- `GET /api/tags/popular` - Get popular tags
- `GET /api/tags/{name}/questions` - Get questions by tag

## 🏗️ Project Structure

```
src/main/java/theBugApp/backend/
├── controller/          # REST Controllers
├── service/            # Business Logic Services
├── repository/         # Data Access Layer
├── entity/            # JPA Entities
├── dto/               # Data Transfer Objects
├── exception/         # Custom Exceptions
└── config/            # Configuration Classes
```

## 🔒 Security Features

- JWT token-based authentication
- OAuth2 integration for social login
- Email verification for new accounts
- Password reset functionality
- Reputation-based access control
- CORS configuration for frontend integration

---

**Happy Coding!** 🚀
