import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape.
 * A rectangle is defined by an upper-left corner (x1, y1) and a lower-right corner (x2, y2)
 * with the condition that x1 <= x2 and y1 <= y2.
 *
 * This class provides methods to move the rectangle, change its color,
 * determine whether a point lies within its bounds, and draw itself on a Graphics context.
 *
 * The implementation follows the provided scaffold and assignment specifications.
 *
 * @author Triumph Kia Teh | CS10 | Winter 2025
 *
 *------Professors who provided the scaffold and assignment descriptions------ *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 * @author Tim Pierson, Dartmouth CS 10, provided for Winter 2025
 */
public class Rectangle implements Shape {
	private int x1, y1, x2, y2;  // Coordinates: upper-left (x1,y1) and lower-right (x2,y2)
	private Color color;         // The color of the rectangle

	/**
	 * Constructor for a rectangle starting as a single point.
	 * This constructor initializes the rectangle with the same coordinates for both corners,
	 * which can then be updated as the shape is drawn.
	 *
	 * @param x1 the x-coordinate of the starting point
	 * @param y1 the y-coordinate of the starting point
	 * @param color the color of the rectangle
	 */
	public Rectangle(int x1, int y1, Color color) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x1;  // Initially, the rectangle has zero width
		this.y2 = y1;  // Initially, the rectangle has zero height
		this.color = color;
	}

	/**
	 * Constructor for a rectangle defined by two corners.
	 * This constructor initializes the rectangle with the given corner coordinates
	 * after ensuring that the coordinates are arranged correctly.
	 *
	 * @param x1 the x-coordinate of the first corner
	 * @param y1 the y-coordinate of the first corner
	 * @param x2 the x-coordinate of the second corner
	 * @param y2 the y-coordinate of the second corner
	 * @param color the color of the rectangle
	 */
	public Rectangle(int x1, int y1, int x2, int y2, Color color) {
		setCorners(x1, y1, x2, y2);
		this.color = color;
	}

	/**
	 * Sets the corners of the rectangle.
	 * This method ensures that (x1, y1) represents the upper-left corner
	 * and (x2, y2) represents the lower-right corner regardless of the input order.
	 *
	 * @param x1 the x-coordinate of the first point
	 * @param y1 the y-coordinate of the first point
	 * @param x2 the x-coordinate of the second point
	 * @param y2 the y-coordinate of the second point
	 */
	public void setCorners(int x1, int y1, int x2, int y2) {
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	/**
	 * Moves the rectangle by the specified offsets.
	 *
	 * @param dx the change in x-coordinate
	 * @param dy the change in y-coordinate
	 */
	@Override
	public void moveBy(int dx, int dy) {
		x1 += dx;
		y1 += dy;
		x2 += dx;
		y2 += dy;
	}

	/**
	 * Returns the current color of the rectangle.
	 *
	 * @return the color of the rectangle
	 */
	@Override
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color of the rectangle to the specified new color.
	 *
	 * @param color the new color to set
	 */
	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Determines whether the specified point (x, y) lies within the rectangle.
	 *
	 * @param x the x-coordinate of the point to test
	 * @param y the y-coordinate of the point to test
	 * @return true if the point is inside the rectangle, false otherwise
	 */
	@Override
	public boolean contains(int x, int y) {
		return (x >= x1 && x <= x2 && y >= y1 && y <= y2);
	}

	/**
	 * Draws the rectangle on the provided Graphics context.
	 * The rectangle is drawn as a filled shape.
	 *
	 * @param g the Graphics context on which to draw the rectangle
	 */
	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		// Draw a filled rectangle based on the current coordinates.
		g.fillRect(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * Returns a string representation of the rectangle.
	 * The format is:
	 * "rectangle <x1> <y1> <x2> <y2> <rgb>"
	 *
	 * @return a string representing the rectangle's state
	 */
	@Override
	public String toString() {
		return "rectangle " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + color.getRGB();
	}

	/**
	 * Returns the x-coordinate of the upper-left corner.
	 *
	 * @return the x1 coordinate
	 */
	public int getX1() {
		return x1;
	}

	/**
	 * Returns the y-coordinate of the upper-left corner.
	 *
	 * @return the y1 coordinate
	 */
	public int getY1() {
		return y1;
	}

	/**
	 * Returns the x-coordinate of the lower-right corner.
	 *
	 * @return the x2 coordinate
	 */
	public int getX2() {
		return x2;
	}

	/**
	 * Returns the y-coordinate of the lower-right corner.
	 *
	 * @return the y2 coordinate
	 */
	public int getY2() {
		return y2;
	}
}
