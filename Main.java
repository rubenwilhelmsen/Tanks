import processing.core.PApplet;
import processing.core.PVector;

import java.util.LinkedList;

public class Main extends PApplet {
	
	public static void main(String[] args) {
		PApplet.main("Main");
	}

	/*
	 * JUST NU:
	 * 
	 */

	public boolean debugOn = false;
	boolean pause = false;

	Grid grid;
	int cols = 15;
	int rows = 15;
	int grid_size = 50;

	int tankInFocus;

	int team0Color = color(204, 50, 50);
	int team1Color = color(0, 150, 200);

	Tree[] allTrees = new Tree[3];

	Tank[] allTanks = new Tank[6];

	Team[] teams = new Team[2];

	int tank_size = 50;
	// Team0
	PVector team0_tank0_startpos;
	PVector team0_tank1_startpos;
	PVector team0_tank2_startpos;

	// Team1
	PVector team1_tank0_startpos;
	PVector team1_tank1_startpos;
	PVector team1_tank2_startpos;

	
	public void settings() {
		size(800, 800);
	}
	
	public void setup() {

		// Skapa alla träd
		allTrees[0] = new Tree(230, 600, this);
		allTrees[1] = new Tree(280, 230, this);// 280,230(300,300)
		allTrees[2] = new Tree(530, 520, this);// 530, 520(500,500);

		grid = new Grid(cols, rows, grid_size, this, allTrees);

		// Team0
		team0_tank0_startpos = new PVector(50, 50);
		team0_tank1_startpos = new PVector(50, 150);
		team0_tank2_startpos = new PVector(50, 250);

		// Team1
		team1_tank0_startpos = new PVector(width - 50, height - 250);
		team1_tank1_startpos = new PVector(width - 50, height - 150);
		team1_tank2_startpos = new PVector(width - 50, height - 50);

		// nytt Team: id, color, tank0pos, id, shot
		teams[0] = new Team(this, 0, tank_size, team0Color, team0_tank0_startpos, 0, team0_tank1_startpos, 1,
				team0_tank2_startpos, 2);

		allTanks[0] = teams[0].tanks[0];
		allTanks[1] = teams[0].tanks[1];
		allTanks[2] = teams[0].tanks[2];

		teams[1] = new Team(this, 1, tank_size, team1Color, team1_tank0_startpos, 3, team1_tank1_startpos, 4,
				team1_tank2_startpos, 5);

		allTanks[3] = teams[1].tanks[0];
		allTanks[4] = teams[1].tanks[1];
		allTanks[5] = teams[1].tanks[2];

		tankInFocus = 0;

	}

	public void draw() {
		background(200);


		// UPDATE LOGIC
		//updateTanksLogic();
		updateTeamsLogic();

		// UPDATE TANKS
		updateTanks();

		// CHECK FOR COLLISIONS
		//checkForCollisionsTanks();

		// UPDATE DISPLAY
		teams[0].displayHomeBase();
		teams[1].displayHomeBase();
		displayTrees();
		updateTanksDisplay();
		/*if (debugOn) {
			grid.display();
		}*/
		showGUI();
	}

	/*
	public void checkForCollisionsTanks() {
		// Check for collisions with Canvas Boundaries
		for (int i = 0; i < allTanks.length; i++) {
			allTanks[i].checkEnvironment();

			// Check for collisions with "no Smart Objects", Obstacles (trees, etc.)
			for (int j = 0; j < allTrees.length; j++) {
				allTanks[i].checkCollision(allTrees[j]);
			}

			// Check for collisions with "Smart Objects", other Tanks.
			for (int j = 0; j < allTanks.length; j++) {
				// if ((allTanks[i].getId() != j) && (allTanks[i].health > 0)) {
				if (allTanks[i].getId() != j) {
					allTanks[i].checkCollision(allTanks[j]);
				}
			}
		}
	}*/

	/*
	private void updateTanksLogic() {
		for (Tank tank : allTanks) {
			tank.updateLogic();
		}
	}*/

	private void updateTeamsLogic() {
		teams[0].updateLogic();
		teams[1].updateLogic();
	}

	private void updateTanks() {
		/*for (Tank2 t : allTanks) {
			t.update();
		}*/
		allTanks[0].update();
	}

	private void showGUI() {
		// println("*** showGUI()");

		textSize(14);
		fill(30);
		text("Team1: " + teams[0].numberOfHits, 10, 20);
		text("Team2: " + teams[1].numberOfHits, width - 100, 20);
		textSize(24);
		textSize(14);

		if (debugOn) {
			// Visa framerate.
			fill(30);
			text("FPS:" + floor(frameRate), 10, height - 10);

			// Visa grid.
			fill(205);
			gridDisplay();

			// Visa musposition och den närmaste noden.
			fill(255, 92, 92);
			ellipse(mouseX, mouseY, 5, 5);
			grid.displayNearestNode(mouseX, mouseY);
		}

		if (pause) {
			textSize(36);
			fill(30);
			text("Paused!", width / 2 - 100, height / 2);
		}

	}

	private void displayTrees() {
		for (int i = 0; i < allTrees.length; i++) {
			allTrees[i].display();
		}
	}

	private void gridDisplay() {
		strokeWeight(0.3f);
		grid.display();
	}

	private void updateTanksDisplay() {
		for (Tank tank : allTanks) {
			tank.display();
		}
	}

	public int getGrid_size(){
		return grid_size;
	}

	public Node gridSearch(Node currentPosition){
		return grid.getNearestNode(currentPosition.position);
	}
	public LinkedList<Node> getAdjacentNodes(Node node) {
		return grid.getChildrenNodes(node);
	}


	public Node gridSearch(PVector currentPosition){
		Node temp = grid.getNearestNode(currentPosition);
		//System.out.println(Arrays.toString(grid.getChildrenNodes(temp).toArray()));
		return temp;
	}

	@Override
	public void keyPressed() {
		if (key == 'd') {
			debugOn = !debugOn;
		}
	}

}