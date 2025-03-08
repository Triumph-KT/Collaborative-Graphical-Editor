import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Sketch class maintains a collection of shapes, each with a unique ID.
 * It is used by both the server and client to keep a consistent shared view
 * of the drawing. All public methods are synchronized to ensure thread safety
 * in a multi-client environment.
 *
 * Key functionalities include:
 * - Adding a shape (with a unique ID assignment).
 * - Deleting, moving, and recoloring shapes by their unique ID.
 * - Retrieving the current state as a list or as a formatted string for message passing.
 *
 * A TreeMap is used to store shapes, so that shapes are maintained in sorted order
 * by their unique IDs.
 *
 * Author: Triumph Kia Teh | CS10 | Winter 2025
 */
public class Sketch {
    // TreeMap to store shapes in sorted order by their unique IDs.
    private Map<Integer, Shape> shapes;
    // Counter to assign unique IDs to shapes.
    private int nextId;

    /**
     * Constructor that initializes an empty sketch.
     */
    public Sketch() {
        shapes = new TreeMap<>();
        nextId = 1;
    }

    /**
     * Adds a shape to the sketch, assigns it a unique ID, and returns that ID.
     *
     * @param s the shape to add
     * @return the unique ID assigned to the shape
     */
    public synchronized int addShape(Shape s) {
        int id = nextId++;
        shapes.put(id, s);
        return id;
    }

    /**
     * Adds a shape with a given ID or updates it if it already exists.
     * This is useful for updating the local sketch when receiving messages.
     *
     * @param id the unique ID to associate with the shape
     * @param s the shape to add or update
     */
    public synchronized void addOrUpdateShape(int id, Shape s) {
        shapes.put(id, s);
    }

    /**
     * Checks if the sketch contains a shape with the given ID.
     *
     * @param id the unique ID to check
     * @return true if a shape with the given ID exists, false otherwise
     */
    public synchronized boolean containsId(int id) {
        return shapes.containsKey(id);
    }

    /**
     * Deletes the shape with the given ID from the sketch.
     *
     * @param id the unique ID of the shape to delete
     * @return true if the shape was successfully deleted, false if not found
     */
    public synchronized boolean deleteShape(int id) {
        return shapes.remove(id) != null;
    }

    /**
     * Moves the shape with the given ID by the specified offsets.
     *
     * @param id the unique ID of the shape to move
     * @param dx the change in x-coordinate
     * @param dy the change in y-coordinate
     */
    public synchronized void moveShape(int id, int dx, int dy) {
        Shape s = shapes.get(id);
        if (s != null) {
            s.moveBy(dx, dy);
        }
    }

    /**
     * Recolors the shape with the given ID to the specified new color.
     *
     * @param id the unique ID of the shape to recolor
     * @param newColor the new color to set
     */
    public synchronized void recolorShape(int id, Color newColor) {
        Shape s = shapes.get(id);
        if (s != null) {
            s.setColor(newColor);
        }
    }

    /**
     * Retrieves a list of all shapes currently in the sketch.
     *
     * @return an ArrayList containing all shapes
     */
    public synchronized List<Shape> getShapes() {
        return new ArrayList<>(shapes.values());
    }

    /**
     * Returns a string representation of the entire sketch.
     * Each line contains a shape's unique ID and its string representation.
     * This is useful for sending the full state to a new client.
     *
     * @return a formatted string representing the sketch
     */
    public synchronized String toMessageString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Shape> entry : shapes.entrySet()) {
            sb.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().toString())
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * Retrieves the unique ID associated with the given shape.
     * Returns -1 if the shape is not found.
     *
     * Note: This method assumes that the Shape classes correctly implement equals().
     *
     * @param s the shape to look up
     * @return the unique ID of the shape, or -1 if not found
     */
    public synchronized int getIdForShape(Shape s) {
        for (Map.Entry<Integer, Shape> entry : shapes.entrySet()) {
            if (entry.getValue().equals(s)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * Main method for independent testing of the Sketch class.
     * It adds sample shapes, performs move and recolor operations, and then deletes one shape.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Sketch sketch = new Sketch();
        // Create sample shapes using Ellipse and Segment implementations.
        Shape ellipse = new Ellipse(50, 50, 150, 150, Color.RED);
        Shape segment = new Segment(200, 200, 300, 300, Color.BLUE);
        int ellipseId = sketch.addShape(ellipse);
        int segmentId = sketch.addShape(segment);
        System.out.println("After adding shapes:");
        System.out.println(sketch.toMessageString());
        // Move the ellipse and recolor the segment.
        sketch.moveShape(ellipseId, 10, 20);
        sketch.recolorShape(segmentId, Color.GREEN);
        System.out.println("After moving ellipse and recoloring segment:");
        System.out.println(sketch.toMessageString());
        // Delete the segment.
        sketch.deleteShape(segmentId);
        System.out.println("After deleting segment:");
        System.out.println(sketch.toMessageString());
    }
}
