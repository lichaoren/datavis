package lab3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import processing.core.*;

public class MyProcessingSketch extends PApplet {
	private static final long serialVersionUID = 1L;

	// window
	int win_margin = 0;
	int win_width = 1280;
	int win_height = 720;
	PGraphics pg;

	// data
	FloatTable _data;
	ArrayList<String> _selectedManu;

	// font
	PFont[] _fontList;
	int BLACK = color(0);
	int DARKGREY = color(63);
	int MIDGREY = color(127);
	int LIGHTGREY = color(191);
	int WHITE = color(255);

	// content in Section A
	int secA_x0, secA_y0, secA_x1, secA_y1;
	int secA_width, secA_height;
	int secA_margin;
	int secA_bgColor;
	int secA_buttonHeight;
	int secA_rowDistance;
	int secA_labelSize;
	boolean secA_selectionChanged;
	Button[] secA_brandButtons;
	ArrayList<String> secA_newSelectedBrand;
	ArrayList<String> secA_oldSelectedBrand;
	Myutils secA_bbox;

	// content in Section B
	int secB_x0, secB_y0, secB_x1, secB_y1;
	int secB_width, secB_height;
	int secB_margin;
	int secB_bgColor;
	int secB_buttonHeight;
	int secB_rowDistance;
	int secB_labelSize;
	ArrayList<String> secB_tagNames;;
	ArrayList<Button> secB_tags;

	// content in Section C
	int secC_x0, secC_y0, secC_x1, secC_y1;
	int secC_width, secC_height;
	int secC_margin;
	int secC_bgColor;
	int secC_buttonHeight;
	int secC_rowDistance;
	int secC_labelSize;
	Button[] secC_attrButtons;
	boolean secC_selectionChanged;
	ArrayList<String> secC_selectedAttr;
	Myutils secC_bbox;
	int secC_colorUsing = 0;

	// content in Section E
	int secE_x0, secE_y0, secE_x1, secE_y1;
	int secE_width, secE_height;
	int secE_margin;
	int secE_bgColor;
	int secE_buttonHeight;
	int secE_rowDistance;
	int secE_labelSize;
	int secE_axisHeight, secE_axisWidth;
	LinkedList<Axis> secE_axises;
	Button[] secE_flipButtons;
	int secE_flipButtonYmin, secE_flipButtonYmax;
	Button[] secE_orderButtons;
	int secE_orderButtonYmin, secE_orderButtonYmax;
	int[] secE_linesColor;
	Myutils secE_bbox;
	LinkedList<Integer> secE_rowsToDraw;
	int secE_currentPressesAxis;
	int interval;

	private void secAInitialize() {
		secA_x0 = 0;
		secA_y0 = 0;
		secA_x1 = 180;
		secA_y1 = 150;
		secA_width = secA_x1 - secA_x0;
		secA_height = secA_y1 - secA_y0;
		secA_margin = 10;
		secA_bgColor = color(color(244, 230, 230));
		secA_buttonHeight = 16;
		secA_rowDistance = 5;
		secA_labelSize = 14;
		secA_oldSelectedBrand = new ArrayList<String>();
		secA_newSelectedBrand = new ArrayList<String>();
		secA_bbox = new Myutils(secA_x0, secA_y0, secA_x1, secA_y1);

		List<String> tmp = new ArrayList<String>(_data.manufactures.keySet());
		Collections.sort(tmp);
		secA_brandButtons = new Button[tmp.size()];
		int x = secA_x0 + secA_margin, y = secA_y0 + 30;
		textSize(secA_labelSize);
		textFont(_fontList[0]);
		for (int i = 0; i < tmp.size(); ++i) {
			String str = tmp.get(i);
			int w = floor(textWidth(str)) + 6;
			if (x + w > secA_x1 - secA_margin) {
				x = secA_x0 + secA_margin + 5;
				y += secA_buttonHeight + secA_rowDistance;
			}
			secA_brandButtons[i] = new Button(this, x, y, w, secA_buttonHeight,
					str);
			secA_brandButtons[i].font(_fontList[0]);
			x += w + secA_margin / 2;
		}
	}

