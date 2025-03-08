import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Collaborative Graphical Editor
 *
 * This class implements a client-server graphical editor with unique ID tracking.
 * It uses an EditorCommunicator to manage network communication with the SketchServer,
 * and maintains a local Sketch to store drawn shapes using the unique IDs assigned by the server.
 *
 * The editor supports the following modes:
 * - DRAW: Allows the user to draw new shapes (ellipse, rectangle, segment, freehand/polyline).
 * - MOVE: Selects and moves an existing shape.
 * - RECOLOR: Changes the color of a selected shape.
 * - DELETE: Deletes a selected shape.
 *
 * Key Design Points:
 * - In DRAW mode, shapes are only finalized (added to the local Sketch) when the server's broadcast
 *   is received. This avoids duplicate additions.
 * - In non-DRAW modes, the editor uses the local Sketch to determine the unique ID of the shape
 *   under the mouse (using findShapeAt and getIdForShape) and sends operations (move, recolor, delete)
 *   referencing that ID.
 * - The processMessage method parses incoming network messages to update the local Sketch,
 *   ensuring that all clients remain synchronized.
 *
 * The code is based on the provided scaffold from Dartmouth CS10 and has been updated for Winter 2025.
 *
 * @author Triumph Kia Teh | CS10 | Winter 2025
 *
 * ------Professors who provided the scaffold and project descriptions:------>
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2025
 */

public class Editor extends JFrame {
	// Server connection settings
	private static String serverIP = "localhost";
	private static final int width = 800, height = 800;

	// Modes available in the editor
	public enum Mode { DRAW, MOVE, RECOLOR, DELETE }
	private Mode mode = Mode.DRAW;

	// Type of shape to be drawn (as selected by the user)
	private String shapeType = "ellipse";

	// Current drawing color (modifiable by the user via color chooser)
	private Color color = Color.black;

	// Drawing state variables:
	// 'curr' holds the current shape being drawn or selected for manipulation.
	private Shape curr = null;
	// 'sketch' stores all finalized shapes (local copy of the shared state).
	private Sketch sketch;
	// 'drawFrom' is the initial point when drawing a shape.
	private Point drawFrom = null;
	// 'moveFrom' is used to track the previous mouse location during a move operation.
	private Point moveFrom = null;

	// Communication component to talk with the server.
	private EditorCommunicator comm;

