import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points.
 * This class represents freehand drawing or a polyline.
 *
 * @author Triumph Kia Teh | CS10 | Winter 2025
 *
 *------Professors who provided the scaffold and assignment descriptions------ *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author Tim Pierson, Dartmouth CS 10, provided for Winter 2025
 */
public class Polyline implements Shape {
	private List<Point> points;   // List to store the joint points of the polyline
	private Color color;          // The color of the polyline

	/**
	 * Constructor that creates a polyline starting at a given point with the specified color.
	 *
	 * @param x the starting x-coordinate
	 * @param y the starting y-coordinate
	 * @param color the initial color of the polyline
	 */
	public Polyline(int x, int y, Color color) {
		points = new ArrayList<Point>();
		points.add(new Point(x, y));
		this.color = color;
	}

	/**
	 * Constructor that creates a polyline from a pre-defined list of points with the specified color.
	 *
	 * @param pts the list of points defining the polyline
	 * @param color the color of the polyline
	 */
	public Polyline(List<Point> pts, Color color) {
		points = new ArrayList<Point>(pts);
		this.color = color;
	}

	/**
	 * Adds a new point to the polyline.
	 *
	 * @param x the x-coordinate of the new point
	 * @param y the y-coordinate of the new point
	 */
	public void addPoint(int x, int y) {
		points.add(new Point(x, y));
	}

	/**
	 * Moves the entire polyline by translating each point by the given offsets.
	 *
	 * @param dx the offset to add to each point's x-coordinate
	 * @param dy the offset to add to each point's y-coordinate
	 */
	@Override
	public void moveBy(int dx, int dy) {
		for (Point p : points) {
			p.translate(dx, dy);
		}
	}

	/**
	 * Returns the current color of the polyline.
	 *
	 * @return the polyline's color
	 */
	@Override
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color of the polyline to the specified new color.
	 *
	 * @param color the new color to set
	 */
	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Determines if the given point (x, y) is close enough to any segment of the polyline.
	 * It leverages the static method Segment.pointToSegmentDistance to measure the distance
	 * between the point and each segment of the polyline.
	 *
	 * @param x the x-coordinate of the point to test
	 * @param y the y-coordinate of the point to test
	 * @return true if the point is near any segment of the polyline (within a distance of 3 pixels), false otherwise
	 */
	@Override
	public boolean contains(int x, int y) {
		if (points.size() < 2)
			return false;
		for (int i = 0; i < points.size() - 1; i++) {
			Point p1 = points.get(i);
			Point p2 = points.get(i + 1);
			if (Segment.pointToSegmentDistance(x, y, p1.x, p1.y, p2.x, p2.y) <= 3)
				return true;
		}
		return false;
	}

	/**
	 * Draws the polyline on the provided Graphics context.
	 * It iterates over the list of points and draws lines between consecutive points.
	 *
	 * @param g the Graphics context on which to draw the polyline
	 */
	@Override
	public void draw(Graphics g) {
		if (points.isEmpty())
			return;
		g.setColor(color);
		for (int i = 0; i < points.size() - 1; i++) {
			Point p1 = points.get(i);
			Point p2 = points.get(i + 1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	/**
	 * Converts the polyline to a string representation.
	 * The format is:
	 * "polyline <numPoints> <x1> <y1> <x2> <y2> ... <colorRGB>"
	 * This format is used for network message passing.
	 *
	 * @return a string representing the polyline
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("polyline ").append(points.size()).append(" ");
		for (Point p : points) {
			sb.append(p.x).append(" ").append(p.y).append(" ");
		}
		sb.append(color.getRGB());
		return sb.toString();
	}

	/**
	 * Returns the x-coordinate of the first point in the polyline.
	 *
	 * @return the x-coordinate of the first point
	 */
	public int getX1() {
		return points.get(0).x;
	}

	/**
	 * Returns the y-coordinate of the first point in the polyline.
	 *
	 * @return the y-coordinate of the first point
	 */
	public int getY1() {
		return points.get(0).y;
	}

	/**
	 * Returns the x-coordinate of the last point in the polyline.
	 *
	 * @return the x-coordinate of the last point
	 */
	public int getX2() {
		return points.get(points.size() - 1).x;
	}

	/**
	 * Returns the y-coordinate of the last point in the polyline.
	 *
	 * @return the y-coordinate of the last point
	 */
	public int getY2() {
		return points.get(points.size() - 1).y;
	}
}
