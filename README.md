# Fuel SA

## Overview

Fuel SA is a JavaFX desktop application that simulates a fuel station management system. The project was developed to demonstrate the use of JavaFX for building graphical user interfaces while applying object-oriented programming principles in Java.

The application provides a user-friendly interface for interacting with fuel station functionality through a desktop environment.

## Features

* JavaFX graphical user interface
* Fuel request functionality
* Form validation
* Modular project structure
* Object-oriented design

## Technologies Used

* Java
* JavaFX
* FXML
* Scene Builder
* CSS

## Project Structure

```
FuelSA/
├── src/
│   ├── application/
│   ├── controller/
│   ├── model/
│   ├── view/
│   └── resources/
├── lib/
└── README.md
```

*The exact structure may differ depending on your IDE and project configuration.*

## Requirements

Before running the project, ensure you have the following installed:

* Java Development Kit (JDK) 17 or later
* JavaFX SDK
* An IDE such as IntelliJ IDEA, Eclipse, or NetBeans

## Running the Project

### Prerequisites

* Java Development Kit (JDK) 17 or later
* JavaFX SDK
* An IDE such as IntelliJ IDEA or Eclipse

### Setup

1. Clone this repository.

   ```bash
   git clone https://github.com/yourusername/FuelSA.git
   ```

2. Open the project in your preferred IDE.

3. If JavaFX is not configured on your system, download the JavaFX SDK and add it to your project.

4. Configure the VM options for the application's run configuration:

   ```text
   --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
   ```

5. Run the project's main class to launch the application.
