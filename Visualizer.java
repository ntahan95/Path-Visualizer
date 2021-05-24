package pathVisualser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Visualizer extends JPanel
implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	GUI ch;
	JFrame window;
	APathfinding aPathFinder;
	boolean btnShowSteps, btnHover;
	int size;
	double a1, a2;
	char currentKey = (char) 0;
	Node startNode, endNode;
	String mode;
	
	Timer timer = new Timer(100, this);

	private boolean aStar = true, dijk = false;
	
	public static void main(String[] args) {
		new Visualizer();
	}
	
	public Visualizer() {
		ch = new GUI(this);
		size = 25;
		mode = "Map Creation";
		btnShowSteps = true;
		btnHover = false;
		setLayout(null);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		// Set up pathfinding
		aPathFinder = new APathfinding(this, size);
		aPathFinder.setDiagonal(true);
		
		// Calculating value of a in speed function 1
		a1 = (5000.0000 / (Math.pow(25.0000/5000, 1/49)));
		a2 = 625.0000;
		
		// Set up window
		window = new JFrame();
		window.setContentPane(this);
		window.setTitle("Algorithm Visualizer");
		window.getContentPane().setPreferredSize(new Dimension(800, 800));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		
		// Add all controls
		ch.addAll();
		
		this.revalidate();
		this.repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Grab dimensions of panel
		int height = getHeight();
		int width = getWidth();
		
		
		if (aStar) {
			// If no path is found
			if (aPathFinder.isNoPath()) {
				// Set timer for animation
				timer.setDelay(50);
				timer.start();
	
				// Set text of "run" button to "clear"
				ch.getB("run").setText("clear");
				
				// Set mode to "No Path"
				mode = "No Path";
				
				g.setColor(Color.RED);
				g.fillRect(0, 0, getWidth(), getHeight());
	
				// Place "No Path" text on screen in center
				ch.noPathTBounds();
				ch.getL("noPathT").setVisible(true);
				this.add(ch.getL("noPathT"));
				this.revalidate();
			}
	
			// If pathfinding is complete (found path)
			if (aPathFinder.isComplete()) {
				
				// Set run button to clear
				ch.getB("run").setText("clear");
				
				// Set timer delay, start for background animation
				timer.setDelay(50);
				timer.start();
	
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				
				// Set completed mode
				if(btnShowSteps) {
					mode = "Completed";
				}
				else {
					mode = "Completed in " + aPathFinder.getRunTime() + "ms";
				}
			}

		}

		// Draws grid
		g.setColor(Color.lightGray);
		for (int j = 0; j < this.getHeight() - 100; j += size) {
			for (int i = 0; i < this.getWidth(); i += size) {
				g.drawRect(i, j, size, size);
			}
		}

		// Draws all borders
		g.setColor(Color.black);
		for (int i = 0; i < aPathFinder.getBorderList().size(); i++) {
			g.fillRect(aPathFinder.getBorderList().get(i).getX() + 1, aPathFinder.getBorderList().get(i).getY() + 1,
					size - 1, size - 1);
		}

		// Draws all open Nodes (path finding nodes)
		for (int i = 0; i < aPathFinder.getOpenList().size(); i++) {
			Node current = aPathFinder.getOpenList().get(i);
			g.setColor(Styling.greenHighlight);
			g.fillRect(current.getX() + 1, current.getY() + 1, size - 1, size - 1);

			drawInfo(current, g);
		}

		// Draws all closed nodes
		for (int i = 0; i < aPathFinder.getClosedList().size(); i++) {
			Node current = aPathFinder.getClosedList().get(i);

			g.setColor(Styling.redHighlight);
			g.fillRect(current.getX() + 1, current.getY() + 1, size - 1, size - 1);

			drawInfo(current, g);
		}

		// Draw all final path nodes
		for (int i = 0; i < aPathFinder.getPathList().size(); i++) {
			Node current = aPathFinder.getPathList().get(i);

			g.setColor(Styling.blueHighlight);
			g.fillRect(current.getX() + 1, current.getY() + 1, size - 1, size - 1);

			drawInfo(current, g);
		}

		// Draws start of path
		if (startNode != null) {
			g.setColor(Color.blue);
			g.fillRect(startNode.getX() + 1, startNode.getY() + 1, size - 1, size - 1);
		}
		// Draws end of path
		if (endNode != null) {
			g.setColor(Color.red);
			g.fillRect(endNode.getX() + 1, endNode.getY() + 1, size - 1, size - 1);
		}
		
		// If control panel is being hovered, change colours
		if(btnHover) {
			g.setColor(Styling.darkText);
			ch.hoverColour();
		}
		else {
			g.setColor(Styling.btnPanel);
			ch.nonHoverColour();
		}
		// Drawing control panel rectangle
		g.fillRect(10, height-96, 782, 90);

		// Setting mode text
		ch.getL("modeText").setText("Mode: " + mode);
		
		// Position all controls
		ch.position();
		
		// Setting numbers in pathfinding lists
		ch.getL("openC").setText(Integer.toString(aPathFinder.getOpenList().size()));
		ch.getL("closedC").setText(Integer.toString(aPathFinder.getClosedList().size()));
		ch.getL("pathC").setText(Integer.toString(aPathFinder.getPathList().size()));
				
		// Setting speed number text in showSteps or !showSteps mode
		if(btnShowSteps) {
			ch.getL("speedC").setText(Integer.toString(ch.getS("speed").getValue()));
		}
		else {
			ch.getL("speedC").setText("N/A");
		}
					
		// Getting values from checkboxes
		btnShowSteps = ch.getC("showStepsCheck").isSelected();
		aPathFinder.setDiagonal(ch.getC("diagonalCheck").isSelected());
		aPathFinder.setTrig(ch.getC("trigCheck").isSelected());
	}
	
	// Draws info (f, g, h) on current node
	public void drawInfo(Node current, Graphics g) {
		if (size > 50) {
			g.setFont(Styling.numbers);
			g.setColor(Color.black);
			g.drawString(Integer.toString(current.getF()), current.getX() + 4, current.getY() + 16);
			g.setFont(Styling.smallNumbers);
			g.drawString(Integer.toString(current.getG()), current.getX() + 4, current.getY() + size - 7);
			g.drawString(Integer.toString(current.getH()), current.getX() + size - 26, current.getY() + size - 7);
		}
	}

	public void MapCalculations(MouseEvent e) {
		// If left mouse button is clicked
		if (SwingUtilities.isLeftMouseButton(e)) {
			// If 's' is pressed create start node
			if (currentKey == 's') {
				int xRollover = e.getX() % size;
				int yRollover = e.getY() % size;

				if (startNode == null) {
					startNode = new Node(e.getX() - xRollover, e.getY() - yRollover);
				} else {
					startNode.setXY(e.getX() - xRollover, e.getY() - yRollover);
				}
				repaint();
			} 
			// If 'e' is pressed create end node
			else if (currentKey == 'e') {
				int xRollover = e.getX() % size;
				int yRollover = e.getY() % size;

				if (endNode == null) {
					endNode = new Node(e.getX() - xRollover, e.getY() - yRollover);
				} else {
					endNode.setXY(e.getX() - xRollover, e.getY() - yRollover);
				}
				repaint();
			} 
			// Otherwise, create a wall
			else {
				
				if (e.getY() < this.getHeight() - 100) {
					int xBorder = e.getX() - (e.getX() % size);
					int yBorder = e.getY() - (e.getY() % size);
	
					Node newBorder = new Node(xBorder, yBorder);
					aPathFinder.addBorder(newBorder);
	
					repaint();
				}
			}
		} 
		// If right mouse button is clicked
		else if (SwingUtilities.isRightMouseButton(e)) {
			int mouseBoxX = e.getX() - (e.getX() % size);
			int mouseBoxY = e.getY() - (e.getY() % size);

			// If 's' is pressed remove start node
			if (currentKey == 's') {
				if (startNode != null && mouseBoxX == startNode.getX() && startNode.getY() == mouseBoxY) {
					startNode = null;
					repaint();
				}
			} 
			// If 'e' is pressed remove end node
			else if (currentKey == 'e') {
				if (endNode != null && mouseBoxX == endNode.getX() && endNode.getY() == mouseBoxY) {
					endNode = null;
					repaint();
				}
			} 
			// Otherwise, remove wall
			else {
				int Location = aPathFinder.searchBorder(mouseBoxX, mouseBoxY);
				if (Location != -1) {
					aPathFinder.removeBorder(Location);
				}
				repaint();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		MapCalculations(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		MapCalculations(e);
	}

	@Override
	// Track mouse on movement
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int height = this.getHeight();
		
		// Detects if mouse is within button panel
		if(x >= 10 && x <= 792 && y >= (height-96) && y <= (height-6)) {
			btnHover = true;
		}
		else {
			btnHover = false;
		}
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		currentKey = key;

		// Start if space is pressed
		if (currentKey == KeyEvent.VK_SPACE) {
			ch.getB("run").setText("stop");
			start();
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		currentKey = (char) 0;
	}
	
	// Starts path finding
	void start() {
		if(startNode != null && endNode != null) {
			if (!btnShowSteps) {
				aPathFinder.start(startNode, endNode);
			} else {
				aPathFinder.setup(startNode, endNode);
				setSpeed();
				timer.start();
			}
		}
		else {
			System.out.println("ERROR: Needs start and end points to run.");
		}
	}
	
	@Override
	// Scales the map with mouse wheel scroll
	public void mouseWheelMoved(MouseWheelEvent m) {
		int rotation = m.getWheelRotation();
		double prevSize = size;
		int scroll = 3;

		// Changes size of grid based on scroll
		if (rotation == -1 && size + scroll < 200) {
			size += scroll;
		} else if (rotation == 1 && size - scroll > 2) {
			size += -scroll;
		}
		aPathFinder.setSize(size);
		double ratio = size / prevSize;

		// new X and Y values for Start
		if (startNode != null) {
			int sX = (int) Math.round(startNode.getX() * ratio);
			int sY = (int) Math.round(startNode.getY() * ratio);
			startNode.setXY(sX, sY);
		}

		// new X and Y values for End
		if (endNode != null) {
			int eX = (int) Math.round(endNode.getX() * ratio);
			int eY = (int) Math.round(endNode.getY() * ratio);
			endNode.setXY(eX, eY);
		}

		// new X and Y values for borders
		for (int i = 0; i < aPathFinder.getBorderList().size(); i++) {
			int newX = (int) Math.round((aPathFinder.getBorderList().get(i).getX() * ratio));
			int newY = (int) Math.round((aPathFinder.getBorderList().get(i).getY() * ratio));
			aPathFinder.getBorderList().get(i).setXY(newX, newY);
		}

		// New X and Y for Open nodes
		for (int i = 0; i < aPathFinder.getOpenList().size(); i++) {
			int newX = (int) Math.round((aPathFinder.getOpenList().get(i).getX() * ratio));
			int newY = (int) Math.round((aPathFinder.getOpenList().get(i).getY() * ratio));
			aPathFinder.getOpenList().get(i).setXY(newX, newY);
		}

		// New X and Y for Closed Nodes
		for (int i = 0; i < aPathFinder.getClosedList().size(); i++) {
			if (!Node.isEqual(aPathFinder.getClosedList().get(i), startNode)) {
				int newX = (int) Math.round((aPathFinder.getClosedList().get(i).getX() * ratio));
				int newY = (int) Math.round((aPathFinder.getClosedList().get(i).getY() * ratio));
				aPathFinder.getClosedList().get(i).setXY(newX, newY);
			}
		}
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Moves one step ahead in path finding (called on timer)
		if (aPathFinder.isRunning() && btnShowSteps) {
			aPathFinder.findPath(aPathFinder.getPar());
			mode = "Running";
		}
		
		// Actions of run/stop/clear button
		if(e.getActionCommand() != null) {
			if(e.getActionCommand().equals("run") && !aPathFinder.isRunning()) {
				ch.getB("run").setText("stop");
				start();
			}
			else if(e.getActionCommand().equals("clear")) {
				ch.getB("run").setText("run");
				mode = "Map Creation";
				ch.getL("noPathT").setVisible(false);
				aPathFinder.reset();
			}
			else if(e.getActionCommand().equals("stop")) {
				ch.getB("run").setText("start");
				timer.stop();
			}
			else if(e.getActionCommand().equals("start")) {
				ch.getB("run").setText("stop");
				timer.start();
			}
		}
		repaint();
		
	}
	
	// Returns random number between min and max
	int randomWithRange(int min, int max)
	{
	   int range = (max - min) + 1;     
	   return (int)(Math.random() * range) + min;
	}
	
	// Calculates delay with two exponential functions
	void setSpeed() {
		int delay = 0;
		int value = ch.getS("speed").getValue();
		
		if(value == 0) {
			timer.stop();
		}
		else if(value >= 1 && value < 50) {
			if(!timer.isRunning()) {
				timer.start();
			}
			// Exponential function. value(1) == delay(5000). value (50) == delay(25)
			delay = (int)(a1 * (Math.pow(25/5000.0000, value / 49.0000)));
		}
		else if(value >= 50 && value <= 100) {
			if(!timer.isRunning()) {
				timer.start();
			}
			// Exponential function. value (50) == delay(25). value(100) == delay(1).
			delay = (int)(a2 * (Math.pow(1/25.0000, value/50.0000)));
		}
		timer.setDelay(delay);
	}
	
	boolean showSteps() {
		return btnShowSteps;
	}
	
}
