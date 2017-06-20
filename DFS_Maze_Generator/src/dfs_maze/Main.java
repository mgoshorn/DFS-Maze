package dfs_maze;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * 
 * @author Mitchell Goshorn
 *
 * CONFIGURATION
 * 
 * * DIFFICULTY - a double between 0 and 1. Changes the odds that the neighbors will be shuffled before returned. 0 means it will never be
 * shuffled, 1 means it will always be shuffled. This affects the difficulty of the returned maze. 1 is left as default, but if this is too
 * challenging for the size, the number can be lowered to make the maze easier to solve. .75 is a good value for a significantly easier
 * maze. Below 0.5 can quickly make it trivial.
 * 
 * 
 * *STEP_THROUGH_MAZE_GENERATION - When true will use a timer to generate the maze step by step, so the process can be viewed by a user, when
 * false will immediately generate the maze.
 * 
 * *STEP_THROUGH_SOLUTION_GENERATION - When true will use a timer to step solve the maze over time from start to finish, when false will immediately
 * complete the maze.
 * 
 * 
 * 
 */
public class Main extends JPanel {
	private static final long serialVersionUID = 0L;
	/*
	 * Delays used for timers
	 */
	
	public static final boolean STEP_THROUGH_MAZE_GENERATION = false;
	public static final boolean STEP_THROUGH_SOLUTION_GENERATION = false;
	public static final double DIFFICULTY = .75;
	
	
	//Used for testing, if SIZE_OVERRIDE true, then overrides matrix size with the value provided in SIZE_OVERRIDE_VALUE
	public static final boolean SIZE_OVERRIDE = false;
	public static final int SIZE_OVERRIDE_VALUE = 25;
	
	public static final int EDGES_DRAWN_PER_REPAINT = 1;

	public static final int CREATE_EDGE_DELAY 	= 1000/60;
	public static final int REPAINT_DELAY		= CREATE_EDGE_DELAY * EDGES_DRAWN_PER_REPAINT;
	
	public static final int MAX_NODES_X		= 100;
	public static final int MAX_NODES_Y		= 100;
	
	/*
	 * Constant NODE_SIZE defines the width and height of a single node rectangle in the matrix.
	 * Constant NODE_PADDING defines the padding each node has around the node rectangle.
	 * Constant NODE_TOTAL_SIZE used to hold the total size each node will utilize in the view.
	 * Constant MAP_PADDING defines the padding around the matrix.
	 * Constant WINDOW_UNDECORATED used to constrain visual elements when 
	 */
	public static final int NODE_SIZE		= 25;
	public static final int NODE_PADDING	= 5;
	public static final int NODE_TOTAL_SIZE = NODE_SIZE + NODE_PADDING * 2;
	public static final int MAP_PADDING 	= 25;
	public static final boolean WINDOW_UNDECORATED = true;
	
	public static final Color SOLUTION_START_COLOR = Color.GREEN;
	public static final Color SOLUTION_END_COLOR = Color.RED;
	
	
	public static int node_size;
	public static int total_node_size;
	public static int nodes_x;
	public static int nodes_y;
	
	public static Node[][] nodeMap;
	public static LinkedList<Node> stack = new LinkedList<>();
	public static ArrayList<Node> solutionList = new ArrayList<>();
	public static Node startNode;
	public static Node goalNode;
	public static int solutionStepsDrawn = 0;

	/**
	 * Method sets up display elements.
	 * @return the configured JFrame instance.
	 */
	public JFrame setupDisplay() {
		JFrame application = new JFrame();
		application.add(this);
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		application.setSize(1000, 300);
		application.setExtendedState(JFrame.MAXIMIZED_BOTH);
		application.setBackground(Color.BLACK);
		application.setUndecorated(WINDOW_UNDECORATED);
		application.setVisible(true);
		return application;
	}
	
