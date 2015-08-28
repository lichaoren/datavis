package lab4;

import java.util.LinkedList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

class HScrollbar {
	PApplet _parent;
	int barWidth, barHeight; // width and height of bar. NOTE: barHeight also
								// used as slider button width.
	int Xpos, Ypos; // upper-left position of bar
	float Spos, newSpos; // x (leftmost) position of slider
	int SposMin, SposMax; // max and min values of slider
	int loose; // how loose/heavy
	boolean over; // True if hovering over the scrollbar
	boolean locked; // True if a mouse button is pressed while on the scrollbar
	int barOutlineCol;
	int barFillCol;
	int barHoverCol;
	int sliderFillCol;
	int sliderPressCol;

	HScrollbar(PApplet p, int X_start, int Y_start, int bar_width,
			int bar_height, int loosiness, int bar_outline, int bar_background,
			int slider_bg, int barHover, int slider_press) {
		_parent = p;
		barWidth = bar_width;
		barHeight = bar_height;
		Xpos = X_start;
		Ypos = Y_start;
		Spos = Xpos + barWidth / 2 - barHeight / 2; // center it initially
		newSpos = Spos;
		SposMin = Xpos;
		SposMax = Xpos + barWidth - barHeight;
		loose = loosiness;
		if (loose < 1)
			loose = 1;
		barOutlineCol = bar_outline;
		barFillCol = bar_background;
		sliderFillCol = slider_bg;
		barHoverCol = barHover;
		sliderPressCol = slider_press;
	}

	void update() {
		over = over();
		if (_parent.mousePressed && over)
			locked = true;
		else
			locked = false;

		if (locked) {
			newSpos = constrain(_parent.mouseX - barHeight / 2, SposMin,
					SposMax);
		}

		if (Math.abs(newSpos - Spos) > 0) {
			Spos = Spos + (newSpos - Spos) / loose;
		}
	}

	int constrain(int val, int minv, int maxv) {
		return Math.min(Math.max(val, minv), maxv);
	}

	boolean over() {
		if (_parent.mouseX > Xpos && _parent.mouseX < Xpos + barWidth
				&& _parent.mouseY > Ypos && _parent.mouseY < Ypos + barHeight) {
			return true;
		} else {
			return false;
		}
	}

	void display() {
		_parent.stroke(barOutlineCol);
		_parent.fill(barFillCol);
		_parent.rect(Xpos, Ypos, barWidth, barHeight);
		if (over) {
			_parent.fill(barHoverCol);
		}
		if (locked) {
			_parent.fill(sliderPressCol);
		}
		if (!over && !locked) {
			_parent.fill(sliderFillCol);
		}
		if (Math.abs(Spos - newSpos) > 0.1)
			_parent.fill(sliderPressCol);
		_parent.rect(Spos, Ypos, barHeight, barHeight);
	}

	float value() {
		// Convert slider position Spos to a value between 0 and 1
		return (Spos - Xpos) / (barWidth - barHeight);
	}

	void setValue(float value) {
		// convert a value (0 to 1) to slider position
		if (value < 0)
			value = 0;
		if (value > 1)
			value = 1;
		Spos = Xpos + ((barWidth - barHeight) * value);
		newSpos = Spos;
	}
}

/**
 * Vertical scrollbar
 * 
 * Modified from Hscrollbar class Returns or sets a float value from 0 - 1
 */

class VScrollbar {
	PApplet _parent;
	int barWidth, barHeight; // width and height of bar. NOTE: barWidth also
								// used as slider button height.
	int Xpos, Ypos; // upper-left position of bar
	float Spos, newSpos; // y (upper) position of slider
	int SposMin, SposMax; // max and min values of slider
	int loose; // how loose/heavy
	boolean over; // True if hovering over the scrollbar
	boolean locked; // True if a mouse button is pressed while on the scrollbar
	int barOutlineCol;
	int barFillCol;
	int barHoverCol;
	int sliderFillCol;
	int sliderPressCol;

