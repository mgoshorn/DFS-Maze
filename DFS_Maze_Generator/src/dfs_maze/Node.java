package dfs_maze;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * 
 * @author MitchellGoshorn
 * 
 *
 */
public class Node {
	private static final Color DEFAULT_NODE_COLOR =	Color.RED;
	private static final Color EDGE_COLOR = 		Color.WHITE;
	private static final Color SOLUTION_EDGE =		Color.RED;
	private static final int NEIGHBOR_SHIFT = (int)(Math.random() * 4);
	
	private static final BasicStroke EDGE_STROKE = new BasicStroke(20.0f,
			BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	
	private boolean visited, passable;
	private int x, y;
	
	Rectangle2D rectangle;
	private Node parent;
	ArrayList<Node> Edge = new ArrayList<>();
	
	
	/* START 	Getters, Setters */
	public boolean isVisited() {
		return visited;
	}

	public void setVisited() {
		this.visited = true;
	}
	
	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	/* END 		Getters, Setters */
	
	
	/**
	 * Constructor for Node which assumes passable to be true
	 * @param x2 	x-value to be assigned
	 * @param y2 	y-value to be assigned
	 */
	public Node(int x2, int y2) {
		this.x = x2;
		this.y = y2;
		this.passable = true;
		this.rectangle = new Rectangle2D.Double( x2 * Main.NODE_TOTAL_SIZE + Main.MAP_PADDING,
				y2 * Main.NODE_TOTAL_SIZE + Main.MAP_PADDING,
				Main.NODE_SIZE, Main.NODE_SIZE);
	}
	
	/**
	 * Constructor for assigning passable state
	 * @param x2 	x-value to be assigned
	 * @param y2 	y-value to be assigned
	 * @param b  	passable state to be assigned
	 */
	public Node(int x2, int y2, boolean b) {
		this.x = x2;
		this.y = y2;
		this.passable = b;
	}

	/**
	 * 
	 * Finds all accessible surrounding neighbors of the Node and returns them as an array of Nodes in random order.
	 * 
	 * @param NodeMap	A 2D array of Nodes, used to find the neighbors of the Node the method is called upon
	 * @return			A 1D array of all accessible neighboring nodes ordered randomly.
	 */
	public Node[] findNeighbors(Node[][] NodeMap) {
		LinkedList<Node> AccessibleNeighbors = new LinkedList<>();
		//Iterate through nearby nodes
		for(int dx = this.x - 1; dx <= this.x + 1; dx++) {
			for(int dy = this.y - 1; dy <= this.y + 1; dy++) {
				//Check to make sure indices are in range
				if(dx >= 0 && dy >= 0 && dx < NodeMap.length && dy < NodeMap[dx].length) {
					if(Math.abs(x - dx) + Math.abs(y - dy) != 1) continue;
					//Temporarily assign to neater reference
					Node neighbor = NodeMap[dx][dy];
					//Confirm neighbor has not already been visited and that it can be passed
					if(!neighbor.visited && neighbor.passable && !this.equals(neighbor)) {
						//Add neighbor to list of accessible neighbors
						AccessibleNeighbors.add(neighbor);
					}
				}
			}
		}
		//Randomize order of array so that the maze contains randomly placed edges
		if(Math.random() < Main.DIFFICULTY) { Collections.shuffle(AccessibleNeighbors); }
		else {
			for(int i = 0; i < NEIGHBOR_SHIFT; i++) {
				AccessibleNeighbors.add(AccessibleNeighbors.poll());
			}
		}
		return AccessibleNeighbors.toArray(new Node[0]);
		
	}
	
	/**
	 * Draw the rectangle for a given Node to the display.
	 * @param g 	Graphics2D object.
	 */
	public void draw(Graphics2D g) {
		if(!this.passable) return;
		g.setColor(DEFAULT_NODE_COLOR);
		g.draw(this.rectangle);
	}
	
	
	/**
	 * Draw edges between Nodes.
	 * @param g 	Graphics2D object
	 */
	public void drawEdges(Graphics2D g) {
		g.setColor(EDGE_COLOR);
		g.setStroke(EDGE_STROKE);
		for(Node e : this.Edge) {
			g.drawLine((int)this.rectangle.getCenterX(), (int)this.rectangle.getCenterY(), 
					(int)e.rectangle.getCenterX(), (int)e.rectangle.getCenterY());
		}
	}
	
	/**
	 * Draw the solution path.
	 * @param g		Graphics2D object
	 */
	public void drawSolution(Graphics2D g) {
		g.setColor(SOLUTION_EDGE);
		g.setStroke(EDGE_STROKE);
		Node n = this;
		while(n.parent != null) {
			n.drawSolutionEdge(g);
			n = n.parent;
		}
	}
	
	/**
	 * Draws individual edge of the solution map
	 * @param 	Graphics2D object
	 */
	public void drawSolutionEdge(Graphics2D g) {
		if(this.parent == null) return;
		g.setColor(SOLUTION_EDGE);
		g.setStroke(EDGE_STROKE);
		g.drawLine((int)this.rectangle.getCenterX(), (int)this.rectangle.getCenterY(),
				(int)this.parent.rectangle.getCenterX(), (int)this.parent.rectangle.getCenterY());
	}
	
	public void drawSolutionEdge(Graphics2D g, Color color) {
		if(this.parent == null) return;
		g.setColor(color);
		g.setStroke(EDGE_STROKE);
		g.drawLine((int)this.rectangle.getCenterX(), (int)this.rectangle.getCenterY(),
				(int)this.parent.rectangle.getCenterX(), (int)this.parent.rectangle.getCenterY());
	}
}