	private void secADraw() {
		stroke(LIGHTGREY);
		strokeWeight(1.5f);
		fill(secA_bgColor);
		rect(secA_x0, secA_y0, secA_width, secA_height);
		fill(DARKGREY);
		textFont(_fontList[1]);
		textSize(18);
		textAlign(CENTER, TOP);
		text("Manufacture", (secA_x0 + secA_x1) / 2, secA_y0 + 5);
		for (Button b : secA_brandButtons)
			b.render();
	}

	private void secBInitialize() {
		secB_x0 = 180;
		secB_y0 = 0;
		secB_x1 = 580;
		secB_y1 = 150;
		secB_width = secB_x1 - secB_x0;
		secB_height = secB_y1 - secB_y0;
		secB_margin = 10;
		secB_bgColor = color(color(192, 244, 192));
		secB_buttonHeight = 20;
		secB_rowDistance = 5;
		secB_labelSize = 14;
		secB_tagNames = new ArrayList<String>();
		secB_tags = new ArrayList<Button>();

	}

	private void secBDraw() {
		stroke(LIGHTGREY);
		strokeWeight(1.5f);
		fill(secB_bgColor);
		rect(secB_x0, secB_y0, secB_width, secB_height);
		textFont(_fontList[1]);
		fill(DARKGREY);
		textSize(18);
		textAlign(CENTER, TOP);
		text("Models", (secB_x0 + secB_x1) / 2, secB_y0 + 5);

		if (secA_newSelectedBrand.size() == 0) {
			// draw all models or draw nothing?
		} else {
			// generate name list<String> for tags
			// can be optimized by checking change
			secB_tagNames.clear();
			secB_tags.clear();
			for (int i = 0; i < secA_newSelectedBrand.size(); ++i) {
				secB_tagNames.addAll(_data.models.get(secA_newSelectedBrand
						.get(i)));
			}
			// then generate tags using the name list
			textSize(secB_labelSize);
			textFont(_fontList[0]);
			int x = secB_x0 + secB_margin, y = secB_y0 + 30;
			for (int i = 0; i < secB_tagNames.size(); ++i) {
				String str = secB_tagNames.get(i);
				int w = floor(textWidth(str)) + 6;
				if (x + w > secB_x1 - secB_margin) {
					x = secB_x0 + secB_margin;
					y += secB_buttonHeight + secB_rowDistance;
					if (y > secB_y1)
						break;
				}
				secB_tags
						.add(new Button(this, x, y, w, secB_buttonHeight, str));
				x += w + secB_margin / 2;
			}
			// render tags
			for (Button b : secB_tags)
				b.render();
		}
	}

	private void secCInitialize() {
		secC_x0 = 580;
		secC_y0 = 0;
		secC_x1 = 980;
		secC_y1 = 150;
		secC_width = secC_x1 - secC_x0;
		secC_height = secC_y1 - secC_y0;
		secC_margin = 10;
		secC_bgColor = color(color(192, 192, 244));
		secC_buttonHeight = 16;
		secC_rowDistance = 5;
		secC_labelSize = 14;
		secC_selectedAttr = new ArrayList<String>();
		secC_bbox = new Myutils(secC_x0, secC_y0, secC_x1, secC_y1);

		secC_attrButtons = new Button[_data.columnCount];
		textSize(secC_labelSize);
		textFont(_fontList[0]);
		int x = secC_x0 + secC_margin, y = secC_y0 + 30;
		for (int col = 0; col < _data.columnCount; ++col) {
			String str = _data.columnNames[col];
			int w = floor(textWidth(str)) + 6;
			if (x + w > secC_x1 - secC_margin) {
				x = secC_x0 + secC_margin;
				y += secC_buttonHeight + secC_rowDistance;
			}
			secC_attrButtons[col] = new Button(this, x, y, w,
					secC_buttonHeight, str);
			secC_attrButtons[col].font(_fontList[0]);
			x += w + secC_margin / 2;
		}
	}

	private void secCDraw() {
		stroke(LIGHTGREY);
		strokeWeight(1.5f);
		fill(secC_bgColor);
		rect(secC_x0, secC_y0, secC_width, secC_height);
		fill(DARKGREY);
		textFont(_fontList[1]);
		textSize(18);
		textAlign(CENTER, TOP);
		text("Attribute", (secC_x0 + secC_x1) / 2, secC_y0 + 5);
		for (Button b : secC_attrButtons)
			b.render();
	}

