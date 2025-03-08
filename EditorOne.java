import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Basic shape drawing GUI
 *
 * @author Tim Pierson, Dartmouth CS 10, based on prior term code, provided for Winter 2025
 */

public class EditorOne extends JFrame {	
	private static final int width = 800, height = 800;

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	private Ellipse shape = null;				// the only object (if any) in our editor
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged

	public EditorOne() {
		super("Graphical Editor");

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				// Call helper method to draw the sketch on g
				drawSketch(g);
				System.out.println("repainting!");
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				// Call helper method to handle the mouse press
				handlePress(event.getPoint());
				System.out.println("pressed at "+event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				// Call helper method to handle the mouse release
				handleRelease();
				System.out.println("released at "+event.getPoint());
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				// Call helper method to handle the mouse drag
				handleDrag(event.getPoint());
				System.out.println("dragged to "+event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}
	
	/**
	 * Helper method for press at point
	 */
	private void handlePress(Point p) {
		// In drawing mode, start drawing a new shape
		// In moving mode, start dragging if clicked in the shape
		// In recoloring mode, change the shape's color if clicked in it
		// In deleting mode, delete the shape if clicked in it
		// Be sure to refresh the canvas (repaint) if the appearance has changed
		
		if (mode == Mode.DRAW) {
			//draw new shape and set drawFrom
			shape = new Ellipse(p.x,p.y,color);
			drawFrom = p;
		}
		else if (mode == Mode.MOVE) {
			//move if clicked in shape
			if (shape.contains(p.x, p.y)) {
				moveFrom = p;
			}
		}
		else if (mode == Mode.RECOLOR) {
			//recolor
			if (shape.contains(p.x, p.y)) {
				shape.setColor(color);
			}
		}
		else if (mode == Mode.DELETE) {
			//delete
			if (shape.contains(p.x, p.y)) {
				shape = null;
			}
		}
		repaint();
	}

	/**
	 * Helper method for drag to new point
	 */
	private void handleDrag(Point p) {
		// In drawing mode, revise the shape as it is stretched out
		// In moving mode, shift the object and keep track of where next step is from
		// Be sure to refresh the canvas (repaint) if the appearance has changed
		if (mode == Mode.DRAW) {
			//draw
			shape.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
		}
		else if (mode == Mode.MOVE) {
			if (moveFrom != null) {
				shape.moveBy(p.x - moveFrom.x, p.y - moveFrom.y);
				moveFrom = p;
			}
		}
		repaint();
	}

	/**
	 * Helper method for release
	 */
	private void handleRelease() {
		// In moving mode, stop dragging the object
		// Be sure to refresh the canvas (repaint) if the appearance has changed
		moveFrom = null;
	}

	/**
	 * Draw the whole sketch (here maybe a single shape)
	 */
	private void drawSketch(Graphics g) {
		// Draw the current shape if it exists
		if (shape != null) {
			shape.draw(g);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new EditorOne();
			}
		});	
	}
}
