package lab3;

import processing.core.*;

enum DIRECTION {
	VERTICAL, HORIZONTAL
}

enum TICK_LAYOUT {
	LEFT, RIGHT, BOTH
}

// those values are in terms of the
// direction of the axis pointing to
enum LABEL_POSITION {
	LEFT, RIGHT, TOP
}

class Axis {
	PApplet _parent;
	// visual variables
	DIRECTION _direction;
	int _x0, _y0, _x1, _y1;
	int _large, _small;
	int _width;
	int _color;
	float _strokeWidth = 1.5f;
	Myutils _bbox;
	int _colIndex;

	// ticks variables
	TICK_LAYOUT _tickLayout;
	float _max, _min;
	float _majorTick, _minorTick, _numbOfTicks, _numbMinorTicks;

	String _labelName;
	LABEL_POSITION _labelLayout;

	// text
	PFont _textFont1;
	PFont _textFont2;
	int _labelOffset;
	int _textTicksSize;
	int _textLabelSize;
	int _labelColor;

	// interactive params
	boolean _creating = false;
	boolean _cleaned = true;
	boolean _dragging = false;
	int[] _locX;
	int[] _locY;
	int[] _offset;
	float _selectedMin;
	float _selectedMax;
	int _time;

	boolean _inversed = false;

	// ctor
	Axis(PApplet p, DIRECTION d, int x0, int y0, int x1, int y1,
			LABEL_POSITION labL, String labN) {
		_parent = p;
		_direction = d;
		_x0 = x0; // x0 and y0 stands for the start point of the axis
		_y0 = y0; // x1 and y1 : end point
		_x1 = x1;
		_y1 = y1;
		_width = 10;
		_labelLayout = labL;
		_labelOffset = 10;
		_labelName = labN;

		if (_direction == DIRECTION.VERTICAL) {
			_bbox = new Myutils(_x0 - _width / 2, _y0, _x0 + _width, _y1);
			if (_y0 >= _y1) {
				_large = _y0;
				_small = _y1;
			} else {
				_large = _y1;
				_small = _y0;
			}
		} else if (_direction == DIRECTION.HORIZONTAL) {
			_bbox = new Myutils(_x0, _y0 - _width / 2, _x1, _y0 + _width);
		}

		// default values for other variables
		_color = _parent.color(0, 0, 0);
		_tickLayout = TICK_LAYOUT.RIGHT;
		_max = 100;
		_min = 0;
		_numbOfTicks = 6;
		_numbMinorTicks = 5;
		numberOfTicks(6);

		_textTicksSize = 10;
		_textLabelSize = 12;
		_labelColor = _color;
		_textFont1 = _parent.createFont("Ubuntu", 12);
		_textFont2 = _parent.createFont("Ubuntu Bold", 12);

		_locX = new int[4];
		_locY = new int[4];
		_offset = new int[2];
	}
	
	

	void ticks(float n) {
		_majorTick = PApplet.floor(n);
		_minorTick = _majorTick / 4.0f;
		_numbOfTicks = PApplet.ceil((_max - _min) / _majorTick);
	}

	void numberOfTicks(int n) {
		_numbOfTicks = n;
		float v = (_max - _min) / (float)n;
		
		_majorTick = v < 1.0f ? v : PApplet.floor(v);

		_minorTick = _majorTick / (float)_numbMinorTicks;
	}

	void tickLayout(TICK_LAYOUT l) {
		_tickLayout = l;
	}

	boolean bbox(int x, int y) {
		return _bbox.inBBox(x, y);
	}

	void setMax(float m) {
		_max = m;
	}

	void setMin(float m) {
		_min = m;
	}

	int getX0() {
		return _x0;
	}

	int getX1() {
		return _x1;
	}

	int getY0() {
		return _y0;
	}

	int getY1() {
		return _y1;
	}

	int mousePressed(int x, int y) { // call this in mousePressed()
		if (bbox(x, y)) {
			if (_direction == DIRECTION.VERTICAL) {
				if (!_cleaned) {
					if (y >= PApplet.max(_locY) || y <= PApplet.min(_locY)) {
						_cleaned = true;
						_selectedMax = -1;
						return 1;
					}
					// when dragging, record offset
					else {
						_dragging = true;
						_offset[0] = _locY[0] - y;
						_offset[1] = _locY[2] - y;
						return 2;
					}
				} else {
					// create selection
					_creating = true;
					_cleaned = false;
					_time = _parent.millis();
					_locX[0] = _locX[3] = _x0 - _width / 2;
					_locX[1] = _locX[2] = _locX[0] + _width;
					_locY[0] = _locY[1] = y;
					_locY[2] = _locY[3] = _parent.mouseY;
					PApplet.sort(_locY);
					return 3;
				}
			} else if (_direction == DIRECTION.HORIZONTAL) {
			}

			if (!_cleaned)
				updateSelectedValues();
		}
		return 0;
	}

	void mouseDragged() {
		if (!_cleaned) {
			if (_direction == DIRECTION.VERTICAL) {
				_time = _parent.millis() - _time;
				if (_creating) {
					_locY[2] = _locY[3] = _parent.mouseY;
					if (_locY[2] > _large)
						_locY[2] = _locY[3] = _large;
					else if (_locY[2] < _small)
						_locY[2] = _locY[3] = _small;
				} else if (_dragging) {
					int top, bottom;
					top = _parent.mouseY + _offset[0];
					bottom = _parent.mouseY + _offset[1];
					if (top < _small) {
						_locY[0] = _locY[1] = _small;
					} else if (bottom > _large)
						_locY[2] = _locY[3] = _large;
					else {
						_locY[0] = _locY[1] = top;
						_locY[2] = _locY[3] = bottom;
					}
				}
			} else if (_direction == DIRECTION.HORIZONTAL) {
			}

			updateSelectedValues();
		}
	}