	private void secEInitialize() {
		secE_x0 = 0;
		secE_y0 = 150;
		secE_x1 = win_width;
		secE_y1 = win_height;
		secE_width = secE_x1 - secE_x0;
		secE_height = secE_y1 - secE_y0;
		secE_margin = 40;
		secE_bgColor = color(255);
		secE_axisWidth = 6;
		secE_axisHeight = 400;
		secE_bbox = new Myutils(secE_x0, secE_y0, secE_x1, secE_y1);

		secE_axises = new LinkedList<Axis>();
		secE_flipButtons = new Button[_data.columnCount];
		secE_rowsToDraw = new LinkedList<Integer>();
		secE_orderButtons = new Button[_data.columnCount];

		interval = (secE_width - 2 * secE_margin) / _data.columnCount;
		int x = secE_x0 + 70;
		int y = secE_y1 - secE_margin;
		int buttonHeight = 14;
		secE_flipButtonYmin = y - secE_axisHeight - 20;
		secE_flipButtonYmax = secE_flipButtonYmin + buttonHeight;
		secE_orderButtonYmin = y + 10;
		secE_orderButtonYmax = secE_orderButtonYmin + buttonHeight;
		for (int col = 0; col < _data.columnCount; ++col) {
			secE_axises.add(new Axis(this, DIRECTION.VERTICAL, x + interval
					* col, y, x + interval * col, y - secE_axisHeight,
					LABEL_POSITION.TOP, _data.columnNames[col]));
			secE_axises.getLast().setMax(_data.getColumnMax(col));
			secE_axises.getLast().setMin(_data.getColumnMin(col));
			secE_axises.getLast().numberOfTicks(6);
			secE_axises.getLast()._inversed = false;
			secE_axises.getLast()._colIndex = col;
			secE_flipButtons[col] = new Button(this, x + interval * col - 5, 
					secE_flipButtonYmin , buttonHeight, buttonHeight, "I");
			secE_flipButtons[col].font(_fontList[1]);
			secE_orderButtons[col] = new Button(this, x + interval * col - 5,
					secE_orderButtonYmin, buttonHeight, buttonHeight, "+");
		}
	}

	private void secEDraw() {
		stroke(LIGHTGREY);
		strokeWeight(1.5f);
		fill(secE_bgColor);
		rect(secE_x0, secE_y0, secE_width, secE_height);
		secE_drawLines();
		for (Axis a : secE_axises)
			a.render();
		for (Button b : secE_flipButtons)
			b.render();
		for (Button b : secE_orderButtons)
			b.render();
	}

	private void dataInitialize() {
		_data = new FloatTable(this, "../cameras.tsv");
		_data.genDict();
	}

	private void fontsInitialize() {
		_fontList = new PFont[4];
		_fontList[0] = createFont("Ubuntu", 12);
		_fontList[1] = createFont("Ubuntu Bold", 12);
		_fontList[2] = createFont("Ubuntu", 12);
		_fontList[3] = createFont("Ubuntu", 12);
	}

	private void initialize() {
		dataInitialize();
		fontsInitialize();
		secAInitialize();
		secBInitialize();
		secCInitialize();
		secEInitialize();
	}

	public void setup() {
		size(win_width, win_height);
		smooth();
		initialize();
	}

	public void draw() {
		fill(255);
		rect(0, 0, win_width, win_height);
		secADraw();
		secBDraw();
		secCDraw();
		secEDraw();
	}

