import processing.core.PApplet;
import processing.core.PVector;

public class Tank extends Sprite {

	public PApplet parent;
	private PVector startpos;
	private PVector positionPrev;
	private PVector acceleration;
	private Team team;
	private int id;
	private float heading;
	
	private boolean colliding = false;
	
	private int counter = 0;

	public Tank(PApplet parent, int id, Team team, PVector _startpos, float diameter) {
		this.parent = parent;
		this.id = id;
		this.team = team;
		this.diameter = diameter;
		radius = this.diameter / 2;
		startpos = new PVector(_startpos.x, _startpos.y);
		position = new PVector(startpos.x, startpos.y);
		positionPrev = new PVector(0, 0);
		acceleration = new PVector(0, 0);
		acceleration.limit(10);

		if (this.team.getId() == 0)
			this.heading = PApplet.radians(0);
		if (this.team.getId() == 1)
			this.heading = PApplet.radians(180);
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

	public int getId() {
		return id;
	}

	public void checkCollision(Tank tank) {
		// TODO Auto-generated method stub

	}

	public void updateLogic() {
		// TODO Auto-generated method stub

	}
	
	private void rotate() {
		float max = 0.01f;
		heading += 1 * max;
	}

	public void update() {
		counter++;
		
		PVector force = new PVector(PApplet.cos(heading), PApplet.sin(heading));
		force.mult(0.1f);
		positionPrev.set(position);
		acceleration.add(force);
			position.add(acceleration);

			
		
		rotate();
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
