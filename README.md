# Faculty LMS - Learning Management System

A comprehensive Learning Management System built with Spring Boot for managing students, teachers, classes, grades, and educational content.

## ✨ Features

### User Management
- Role-based access control (Admin, Teacher, Student, Department Head)
- Secure authentication and authorization
- User profile management

### Academic Management
- Department and class organization
- Student grouping and assignment
- Subject management with coefficients
- Teacher-subject-group assignments

### Grade Management
- Comprehensive grading system (TD, TP, Continuous Assessment, Exams)
- Semester-based grade tracking
- Academic year management
- Bulk grade operations

### Content Management
- Post creation and sharing
- Multi-level approval workflow
- File attachments (PDF, Word, Excel, Images)
- Activity feed with real-time updates

### Excel Import/Export
- Bulk student/teacher import via Excel
- Template generation with existing data
- Data validation and error reporting
- Flexible password management

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.2.3, Java 21
- **Security**: Spring Security (Session-based + BCrypt)
- **Database**: H2 (dev) / PostgreSQL (prod)
- **ORM**: Hibernate/JPA
- **Frontend**: Thymeleaf + Bootstrap 5.3.2
- **File Processing**: Apache POI 5.2.5
- **Build Tool**: Maven 3.9+

## 📋 Prerequisites

- Java JDK 21+
- Maven 3.9+ (or use included wrapper)

## 🚀 Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/lms-spring-boot.git
   cd lms-spring-boot/backend
   ```

2. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the application**
   - URL: http://localhost:8080
   - Default credentials: `admin` / `admin123`

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/lms/
│   │   ├── controller/       # REST & Web controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── model/            # JPA entities
│   │   ├── repository/       # Database repositories
│   │   ├── security/         # Security configuration
│   │   ├── service/          # Business logic
│   │   └── LmsApplication.java
│   └── resources/
│       ├── application.properties
│       └── templates/        # Thymeleaf templates
└── uploads/                  # File storage directory
```

## 🔐 Default Accounts

The system initializes with the following accounts:

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| HOD | hod1 | hod123 |
| Teacher | prof001 | prof123 |
| Student | s001 | S0012025 |

## 🎯 Core Functionality

### For Administrators
- User management (students, teachers, department heads)
- Academic structure (departments, classes, groups, subjects)
- Excel-based bulk import/export
- System-wide configuration

### For Teachers
- Create educational posts with rich text editor
- Upload file attachments (PDF, Word, Excel, images)
- Manage student grades across assessment types
- View and filter students by group
- Semester-based grade entry

### For Students
- Access personalized content feed
- View grades and academic progress
- Download course materials
- Filter content by department/class/subject

### For Department Heads
- Approve/reject educational posts
- Batch approval workflow
- Department-level content moderation
- Access to analytics

## ⚙️ Configuration

### Development
Uses H2 in-memory database by default. Configuration in `application.properties`:

```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:lmsdb
spring.jpa.hibernate.ddl-auto=create-drop
file.upload-dir=uploads
```

### Production
Configure PostgreSQL in `application-prod.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lmsdb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
file.upload-dir=/var/lms/uploads
```

### File Storage

```properties
# Development (relative path)
file.upload-dir=uploads

# Production (absolute path)
file.upload-dir=/var/lms/uploads
```

## 🐳 Docker Deployment

```bash
# Build and run
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## 📊 Database Schema

Key entities:
- **Users**: Authentication and profiles
- **Students/Teachers**: Extended user information
- **Departments**: Academic departments
- **Classes**: Student classes with levels
- **Groups**: Smaller groups within classes
- **Subjects**: Course subjects with coefficients
- **ClassSubjects**: Subject-class mappings with coefficients
- **TeacherAssignments**: Teacher-subject-group assignments
- **Grades**: Assessment records (TD, TP, Exam, Final)
- **Posts**: Educational content with approval workflow
- **PostAttachments**: File uploads

## 🔒 Security Features

- BCrypt password hashing
- Role-based access control (RBAC)
- Session management
- CSRF protection
- File upload validation
- XSS prevention
- Secure password policies

## 📦 Production Build

```bash
# Build JAR
./mvnw clean package -DskipTests

# Run with production profile
java -jar target/lms-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 🔧 Systemd Service (Linux)

Create `/etc/systemd/system/lms.service`:

```ini
[Unit]
Description=Faculty LMS
After=network.target

[Service]
Type=simple
User=lmsuser
WorkingDirectory=/opt/lms
ExecStart=/usr/bin/java -jar /opt/lms/lms.jar
Restart=on-failure
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable lms
sudo systemctl start lms
sudo systemctl status lms
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Support

For questions or issues, please open an issue on GitHub.

---

**Built with ❤️ using Spring Boot**
