# Text Adventure as a Web App

This project represents an exploration into building a backend-driven web application using enterprise-grade Java technologies. While this is a fun and simple "game," the primary goal was to develop a robust RESTful API capable of delivering a text-based Role-Playing Game (RPG), all written with Java as the backend. This is almost unusual, given other more common tools, but I just thought it was be a fun proof-of-concept given that so much work is done in Java in the professional world.

The software is a "backend-driven" RPG system as a text-based RPG. Players interact with the game world through API calls, allowing for character creation, story progression, exploration, and even combat. While it is fairly limited in its current form, the infrastructure is in place to now take the program really to the next level. The narrative is a multi-branching storyline about a BYU student.

[CLICK HERE to Experience the Online Text Adventure RPG Yourself!](https://byu-student-java-text-rpg-ui.onrender.com/)

(Please be aware, Render can take up to 50 seconds to spin up after hitting the "Start Game" button - Please be patient)

[Software Demo Video](https://youtu.be/cjd-2J_Zaag)

# Development Environment

This project was primarily developed using **Visual Studio Code (VS Code)** as the integrated development environment. Key tools and technologies involved include:

*   **Java Development Kit (JDK):** Version 21 was used for compiling and running the Java application.
*   **Apache Maven:** As the build automation and project management tool for the Java backend.
*   **Spring Boot:** The core framework
*   **Spring Web:** For creating RESTful APIs to handle game interactions.
*   **Spring Data JPA & Hibernate:** For data managing game state, player characters, and other game entities in a relational database.
*   **Docker & Docker Hub:** For containerizing the Spring Boot backend application, creating a portable and consistent runtime environment, and hosting the image. ALL DEPLOYMENTS SITES REQUIRED DOCKER FOR JAVA!
*   **Render.com:** As the cloud platform for deploying both the Dockerized Spring Boot backend (as a Web Service) and the static HTML/JS/CSS frontend (as a Static Site).
*   **Postman:** For testing the REST APIs during development.
*   **Git & GitHub:** For version control and source code management.
*   **H2 Database Engine:** Used as an in-memory and file-based database during local development.
*   **HTML, CSS, Vanilla JavaScript:** For building the simple, text-based frontend to interact with the backend API.

[Public Docker Image](https://hub.docker.com/r/aaronjtopping/byu-student-java-text-rpg)

**Backend Deployment API Example - Postman Query - POST**
![POST Query](https://i.imgur.com/QgPl6n8.jpeg)


# Useful Websites

* [Spring Initializer](https://start.spring.io/) To quickly and easily bootstrap setting up a working enviornment for Java and Spring Boot and Maven
* [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/jpa.html) - CRITICAL source for built-in CRUD functionality documentation
* [Building a Java app with Spring Boot - Official Guide](https://spring.io/guides/gs/spring-boot)
* [Java Spring Tutorial - REST](https://medium.com/@alexandre.therrien3/java-spring-tutorial-the-only-tutorial-you-will-need-to-get-started-vs-code-13413e661db5) - Full setup of REST API using Java Spring. Critical documentation to integrate web functionality using REST APIs GET, POST, etc for web functionality
* [Creating a text adventure game in Java](https://www.javacoffeebreak.com/text-adventure/) - While old, creating the overall structure of all my classes this was absolutely critical to follow some guidelines on how to use.

# Future Work

* **Expanded Story Content:** I really want to add more layers and branching narratives, more scenes, etc...
* **Expanded Combat:** The combat in this stands more as a demo and proof of concept than a dynamic combat experience.
* **Character Progression:** This is an RPG. As of now, the only true dynamic implemented is your "Class" to choose.