	VScrollbar(PApplet p, int X_start, int Y_start, int bar_width,
			int bar_height, int loosiness, int bar_outline, int bar_background,
			int slider_bg, int barHover, int slider_press) {
		_parent = p;
		barWidth = bar_width;
		barHeight = bar_height;
		Xpos = X_start;
		Ypos = Y_start;
		Spos = Ypos + barHeight / 2 - barWidth / 2; // center it initially
		newSpos = Spos;
		SposMin = Ypos;
		SposMax = Ypos + barHeight - barWidth;
		loose = loosiness;
		if (loose < 1)
			loose = 1;
		barOutlineCol = bar_outline;
		barFillCol = bar_background;
		sliderFillCol = slider_bg;
		barHoverCol = barHover;
		sliderPressCol = slider_press;
	}

	void update() {
		over = over();
		if (_parent.mousePressed && over)
			locked = true;
		else
			locked = false;

		if (locked) {
			newSpos = constrain(_parent.mouseY - barWidth / 2, SposMin, SposMax);
		}
		if (Math.abs(newSpos - Spos) > 0) {
			Spos = Spos + (newSpos - Spos) / loose;
		}
	}

	int constrain(int val, int minv, int maxv) {
		return Math.min(Math.max(val, minv), maxv);
	}

	boolean over() {
		if (_parent.mouseX > Xpos && _parent.mouseX < Xpos + barWidth
				&& _parent.mouseY > Ypos && _parent.mouseY < Ypos + barHeight) {
			return true;
		} else {
			return false;
		}
	}

	void display() {
		_parent.stroke(barOutlineCol);
		_parent.fill(barFillCol);
		_parent.rect(Xpos, Ypos, barWidth, barHeight);
		if (over) {
			_parent.fill(barHoverCol);
		}
		if (locked) {
			_parent.fill(sliderPressCol);
		}
		if (!over && !locked) {
			_parent.fill(sliderFillCol);
		}
		if (Math.abs(Spos - newSpos) > 0.1)
			_parent.fill(sliderPressCol);
		_parent.rect(Xpos, Spos, barWidth, barWidth);
	}

	float value() {
		// Convert slider position Spos to a value between 0 and 1
		return (Spos - Ypos) / (barHeight - barWidth);
	}

	void setValue(float value) {
		// convert a value (0 to 1) to slider position Spos
		if (value < 0)
			value = 0;
		if (value > 1)
			value = 1;
		Spos = Ypos + ((barHeight - barWidth) * value);
		newSpos = Spos;
	}
}


class BBox {
	int _x0, _y0, _x1, _y1;
	
