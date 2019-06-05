import processing.core.PApplet;
import processing.core.PVector;

import java.util.*;

public class Tank2 extends Sprite {
	private LinkedList<Node> frontier;
	private PriorityQueue<Node> prioFront;
	private HashMap<Node, Boolean> visitedNodes;
	private HashSet<Node> obstacles;
	private HashMap<Tank2,Node> locatedEnemiesPosition, friendlyTankLocation;
	public Main parent;
	private PVector startpos, velocity, acceleration;
	private Team team;
	private int id;
	private float heading;
	private boolean collided, onTheMove, returningToReport, reporting;
	private Node nextNode, currentNode, prevNode;
	private boolean bestFirst = false;
	private Node bestFirstTarget = null;
    private long reportStarted;
	private HashSet<Node> bestFirstExceptions;
	private PVector[] sensor;

	public Tank2(Main parent, int id, Team team, PVector _startpos, float diameter) {
	    prioFront = new PriorityQueue<>(new NodeComparator());
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
        locatedEnemiesPosition = new HashMap<>();
        friendlyTankLocation = new HashMap<>();
		collided = onTheMove = returningToReport = reporting = false;
		if (this.team.getId() == 0) {
			this.heading = PApplet.radians(0);
		}
		if (this.team.getId() == 1) {
			this.heading = PApplet.radians(180);
		}


		sensor = new PVector[]{new PVector(position.x,position.y), new PVector(position.x,position.y)};
		bestFirstExceptions = new HashSet<>();

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




				bestFirstTarget = frontier.peek();
				System.out.println("bestFirstTarget " + bestFirstTarget);
				nextNode = currentNode;
				bestFirst = true;


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
                            isFriendOrFoe(((Tank2)nearestSensorNode.content()), nearestSensorNode);
						} else {
							boolean notAdded = obstacles.add(nearestSensorNode);
							if (notAdded) {
								System.out.println(nearestSensorNode + " added to obstacles");
							}
						}
						break;
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
    private boolean isFriendOrFoe(Tank2 tank, Node atPosition){
	    if(tank.team.id != team.id){
            if(!locatedEnemiesPosition.containsKey(tank)){
                locatedEnemiesPosition.put(tank, atPosition);
                System.out.println("Enemy located at: " + atPosition.toString());
            }else if(!locatedEnemiesPosition.get(tank).equals(atPosition)){
                locatedEnemiesPosition.replace(tank,atPosition);
                System.out.println("Enemy located at: " + atPosition.toString());
            }
            System.out.println("ENEMY!!");
            enemyLocated();
	        return true;
        }else if(!tank.equals(this)){
	        if(!friendlyTankLocation.containsKey(tank)){
                friendlyTankLocation.put(tank, atPosition);
                System.out.println("Friend located at: " + atPosition.toString());
            }else if(!friendlyTankLocation.get(tank).equals(atPosition)){
	            friendlyTankLocation.replace(tank,atPosition);
                System.out.println("Friend located at: " + atPosition.toString());
            }


        }
        return false;
    }
    private void enemyLocated(){
	    prioFront.addAll(frontier);
	    frontier.clear();
        startBestFirst(parent.gridSearch(startpos));
        returningToReport = true;
    }
    private void reporting(){
	    returningToReport = false;
        bestFirstCompleted();
	    reporting = true;
	    reportStarted = System.currentTimeMillis();
    }

    private PVector dampenSpeed() {
		PVector desired = PVector.sub(nextNode.position, this.position);  // A vector pointing from the position to the target
		float d = desired.mag();

		// Scale with arbitrary damping within 100 pixels
		if (d < 10) {
			float m = parent.map(d, 0, 5, 0, 1);
			desired.setMag(m);
		} else {
			desired.setMag(5);
		}
		return desired;
	}