	/**
	 * Constructor: Initializes the editor by setting up the local Sketch,
	 * connecting to the server via EditorCommunicator, and building the GUI.
	 */
	public Editor() {
		super("Collaborative Graphical Editor");
		sketch = new Sketch();
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Create the canvas (where shapes are drawn) and control GUI.
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Layout the components in the main frame.
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Standard initialization for a JFrame.
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Sets up the drawing canvas as a custom JComponent.
	 * This component handles mouse events (press, drag, release) and calls
	 * the corresponding handler methods to update the drawing.
	 *
	 * @return the canvas component.
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		canvas.setPreferredSize(new Dimension(width, height));
		// Mouse press triggers handlePress.
		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}
			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});
		// Mouse drag triggers handleDrag.
		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		return canvas;
	}

	/**
	 * Sets up the GUI controls (shape selection, color chooser, mode selection).
	 *
	 * @return the panel containing GUI controls.
	 */
	private JComponent setupGUI() {
		// Dropdown for selecting shape type.
		String[] shapes = {"ellipse", "rectangle", "segment", "freehand"};
		JComboBox<String> shapeB = new JComboBox<>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<?>)e.getSource()).getSelectedItem());

		// Button and dialog for choosing color.
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(color);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },
				null);
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Radio buttons for selecting the operation mode.
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup();
		modes.add(drawB); modes.add(moveB); modes.add(recolorB); modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0));
		modesP.add(drawB); modesP.add(moveB); modesP.add(recolorB); modesP.add(deleteB);

		// Assemble the controls into a panel.
		JPanel gui = new JPanel(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Draws the current state of the sketch.
	 * Iterates through all shapes stored in the local Sketch and draws them.
	 * Also draws the current shape being drawn (if any).
	 *
	 * @param g the Graphics context to draw on.
	 */
	public void drawSketch(Graphics g) {
		// Draw all finalized shapes from the local sketch.
		for (Shape s : sketch.getShapes()) {
			s.draw(g);
		}
		// Draw the shape that is currently being drawn or manipulated.
		if (curr != null) {
			curr.draw(g);
		}
	}

	/**
	 * Processes incoming messages from the server.
	 * Depending on the command ("add", "move", "recolor", "delete"), this method
	 * updates the local Sketch accordingly. For polyline messages, the message format
	 * is: "add polyline <id> <numPoints> <x1> <y1> ... <rgb>".
	 *
	 * @param msg the incoming message.
	 */
	public void processMessage(String msg) {
		System.out.println("Received from server: " + msg);
		String[] tokens = MessageParser.parseMessage(msg);
		if (tokens.length == 0) return;
		String command = tokens[0];

		if (command.equals("add")) {
			// Handle polyline messages.
			if (tokens[1].equals("polyline")) {
				// Expected format: "add polyline <id> <numPoints> <x1> <y1> ... <rgb>"
				if (tokens.length >= 4) {
					int id = Integer.parseInt(tokens[2]);
					int numPoints = Integer.parseInt(tokens[3]);
					if (tokens.length >= 4 + numPoints * 2 + 1) {
						List<Point> pts = new ArrayList<>();
						int index = 4;
						for (int i = 0; i < numPoints; i++) {
							int px = Integer.parseInt(tokens[index++]);
							int py = Integer.parseInt(tokens[index++]);
							pts.add(new Point(px, py));
						}
						int rgb = Integer.parseInt(tokens[index]);
						Shape s = new Polyline(pts, new Color(rgb));
						// Update the local sketch with the unique ID.
						sketch.addOrUpdateShape(id, s);
					}
				}
			} else {
				// For other shapes (ellipse, rectangle, segment)
				if (tokens.length >= 8) {
					String shapeType = tokens[1];
					int id = Integer.parseInt(tokens[2]);
					int x1 = Integer.parseInt(tokens[3]);
					int y1 = Integer.parseInt(tokens[4]);
					int x2 = Integer.parseInt(tokens[5]);
					int y2 = Integer.parseInt(tokens[6]);
					int rgb = Integer.parseInt(tokens[7]);
					Shape s = null;
					if (shapeType.equals("ellipse")) {
						s = new Ellipse(x1, y1, x2, y2, new Color(rgb));
					} else if (shapeType.equals("rectangle")) {
						s = new Rectangle(x1, y1, x2, y2, new Color(rgb));
					} else if (shapeType.equals("segment")) {
						s = new Segment(x1, y1, x2, y2, new Color(rgb));
					}
					if (s != null) {
						sketch.addOrUpdateShape(id, s);
					}
				}
			}
		} else if (command.equals("move")) {
			// Expected format: "move <id> <dx> <dy>"
			if (tokens.length >= 4) {
				int id = Integer.parseInt(tokens[1]);
				int dx = Integer.parseInt(tokens[2]);
				int dy = Integer.parseInt(tokens[3]);
				sketch.moveShape(id, dx, dy);
			}
		} else if (command.equals("recolor")) {
			// Expected format: "recolor <id> <rgb>"
			if (tokens.length >= 3) {
				int id = Integer.parseInt(tokens[1]);
				int rgb = Integer.parseInt(tokens[2]);
				sketch.recolorShape(id, new Color(rgb));
			}
		} else if (command.equals("delete")) {
			// Expected format: "delete <id>"
			if (tokens.length >= 2) {
				int id = Integer.parseInt(tokens[1]);
				sketch.deleteShape(id);
			}
		}
		repaint();
	}

	/**
	 * Searches the local sketch for the topmost shape that contains the point p.
	 *
	 * @param p the point to check.
	 * @return the shape that contains the point, or null if none is found.
	 */
	private Shape findShapeAt(Point p) {
		List<Shape> shapes = sketch.getShapes();
		// Iterate from last to first for the topmost shape.
		for (int i = shapes.size() - 1; i >= 0; i--) {
			Shape s = shapes.get(i);
			if (s.contains(p.x, p.y)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Handles mouse press events.
	 * In DRAW mode, starts a new shape based on the current shape type.
	 * In MOVE, RECOLOR, and DELETE modes, selects an existing shape at the clicked point.
	 *
	 * @param p the point where the mouse was pressed.
	 */
	private void handlePress(Point p) {
		if (mode == Mode.DRAW) {
			// Create a new shape based on the selected shape type.
			switch (shapeType) {
				case "ellipse":
					curr = new Ellipse(p.x, p.y, color);
					break;
				case "rectangle":
					curr = new Rectangle(p.x, p.y, color);
					break;
				case "segment":
					curr = new Segment(p.x, p.y, color);
					break;
				case "freehand":
				case "polyline":
					curr = new Polyline(p.x, p.y, color);
					break;
				default:
					curr = new Ellipse(p.x, p.y, color);
			}
			drawFrom = p;
		} else {
			// For MOVE, RECOLOR, and DELETE modes, select the shape under the mouse.
			curr = findShapeAt(p);
			if (curr != null) {
				int id = sketch.getIdForShape(curr);
				if (mode == Mode.MOVE) {
					moveFrom = p;
				} else if (mode == Mode.RECOLOR) {
					curr.setColor(color);
					if (id != -1) {
						comm.requestRecolorShape(id, color);
					}
				} else if (mode == Mode.DELETE) {
					if (id != -1) {
						comm.requestDeleteShape(id);
						sketch.deleteShape(id);
						curr = null;
					}
				}
			}
		}
		repaint();
	}

	/**
	 * Handles mouse drag events.
	 * In DRAW mode, updates the dimensions of the shape being drawn.
	 * In MOVE mode, moves the selected shape and sends move requests.
	 *
	 * @param p the current mouse point during dragging.
	 */
	private void handleDrag(Point p) {
		if (mode == Mode.DRAW && curr != null) {
			if (curr instanceof Ellipse) {
				((Ellipse) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			} else if (curr instanceof Rectangle) {
				((Rectangle) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			} else if (curr instanceof Segment) {
				((Segment) curr).setEnd(p.x, p.y);
			} else if (curr instanceof Polyline) {
				((Polyline) curr).addPoint(p.x, p.y);
			}
		} else if (mode == Mode.MOVE && moveFrom != null && curr != null) {
			int dx = p.x - moveFrom.x;
			int dy = p.y - moveFrom.y;
			curr.moveBy(dx, dy);
			int id = sketch.getIdForShape(curr);
			if (id != -1) {
				comm.requestMoveShape(id, dx, dy);
			}
			moveFrom = p;
		}
		repaint();
	}

	/**
	 * Handles mouse release events.
	 * In DRAW mode, sends an "add" request to the server (the shape is added only via server broadcast).
	 * In MOVE mode, ends the move operation.
	 *
	 * @return void.
	 */
	private void handleRelease() {
		if (mode == Mode.DRAW && curr != null) {
			if (curr instanceof Ellipse) {
				Ellipse e = (Ellipse) curr;
				comm.requestAddShape("ellipse", e.getX1(), e.getY1(), e.getX2(), e.getY2(), e.getColor());
			} else if (curr instanceof Rectangle) {
				Rectangle r = (Rectangle) curr;
				comm.requestAddShape("rectangle", r.getX1(), r.getY1(), r.getX2(), r.getY2(), r.getColor());
			} else if (curr instanceof Segment) {
				Segment s = (Segment) curr;
				comm.requestAddShape("segment", s.getX1(), s.getY1(), s.getX2(), s.getY2(), s.getColor());
			} else if (curr instanceof Polyline) {
				// Use the dedicated method for polylines.
				comm.requestAddPolyline((Polyline) curr);
			}
			// Do not add locally; rely on server broadcast to update local sketch.
			curr = null;
			drawFrom = null;
		} else if (mode == Mode.MOVE) {
			moveFrom = null;
		}
		repaint();
	}

	/**
	 * Main method to start the Editor.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Editor());
	}
}