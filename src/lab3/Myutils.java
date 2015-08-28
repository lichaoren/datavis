package lab3;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

// BBox always set smaller x and y as upper left point
// and largers for lower right one
class Myutils {
	int _x0, _y0, _x1, _y1;
	
	Myutils(int x0, int y0, int x1, int y1) {
		if (x0 < x1) { _x0 = x0; _x1 = x1; }
		else { _x1 = x0; _x0 = x1; }
		if (y0 < y1) { _y0 = y0; _y1 = y1; }
		else { _y1 = y0; _y0 = y1; }
	}
	boolean inBBox(int x, int y) {
		return (x >= _x0 && y >= _y0 
				&& x <= _x1 && y <= _y1);
	}
}
