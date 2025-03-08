# Collaborative Graphical Editor Java Application

Developed a collaborative graphical editor allowing multiple users to interact with the same sketch in real time. Users can draw, move, recolor, and delete shapes collaboratively using a client-server architecture.

## Project Overview

This project implements a multi-client graphical editor where multiple users can simultaneously interact with and modify a shared sketch. It involves:
- **Real-time collaboration** where any drawing made by one user is instantly reflected on all connected clients.
- **Client-server communication** to maintain the state of the sketch and synchronize operations (e.g., drawing shapes, moving them, recoloring, and deleting).
- **Shape management** supporting multiple shape types including ellipse, rectangle, segment, and freehand polyline with unique IDs assigned for synchronization.

## Technologies Used

- **Java**: Core language used for implementing the editor, server, and communication layers.
- **Swing**: For the graphical user interface (GUI).
- **Sockets (TCP/IP)**: For client-server communication.
- **Concurrency**: Synchronized methods to handle multi-client updates safely.

## Key Features

- **Real-time Synchronization**: All operations performed by one client are immediately broadcast to others.
- **Supported Shapes**:
  - **Ellipse**
  - **Rectangle**
  - **Segment**
  - **Freehand (Polyline)**
- **Client-Server Communication**: Clients communicate with the server to request operations like adding, moving, recoloring, or deleting shapes.
- **Unique ID Management**: Each shape is assigned a unique ID for consistency across clients.
- **Thread Synchronization**: Uses synchronized methods to ensure thread-safe updates to the shared sketch.

## How It Works

### Client-Side:
- **Editor.java**: The main user interface where clients interact with the sketch (drawing, moving, recoloring, and deleting shapes).
- **EditorCommunicator.java**: Handles the communication between the client and the server. It sends requests for actions and receives updates from the server.

### Server-Side:
- **SketchServer.java**: The server that listens for incoming client connections, processes client requests, and broadcasts updates.
- **SketchServerCommunicator.java**: Manages communication with each client. It processes incoming requests and updates the master sketch, ensuring synchronization across clients.
- **Sketch.java**: Maintains the collection of shapes and provides methods to modify, move, recolor, or delete shapes.
- **MessageParser.java**: Utility class for creating and parsing messages sent between clients and the server.

## Setup and Installation

1. **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/Collaborative-Graphical-Editor.git
    cd Collaborative-Graphical-Editor
    ```

2. **Compile the Project**:
    Ensure you have Java installed (Java 8 or later) and compile all Java files:
    ```bash
    javac *.java
    ```

3. **Run the Server**:
    Start the SketchServer:
    ```bash
    java SketchServer
    ```

4. **Run the Client(s)**:
    In one or more terminal windows, run:
    ```bash
    java Editor
    ```

5. **Test the Synchronization**:
    - Open multiple Editor windows (clients).
    - Test drawing, moving, recoloring, and deleting shapes.
    - Observe that all changes are synchronized in real time across all connected clients.

## Features

- **Drawing Shapes**: Create ellipses, rectangles, segments, and freehand polylines.
- **Real-Time Synchronization**: All clients see updates as they happen.
- **Multi-Client Support**: Run multiple instances of the client to test collaborative functionality.
- **Shape Operations**: Move, recolor, and delete shapes across all clients.
- **New Client Synchronization**: When a new client connects, it receives the current state of the sketch.

## Testing

### Single-Client Testing
- Verify that drawing, moving, recoloring, and deleting shapes works as expected.

### Multi-Client Testing
- Run multiple client instances and ensure that all actions (drawing, moving, recoloring, deleting) are reflected across all clients.

### New Client Synchronization
- After multiple operations are performed, add a new client and verify that it receives the full current state of the sketch.

## Results

- **Real-Time Collaboration**: Successful synchronization across multiple clients.
- **Shape Operations**: All shape operations (drawing, moving, recoloring, deleting) work smoothly in multi-client scenarios.
- **Unique IDs**: Shapes are uniquely identified and can be modified correctly across clients.

## Future Improvements

- **Hybrid Drawing Tools**: Add more complex drawing tools (e.g., curves, polygons).
- **Advanced User Features**: Implement undo/redo functionality, layer management, and more interactive tools.
- **Optimized Networking**: Further optimize message passing to handle larger canvases and more simultaneous users.

## Acknowledgments

This project was developed as part of **CS 10: Problem Solving via Object-Oriented Programming** at Dartmouth College. Special thanks to:

- **Chris Bailey-Kellogg** (Dartmouth CS10)
- **Tim Pierson** (Dartmouth CS10)
- **Travis Peters** (Dartmouth CS10)
- And all other professors involoved in developing this project

## License
-----------