	// pass mouse position when in mouseClick and mousePress calls
	public void mouseClicked() {
		// update buttons in section A
		if (secA_bbox.inBBox(mouseX, mouseY)) {
			boolean hitButton = false;
			for (Button b : secA_brandButtons) {
				if (b._bbox.inBBox(mouseX, mouseY)) {
					hitButton = true;
					b._pressed = !b._pressed;

					if (b._pressed)
						secA_newSelectedBrand.add(b._label);
					else
						secA_newSelectedBrand.remove(b._label);
					break;
				}
			}
			if (!hitButton) {
				for (Button b : secA_brandButtons) {
					b._pressed = false;
					secA_newSelectedBrand.remove(b._label);
				}
			}
			//
			// update rowsToDraw by selected brand, needs to filter again if
			// has other selections on axises
			//
			secE_rowsToDraw.clear();
			for (String str : secA_newSelectedBrand)
				secE_rowsToDraw.addAll(_data.manufactures.get(str));
			return;
		}

		if (secC_bbox.inBBox(mouseX, mouseY)) {
			// update tags in section C
			for (int col = 0; col < _data.columnCount; ++col) {
				if (secC_attrButtons[col]._bbox.inBBox(mouseX, mouseY)) {
						secC_colorUsing = col;
						break;
				}
			}
			return;
		}

		if (secE_bbox.inBBox(mouseX, mouseY)) {
			// check inverse in section E
			if (mouseY <= secE_flipButtonYmax && mouseY >= secE_flipButtonYmin) {
				for (int i = 0; i < secE_flipButtons.length; ++i) {
					if (secE_flipButtons[i]._bbox.inBBox(mouseX, mouseY)) {
						secE_flipButtons[i]._pressed = !secE_flipButtons[i]._pressed;
							secE_axises.get(i).invert();;
						return;
					}
				}
			} else if (mouseY <= secE_orderButtonYmax
					&& mouseY >= secE_orderButtonYmin) {
				for (int i = 0; i < secE_orderButtons.length; ++i) {
					if (secE_orderButtons[i]._bbox.inBBox(mouseX, mouseY)) {
						Collections.swap(secE_axises, i, i-1);
						// swap positional variables
						int x0 = secE_axises.get(i)._x0;
						int x1 = secE_axises.get(i)._x1;
						int y0 = secE_axises.get(i)._y0;
						int y1 = secE_axises.get(i)._y1;
//						int[] locY = secE_axises.get(i)._locY;
						Myutils bbox = secE_axises.get(i)._bbox;
						secE_axises.get(i)._x0 = secE_axises.get(i-1)._x0;
						secE_axises.get(i)._x1 = secE_axises.get(i-1)._x1;
						secE_axises.get(i)._y0 = secE_axises.get(i-1)._y0;
						secE_axises.get(i)._y1 = secE_axises.get(i-1)._y1;
//						secE_axises.get(i)._locY = secE_axises.get(i-1)._locY;
						secE_axises.get(i)._bbox = secE_axises.get(i-1)._bbox;
						secE_axises.get(i-1)._x0 = x0;
						secE_axises.get(i-1)._x1 = x1;
						secE_axises.get(i-1)._y0 = y0;
						secE_axises.get(i-1)._y1 = y1;
//						secE_axises.get(i-1)._locY = locY;
						secE_axises.get(i-1)._bbox = bbox;
						for (int k = 0; k < 4; ++k) {
							secE_axises.get(i)._locX[k] += interval;
							secE_axises.get(i-1)._locX[k] -= interval;
						}
						
						return;
					}
				}
			}
		}
	}

	public void mousePressed() {
		if (secE_bbox.inBBox(mouseX, mouseY)) {
			for (int col = 0; col < _data.columnCount; ++col) {
				if (secE_axises.get(col).bbox(mouseX, mouseY)) {
					secE_axises.get(col).mousePressed(mouseX, mouseY);
					secE_currentPressesAxis = col;
					return;
				}
			}
		}
	}

	public void mouseReleased() {
		if (secE_axises.get(secE_currentPressesAxis).bbox(mouseX, mouseY)) {
			secE_axises.get(secE_currentPressesAxis).mouseReleased();
			secE_updateRowsToDraw(secE_currentPressesAxis);
		}
	}

	// use mouseX and mouseY directly when in mouseDrag call
	public void mouseDragged() {
		if (secE_axises.get(secE_currentPressesAxis).bbox(mouseX, mouseY)) {
			secE_axises.get(secE_currentPressesAxis).mouseDragged();
			secE_draggingUpdateRowsToDraw(secE_currentPressesAxis);
		}
	}