	BBox(int x0, int y0, int x1, int y1) {
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

class Button {
	PApplet _parent;
	int _x, _y, _width, _height;
	BBox _bbox;
	int[] _color;

	int timer = 0;
	boolean _pressed = false;
	
	String _label;
	PFont _labelFont;
	int _labelX, _labelY;
	int _textColor, _textSize;
	
	Button(PApplet p, 
			int x, int y, int w, int h,
			String s) {
		_parent = p;
		_x = x; _y = y; _width = w; _height = h;
		_bbox = new BBox(x, y, x+w, y+h);
		_label = s;
		
		_color = new int[2];
		_parent.colorMode(PConstants.RGB);
		_color[0] = _parent.color(255);
		_color[1] = _parent.color(192);
		
		_labelX = x + w/2;
		_labelY = y + h/2;
		_labelFont = _parent.createFont("Ubuntu", 12);
		_textColor = _parent.color(0);
		_textSize = h;
	}
	
	Button(PApplet p, 
			int x, int y, int w, int h,
			int c1, int c2,
			String s) {
		_parent = p;
		_x = x; _y = y; _width = w; _height = h;
		_bbox = new BBox(x, y, x+w, y+h);
		_label = s;
		
		_color = new int[2];
		_parent.colorMode(PConstants.RGB);
		_color[0] = c1;
		_color[1] = c2;
		
		_labelX = x + w/2;
		_labelY = y + h/2;
		_labelFont = _parent.createFont("Ubuntu", 12);
	}
	
	public int[] get_color() {
		return _color;
	}
	
	public void set_color(int[] _color) {
		this._color = _color;
	}
	
	public int get_color(int i) {
		return _color[i];
	}

	public void set_color(int color, int index) {
		this._color[index] = color;
	}
	
	public PFont get_labelFont() {
		return _labelFont;
	}

	public void set_labelFont(PFont _labelFont) {
		this._labelFont = _labelFont;
	}
	
	boolean in() {
		return this._bbox.inBBox(_parent.mouseX, _parent.mouseY);
	}
	
	void click() {
		if (this.in() && _parent.millis() - timer > 50) {
			_pressed = !_pressed;
			timer = _parent.millis();
			PApplet.println("in");
		}
		else PApplet.println("out");
	}
	
	void update() {
		click();
	}
	
	void render() {
		_parent.strokeWeight(1.5f);
		_parent.stroke(96);
		if (_pressed) {
			_parent.fill(_color[1]);
		}
		else {
			_parent.fill(_color[0]);
		}
		_parent.rect(_x, _y, _width, _height);
		_parent.textFont(_labelFont);
		_parent.fill(_textColor);
		_parent.textSize(_textSize);
		_parent.textAlign(PConstants.CENTER, PConstants.CENTER);
		_parent.text(_label, _labelX, _labelY);
	}
}



class CircleButton extends Button {
	
	CircleButton(PApplet p, int x, int y, int r, String l) {
		super(p, x, y, r, r, l);
	}


	boolean in() {
		if (this.in()){
			float disX = _x - _parent.mouseX;
			float disY = _y - _parent.mouseY;
			if (Math.sqrt(PApplet.sq(disX) + PApplet.sq(disY)) < _width / 2) {
				return true;
			} else
				return false;
		}
		return false;
	}
	
	void click() {
		if (this.in() && _parent.millis() - timer > 30) {
			_pressed = !_pressed;
			timer = _parent.millis();
		}
	}

	void render() {
		_parent.strokeWeight(1.5f);
		_parent.stroke(96);
		if (_pressed) {
			_parent.fill(_color[1]);
		}
		else {
			_parent.fill(_color[0]);
		}
		_parent.ellipse(_x, _y, _width, _height);
		_parent.textFont(_labelFont);
		_parent.fill(0);
		_parent.textAlign(PConstants.CENTER, PConstants.CENTER);
		_parent.text(_label, _labelX, _labelY);
	}
}

class ButtonSets {
	PApplet _parent;
	LinkedList<Button> _buttonList;
	int _lastButton;


	
	public ButtonSets(PApplet p) {
		_parent = p;
		_buttonList = new LinkedList<Button>();
		_lastButton = 0;
	}
	public LinkedList<Button> get_buttonList() {
		return _buttonList;
	}
	
	public void set_buttonList(LinkedList<Button> _buttonList) {
		this._buttonList = _buttonList;
	}
	
	public void addButton(int x, int y, int w, int h, String l) {
		_buttonList.addLast(new Button(_parent, x, y, w, h, l));
		if (_buttonList.size() == 1) {
			_buttonList.get(0)._pressed = true;
			_lastButton = 0;
		}
	}

	public void setTextSize(int i, int s) {
		_buttonList.get(i)._textSize = s;
	}
	
	public int selectedIndex() {
		return _lastButton;
	}
	
	public void render() {
		for (Button b : _buttonList)
			b.render();
	}
	
	public void mouseClicked() {
		for (int i = 0; i < _buttonList.size(); ++i){
			if (_buttonList.get(i).in() && i != _lastButton) {
				_buttonList.get(i)._pressed = true;
				_buttonList.get(_lastButton)._pressed = false;
				_lastButton = i;
			}
		}
	}
}

class Histogram{
	PApplet _parent;
	int x, y, w, h;
	String l;
	
//	Axis vAxis, hAxis;
	int barWidth;
	
	public Histogram(PApplet p, int x, int y, int w, int h, String l) {
		_parent = p;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.l = l;
		
	}
	
	public void render() {
		for(int i = 0; i < 10; ++i){
			
		}
	}
	
}