	private void handleNodeArrival() {
		System.out.println("arrived at " + nextNode);
		prevNode = currentNode;
		currentNode = nextNode;
		if (bestFirst) {
			moveTankContent();
			bestFirstExceptions.add(prevNode);
			// ifall den är på bestFirst, lägg till närmaste till bestFirstTarget
			// klar ifall "currentNode.equals(bestFirstTarget)"
			if (currentNode.equals(bestFirstTarget)) {
				if(returningToReport){
					reporting();
				}else{
					bestFirstCompleted();
				}

			} else {
				System.out.println("bestFirst... Target: " + bestFirstTarget);
				nextNode = addClosestToBestFirst();
			}
		} else {
			onTheMove = false;
			addToFrontier();
		}
		moveTankContent();
	}

	private void handleTankRotation() {
		float theta = velocity.heading() + parent.PI / 2 - parent.radians(90);
		int currentAngle = (int)parent.degrees(heading);
		int desiredAngle = (int)parent.degrees(theta);

		if (currentAngle > desiredAngle - 2 && currentAngle < desiredAngle + 2) {
			position.add(velocity);
		} else {

			if (currentAngle < desiredAngle) {
				heading += parent.radians(3);
			} else {
				heading -= parent.radians(3);
			}

		}
	}

	private void nextNodeObstacleHelper() {
		if (obstacles.contains(nextNode)) {
			// lägger till nästa närmaste nod ifall nästa är ett obstacle
			// ändrar bestFirstTarget ifall det är en obstacle
			if (nextNode == bestFirstTarget) {
				onTheMove = false;
				bestFirst = false;
			} else {
				nextNode = addClosestToBestFirst();
			}
		}
	}

	// Används för att sätta nextNode när tanken kör breadth-first search
	// Ändrar till best first search ifall noder inte är bredvid
	private void decideNextNode() {
		if (!onTheMove && (!frontier.isEmpty()|| !prioFront.isEmpty()) && !bestFirst) {
			nextNode = fetchNextPosition();
			System.out.print("nextNode " + nextNode);
			if (position.dist(nextNode.position) >= parent.getGrid_size() + 1) {
				startBestFirst(nextNode);
				System.out.println(", node is not adjecent, bestFirst");
			} else {
				System.out.println(", node is adjecent");
			}
			onTheMove = true;
		} else if (!onTheMove && (frontier.isEmpty() && prioFront.isEmpty())) {
			startPatrol();
		}
	}

	public void update() {
		if(!reporting){
			decideNextNode();

			// Kollar sensorn för obstacels och andra tanks
			checkSensor();

			PVector desired = dampenSpeed();

			PVector steer = PVector.sub(desired, velocity);
			steer.limit(3f);  // Limit to maximum steering force
			acceleration.add(steer);

			// ifall tanken har anlänt vid nextNode
			if (desired.mag() < 0.1f) {
				handleNodeArrival();
			}

			// Tanken förflyttar sig
			velocity.add(acceleration);
			velocity.limit(3);

			//rotering till nästa nod
			handleTankRotation();

			// kollar ifall nextNode är obstacle
			// hanterar det ifall det är sant
			nextNodeObstacleHelper();

			acceleration.mult(0);
		}else if ((reportStarted+3000) < System.currentTimeMillis()){
		    reporting = false;
        }
	}
    private void startBestFirst(Node target){
        bestFirstExceptions.clear();
        bestFirstTarget = target;
        nextNode = addClosestToBestFirst();
        bestFirst = true;
    }
	private void bestFirstCompleted(){
        bestFirstExceptions.clear();
        bestFirst = false;
        bestFirstTarget = null;
        addToFrontier();
        System.out.println("bestFirst complete");
    }

