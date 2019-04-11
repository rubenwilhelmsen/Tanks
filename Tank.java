import processing.core.PApplet;
import processing.core.PVector;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class Tank extends Sprite {
	private LinkedList<Node> frontier;
	private HashSet<Node> visitedNodes;
	public Main parent;
	private PVector startpos;
	private PVector velocity;
	private PVector positionPrev;
	private PVector acceleration;
	private Team team;
	private int id;
	private float heading;
	private int angle, prevAngle;

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
		visitedNodes = new HashSet<>();

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
		frontier.push(parent.gridSearch(startpos));
		visitedNodes.add(frontier.peek());
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
			System.out.println("! Tank[" + id + "] – collided with Tree.");

			this.position.set(positionPrev); // Flytta tillbaka.
			acceleration.normalize();

			// Kontroll om att tanken inte "fastnat" i en annan tank.
			distanceVect = PVector.sub(other.position, this.position);
			distanceVectMag = distanceVect.mag();
			if (distanceVectMag < minDistance) {
				System.out.println("! Tank[" + this.getId() + "] – FAST I ETT TRÄD");
			}
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
		Node newPosition = fetchNextPosition();

		// rotera tills heading mot target.
		PVector desired = PVector.sub(newPosition.position, this.position);  // A vector pointing from the position to the target
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
		steer.limit(0.1f);  // Limit to maximum steering force
		acceleration.add(steer);


		positionPrev.set(position); // spara senaste pos.
		velocity.add(acceleration);
		velocity.limit(3);
		position.add(velocity);
		acceleration.mult(0);


		if (!doneRotatingRight) {
			rotateRight();
		} else {
			if (!doneRotatingLeft) {
				rotateLeft();
			}
		}


/*
		//PVector mouse = new PVector(parent.mouseX,parent.mouseY);
		PVector mouse = new PVector(400, 100);
		if (mouse != position) {
			PVector acceleration = PVector.sub(mouse,position);
			heading = PVector.angleBetween(position, acceleration);
			// Set magnitude of acceleration
			acceleration.setMag(10f);

			// Velocity changes according to acceleration
			velocity.add(acceleration);
			// Limit the velocity by topspeed
			velocity.limit(5);
			// Location changes by velocity
			positionPrev.set(position);
			position.add(velocity);
		} else {
			System.out.println("done");
		}*/


		/*
		PVector force = new PVector(PApplet.cos(heading), PApplet.sin(heading));
		force.mult(0.1f);
		positionPrev.set(position);
		acceleration.add(force);
		position.add(acceleration);
		rotate();
		*/
	}
	private Node fetchNextPosition(){
		Node next = frontier.pop();
		//List<Node> children = METOD.MAIN();
		/*for(Node child: children){
			if(!visitedNodes.contains(child)){
				frontier.push(child);
			}
		}*/
		visitedNodes.add(next);
		return next;
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
