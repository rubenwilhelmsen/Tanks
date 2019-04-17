import processing.core.PApplet;
import processing.core.PVector;

import java.util.*;

public class Tank extends Sprite {
	private LinkedList<Node> frontier;
	private HashMap<Node, Boolean> visitedNodes;
	public Main parent;
	private PVector startpos, velocity, positionPrev, acceleration;
	private Team team;
	private int id;
	private float heading;
	private int angle, prevAngle;
	private boolean collided, onTheMove;
	private Node nextNode, prevNode;
	private boolean doneRotatingRight, doneRotatingLeft = false;
	private int counter = 0;

	public Tank(Main parent, int id, Team team, PVector _startpos, float diameter) {
		this.parent = parent;
		this.id = id;
		this.team = team;
		this.diameter = diameter;
		radius = this.diameter / 2;
		startpos = new PVector(_startpos.x, _startpos.y);
		position = new PVector(startpos.x, startpos.y);
		velocity = new PVector(0,0);
		positionPrev = new PVector(0, 0);
		acceleration = new PVector(0, 0);
		acceleration.limit(10);
		frontier = new LinkedList<>();
		visitedNodes = new HashMap<>();
		collided = onTheMove = false;
		if (this.team.getId() == 0) {
			this.heading = PApplet.radians(0);
			angle = 0;
			prevAngle = 0;
		}
		if (this.team.getId() == 1) {
			this.heading = PApplet.radians(180);
			angle = 180;
			prevAngle = 180;
		}


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
			System.out.println("! Tank[" + id + "] â€“ collided with Tree.");

			 // Flytta tillbaka.
			collided = true;
			nextNode = prevNode;
			acceleration.normalize();

			// Kontroll om att tanken inte "fastnat" i en annan tank.
			distanceVect = PVector.sub(other.position, this.position);
			distanceVectMag = distanceVect.mag();
			if (distanceVectMag < minDistance) {
				System.out.println("! Tank[" + this.getId() + "] â€“ FAST I ETT TRÃ„D");
			}
		}else{
			collided = false;
		}
	}

	public void checkCollision(Tank tank) {
		// TODO Auto-generated method stub

	}

	public int getId() {
		return id;
	}

	public void updateLogic() {
		// TODO Auto-generated method stub

	}
	
	private void rotateRight() {
		heading += parent.radians(1);
		angle++;
		if (angle == prevAngle + 90) {
			prevAngle = angle;
			doneRotatingRight = true;
		}
	}

	private void rotateLeft() {
		heading -= parent.radians(1);
		angle--;
		if (angle == prevAngle - 90) {
			prevAngle = angle;
			doneRotatingLeft = true;
		}
	}

	public void update() {
		if(!onTheMove && !frontier.isEmpty()){
			 nextNode = fetchNextPosition();
			 onTheMove = true;
		}else if(!onTheMove && frontier.isEmpty()){
            startPatrol();
        }
		System.out.println(nextNode.toString() + " " + collided + " SIZE: " + visitedNodes.size());


		// rotera tills heading mot target.
		PVector desired = PVector.sub(nextNode.position, this.position);  // A vector pointing from the position to the target
		
		// jag tänker mig att ska rotera tills det är 0 graders vinkel mellan nästa nod och nuvarande nod, detta är kanske något på spåret
		System.out.println("Angle: " + parent.degrees(PVector.angleBetween(nextNode.position, this.position)));
		if(parent.degrees(PVector.angleBetween(nextNode.position, this.position)) < 1.f) {
			rotateLeft();
		}else {
			rotateRight();
		}
		
		float d = desired.mag();
		// If arrived

		// Scale with arbitrary damping within 100 pixels
		if (d < 100) {
			float m = parent.map(d, 0, 100, 0, 5);
			desired.setMag(m);
		} else {
			desired.setMag(5);
		}
		// Steering = Desired minus Velocity
		PVector steer = PVector.sub(desired, velocity);
		steer.limit(3f);  // Limit to maximum steering force
		acceleration.add(steer);
		if(desired.mag() < 0.1f){
			onTheMove = false;
			prevNode = nextNode;
			addToFrontier();
		}

		positionPrev.set(position); // spara senaste pos.
		velocity.add(acceleration);
		velocity.limit(3);
		position.add(velocity);
		acceleration.mult(0);


		/*if (!doneRotatingRight) {
			rotateRight();
		} else {
			if (!doneRotatingLeft) {
				rotateLeft();
			}
		}*/
		
	}
	private Node fetchNextPosition(){
		Node next = null;
		try{
			next = frontier.pop();
			visitedNodes.replace(next,true);

		}catch(NoSuchElementException nse){
			if(this.position != startpos){
				System.out.println("Total area searched: " +(100.0*((double)visitedNodes.size()/(parent.grid.rows*parent.grid.cols)) + " Num: " + 0 + "\n Returning to base!"));
				next = parent.gridSearch(startpos);
			}
		}
		return next;
	}
	private void addToFrontier(){
		LinkedList<Node> children = parent.getAdjencentNodes(nextNode);
		for(Node child: children){
			if(!visitedNodes.containsKey(child) && !collided){
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
		parent.pushMatrix();
		drawTank(position.x, position.y);
		drawTurret();
		parent.popMatrix();

	}

	private void drawTank(float x, float y) {
		parent.translate(position.x, position.y);
		parent.rotate(heading);

		parent.fill(team.getColor());

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