	public void initializeGraph(JFrame display) {
		/* Find the maximum number which can fit onto the screen */
		nodes_x = ((int)display.getWidth() - MAP_PADDING * 2) / NODE_TOTAL_SIZE;
		nodes_y = ((int)display.getHeight() - MAP_PADDING * 2) / NODE_TOTAL_SIZE;
		
		//Testing config, for overriding automatic settings
		if(SIZE_OVERRIDE) {
			nodes_x = SIZE_OVERRIDE_VALUE;
			nodes_y = SIZE_OVERRIDE_VALUE;
		}
		
		  
		
		/* If number greater than the max, set it to max */
		if(nodes_x > MAX_NODES_X) nodes_x = MAX_NODES_X;
		if(nodes_y > MAX_NODES_Y) nodes_y = MAX_NODES_Y;
		if(!WINDOW_UNDECORATED) nodes_y--;
		
		nodeMap = new Node[nodes_x][nodes_y];
		
		
		/**
		 * Loops for instantiating Node matrix objects
		 */
		for(int x = 0; x < nodeMap.length; x++) {
			for(int y = 0; y < nodeMap[x].length; y++) {
				if(x != 0 && x != nodeMap.length - 1) {
					//Instantiate general maze area (x = 1 ~ n-2).
					nodeMap[x][y] = new Node(x, y);
				} else if( (x == 0 && y == 0) || (x == nodeMap.length - 1 && y == nodeMap[nodeMap.length - 1].length - 1) ) {
					//Instantiate starting and end nodes.
					nodeMap[x][y] = new Node(x, y);
				} else {
					//Instantiate remaining Nodes as impassable
					nodeMap[x][y] = new Node(x, y, false);
				}
			}
		}
		
		//Create static reference to start and goal nodes.
		startNode = nodeMap[0][0];
		goalNode = nodeMap[nodeMap.length - 1][nodeMap[nodeMap.length - 1].length - 1];
	}
	
	
	public static Color combineColors(Color a, Color b, double index) {
		return new Color((int)((1 - index) * a.getRed() + index * b.getRed()),
				(int)((1 - index) * a.getGreen() + index * b.getGreen()),
				(int)((1 - index) * a.getBlue() + index * b.getBlue()),
				(int)((1 - index) * a.getAlpha() + index * b.getAlpha()));
	}
	@Override
	public void paintComponent(Graphics g) {
		super.repaint();
		
		Graphics2D g2 = (Graphics2D)g;
		for(Node[] column : nodeMap) {
			for(Node n : column) {
				n.drawEdges(g2);
			}
		}
		
		if(!STEP_THROUGH_SOLUTION_GENERATION) {
			for(int steps = 0; steps < solutionList.size(); steps++) {
				double index = (double)steps / solutionList.size();
				Color color = combineColors(SOLUTION_START_COLOR, SOLUTION_END_COLOR, index);
				solutionList.get(steps).drawSolutionEdge(g2, color);
			}
		}
		else {
			for(int steps = 0; steps < solutionStepsDrawn && steps < solutionList.size(); steps++) {
				double index = (double)steps / solutionStepsDrawn;
				Color color = combineColors(SOLUTION_START_COLOR, SOLUTION_END_COLOR, index);
				solutionList.get(steps).drawSolutionEdge(g2, color);
			}
			
			/* refactored to use list
			Node n = goalNode;
			while(steps < solutionStepsDrawn && n.getParent() != null) {
				n.drawSolutionEdge(g2);
				n = n.getParent();
				steps++;
			}
			*/
		}
		
	}
	
	/**
	 * Method pops the stack and adds an edge
	 * 
	 * @return boolean false when stack is empty, used to stop timer
	 */
	public boolean addEdge() {
		if(stack.isEmpty()) return false;
		Node n = stack.pollLast();
		while(n.isVisited() && !stack.isEmpty()) {
			n = stack.pollLast();
		}
		if(n.getParent() != null) n.getParent().Edge.add(n);
		if(n.isVisited() && stack.isEmpty()) {
			return false;
		}
		n.setVisited();
		
		Node[] neighbors = n.findNeighbors(nodeMap);
		
		for(Node neighbor : neighbors) {
			if(neighbor == null) continue;
			neighbor.setParent(n);
			stack.add(neighbor);
		}
		if(stack.isEmpty()) return false;
		return true;
	}
	
	public void fillEdgeList() {
		while(!stack.isEmpty()) {
			addEdge();
		}
	}
	
	public void generateSolutionList() {
		//Creates list to use for drawing the solution
		Node n = goalNode;
		while(n != null) {
			solutionList.add(n);
			n = n.getParent();
		}
		Collections.reverse(solutionList);
	}
	
	public static void main(String[] args) {
		Main application = new Main();
		JFrame display = application.setupDisplay();
		application.initializeGraph(display);
		display.repaint();
		
		stack.add(nodeMap[0][0]);
		
		ActionListener repainter = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				display.repaint();
			}
		};
		
		
		ActionListener solutionStep = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				solutionStepsDrawn++;
			}
		};
		Timer solutionStepTimer = new Timer(CREATE_EDGE_DELAY, solutionStep);
		ActionListener addEdge = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				application.addEdge();
				
				if(!application.addEdge()) {
					application.generateSolutionList();
					if(STEP_THROUGH_SOLUTION_GENERATION) {
						solutionStepTimer.start();
					}
					((Timer)e.getSource()).stop();
				}
			}
		};
		
		
		
		Timer repaintTimer = new Timer(REPAINT_DELAY, repainter);
		Timer edgeTimer = new Timer(CREATE_EDGE_DELAY, addEdge);
		
		if(STEP_THROUGH_MAZE_GENERATION) edgeTimer.start();
		else {
			application.fillEdgeList();
			if(STEP_THROUGH_SOLUTION_GENERATION) {
				application.generateSolutionList();
				solutionStepTimer.start();
			} else {
				application.generateSolutionList();
			}
		}
		  
		repaintTimer.start();
		
		
	}
}
