import java.io.*;

/**
 * Utility class for creating and parsing messages for the collaborative sketch application.
 *
 * This class provides static methods to generate properly formatted messages for adding, moving,
 * recoloring, and deleting shapes in the collaborative drawing application. It also includes a
 * method to parse an incoming message string into tokens for further processing.
 *
 * Example usage:
 * <pre>
 *   String addMsg = MessageParser.createAddMessage("rectangle", 100, 100, 200, 200, 16711680);
 *   String[] tokens = MessageParser.parseMessage(addMsg);
 * </pre>
 *
 * @author
 * Triumph Kia Teh | CS10 | Winter 2025
 */
public class MessageParser {

    /**
     * Creates an "add" message.
     *
     * The message format is:
     * "add <shapeType> <x1> <y1> <x2> <y2> <rgb>"
     *
     * @param shapeType the type of shape (e.g., "rectangle", "ellipse", "polyline", "segment")
     * @param x1 first x-coordinate
     * @param y1 first y-coordinate
     * @param x2 second x-coordinate
     * @param y2 second y-coordinate
     * @param rgb integer representation of the color
     * @return the formatted "add" message string
     */
    public static String createAddMessage(String shapeType, int x1, int y1, int x2, int y2, int rgb) {
        return "add " + shapeType + " " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + rgb;
    }

    /**
     * Creates a "move" message.
     *
     * The message format is:
     * "move <id> <dx> <dy>"
     *
     * @param id the shape id
     * @param dx change in x
     * @param dy change in y
     * @return the formatted "move" message string
     */
    public static String createMoveMessage(int id, int dx, int dy) {
        return "move " + id + " " + dx + " " + dy;
    }

    /**
     * Creates a "recolor" message.
     *
     * The message format is:
     * "recolor <id> <rgb>"
     *
     * @param id the shape id
     * @param rgb the new color as an integer
     * @return the formatted "recolor" message string
     */
    public static String createRecolorMessage(int id, int rgb) {
        return "recolor " + id + " " + rgb;
    }

    /**
     * Creates a "delete" message.
     *
     * The message format is:
     * "delete <id>"
     *
     * @param id the shape id to delete
     * @return the formatted "delete" message string
     */
    public static String createDeleteMessage(int id) {
        return "delete " + id;
    }

    /**
     * Parses a message into tokens.
     *
     * This method splits the given message string by whitespace.
     * For example, the message "move 5 10 -5" would be split into:
     * ["move", "5", "10", "-5"]
     *
     * @param msg the incoming message string
     * @return an array of tokens parsed from the message
     */
    public static String[] parseMessage(String msg) {
        return msg.trim().split("\\s+");
    }

    /**
     * Main method for simple testing of MessageParser functionality.
     * It demonstrates the creation and parsing of "add", "move", "recolor", and "delete" messages.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Test "add" message creation and parsing.
        String addMsg = createAddMessage("rectangle", 100, 100, 200, 200, 16711680);
        System.out.println("Add Message: " + addMsg);
        String[] tokens = parseMessage(addMsg);
        for (String token : tokens) {
            System.out.println("Token: " + token);
        }

        // Test "move" message.
        String moveMsg = createMoveMessage(5, 10, -5);
        System.out.println("Move Message: " + moveMsg);

        // Test "recolor" message.
        String recolorMsg = createRecolorMessage(3, 255);
        System.out.println("Recolor Message: " + recolorMsg);

        // Test "delete" message.
        String deleteMsg = createDeleteMessage(2);
        System.out.println("Delete Message: " + deleteMsg);
    }
}
