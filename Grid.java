import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.core.PVector;

public class Grid {
	private PApplet parent;
	private int cols;
	private int rows;
	private int grid_size;
	private Node[][] nodes;
	private Tree[] obstacles;

	// ***************************************************
	public Grid(int _cols, int _rows, int _grid_size, PApplet parent, Tree[] obstacles) {
		this.parent = parent;
		this.obstacles = obstacles;
		cols = _cols;
		rows = _rows;
		grid_size = _grid_size;
		nodes = new Node[cols][rows];
		createGridNodes();
	}

	// ***************************************************
	private void createGridNodes() {

		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				// Initialize each object
				nodes[i][j] = new Node(i, j, i * grid_size + grid_size, j * grid_size + grid_size);
				calcIfObstacleNode(nodes[i][j]);
			}
		}
	}


	private void calcIfObstacleNode(Node node){
		for(Tree obst : obstacles){
			PVector distanceVect = PVector.sub(node.position, obst.position);
			float distance = distanceVect.mag();
			float minDistance = obst.radius + grid_size/2.0f;
			if ( distance <= minDistance){
				node.addContent(obst);
				System.out.println(node.toString() + " " + obst.position.toString());
				break;
			}
		}
	}



	// ***************************************************
	// ANVÄNDS INTE!
	void display1() {
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				// Initialize each object
				parent.line(j * grid_size + grid_size, 0, j * grid_size + grid_size, parent.height);
			}
			parent.line(0, i * grid_size + grid_size, parent.width, i * grid_size + grid_size);
		}
	}

	// ***************************************************
	void display() {
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				// Initialize each object
				parent.ellipse(nodes[i][j].position.x, nodes[i][j].position.y, 5.0f, 5.0f);
				// println("nodes[i][j].position.x: " + nodes[i][j].position.x);
				// println(nodes[i][j]);
				if (!nodes[i][j].isEmpty) {
					parent.fill(255,0,255);
					parent.ellipse(nodes[i][j].position.x, nodes[i][j].position.y, 25, 25);
					parent.noFill();
				}
			}
			// line(0, i*grid_size+grid_size, width, i*grid_size+grid_size);
		}
	}

	// ***************************************************
	// ANVÄNDS INTE!
	PVector getNearestNode1(PVector pvec) {
		// PVector pvec = new PVector(x,y);
		PVector vec = new PVector(0, 0);
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				if (nodes[i][j].position.dist(pvec) < grid_size / 2) {
					vec.set(nodes[i][j].position);
				}
			}
		}
		return vec;
	}

	// ***************************************************
	Node getNearestNode(PVector pvec) {
		// En justering för extremvärden.
		float tempx = pvec.x;
		float tempy = pvec.y;
		if (pvec.x < 5) {
			tempx = 5;
		} else if (pvec.x > parent.width - 5) {
			tempx = parent.width - 5;
		}
		if (pvec.y < 5) {
			tempy = 5;
		} else if (pvec.y > parent.height - 5) {
			tempy = parent.height - 5;
		}

		pvec = new PVector(tempx, tempy);

		ArrayList<Node> nearestNodes = new ArrayList<Node>();

		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				if (nodes[i][j].position.dist(pvec) < grid_size) {
					nearestNodes.add(nodes[i][j]);
				}
			}
		}

		Node nearestNode = new Node(0, 0);
		for (int i = 0; i < nearestNodes.size(); i++) {
			if (nearestNodes.get(i).position.dist(pvec) < nearestNode.position.dist(pvec)) {
				nearestNode = nearestNodes.get(i);
			}
		}

		return nearestNode;
	}

	// Node getNearestNodePosition(PVector pvec) {

	// ArrayList<Node> nearestNodes = new ArrayList<Node>();

	// for (int i = 0; i < cols; i++) {
	// for (int j = 0; j < rows; j++) {
	// if (nodes[i][j].position.dist(pvec) < grid_size) {
	// nearestNodes.add(nodes[i][j]);
	// }
	// }
	// }

	// Node nearestNode = new Node(0,0);
	// for (int i = 0; i < nearestNodes.size(); i++) {
	// if (nearestNodes.get(i).position.dist(pvec) < nearestNode.position.dist(pvec)
	// ) {
	// nearestNode = nearestNodes.get(i);
	// }
	// }

	// return nearestNode;
	// }

	// ***************************************************
	PVector getNearestNodePosition(PVector pvec) {
		Node n = getNearestNode(pvec);

		return n.position;
	}

	// ***************************************************
	// ANVÄNDS INTE?
	void displayNearestNode(float x, float y) {
		PVector pvec = new PVector(x, y);
		displayNearestNode(pvec);
	}

	// ***************************************************
	// ANVÄNDS INTE!
	void displayNearestNode1(PVector pvec) {
		// PVector pvec = new PVector(x,y);
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				if (nodes[i][j].position.dist(pvec) < grid_size / 2) {
					PVector vec = nodes[i][j].position;
					parent.ellipse(vec.x, vec.y, 5, 5);
				}
			}
		}
	}

	// ***************************************************
	void displayNearestNode(PVector pvec) {

		PVector vec = getNearestNodePosition(pvec);
		parent.ellipse(vec.x, vec.y, 5, 5);
	}

	// ***************************************************
	PVector getRandomNodePosition() {
		int c = (int) parent.random(cols);
		int r = (int) parent.random(rows);

		PVector rn = nodes[c][r].position;

		return rn;
	}

	// ***************************************************
	// Används troligen tillsammans med getNearestNode().empty
	// om tom så addContent(Sprite)
	void addContent(Sprite s) {
		Node n = getNearestNode(s.position);
		n.addContent(s);
	}

	//ändrade ordningen
	public LinkedList<Node> getChildrenNodes(Node node) {
		LinkedList<Node> temp = new LinkedList<>();
		// NW
		if (0 <= node.row - 1 && 0 <= node.col - 1) {
			temp.add(nodes[node.col-1][node.row - 1]);
		}
		// W
		if (0 <= node.col - 1) {
			temp.add(nodes[node.col - 1][node.row]);
		}
		// SW
		if (0 <= node.col - 1 && nodes.length > node.row + 1) {
			temp.add(nodes[node.col - 1][node.row+1]);
		}
		// S
		if (nodes.length > node.row + 1) {
			temp.add(nodes[node.col][node.row + 1]);
		}
		// SE
		if (nodes.length > node.row + 1 && nodes.length > node.col + 1) {
			temp.add(nodes[node.col + 1][node.row + 1]);
		}
		// E
		if (nodes.length > node.col + 1) {
			temp.add(nodes[node.col + 1][node.row]);
		}
		// NE
		if (nodes.length > node.col + 1 && 0 <= node.row - 1) {
			temp.add(nodes[node.col + 1][node.row-1]);
		}
		// N
		if (0 <= node.row - 1) {
			temp.add(nodes[node.col][node.row - 1]);
		}

		return temp;
	}



	public int getCols() {
		return cols;
	}

	public int getRows() {
		return rows;
	}

	public int getGrid_size() {
		return grid_size;
	}

}