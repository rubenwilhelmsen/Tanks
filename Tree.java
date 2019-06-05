/**
 *
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 *
 */
import processing.core.PImage;
import processing.core.PVector;

public class Tree extends Sprite {

	Main parent;

	PImage img;
	// PVector hitArea;
	// float diameter, radius, m;
	// float m;

	// **************************************************
	public Tree(int posx, int posy, Main parent) {
		this.parent = parent;
		img = parent.loadImage("tree01_v2.png");
		position = new PVector(posx, posy);
		// this.hitArea = new PVector(posx, posy); // Kanske inte kommer att användas.
		diameter = this.img.width / 2;
		radius = diameter / 2;
		// this.m = radius*.1;

		name = "tree";
	}

	// **************************************************
	void checkCollision(Tank other) {

		// Get distances between the balls components
		PVector distanceVect = PVector.sub(other.position, position);

		// Calculate magnitude of the vector separating the balls
		float distanceVectMag = distanceVect.mag();

		// Minimum distance before they are touching
		float minDistance = radius + other.radius;

		if (distanceVectMag < minDistance) {
			System.out.println("! collision med en tank [Tree]");

		}

	}

	// **************************************************
	void display() {
		parent.pushMatrix();

		//parent.translate((float) position.x, (float) position.y);

		parent.fill(204, 102, 0, 100);
		parent.ellipse(position.x, position.y, diameter, diameter);
		parent.image(img, position.x-diameter, position.y-diameter);

		if (parent.debugOn) {
			parent.noFill();
			parent.stroke(255, 0, 0);
			parent.ellipse(0, 0, diameter(), diameter());
			parent.stroke(30);

		}

		parent.popMatrix();

	}
}