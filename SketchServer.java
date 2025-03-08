import java.net.*;
import java.util.*;
import java.io.*;

/**
 * A server to handle sketches for the collaborative graphical editor.
 *
 * This class is responsible for receiving drawing requests from clients,
 * updating the master sketch (which represents the shared state of the drawing),
 * and broadcasting updates to all connected clients. It uses a ServerSocket to
 * accept client connections and creates a dedicated SketchServerCommunicator for
 * each client.
 *
 * The design ensures that all changes made by one client are synchronized across
 * all clients by broadcasting the same messages to everyone.
 *
 * @author Triumph Kia Teh | CS10 | Winter 2025
 *
 *------Professors who provided the scaffold and assignment descriptions------
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2025
 */
public class SketchServer {
	private ServerSocket listen;                      // ServerSocket for accepting client connections
	private ArrayList<SketchServerCommunicator> comms;  // List of communicators handling each client
	private Sketch sketch;                            // Master sketch representing the shared drawing

	/**
	 * Constructor that initializes the server with the given ServerSocket.
	 *
	 * @param listen the ServerSocket used to accept client connections
	 */
	public SketchServer(ServerSocket listen) {
		this.listen = listen;
		sketch = new Sketch();          // Initialize the master sketch
		comms = new ArrayList<>();      // Initialize the list of client communicators
	}

	/**
	 * Returns the master sketch.
	 *
	 * @return the Sketch object representing the current state of the drawing
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Adds a client communicator to the list of active communicators.
	 * This is used to keep track of all connected clients.
	 *
	 * @param comm the SketchServerCommunicator handling a client connection
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
	}

	/**
	 * Removes a client communicator from the active list.
	 *
	 * @param comm the SketchServerCommunicator to remove
	 */
	public synchronized void removeCommunicator(SketchServerCommunicator comm) {
		comms.remove(comm);
	}

	/**
	 * Broadcasts a message to all connected clients.
	 * This ensures that every client is updated with the latest changes to the sketch.
	 *
	 * @param msg the message to broadcast
	 */
	public synchronized void broadcast(String msg) {
		for (SketchServerCommunicator comm : comms) {
			comm.send(msg);
		}
	}

	/**
	 * Main loop that continuously accepts client connections.
	 * For each new connection, a new SketchServerCommunicator is created and started in its own thread.
	 *
	 * @throws IOException if an I/O error occurs when waiting for a connection
	 */
	public void getConnections() throws IOException {
		System.out.println("Server ready for connections.");
		while (true) {
			// Accept a new client connection and create a communicator for it.
			SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);  // Set as daemon so the thread will exit when the main program ends
			comm.start();
			addCommunicator(comm); // Add the communicator to the list of active clients
		}
	}

	/**
	 * Main method to start the SketchServer.
	 * It creates a new ServerSocket on port 4242 and calls getConnections() to begin accepting clients.
	 *
	 * @param args command-line arguments (not used)
	 * @throws Exception if an error occurs during startup
	 */
	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();
	}
}
