import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.awt.*;

/**
 * Handles communication to/from the server for the graphical editor.
 *
 * This class establishes a socket connection to the SketchServer and manages
 * the input and output streams for sending and receiving messages. It runs in
 * its own thread and continuously listens for messages from the server, passing
 * them to the Editor for further processing.
 *
 * The class also provides helper methods that allow the Editor to send requests
 * (such as add, move, recolor, delete, and add polyline) to the server in a
 * standardized message format using the MessageParser.
 *
 * @author Triumph Kia Teh | CS10 | Winter 2025
 *
 * ------Professors who provided the scaffold and project descriptions:------>
 * @author - Chris Bailey-Kellogg, Dartmouth CS10, Fall 2012
 * @author - CBK, Winter 2014; Travis Peters, Dartmouth CS10, Winter 2015
 * @author - CBK, Spring & Fall 2016; Tim Pierson, Dartmouth CS10, Winter 2025
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;       // Stream for sending messages to the server
	private BufferedReader in;     // Stream for receiving messages from the server
	protected Editor editor;       // Reference to the Editor instance that uses this communicator

	/**
	 * Constructor that establishes a connection to the server and sets up the I/O streams.
	 *
	 * @param serverIP the IP address of the SketchServer
	 * @param editor   the Editor instance using this communicator
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			// Create a new socket and connect to the server on port 4242 with a 2000ms timeout.
			Socket sock = new Socket();
			sock.connect(new InetSocketAddress(serverIP, 4242), 2000);
			// Set up the output stream (for sending messages) and input stream (for receiving messages).
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		} catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends a message to the server.
	 *
	 * @param msg the message to send
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Continuously listens for messages from the server and passes them to the Editor for processing.
	 * The loop continues until the connection is closed.
	 */
	public void run() {
		try {
			String msg;
			// Loop reading messages from the input stream.
			while ((msg = in.readLine()) != null) {
				// Forward each message to the Editor's processMessage method.
				editor.processMessage(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("server hung up");
		}
	}

	// ============================================================
	// Helper methods to send editor requests to the server
	// ============================================================

	/**
	 * Sends a request to add a new shape.
	 * Constructs an "add" message using the MessageParser helper.
	 *
	 * @param shapeType the type of shape (e.g., "ellipse", "rectangle", etc.)
	 * @param x1        the first x-coordinate
	 * @param y1        the first y-coordinate
	 * @param x2        the second x-coordinate
	 * @param y2        the second y-coordinate
	 * @param color     the color of the shape
	 */
	public void requestAddShape(String shapeType, int x1, int y1, int x2, int y2, Color color) {
		String msg = MessageParser.createAddMessage(shapeType, x1, y1, x2, y2, color.getRGB());
		send(msg);
	}

	/**
	 * Sends a request to move an existing shape.
	 *
	 * @param id the unique identifier of the shape
	 * @param dx the change in x-coordinate
	 * @param dy the change in y-coordinate
	 */
	public void requestMoveShape(int id, int dx, int dy) {
		String msg = MessageParser.createMoveMessage(id, dx, dy);
		send(msg);
	}

	/**
	 * Sends a request to recolor an existing shape.
	 *
	 * @param id    the unique identifier of the shape
	 * @param color the new color for the shape
	 */
	public void requestRecolorShape(int id, Color color) {
		String msg = MessageParser.createRecolorMessage(id, color.getRGB());
		send(msg);
	}

	/**
	 * Sends a request to delete an existing shape.
	 *
	 * @param id the unique identifier of the shape to delete
	 */
	public void requestDeleteShape(int id) {
		String msg = MessageParser.createDeleteMessage(id);
		send(msg);
	}

	/**
	 * Sends a request to add a freehand (polyline) shape.
	 * This method converts the polyline's string representation into the proper format.
	 *
	 * Expected polyline format from toString():
	 * "polyline <numPoints> <x1> <y1> <x2> <y2> ... <rgb>"
	 * The message sent is:
	 * "add polyline <numPoints> <x1> <y1> ... <rgb>"
	 *
	 * @param p the Polyline to add
	 */
	public void requestAddPolyline(Polyline p) {
		// Obtain the polyline's string representation.
		String polyStr = p.toString();
		// Ensure it starts with "polyline " and replace it with "add polyline ".
		if(polyStr.startsWith("polyline ")) {
			String msg = "add polyline " + polyStr.substring("polyline ".length());
			send(msg);
		} else {
			// Fallback: send the original string with "add polyline " prefixed.
			send("add polyline " + polyStr);
		}
	}
}
