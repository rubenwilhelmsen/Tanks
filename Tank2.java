import processing.core.PApplet;
import processing.core.PVector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Tank2 extends Sprite {
	private LinkedList<Node> frontier;
	private HashMap<Node, Boolean> visitedNodes;
	private HashSet<Node> obstacles;
	public Main parent;
	private PVector startpos, velocity, acceleration;
	private Team team;
	private int id;
	private float heading;
	private boolean collided, onTheMove;
	private Node nextNode, currentNode, prevNode;
	private boolean detouring = false;
	private Node detourTarget = null;

	private HashSet<Node> detourExceptions;
	private PVector[] sensor;

	public Tank2(Main parent, int id, Team team, PVector _startpos, float diameter) {
		this.parent = parent;
		this.id = id;
		this.team = team;
		this.diameter = diameter;
		radius = this.diameter / 2;
		startpos = new PVector(_startpos.x, _startpos.y);
		position = new PVector(startpos.x, startpos.y);
		velocity = new PVector(0,0);
		acceleration = new PVector(0, 0);
		acceleration.limit(10);
		frontier = new LinkedList<>();
		visitedNodes = new HashMap<>();
		obstacles = new HashSet<>();
		collided = onTheMove = false;
		if (this.team.getId() == 0) {

			//Hembasen läggs till så tanken redan vet om den
			/*
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 7; j++) {
					visitedNodes.put(parent.grid.nodes[i][j], true);
				}
			}
			frontier.add(parent.grid.nodes[3][0]);
			*/

			this.heading = PApplet.radians(0);
		}
		if (this.team.getId() == 1) {
			this.heading = PApplet.radians(180);
		}

		//lägger till alla obstacles hårdkodat just nu, men detta bör ju göras dynamiskt under runtime
		/*
		obstacles.add(parent.grid.nodes[4][4]);
		obstacles.add(parent.grid.nodes[4][3]);
		obstacles.add(parent.grid.nodes[5][4]);
		obstacles.add(parent.grid.nodes[5][3]);

		obstacles.add(parent.grid.nodes[3][10]);
		obstacles.add(parent.grid.nodes[3][11]);
		obstacles.add(parent.grid.nodes[3][12]);
		obstacles.add(parent.grid.nodes[4][10]);
		obstacles.add(parent.grid.nodes[4][11]);
		obstacles.add(parent.grid.nodes[4][12]);

		obstacles.add(parent.grid.nodes[9][9]);
		obstacles.add(parent.grid.nodes[9][10]);
		obstacles.add(parent.grid.nodes[10][9]);
		obstacles.add(parent.grid.nodes[10][10]);
		*/

		sensor = new PVector[]{new PVector(position.x,position.y), new PVector(position.x,position.y)};
		detourExceptions = new HashSet<>();

		startPatrol();

	}

	public void checkEnvironment() {
		// TODO Auto-generated method stub


	}

	public void checkCollision(Tree other) {
		PVector distanceVect = PVector.sub(other.position, this.position);

		// Calculate magnitude of the vector separating the tank and the tree
		float distanceVectMag = distanceVect.mag();

		// Minimum distance before they are touching
		float minDistance = this.radius + other.radius;

		if (distanceVectMag <= minDistance) {
			if (!collided) {
				System.out.println("! Tank[" + id + "] – collided with Tree.");
/*
				System.out.println("collided");
				// Flytta tillbaka.
				collided = true;


				if (!nextNode.equals(currentNode)) {
					Node colliededNode = nextNode;
					obstacles.add(colliededNode);
					frontier.remove(colliededNode);
					System.out.println(colliededNode + " put in obstacles and removed from frontier");
				}




				detourTarget = frontier.peek();
				System.out.println("detourtarget " + detourTarget);
				nextNode = currentNode;
				detouring = true;


				// Kontroll om att tanken inte "fastnat" i en annan tank.
				distanceVect = PVector.sub(other.position, this.position);
				distanceVectMag = distanceVect.mag();
				if (distanceVectMag < minDistance) {
					//System.out.println("! Tank[" + this.getId() + "] – FAST I ETT TRÄD");
				}
				*/
			}
		}else{
			collided = false;
		}
	}

	public void checkCollision(Tank2 tank) {
		// TODO Auto-generated method stub

	}

	public int getId() {
		return id;
	}

	public void updateLogic() {
		// TODO Auto-generated method stub

	}

	public void checkSensor() {
		sensor[0] = getSensorPositionFromTankAngle(getTankAngle());
		sensor[1] = getSensorPositionFromTankAngle2(getTankAngle());

		for (int i = 0; i < 2; i++) {
			Node nearestSensorNode = parent.grid.getNearestNode(sensor[i]);
			if (nearestSensorNode != null) {
				if (!(nearestSensorNode.row == 0 && nearestSensorNode.col == 0)) {
					if (!nearestSensorNode.isEmpty) {
						if (isContentTank(nearestSensorNode)) {

						} else {
							boolean notAdded = obstacles.add(nearestSensorNode);
							if (notAdded) {
								System.out.println(nearestSensorNode + " added to obstacles");
							}
						}
					}
				}
			}
		}

	}

	private boolean isContentTank(Node n) {
		if (n.content() instanceof Tank2) {
			return true;
		}
		return false;
	}

	public void update() {
		try {
			//ta nästa nod, kolla ifall den är längre bort än ett hopp
			//kör best-first-search ifall den är längre bort (detour)
			if (!onTheMove && !frontier.isEmpty() && !detouring) {

				nextNode = fetchNextPosition();
				System.out.print("nextNode " + nextNode);
				if (position.dist(nextNode.position) >= parent.getGrid_size() + 1) {
					detourTarget = nextNode;
					nextNode = addClosestToDetour();
					detouring = true;
					System.out.println(", node is not adjecent, detouring");
				} else {
					System.out.println(", node is adjecent");
				}
				onTheMove = true;
			} else if (!onTheMove && frontier.isEmpty()) {
				startPatrol();
			}

			checkSensor();

			PVector desired = PVector.sub(nextNode.position, this.position);  // A vector pointing from the position to the target
			float d = desired.mag();

			// Scale with arbitrary damping within 100 pixels
			if (d < 10) {
				float m = parent.map(d, 0, 10, 0, 2);
				desired.setMag(m);
			} else {
				desired.setMag(5);
			}

			// Steering = Desired minus Velocity
			PVector steer = PVector.sub(desired, velocity);
			steer.limit(3f);  // Limit to maximum steering force
			acceleration.add(steer);

			// tanken har anlänt vid nextNode
			if (desired.mag() < 0.1f) {
				System.out.println("arrived at " + nextNode);

				prevNode = currentNode;
				currentNode = nextNode;
				if (detouring) {
					detourExceptions.add(prevNode);
					// ifall den är på detour, lägg till närmaste till detourTarget
					// klar ifall "currentNode.equals(detourTarget)"
					if (currentNode.equals(detourTarget)) {
						detourExceptions.clear();
						detouring = false;
						detourTarget = null;
						addToFrontier();
						System.out.println("detour complete");
					} else {
						System.out.println("detouring...");
						nextNode = addClosestToDetour();
					}
				} else {
					onTheMove = false;
					addToFrontier();
				}
			}



			velocity.add(acceleration);
			velocity.limit(3);

			//rotering till nästa nod
			float theta = velocity.heading() + parent.PI / 2 - parent.radians(90);
			int currentAngle = (int)parent.degrees(heading);
			int desiredAngle = (int)parent.degrees(theta);

			if (obstacles.contains(nextNode)) {
				// lägger till nästa närmaste nod ifall nästa är ett obstacle
				// ändrar detourTarget ifall det är en obstacle
				if (nextNode == detourTarget) {
					onTheMove = false;
					detouring = false;
				} else {
					//detourExceptions.add(prevNode);
					nextNode = addClosestToDetour();
				}
			}

			if (currentAngle > desiredAngle - 2 && currentAngle < desiredAngle + 2) {
				position.add(velocity);
			} else {

				if (currentAngle < desiredAngle) {
					heading += parent.radians(3);
				} else {
					heading -= parent.radians(3);
				}

			}

			acceleration.mult(0);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

	private int getTankAngle() {
		int angle = -1;
		float heading = parent.degrees(this.heading);
		if (heading == 0) {
			//System.out.println("E");
			angle = 0;
		} else if (heading > 40 && heading < 50) {
			//System.out.println("SE");
			angle = 1;
		} else if (heading > 85 && heading < 95) {
			//System.out.println("S");
			angle = 2;
		} else if (heading > 130 && heading < 140) {
			//System.out.println("SW");
			angle = 3;
		} else if ((heading > 175 && heading < 180) || (heading < -175 && heading > -180)) {
			//System.out.println("W");
			angle = 4;
		} else if (heading < -130 && heading > -140) {
			//System.out.println("NW");
			angle = 5;
		} else if (heading < -85 && heading > -95) {
			//System.out.println("N");
			angle = 6;
		} else if (heading < -40 && heading > -50) {
			//System.out.println("NE");
			angle = 7;
		}
		return angle;
	}

	private PVector getSensorPositionFromTankAngle(int tankAngle) {
		PVector temp = position;
		switch (tankAngle) {
			case 0:
				temp = new PVector(position.x + 50, position.y);
				break;
			case 1:
				temp = new PVector(position.x + 50, position.y + 50);
				break;
			case 2:
				temp = new PVector(position.x, position.y + 50);
				break;
			case 3:
				temp = new PVector(position.x - 50, position.y + 50);
				break;
			case 4:
				temp = new PVector(position.x - 50, position.y);
				break;
			case 5:
				temp = new PVector(position.x - 50, position.y - 50);
				break;
			case 6:
				temp = new PVector(position.x, position.y - 50);
				break;
			case 7:
				temp = new PVector(position.x + 50, position.y - 50);
				break;
		}
		return temp;
	}

	private PVector getSensorPositionFromTankAngle2(int tankAngle) {
		PVector temp = position;
		switch (tankAngle) {
			case 0:
				temp = new PVector(position.x + 100, position.y);
				break;
			case 1:
				temp = new PVector(position.x + 100, position.y + 100);
				break;
			case 2:
				temp = new PVector(position.x, position.y + 100);
				break;
			case 3:
				temp = new PVector(position.x - 100, position.y + 100);
				break;
			case 4:
				temp = new PVector(position.x - 100, position.y);
				break;
			case 5:
				temp = new PVector(position.x - 100, position.y - 100);
				break;
			case 6:
				temp = new PVector(position.x, position.y - 100);
				break;
			case 7:
				temp = new PVector(position.x + 100, position.y - 100);
				break;
		}
		return temp;
	}

	//lägger till närmaste noden från getAdjecentNode i nextNode när tanken detour:ar
	private Node addClosestToDetour() {
		float closest = 0;
		Node temp = null;
		LinkedList<Node> adjacent = parent.getAdjacentNodes(currentNode);
		for(Node n: adjacent) {
			if (!obstacles.contains(n) && (detourExceptions == null || !detourExceptions.contains(n))) {
				if (!n.equals(detourTarget)) {
					if (closest == 0 || closest > detourTarget.position.dist(n.position)) {
						closest = detourTarget.position.dist(n.position);
						temp = n;
					}
				} else {
					return n;
				}

			}
		}
		return temp;
	}

	private Node fetchNextPosition(){
		Node next = null;
		try{
			next = frontier.pop();
			if (obstacles.contains(next)) {
				next = fetchNextPosition();
			}
			visitedNodes.replace(next,true);

		}catch(NoSuchElementException nse){
			if(this.position != startpos){
				System.out.println("Total area searched: " +(100.0*((double)visitedNodes.size()/(parent.grid.getRows()*parent.grid.getCols())) + " Num: " + 0 + "\n Returning to base!"));
				next = parent.gridSearch(startpos);
			}
		}
		return next;
	}

	private void addToFrontier(){
		LinkedList<Node> children = parent.getAdjacentNodes(nextNode);
		for(Node child: children){
			if(!visitedNodes.containsKey(child) && !obstacles.contains(child)){
				frontier.add(child);
				visitedNodes.put(child, false);
			}
		}
	}
	private void startPatrol(){
        frontier.push(parent.gridSearch(startpos));
        visitedNodes.put(frontier.peek(),false);
    }


	public void display() {
		drawSensor();
		parent.pushMatrix();
		drawTank(position.x, position.y);
		drawTurret();
		parent.popMatrix();
	}

	private void drawSensor() {
		parent.fill(team.getColor(),50);
		parent.strokeWeight(1);
		parent.ellipse(sensor[0].x, sensor[0].y, 20, 20);
		parent.ellipse(sensor[1].x, sensor[1].y, 20, 20);
	}

	private void drawTank(float x, float y) {
		parent.fill(team.getColor());

		parent.translate(position.x, position.y);
		parent.rotate(heading);

		if (this.team.getId() == 0) {
			parent.fill((((255 / 6) * 3) * 40), 50 * 3, 50 * 3, 255 - 3 * 60);
		}

		if (this.team.getId() == 1) {
			parent.fill(10 * 3, (255 / 6) * 3, (((255 / 6) * 3) * 3), 255 - 3 * 60);
		}

		parent.strokeWeight(1);

		parent.ellipse(0, 0, 50, 50);
		parent.strokeWeight(1);
		parent.line(0, 0, 25, 0);

		parent.fill(team.getColor(), 255);
	}

	private void drawTurret() {
		parent.strokeWeight(1);
		// fill(204, 50, 50);
		parent.ellipse(0, 0, 25, 25);
		parent.strokeWeight(3);
		parent.line(0, 0, diameter / 2, 0);
	}

}