	private void moveTankContent(){
		if(currentNode != null && currentNode.equals(nextNode)){
			//System.out.print("ADDED: " + currentNode.toString() + " ");
			currentNode.addContent(this);
			if(!currentNode.equals(prevNode)){
              //  System.out.println("Removed: " + prevNode.toString());
                prevNode.removeContent();
            }


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

	//lägger till närmaste noden från getAdjecentNode i nextNode när tanken kör best-first search
	private Node addClosestToBestFirst() {
		float closest = 0;
		Node temp = null;
		LinkedList<Node> adjacent = parent.getAdjacentNodes(currentNode);
		Node closestEnemy = null;
		for(Node n: adjacent) {
			if (!obstacles.contains(n) && (bestFirstExceptions == null || !bestFirstExceptions.contains(n)) && n.isEmpty) {
				if (!n.equals(bestFirstTarget)) {
					if (closest == 0 || closest > bestFirstTarget.position.dist(n.position)) {
						if(locatedEnemiesPosition.isEmpty()){
							closest = bestFirstTarget.position.dist(n.position);
							temp = n;
						}else {
							if(closestEnemy == null){
								closestEnemy = findClosetEnemy();
							}
							float enemyDist = n.position.dist(closestEnemy.position);
							if(enemyDist > 400){
								closest = bestFirstTarget.position.dist(n.position);
								temp = n;
							}else if(temp == null || enemyDist < temp.position.dist(closestEnemy.position)){
								closest = bestFirstTarget.position.dist(n.position);
								temp = n;
							}
						}
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
		    if(locatedEnemiesPosition.isEmpty()){
                next = frontier.pop();
            }else{
		        next = prioFront.poll();
            }
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
	    if(locatedEnemiesPosition.isEmpty()){
	        simpleFrontier();
        }else{
	        prioFrontier();
	    }
	}
	private void simpleFrontier(){
        LinkedList<Node> children = parent.getAdjacentNodes(nextNode);
        for(Node child: children){
            if(!visitedNodes.containsKey(child) && !obstacles.contains(child) && child.isEmpty){
                frontier.add(child);
                visitedNodes.put(child, false);
            }
        }
    }
    private void prioFrontier(){
        LinkedList<Node> children = parent.getAdjacentNodes(nextNode);
        for(Node child: children){
            if(!visitedNodes.containsKey(child) && !obstacles.contains(child) && child.isEmpty){
                prioFront.add(child);
                visitedNodes.put(child, false);
            }
        }
    }

    private void startPatrol(){

	    if(locatedEnemiesPosition.isEmpty()){
            frontier.push(parent.gridSearch(startpos));
            prevNode = currentNode = nextNode = frontier.pop();
        }else{
            prioFront.add(parent.gridSearch(startpos));
            prevNode = currentNode = nextNode = prioFront.poll();
        }


        visitedNodes.put(nextNode,true);
        moveTankContent();
        addToFrontier();
    }

	public void display() {
		drawSensor();
		parent.pushMatrix();
		drawTank();
		drawTurret();
		parent.popMatrix();
	}

	private void drawSensor() {
		parent.fill(team.getColor(),50);
		parent.strokeWeight(1);
		parent.ellipse(sensor[0].x, sensor[0].y, 20, 20);
		parent.ellipse(sensor[1].x, sensor[1].y, 20, 20);
	}

	private void drawTank() {
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

	@Override
	public boolean equals(Object other) {
        boolean equal = false;
        if(other instanceof Tank2){
            Tank2 temp = (Tank2) other;
            equal = (id == temp.id);
        }

        return equal;
    }

    @Override
    public int hashCode() {
	    return ((Integer)id).hashCode();
    }

	private Node findClosetEnemy(){
		Iterator<Node> knownEnemyPositions = locatedEnemiesPosition.values().iterator();
		Node closest = null;
		while(knownEnemyPositions.hasNext()){
			Node temp = knownEnemyPositions.next();
			if(closest == null || (closest.position.dist(this.position) > temp.position.dist(this.position))){
				closest = temp;
			}
		}

		return closest;
	}




    private class NodeComparator implements Comparator<Node>{


        @Override
        public int compare(Node o1, Node o2) {
            PVector closestEnemyPosition = findClosetEnemy().position;

            if(o1.position.dist(closestEnemyPosition) < o2.position.dist(closestEnemyPosition)){
                return 1;
            }else if(o1.position.dist(closestEnemyPosition) > o2.position.dist(closestEnemyPosition)){
                return -1;
            }
            return 0;
        }
    }


}
