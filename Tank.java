import processing.core.PApplet;
import processing.core.PVector;

import java.util.*;

public class Tank extends Sprite {
	private LinkedList<Node> frontier;
	private HashMap<Node, Boolean> visitedNodes;
	private HashSet<Node> obst;
	public Main parent;
	private PVector startpos, velocity, positionPrev, acceleration;
	private Team team;
	private int id;
	private float heading;
	private int angle, prevAngle;
	private boolean collided, onTheMove, onTheRightTrack, regrouped, scanning;
	private Node nextNode, prevNode, targetNode;
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
		obst = new HashSet<>();
		collided = onTheMove = scanning = false;
		onTheRightTrack = regrouped = true;
		if (this.team.getId() == 0) {
			this.heading = PApplet.radians(0);
			angle = 0;
			prevAngle = 0;
		}
		if (this.team.getId() == 1) {
			this.heading = PApplet.radians(270);
			angle = 180;
			prevAngle = 180;
		}


		startPatrol();

	}

	public void checkEnvironment() {
		// TODO Auto-generated method stub



	}
	private void scanSurroundings(){
		float scanDistance = (parent.getGrid_size()*2);
		if(angle < 361){
			angle++;
		} else {
			angle = 0;
		}
		if(angle != prevAngle) {
			if (angle < 91) {
				scanEastToSouth(scanDistance);
			} else if (angle < 181) {
				scanSouthToWest(scanDistance);
			} else if (angle < 271) {
				scanWestToNorth(scanDistance);
			} else if (angle < 361) {
				scanNorthToEast(scanDistance);
			}
			this.heading = PApplet.radians(angle);
		}else {
			scanning = false;
			addToFrontier();
		}


	}
	private void scanEastToSouth(float scanDistance){
		Node temp = null;
		switch (angle){
			case 0:
				isObstacle(getNodeAt(this.position.x + scanDistance, this.position.y));
				break;
			case 22:
				isObstacle(getNodeAt(this.position.x + scanDistance, this.position.y+ (scanDistance/2.0f)));
				break;
			case 45:
				isObstacle(getNodeAt(this.position.x + scanDistance, this.position.y+ scanDistance));
				break;
			case 67:
				isObstacle(getNodeAt(this.position.x + scanDistance/2.0f, this.position.y+ scanDistance));
				break;
			case 90:
				isObstacle(getNodeAt(this.position.x, this.position.y + scanDistance));
				break;
		}


	}
	private void scanSouthToWest(float scanDistance){
		Node temp = null;
		switch (angle){
			case 112:
				isObstacle(getNodeAt(this.position.x - scanDistance/2.0f, this.position.y + scanDistance));
				break;
			case 135:
				isObstacle(getNodeAt(this.position.x - scanDistance, this.position.y + scanDistance));
				break;
			case 157:
				isObstacle(getNodeAt(this.position.x - scanDistance, this.position.y + scanDistance/2.0f));
				break;
			case 180:
				isObstacle( getNodeAt(this.position.x - scanDistance, this.position.y ));
				break;
		}


	}
	private void scanWestToNorth(float scanDistance){
		Node temp = null;
		switch (angle){
			case 202:
				isObstacle(getNodeAt(this.position.x - scanDistance, this.position.y + scanDistance/2.0f));
				break;
			case 225:
				isObstacle(getNodeAt(this.position.x - scanDistance, this.position.y + scanDistance));
				break;
			case 247:
				isObstacle(getNodeAt(this.position.x - scanDistance/2.0f, this.position.y + scanDistance));
				break;
			case 270:
				isObstacle(getNodeAt(this.position.x, this.position.y + scanDistance));
				break;
		}

	}

	private void scanNorthToEast(float scanDistance){
		Node temp = null;
		switch (angle){
			case 292:
				isObstacle(getNodeAt(this.position.x - scanDistance/2.0f, this.position.y + scanDistance));
				break;
			case 315:
				isObstacle(getNodeAt(this.position.x - scanDistance, this.position.y + scanDistance));
				break;
			case 337:
				isObstacle(getNodeAt(this.position.x - scanDistance, this.position.y + scanDistance/2.0f));
				break;
			case 360:
				isObstacle(getNodeAt(this.position.x + scanDistance, this.position.y));
				break;
		}

	}


	private Node getNodeAt(float x, float y){
		return parent.grid.getNearestNode(new PVector(x, y));
	}


	public void checkCollision(Tree other) {
		PVector distanceVect = PVector.sub(other.position, this.position);

		// Calculate magnitude of the vector separating the tank and the tree
		float distanceVectMag = distanceVect.mag();

		// Minimum distance before they are touching
		float minDistance = this.radius + other.radius;

		if (distanceVectMag <= minDistance) {
			System.out.println("! Tank[" + id + "] – collided with Tree Heading towards: " + targetNode.toString() + " through " + nextNode.toString());


			 // Flytta tillbaka.
			collided = true;
			if(onTheRightTrack){
				getBackOnRightTrack(other);
			}

			//acceleration.normalize();

			// Kontroll om att tanken inte "fastnat" i en annan tank.
			distanceVect = PVector.sub(other.position, this.position);
			distanceVectMag = distanceVect.mag();
			if (distanceVectMag < minDistance) {
				System.out.println("! Tank[" + this.getId() + "] – FAST I ETT TRÄD");
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
		if(!scanning){
			if(onTheRightTrack) {
				if (!onTheMove && !frontier.isEmpty()) {
					visitedNodes.replace(nextNode, true);
					if(targetNode == null || atTarget()){
						targetNode = fetchTargetPosition();
					}
					if(this.position.dist(targetNode.position) <= parent.getGrid_size()*2){
						nextNode = targetNode;
					}else{
						nextNode = calcBestRoute();
					}
					onTheMove = true;
				} else if (!onTheMove && frontier.isEmpty()) {
					startPatrol();
				}
				move();

			}else {
				if(regrouped){
					detouring();
				}else{
					move();
				}

			}
		}else{
			scanSurroundings();
		}



	}
	private boolean atTarget(){
		return targetNode.equals(nextNode);
	}
	private void move(){
		// rotera tills heading mot target.

		PVector desired = PVector.sub(nextNode.position, this.position);  // A vector pointing from the position to the target

		float d = desired.mag();
		// jag t�nker mig att ska rotera tills det �r 0 graders vinkel mellan n�sta nod och nuvarande nod, detta �r kanske n�got p� sp�ret.
		//System.out.println("Angle: " + parent.degrees(PVector.angleBetween(nextNode.position, this.position)));
		/*if(parent.degrees(PVector.angleBetween(nextNode.position, this.position)) < 1.f) {
			rotateLeft();
		}else {
			rotateRight();
		}*/
		// If arrived

		// Scale with arbitrary damping within 100 pixels
		if (d < 10) {
			float m = parent.map(d, 0, 100, 0, 5);
			desired.setMag(m);
		} else {
			desired.setMag(5);
		}

		// Steering = Desired minus Velocity
		PVector steer = PVector.sub(desired, velocity);
		steer.limit(3f);  // Limit to maximum steering force
		acceleration.add(steer);



		destinationReached(d);

		positionPrev.set(position); // spara senaste pos.
		velocity.add(acceleration);
		velocity.limit(3);
		position.add(velocity);
		acceleration.mult(0);

	}

	private void getBackOnRightTrack(Tree obstacle){
		//add node as obstacle

		onTheRightTrack = regrouped = false;
		if(isObstacle(nextNode,obstacle)){
			addToFrontier();
			obst.add(nextNode);
			nextNode = prevNode;
			onTheMove = true;


			Node temp = frontier.peek();
			if( isObstacle(temp,obstacle)){
				obst.add(frontier.pop());
				targetNode = frontier.pop();
				System.out.println("DIT VILL JAG INTE ÅKA ->"  + temp.toString() + " MEN DIT KANSKE: " + targetNode.toString());
			}else{
				targetNode = frontier.pop();
			}
		}else {
			nextNode = prevNode;
			onTheMove = true;
		}

	}

	private boolean isObstacle(Node node, Tree obstacle){
		if(node.isEmpty) {
			node.addContent(this);
			float minDistance = node.content.radius + obstacle.radius;
			PVector distanceVect = PVector.sub(obstacle.position, node.position);
			node.removeContent();
			return distanceVect.mag() <= minDistance;
		}else{
			return true;
		}
	}
	private boolean isObstacle(Node node){
		angle += 15;
		if(!node.isEmpty){
			obst.add(node);
			return true;
		}else{
			return false;
		}


	}
	private boolean destinationReached(float distanceToDest){
		if(distanceToDest < 0.1f && onTheMove){
			onTheMove = false;
			prevNode = nextNode;
			if (onTheRightTrack) {
				scanning = true;
				prevAngle = angle;
			}
			regrouped = true;

		}
		return onTheMove;
	}


	private void detouring(){
		if(!onTheMove){
			nextNode = calcBestRoute();
			onTheMove = true;
			if(nextNode.equals(targetNode)){
				onTheRightTrack = true;
				System.out.println("I AM ON THE RIGHT TRACK! " +  onTheRightTrack);
			}

		}else{
			move();
		}
	}


	private Node calcBestRoute(){
		double currentDist;
		Node bestPath = null;
		LinkedList<Node> surroundings = parent.getAdjacentNodes(nextNode);
		for(Node node : surroundings){
			currentDist = node.position.dist(targetNode.position);
			System.out.println("Pot: "  + node.toString() + " DIST: " + node.position.dist(targetNode.position) + " TARGET: " + targetNode.toString());
			if(!obst.contains(node) && (bestPath == null  || (int)bestPath.position.dist(targetNode.position) > (int)currentDist)){
				bestPath = node;
				System.out.println(bestPath.toString());
			}
		}

		//System.out.println("Dist: " + dist + " This-X: "  + nextNode.row + " This-Y: " + nextNode.col + " Traget-X: " +  targetNode.row + " Target-Y: " + targetNode.col + " Temp-X: " + temp.x  + " Temp-Y: " + temp.y);
		return bestPath;
	}

	private Node fetchTargetPosition(){
		Node next = null;
		try{
			next = frontier.pop();
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
			if(!visitedNodes.containsKey(child) && !obst.contains(child)){
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
