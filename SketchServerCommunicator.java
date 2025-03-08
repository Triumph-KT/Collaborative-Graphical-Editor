import java.io.*;
import java.net.Socket;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

/**
 * Handles communication between the SketchServer and a single client.
 *
 * This class is responsible for:
 * - Sending the current master sketch (the shared drawing) to a newly connected client.
 * - Continuously reading messages from the client, parsing them to update the master sketch,
 *   and broadcasting the corresponding update to all connected clients.
 * - Handling messages for adding (including polyline), moving, recoloring, and deleting shapes.
 * - Cleaning up resources when a client disconnects.
 *
 * The message formats are as follows:
 * - "add <shapeType> <id> <x1> <y1> <x2> <y2> <rgb>" for ellipse, rectangle, and segment.
 * - "add polyline <numPoints> <x1> <y1> ... <rgb>" for freehand/polyline shapes (the server assigns a unique id).
 * - "move <id> <dx> <dy>" for moving a shape.
 * - "recolor <id> <rgb>" for recoloring a shape.
 * - "delete <id>" for deleting a shape.
 *
 * @author Triumph Kia Teh | CS10 | Winter 2025
 *
 *------Professors who provided the scaffold and assignment descriptions------ *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2025
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;            // Connection to the client
	private BufferedReader in;      // Input stream from the client
	private PrintWriter out;        // Output stream to the client
	private SketchServer server;    // Reference to the main server instance

	/**
	 * Constructor that sets up the communicator with the given client socket and server.
	 *
	 * @param sock   the client socket
	 * @param server the main SketchServer instance
	 */
	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the connected client.
	 *
	 * @param msg the message to send
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Main method for the communicator thread.
	 *
	 * When a client connects, this method:
	 * - Sends the current master sketch to the client.
	 * - Enters a loop to read messages from the client.
	 * - Parses each message and updates the master sketch accordingly.
	 * - Broadcasts the original (or updated) message to all connected clients.
	 * - Cleans up and removes the communicator when the client disconnects.
	 */
	public void run() {
		try {
			System.out.println("Client connected.");
			// Set up input and output streams for communication with the client.
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Send the current master sketch to the newly connected client.
			// For each shape in the master sketch, we prepend its unique ID to the message.
			for (Shape s : server.getSketch().getShapes()) {
				int id = server.getSketch().getIdForShape(s);
				// We assume that the shape's toString() returns a message without the id,
				// so we use replaceFirst to insert the id after the shape type.
				out.println("add " + s.toString().replaceFirst("^(\\S+)", "$1 " + id));
			}

			// Continuously read messages from the client.
			String msg;
			while ((msg = in.readLine()) != null) {
				System.out.println("Received from client: " + msg);
				// Parse the message using MessageParser.
				String[] tokens = MessageParser.parseMessage(msg);
				if (tokens.length > 0) {
					String command = tokens[0];
					if (command.equals("add")) {
						// Check if this is a polyline message.
						if (tokens[1].equals("polyline")) {
							// Expected format from client: "add polyline <numPoints> <x1> <y1> ... <rgb>"
							if (tokens.length >= 3) {
								int numPoints = Integer.parseInt(tokens[2]);
								// Total tokens required: 3 (for "add polyline <numPoints>")
								// + (numPoints * 2) for points + 1 token for the rgb value.
								if (tokens.length >= 3 + numPoints * 2 + 1) {
									List<Point> pts = new ArrayList<>();
									int index = 3;
									// Parse each point.
									for (int i = 0; i < numPoints; i++) {
										int px = Integer.parseInt(tokens[index++]);
										int py = Integer.parseInt(tokens[index++]);
										pts.add(new Point(px, py));
									}
									int rgb = Integer.parseInt(tokens[index]);
									// Create a new polyline shape using the parsed points and color.
									Shape s = new Polyline(pts, new Color(rgb));
									// Add the shape to the master sketch; this assigns a unique ID.
									int id = server.getSketch().addShape(s);
									// Rebuild the message to include the unique ID.
									StringBuilder newMsg = new StringBuilder();
									newMsg.append("add polyline ").append(id).append(" ").append(numPoints);
									for (Point p : pts) {
										newMsg.append(" ").append(p.x).append(" ").append(p.y);
									}
									newMsg.append(" ").append(rgb);
									// Broadcast the updated message with the unique ID.
									server.broadcast(newMsg.toString());
								}
							}
						} else {
							// Handle adding ellipse, rectangle, or segment.
							if (tokens.length >= 7) {
								String shapeType = tokens[1];
								int x1 = Integer.parseInt(tokens[2]);
								int y1 = Integer.parseInt(tokens[3]);
								int x2 = Integer.parseInt(tokens[4]);
								int y2 = Integer.parseInt(tokens[5]);
								int rgb = Integer.parseInt(tokens[6]);
								Shape s = null;
								if (shapeType.equals("ellipse")) {
									s = new Ellipse(x1, y1, x2, y2, new Color(rgb));
								} else if (shapeType.equals("rectangle")) {
									s = new Rectangle(x1, y1, x2, y2, new Color(rgb));
								} else if (shapeType.equals("segment")) {
									s = new Segment(x1, y1, x2, y2, new Color(rgb));
								}
								if (s != null) {
									int id = server.getSketch().addShape(s);
									// Build a new message with the unique ID included.
									String newMsg = "add " + shapeType + " " + id + " " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + rgb;
									server.broadcast(newMsg);
								}
							}
						}
					} else if (command.equals("move")) {
						// Expected format: "move <id> <dx> <dy>"
						if (tokens.length >= 4) {
							int id = Integer.parseInt(tokens[1]);
							int dx = Integer.parseInt(tokens[2]);
							int dy = Integer.parseInt(tokens[3]);
							server.getSketch().moveShape(id, dx, dy);
						}
					} else if (command.equals("recolor")) {
						// Expected format: "recolor <id> <rgb>"
						if (tokens.length >= 3) {
							int id = Integer.parseInt(tokens[1]);
							int rgb = Integer.parseInt(tokens[2]);
							server.getSketch().recolorShape(id, new Color(rgb));
						}
					} else if (command.equals("delete")) {
						// Expected format: "delete <id>"
						if (tokens.length >= 2) {
							int id = Integer.parseInt(tokens[1]);
							server.getSketch().deleteShape(id);
						}
					}
				}
				// Broadcast the original message to all clients.
				server.broadcast(msg);
			}
			// If we exit the loop, the client has disconnected.
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
