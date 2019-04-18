import processing.core.PVector;

public class Node {
	// A node object knows about its location in the grid
	// as well as its size with the variables x,y,w,h
	float x, y; // x,y location
	float w, h; // width and height
	float angle; // angle for oscillating brightness

	PVector position;
	int col, row;

	Sprite content;
	boolean isEmpty;

	// ***************************************************
	// Node Constructor
	// Denna används för temporära jämförelser mellan Node objekt.
	Node(float _posx, float _posy) {
		this.position = new PVector(_posx, _posy);
	}

	// ***************************************************
	// Används vid skapande av grid
	Node(int _id_col, int _id_row, float _posx, float _posy) {
		this.position = new PVector(_posx, _posy);
		this.col = _id_col;
		this.row = _id_row;

		this.content = null;
		this.isEmpty = true;
	}

	// ***************************************************
	Node(float tempX, float tempY, float tempW, float tempH, float tempAngle) {
		x = tempX;
		y = tempY;
		w = tempW;
		h = tempH;
		angle = tempAngle;
	}

	// ***************************************************
	void addContent(Sprite s) {
		if (this.isEmpty) {
			this.content = s;
		}
	}
	void removeContent(){
		if(!this.isEmpty){
			content = null;
		}
	}

	// ***************************************************
	boolean empty() {
		return this.isEmpty;
	}

	// ***************************************************
	Sprite content() {
		return this.content;
	}

	@Override
	public boolean equals(Object other){
		boolean equal = false;
		if(other instanceof Node){
			Node temp = (Node)other;
			equal = (row == temp.row && col == temp.col);
		}

		return equal;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return  "{" + row + "," + col + "}";
	}


}