	void mouseReleased() {
		if (!_cleaned) {
			if (_creating) {// done creating selection
				_creating = false;
				if (_parent.millis() - _time < 100)
					_cleaned = true;
				if (PApplet.abs(_parent.mouseY - _locY[0]) < 10)
					_cleaned = true;
				PApplet.sort(_locY);
			} else if (_dragging) { // stop dragging
				_dragging = false;
			}
			updateSelectedValues();
		}
	}

	void updateSelectedValues() {
		float v;
		if (_inversed) {
			_selectedMin = PApplet.map(_locY[0], _small, _large, _min, _max);
			_selectedMax = PApplet.map(_locY[2], _small, _large, _min, _max);
		} else {
			_selectedMin = PApplet.map(_locY[0], _large, _small, _min, _max);
			_selectedMax = PApplet.map(_locY[2], _large, _small, _min, _max);
		}
		if (_selectedMin > _selectedMax) {
			v = _selectedMax;
			_selectedMax = _selectedMin;
			_selectedMin = v;
		}
	}

	void flipSelection() {
		int t = _locY[0] - (_large + _small) / 2;
		_locY[0] = _locY[1] = _locY[0] - 2 * t;
		t = _locY[2] - (_large + _small) / 2;
		_locY[2] = _locY[3] = _locY[2] - 2 * t;
		PApplet.sort(_locY);
	}

	void invert() {
		_inversed = !_inversed;
		flipSelection();
		updateSelectedValues();
	}

	void render() {
		renderTicks();
		renderLabels();
		renderSelection();
	}

	void renderSelection() {
		_parent.noStroke();
		_parent.fill(_parent.color(127, 127, 127, 127));
		if (!_cleaned) {
			_parent.quad(_locX[0], _locY[0], _locX[1], _locY[1], _locX[2],
					_locY[2], _locX[3], _locY[3]);
		}
	}

	void renderLabels() {
		_parent.noStroke();
		_parent.fill(_labelColor);
		_parent.textSize(_textLabelSize);
		if (_labelLayout == LABEL_POSITION.RIGHT) {
			_parent.textAlign(PConstants.CENTER, PConstants.TOP);
			_parent.text(_labelName, (_x0 + _x1) / 2, _y0 + _labelOffset);
		} else if (_labelLayout == LABEL_POSITION.LEFT) {
			_parent.textAlign(PConstants.CENTER, PConstants.BOTTOM);
			_parent.pushMatrix();
			_parent.translate(_x0 - _labelOffset, (_y0 + _y1) / 2);
			_parent.rotate(-PConstants.HALF_PI);
			_parent.text(_labelName, 0, 0);
			_parent.popMatrix();
		} else if (_labelLayout == LABEL_POSITION.TOP) {
			_parent.textAlign(PConstants.LEFT, PConstants.BOTTOM);
			_parent.pushMatrix();
			_parent.translate(_x1, _y1 - 20);
			_parent.rotate(-PConstants.HALF_PI / 2);
			_parent.text(_labelName, 0, 0);
			_parent.popMatrix();
		}
	}

	void renderTicks() {
		_parent.noFill();
		_parent.stroke(_color);
		_parent.strokeWeight(_strokeWidth);
		_parent.line(_x0, _y0, _x1, _y1);
		if (_direction == DIRECTION.HORIZONTAL) {
			for (float v = _min; v <= _max; v += _minorTick) {
				float x = PApplet.map(v, _min, _max, _x0, _x1);
				if (v % _majorTick == 0) { // If a major tick mark
					if (v == _min) {
						_parent.textAlign(PConstants.CENTER, PConstants.TOP);
					} else if (v == _max) {
						_parent.textAlign(PConstants.CENTER, PConstants.TOP);
					} else {
						_parent.textAlign(PConstants.CENTER, PConstants.TOP);
					}
					_parent.noStroke();
					_parent.fill(_labelColor);
					_parent.text(PApplet.floor(v), x, _y0);
					_parent.stroke(_color);
					_parent.strokeWeight(_strokeWidth);
					_parent.line(x, _y0 - 8, x, _y0); // Draw major tick
				} else {
					_parent.stroke(_color);
					_parent.strokeWeight(_strokeWidth);
					_parent.line(x, _y0 - 4, x, _y0); // Draw minor tick
				}
			}
		} else if (_direction == DIRECTION.VERTICAL) {
			float y;
			for (int i = 0; i <= 1000; ++i) {
				float v = _min + i*_minorTick;
				if (v>_max) break;
				if (!_inversed){
					y = PApplet.map(v, _min, _max, _y0, _y1);
				} else {
					y = PApplet.map(v, _min, _max, _y1, _y0);
				}
				
				if (i % _numbMinorTicks == 0) { // If a major tick mark
					_parent.textAlign(PConstants.RIGHT, PConstants.CENTER);
					_parent.noStroke();
					_parent.fill(_parent.color(255));
					_parent.textFont(_textFont2);
					_parent.text(v, _x0 - 4, y);
					_parent.text(v, _x0 - 6, y);
					_parent.text(v, _x0 - 5, y + 1);
					_parent.text(v, _x0 - 5, y - 1);
					_parent.fill(_labelColor);
					_parent.textFont(_textFont1);
					_parent.text(v, _x0 - 5, y);
					_parent.stroke(_color);
					_parent.strokeWeight(_strokeWidth);
					_parent.line(_x0, y, _x0 + 10, y); // Draw major tick
				} else {
					_parent.stroke(_color);
					_parent.strokeWeight(_strokeWidth);
					_parent.line(_x0, y, _x0 + 4, y); // Draw minor tick
				}
			}
		}
	}
}