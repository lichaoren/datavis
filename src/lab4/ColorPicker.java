package lab4;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class ColorPicker {
	PApplet _parent;
	PFont _textFont;

	int ColorPickerX, ColorPickerY, LineY, CrossX, CrossY, ColorSelectorX,
			ColorSelectorY;

	boolean isDraggingCross = false, isDraggingLine = false,
			ShowColorPicker = false;

	int activeColor;
	int timer = 0;
	int interfaceColor;

	public ColorPicker(PApplet p) {
		_parent = p;

		setActiveColor(_parent.color(0xff));
		interfaceColor = _parent.color(127);

		ColorSelectorX = 100;
		ColorSelectorY = 100;
		ColorPickerX = PApplet
				.constrain(ColorSelectorX + 40, 10, _parent.width);
		ColorPickerY = PApplet.constrain(ColorSelectorY + 40, 10,
				_parent.height);
	}

	public PFont get_textFont() {
		return _textFont;
	}

	public void set_textFont(PFont _textFont) {
		this._textFont = _textFont;
	}

	public int getActiveColor() {
		return activeColor;
	}

	private void setCrossAndLine(int c) {
		LineY = (int) (_parent.hue(c) + ColorPickerY);
		CrossX = (int) (_parent.saturation(c) + ColorPickerX);
		CrossY = (int) (255 - (_parent.brightness(c) - ColorPickerY));
	}

	public void setActiveColor(int activeColor) {
		this.activeColor = activeColor;
		setCrossAndLine(activeColor);
	}

	void setSelectorPos(int x, int y) {
		ColorSelectorX = x;
		ColorSelectorY = y;
	}

	void setPickerX(int x, int y) {
		ColorPickerX = x;
		ColorPickerY = y;
		CrossX = ColorPickerX + _parent.color(_parent.saturation(activeColor));
		CrossY = ColorPickerY + _parent.color(_parent.brightness(activeColor));
	}

	void draw() {
		_parent.colorMode(PConstants.HSB);
		drawColorSelector();

		if (ShowColorPicker) {
			_parent.noStroke();
			drawColorPicker();
			drawactiveColor();
			drawLine();
			drawCross();
			drawValues();
		}
	}

	void drawColorSelector() {
		_parent.strokeWeight(1.5f);
		_parent.stroke(interfaceColor);

		if (_parent.mouseX > ColorSelectorX
				&& _parent.mouseX < ColorSelectorX + 20
				&& _parent.mouseY > ColorSelectorY
				&& _parent.mouseY < ColorSelectorY + 20)
			_parent.fill(_parent.hue(activeColor),
					_parent.saturation(activeColor),
					_parent.brightness(activeColor) + 30);
		else
			_parent.fill(activeColor);

		_parent.rect(ColorSelectorX + 1, ColorSelectorY + 1, 18, 18);
	}

	void drawValues() {

		_parent.fill(255);
		_parent.fill(0);
		_parent.textSize(10);

		if (_textFont != null)
			_parent.textFont(_textFont);
		_parent.textSize(12);
		_parent.textAlign(PConstants.LEFT, PConstants.TOP);

		_parent.text("H: " + (int) ((LineY - ColorPickerY) * 1.417647) + "°",
				ColorPickerX + 285, ColorPickerY + 100);
		_parent.text("S: " + (int) ((CrossX - ColorPickerX) * 0.39215 + 0.5)
				+ "%", ColorPickerX + 286, ColorPickerY + 115);
		_parent.text("B: " + (int) (100 - ((CrossY - ColorPickerY) * 0.39215))
				+ "%", ColorPickerX + 285, ColorPickerY + 130);

		_parent.text("R: " + (int) (_parent.red(activeColor)),
				ColorPickerX + 285, ColorPickerY + 155);
		_parent.text("G: " + (int) (_parent.green(activeColor)),
				ColorPickerX + 285, ColorPickerY + 170);
		_parent.text("B: " + (int) (_parent.blue(activeColor)),
				ColorPickerX + 285, ColorPickerY + 185);

		_parent.text("HEX: \n" + PApplet.hex(activeColor, 6),
				ColorPickerX + 285, ColorPickerY + 210);
	}

	void drawCross() {
		if (_parent.brightness(activeColor) < 90)
			_parent.stroke(255);
		else
			_parent.stroke(0);

		_parent.line(CrossX - 5, CrossY, CrossX + 5, CrossY);
		_parent.line(CrossX, CrossY - 5, CrossX, CrossY + 5);
	}

	void drawLine() {
		_parent.stroke(0);
		_parent.strokeWeight(2);
		_parent.line(ColorPickerX + 259, LineY, ColorPickerX + 276, LineY);
	}

	void drawColorPicker() {
		_parent.loadPixels();

		for (int j = 0; j < 255; j++) {
			for (int i = 0; i < 255; i++)
				_parent.set(ColorPickerX + j, ColorPickerY + i,
						_parent.color(LineY - ColorPickerY, j, 255 - i));
		}

		for (int j = 0; j < 255; j++) {
			for (int i = 0; i < 20; i++)
				_parent.set(ColorPickerX + 258 + i, ColorPickerY + j,
						_parent.color(j, 255, 255));
		}
	}

	void drawactiveColor() {
		_parent.fill(activeColor);
		_parent.rect(ColorPickerX + 282, ColorPickerY, 41, 80);
	}

	private boolean inSelector() {
		return _parent.mouseX > ColorSelectorX
				&& _parent.mouseX < ColorSelectorX + 20
				&& _parent.mouseY > ColorSelectorY
				&& _parent.mouseY < ColorSelectorY + 20;
	}

	private boolean inPicker() {
		return _parent.mouseX > ColorPickerX
				&& _parent.mouseX < ColorPickerX + 325
				&& _parent.mouseY > ColorPickerY
				&& _parent.mouseY < ColorPickerY + 260;
	}
	private boolean inSB() {
		return _parent.mouseX > ColorPickerX + 258
				&& _parent.mouseX < ColorPickerX + 277
				&& _parent.mouseY > ColorPickerY - 1
				&& _parent.mouseY < ColorPickerY + 255;
	}
	private boolean inH() {
		return _parent.mouseX > ColorPickerX - 1
				&& _parent.mouseX < ColorPickerX + 255
				&& _parent.mouseY > ColorPickerY - 1
				&& _parent.mouseY < ColorPickerY + 255;
	}

	void mouseClicked() {
		if (!inPicker()) {
			if (inSelector() && _parent.millis() - timer > 50) {
				ShowColorPicker = !ShowColorPicker;
			} else {
				ShowColorPicker = false;
			}
			timer = _parent.millis();
		}
	}

	void mousePressed() {
		if (ShowColorPicker == true) {
			if (inSB() && !isDraggingCross) {
				LineY = _parent.mouseY;
				isDraggingLine = true;
			} else if (inH() && !isDraggingLine) {
				CrossX = _parent.mouseX;
				CrossY = _parent.mouseY;
				isDraggingCross = true;
			}
			activeColor = _parent.color(LineY - ColorPickerY, CrossX
					- ColorPickerX, 255 - (CrossY - ColorPickerY));
		}
	}

	void mouseDragged() {
		if (inPicker()) {
			if (isDraggingLine == true) {
				if (inSB() && !isDraggingCross)
					LineY = _parent.mouseY;
			} else if (isDraggingCross == true) {
				if (inH() && !isDraggingLine) {
					CrossX = _parent.mouseX;
					CrossY = _parent.mouseY;
				}
			}
			activeColor = _parent.color(LineY - ColorPickerY, CrossX
					- ColorPickerX, 255 - (CrossY - ColorPickerY));
		}

	}

	void mouseReleased() {
		isDraggingCross = false;
		isDraggingLine = false;
	}

}