	void secE_draggingUpdateRowsToDraw(int col) {
		if (secE_axises.get(col)._dragging) {
			boolean clean = true;
			for (Axis a : secE_axises)
				clean = clean && a._cleaned;
			if (secA_newSelectedBrand.isEmpty()) {
				secE_rowsToDraw.clear();
				for (String str : _data.manufactures.keySet())
					secE_rowsToDraw.addAll(_data.manufactures.get(str));
			} else {
				secE_rowsToDraw.clear();
				for (String str : secA_newSelectedBrand)
					secE_rowsToDraw.addAll(_data.manufactures.get(str));
			}
			if (!clean) {
				for (int col1 = 0; col1 < secE_axises.size(); ++col1) {
					if (!secE_axises.get(col1)._cleaned) {
						java.util.ListIterator<Integer> iter = secE_rowsToDraw
								.listIterator();
						while (iter.hasNext()) {
							int row = iter.next();
							if (_data.isValid(row, col1)) {
								float value = _data.getFloat(row, col1);
								if (value > secE_axises.get(col1)._selectedMax
										|| value < secE_axises.get(col1)._selectedMin)
									iter.remove();
							}
						}

					}
				}
			}
		}
	}

	// I tried to use an array to filter out overlapped indices by &&
	// but slow for large data set
	// can be optimized using map from values to row number, sort
	private void secE_updateRowsToDraw(int col) {
		// if no brand selected, it's all selected

		if (secE_axises.get(col)._cleaned || secE_axises.get(col)._dragging) {
			// recompute lines to draw if cleaned
			// then filter axis by axis
			boolean clean = true;
			for (Axis a : secE_axises)
				clean = clean && a._cleaned;
			if (secA_newSelectedBrand.isEmpty()) {
				secE_rowsToDraw.clear();
				for (String str : _data.manufactures.keySet())
					secE_rowsToDraw.addAll(_data.manufactures.get(str));
			} else {
				secE_rowsToDraw.clear();
				for (String str : secA_newSelectedBrand)
					secE_rowsToDraw.addAll(_data.manufactures.get(str));
			}
			if (!clean) {
				for (int col1 = 0; col1 < secE_axises.size(); ++col1) {
					if (!secE_axises.get(col1)._cleaned) {
						java.util.ListIterator<Integer> iter = secE_rowsToDraw
								.listIterator();
						while (iter.hasNext()) {
							int row = iter.next();
							if (_data.isValid(row, col1)) {
								float value = _data.getFloat(row, col1);
								if (value > secE_axises.get(col1)._selectedMax
										|| value < secE_axises.get(col1)._selectedMin)
									iter.remove();
							}
						}

					}
				}
			}
		} else {
			// filter rowsToDraw if not cleaned
			java.util.ListIterator<Integer> iter = secE_rowsToDraw
					.listIterator();
			while (iter.hasNext()) {
				int row = iter.next();
				if (_data.isValid(row, col)) {
					float value = _data.getFloat(row, col);
					if (value > secE_axises.get(col)._selectedMax
							|| value < secE_axises.get(col)._selectedMin)
						iter.remove();
				}
			}
		}
	}

	private void secE_drawLines() {
		noFill();
		strokeWeight(1);

		boolean clean = true;
		for (Axis a : secE_axises)
			clean = clean && a._cleaned;
		if (secA_newSelectedBrand.isEmpty() && clean) {
			secE_rowsToDraw.clear();
			for (String str : _data.manufactures.keySet())
				secE_rowsToDraw.addAll(_data.manufactures.get(str));
		}

		for (int row : secE_rowsToDraw) {
			beginShape();
			java.util.ListIterator<Axis> iter = secE_axises.listIterator();
			while (iter.hasNext()) {
				Axis a = iter.next();
				if (_data.isValid(row, a._colIndex)) {
					float cMax = (a._inversed ? a._min : a._max);
					float cMin = (a._inversed ? a._max : a._min);
					float value = _data.getFloat(row, a._colIndex);
					float colorValue = _data.getFloat(row, secC_colorUsing);
					
					float x = a.getX0();
					float y = map(value, cMin, cMax, a.getY0(), a.getY1());
					
					if (a._colIndex == 0) {
						stroke(lerpColor(color(4,90,141), color(253,141,60),
								map(colorValue, secE_axises.get(secC_colorUsing)._min, 
										secE_axises.get(secC_colorUsing)._max,
										0.0f, 1.0f)), 150);
					}
					vertex(x, y);
				}
			}
			endShape();
		}
	}
}