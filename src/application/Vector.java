package application;

class Vector {
	int x;
	int y;
	
	Vector(int column, int row) {  // Constructor
		x = column;
		y = row;
	}
	Vector() {}  // Blank constructor
	
	void copyOf(Vector src) {
		x = src.x;
		y = src.y;
	}
	
	void setXY(int column, int row) {
		x = column;
		y = row;
	}
	
	void add(Vector a) {  // Adds the given vector to the current vector and returns the result
		x += a.x;
		y += a.y;
	}
	void add(int dX, int dY) {
		x += dX;
		y += dY;
	}
	
	void flip() {
		x = -x;
		y = -y;
	}
	
	boolean equals(int x, int y) {
		return (this.x == x) && (this.y == y);
	}
	boolean equals(Vector a) {
		return (x == a.x) && (y == a.y);
	}
	
	boolean onBoard() {  // Returns true if the vector is plausible coordinates
		return x >= 0 && x < 8 && y >= 0 && y < 8;
	}
}