package lab5;

// necessary imports to create control panel as separate window
import processing.core.*;

import java.awt.Frame;
import java.util.Arrays;

import controlP5.*;

// control panel class
public class Controls extends PApplet {
	private static final long serialVersionUID = 5782019638481841576L;

	// position vars
	int cWidth = 860; // control panel
	int cHeight = 500;
	int cutX = 265;
	int xposA = 0;
	int yposA = 0;

	// data variables:
	int gridSize;
	int dataMin, dataMax;
	int myColorChannel = 0;
	int prevSelectedX, prevSelectedY;
	boolean resetPrevSel = true;
	int[] red;
	int[] green;
	int[] blue;
	int[] alpha;
	float[][] LabA;
	int[] histo;
	int[][][] data;
	float[][] limits;
	int offset = 0;
	int colors[][];

	// color conversion
	CIELab cvter;
	RadioButton colorChannel;
	Slider moveSelected;
	Slider2D locator;

	public void setup() {

		blendMode(BLEND);
		cvter = new CIELab();
		colors = new int[4][2];
		colors[0][0] = color(240, 30, 30, 255);
		colors[0][1] = color(240, 30, 30, 63);
		colors[1][0] = color(30, 240, 30, 255);
		colors[1][1] = color(30, 240, 30, 63);
		colors[2][0] = color(30, 30, 240, 255);
		colors[2][1] = color(30, 30, 240, 63);
		colors[3][0] = color(240, 240, 30, 255);
		colors[3][1] = color(240, 240, 30, 63);

		limits = new float[4][2];
		limits[0][0] = 0;
		limits[0][1] = 100;
		limits[1][0] = -128;
		limits[1][1] = 128;
		limits[2][0] = -128;
		limits[2][1] = 128;
		limits[3][0] = 0;
		limits[3][1] = 255;
		histo = new int[256];
		Arrays.fill(histo, 0);

		LabA = new float[4][256];
		Arrays.fill(LabA[0], 50);
		Arrays.fill(LabA[1], 0);
		Arrays.fill(LabA[2], 0);
		Arrays.fill(LabA[3], 255);
		initializeTransferFunction();
		loadData();

		createControlPanel();
		initCtrls();
		updateTransferFunction();
	}

	void syncColor() {
		float rgbf[] = { 0, 0, 0 };
		float labf[] = { 0, 0, 0 };
		for (int i = 0; i < 256; ++i) {
			labf[0] = (float) LabA[0][(i + offset) % 256];
			labf[1] = (float) LabA[1][(i + offset) % 256];
			labf[2] = (float) LabA[2][(i + offset) % 256];
			alpha[i] = (int) LabA[3][(i + offset) % 256];
			rgbf = cvter.toRGB(labf);
			red[i] = (int) (rgbf[0] * 255);
			green[i] = (int) (rgbf[1] * 255);
			blue[i] = (int) (rgbf[2] * 255);
		}
	}

	void updateStates() {
		locator.setMinY(limits[myColorChannel][1]);
		locator.setMaxY(limits[myColorChannel][0]);
	}

	public void draw() {
		background(16);
		updateTransferFunction();
		if (resetPrevSel)
			updateStates();
		myDraw();
	}

	public void loadData() {
		int s = volren.data.length;
		gridSize = (int) pow((float) s, (float) (1.0 / 3.0));
		data = new int[gridSize][gridSize][gridSize];
		int dx = 0;
		int dy = 0;
		int dz = 0;
		dataMin = 255;
		dataMax = 0;
		for (int i = 0; i < s; i++) {

			int d = volren.data[i] & 0xFF;
			data[dx][dy][dz] = d;

			if (d < dataMin)
				dataMin = d;
			if (d > dataMax)
				dataMax = d;
			++histo[d];
			dx++;

			if (dx == gridSize) {
				dy++;
				dx = 0;
				if (dy == gridSize) {
					dz++;
					dy = 0;
				}
			}
		}

		int cmax = 0;
		for (int i = 0; i < histo.length; ++i) {
			cmax = histo[i] > cmax ? histo[i] : cmax;
			histo[i] = (int) pow(histo[i], 0.333f);
		}
		cmax = (int) pow(cmax, 0.333f);
		for (int i = 0; i < histo.length; ++i) {
			histo[i] = (int) map(histo[i], 0, cmax, 0, 0xff);
		}
	}

	void initCtrls() {
		int xpos = cutX + 50;
		int ypos = 10;

		cp5.addTextlabel("mylabel1", "Color Channel")
		   .setPosition(xpos, ypos)
		   .setColor(parent.color(17, 17, 17, 255));
		ypos += 15;

		float status1[] = { 1, 0, 0, 0 };
		colorChannel = cp5.addRadio("myColorChannel", xpos, ypos)
		                  .setId(1)
		                  .setSize(20, 20)
		                  .setColorForeground(color(127, 127, 127, 255))
		                  .setItemsPerRow(6)
		                  .setSpacingColumn(15)
		                  .addItem("L", 0)
		                  .addItem("*a", 1)
		                  .addItem("*b", 2)
		                  .addItem("A", 3)
		                  .setColorLabel(parent.color(75, 75, 75))
		                  .setArrayValue(status1);
		for (int i = 0; i < 4; ++i) {
			colorChannel.getItem(i).setColorActive(colors[i][0]);
			colorChannel.getItem(i).setColorBackground(colors[i][1]);
		}
		ypos += 30;

		xposA = xpos;
		yposA = ypos;

		locator = cp5.addSlider2D("myLocator")
		             .setId(3)
		             .setBroadcast(false)
		             .setPosition(xpos + 10, ypos + 10)
		             .setSize(512, 256)
		             .setArrayValue(new float[] { 50, 50 })
		             .setMinX(0)
		             .setMinY(100)
		             .setMaxX(255)
		             .setMaxY(0)
		             .setArrayValue(127, 0)
		             .setLabelVisible(false)
		             .setColorLabel(color(0, 255))
		             .setColorBackground(color(1, 1, 1, 32))
		             .setColorForeground(color(0, 255, 255, 15))
		             .setColorActive(color(0, 132, 224, 255))
		             .setColorValueLabel(color(224, 224, 0, 255))
		             .setBroadcast(true);

		ypos += 286;

		moveSelected = cp5.addSlider("myHslider")
		                  .setId(4)
		                  .setBroadcast(false)
		                  .setPosition(xpos + 10, ypos)
		                  .setWidth(512)
		                  .setRange(255, 0)
		                  .setValue(255)
		                  .setSliderMode(Slider.FLEXIBLE)
		                  .setBroadcast(true);
		ypos += 30;

	}

	void drawHisto(int xp, int yp, int w, int h) {
		int inxp = xp + 10;
		int inyp = yp + h - 10;
		int inw = w - 20;
		noStroke();
		fill(0x2f - 1, 250);
		rect(xp, yp, w, h + 5);
		fill(127);
		beginShape();
		vertex(inxp, inyp);
		for (int x = 0; x < 512; x++) {
			vertex((float) x + inxp, inyp - histo[x / 2]);
		}
		vertex(inxp + inw, inyp);
		endShape(CLOSE);
	}

	void drawSelection(int xp, int yp, int w, int h) {
		int inxp = xp + 10;
		noFill();
		for (int j = 0; j < 4; ++j) {
			if (j == myColorChannel)
				stroke(colors[j][0]);
			else
				stroke(colors[j][1]);
			beginShape();
			for (int i = 0; i < 512; i++) {
				vertex((float) i + inxp,
				        map(LabA[j][(i / 2 + offset) % 256], limits[j][1],
				                limits[j][0], yposA + 10, yposA + 10 + 256));
			}
			endShape();
		}
	}

	void drawColorBar(int xp, int yp, int w, int h) {
		fill(color(255, 255));
		int x1 = xp + w;
		int i = 0;
		for (int x = xp; x < x1; ++x, ++i) {
			stroke(color(red[i / 2], green[i / 2], blue[i / 2], alpha[i / 2]));
			line(x, yp, x, yp + h);
		}
	}

	public void myDraw() {
		stroke(127);
		strokeWeight(2);
		line(cutX, 0, cutX, cHeight);

		drawHisto(xposA, yposA, 532, 276);
		drawSelection(xposA, yposA + 10, 512, 256);
		drawColorBar(xposA + 10, yposA + 300, 512, 30);
	}

	void update1(int c) {
		myColorChannel = c;
		resetPrevSel = true;
	}

	void update3(int xx, int y) {
		int x = (xx + offset) % 256;
		if (resetPrevSel) {
			prevSelectedX = x;
			prevSelectedY = y;
			resetPrevSel = false;
		}
		else {

		float rgbf[] = { 0, 0, 0 };
		float labf[] = { 0, 0, 0 };
			float t = ((float) y - (float) prevSelectedY)
			        / ((float) x - (float) prevSelectedX);
			int stx, sty, edx, edy;
			if (x < prevSelectedX) {
				stx = x;
				edx = prevSelectedX;
				sty = y;
				edy = prevSelectedY;
			} else {
				edx = x;
				stx = prevSelectedX;
				edy = y;
				sty = prevSelectedY;
			}

			for (int i = stx; i <= edx; ++i) {
				LabA[myColorChannel][i] = sty + (int) (t * (i - stx));
				labf[0] = (float) LabA[0][i];
				labf[1] = (float) LabA[1][i];
				labf[2] = (float) LabA[2][i];
				alpha[i] = (int) LabA[3][i];
				rgbf = cvter.toRGB(labf);
				red[i] = (int) (rgbf[0] * 255);
				green[i] = (int) (rgbf[1] * 255);
				blue[i] = (int) (rgbf[2] * 255);
			}
		}
		prevSelectedX = x;
		prevSelectedY = y;
		updateTransferFunction();
	}

	public void controlEvent(ControlEvent theEvent) {
		if (theEvent.isGroup()) {
			switch (theEvent.getId()) {
			case (1):
				update1((int) theEvent.getGroup().getValue());
				break;
			}
		}

		if (theEvent.isController()) {
			switch (theEvent.getId()) {
			case (3):
				update3((int) (theEvent.getController().getArrayValue(0)),
				        (int) (theEvent.getController().getArrayValue(1)));
				break;
			case (4):
				offset = (int) theEvent.getController().getValue();
				syncColor();
				break;
			}
		}
	}

	//
	//
	// CPSC8810 - END - no need to edit below
	//
	//

	// variables for the control panel
	ColorPicker tfCPick1;
	ColorPicker tfCPick2;
	RadioButton tfMode;
	ControlP5 cp5;
	PApplet parent;
	VolumeRenderer volren;

	// set initial transfer function data (all values to transparent white)
	public void initializeTransferFunction() {
		red = new int[256];
		green = new int[256];
		blue = new int[256];
		alpha = new int[256];
		for (int i = 0; i < 256; i++) {
			red[i] = 0;
			green[i] = 255;
			blue[i] = 0;
			alpha[i] = 255;
		}

		// pass in our initial custom transfer function
		updateTransferFunction();
	}

	// update custom transfer function data & pass to volume renderer
	public void updateTransferFunction() {
		volren.customRed = red;
		volren.customGreen = green;
		volren.customBlue = blue;
		volren.customAlpha = alpha;
		parent.redraw();
	}

	// create control panel interface
	public void createControlPanel() {

		// setup P5 library
		cp5 = new ControlP5(this);
		frameRate(30);

		// variables for creating control panel
		int y = -10, height = 15, spacing = 20;

		// dataset selector
		float status3[] = { 0, 0, 0, 1, 0, 0 };
		cp5.addTextlabel("label5", "Data Set")
		   .setPosition(10, y += spacing)
		   .setHeight(48)
		   .setColor(parent.color(17, 17, 17));
		cp5.addRadio("dataset", 10, y += spacing)
		   .setSize(20, height)
		   .setColorForeground(color(180, 180, 180))
		   .setColorBackground(color(180, 180, 180))
		   .setItemsPerRow(3)
		   .setSpacingColumn(50)
		   .addItem("aneurism", 0)
		   .addItem("bonsai", 1)
		   .addItem("bucky", 2)
		   .addItem("foot", 3)
		   .addItem("fuel", 4)
		   .addItem("skull", 5)
		   .setColorLabel(parent.color(75, 75, 75))
		   .setArrayValue(status3)
		   .plugTo(this, "setData");

		// lighting settings
		y += spacing + height;
		cp5.addTextlabel("label1", "Light Settings")
		   .setPosition(10, y += spacing)
		   .setColor(parent.color(17, 17, 17));
		cp5.addToggle("enabled")
		   .setPosition(190, y)
		   .setSize(20, height - 5)
		   .setColorForeground(color(180, 180, 180))
		   .setColorBackground(color(180, 180, 180))
		   .setColorLabel(parent.color(75, 75, 75))
		   .setValue(volren.lightEnabled)
		   .plugTo(volren, "lightEnabled")
		   .getCaptionLabel()
		   .setPaddingX(5)
		   .align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER);
		cp5.addSlider("ambient")
		   .setPosition(10, y += spacing)
		   .setSize(200, height)
		   .setColorBackground(color(180, 180, 180))
		   .setColorLabel(parent.color(75, 75, 75))
		   .setRange(0, 1)
		   .setValue(volren.lightAmbient)
		   .plugTo(volren, "lightAmbient");
		cp5.addSlider("diffuse")
		   .setPosition(10, y += spacing)
		   .setSize(200, height)
		   .setColorBackground(color(180, 180, 180))
		   .setColorLabel(parent.color(75, 75, 75))
		   .setRange(0, 1)
		   .setValue(volren.lightDiffuse)
		   .plugTo(volren, "lightDiffuse");
		cp5.addSlider("specular")
		   .setPosition(10, y += spacing)
		   .setSize(200, height)
		   .setColorBackground(color(180, 180, 180))
		   .setColorLabel(parent.color(75, 75, 75))
		   .setRange(0, 1)
		   .setValue(volren.lightSpecular)
		   .plugTo(volren, "lightSpecular");
		cp5.addSlider("exponent")
		   .setPosition(10, y += spacing)
		   .setSize(200, height)
		   .setColorBackground(color(180, 180, 180))
		   .setColorLabel(parent.color(75, 75, 75))
		   .setRange(1, 50)
		   .setValue(volren.lightExponent)
		   .plugTo(volren, "lightExponent");

		// sampling settings (removed)
		y += 10;
		cp5.addTextlabel("label3", "Sampling Settings")
		   .setPosition(10, y += spacing)
		   .setColor(parent.color(255, 200, 0));
		cp5.addSlider("Step", 0.001f, 0.01f, 10, y += spacing, 200, height)
		   .setDecimalPrecision(5)
		   .setValue(0.005f)
		   .plugTo(volren, "sampleStep");

		// composite settings (removed)
		// y += 10;
		// float[] status1 = { 1, 0 };
		// cp5.addTextlabel( "label4", "Compositing" )
		// .setPosition( 10, y+=spacing )
		// .setColor( parent.color(255,200,0) );
		// cp5.addRadio( "compositeMode", 10, y+=spacing )
		// .setSize( 30, height )
		// .setItemsPerRow(3)
		// .setSpacingColumn( 40 )
		// .addItem( "LEVOY", 0 )
		// .addItem( "MIP", 1 )
		// .setArrayValue( status1 )
		// .plugTo( this, "setCompositeMode" );

		// transfer function settings
		y += 10;
		cp5.addTextlabel("label2", "Transfer Function Settings")
		   .setPosition(10, y += spacing)
		   .setColor(parent.color(17, 17, 17));
		cp5.addSlider("Center", 0, 255, 10, y += spacing, 200, height)
		   .setColorBackground(color(180, 180, 180))
		   .setColorLabel(parent.color(75, 75, 75))
		   .setValue(77)
		   .plugTo(volren, "tfCenter");
		// cp5.addSlider("Width", 0, 1, 10, y += spacing, 200, height)
		// .setColorBackground(color(180, 180, 180))
		// .setColorLabel(parent.color(75, 75, 75)).setValue(0.1f)
		// .plugTo(volren, "tfWidth");
		cp5.addSlider("Density", 0, 40, 10, y += spacing, 200, height)
		   .setColorBackground(color(180, 180, 180))
		   .setColorLabel(parent.color(75, 75, 75))
		   .setValue(5.0f)
		   .plugTo(volren, "tfDensity");

		// transfer function mode
		y += 10;
		float[] status2 = { 1, 0 };
		cp5.addRadio("tfMode", 10, y += spacing)
		   .setSize(20, height)
		   .setItemsPerRow(3)
		   .setSpacingColumn(40)
		   .addItem("STEP", 0)
		   .addItem("RECT", 1)
		   // .addItem("HAT", 2)
		   .addItem("BUMP", 3)
		   .addItem("CUSTOM TRANSFER FUNCTION", 4)
		   // .addItem( "CUSTOM2", 5 )
		   // .addItem( "CUSTOM3", 6 )
		   // .addItem( "CUSTOM4", 7 )
		   .setColorBackground(color(180, 180, 180))
		   .setColorForeground(color(120, 120, 120))
		   .setColorLabel(parent.color(75, 75, 75))
		   .setArrayValue(status2)
		   .plugTo(this, "setTFMode");

		// transfer function color picker
		y += 15 + spacing;
		cp5.addColorPicker("tfColor1", 10, y += height, 200, 10)
		   .setColorLabel(parent.color(75, 75, 75))
		   .setColorValue(volren.tfColor1)
		   .plugTo(this, "setTFColor1");
		// cp5.addColorPicker( "tfColor2", 10, y+=70, 200, 10 )
		// .setColorValue( volren.tfColor2 )
		// .plugTo( this, "setTFColor2" );

		// update control panel
		cp5.addCallback(new RedrawListener(parent));
	}

	// set transfer function mode
	public void setTFMode(int c) {
		volren.tfMode = c;
		parent.redraw();
	}

	// set transfer function color
	public void setTFColor1(int c) {
		volren.tfColor1 = c;
		parent.redraw();
	}

	// select our dataset
	public void setData(int c) {

		// set data to load
		if (c == 0)
			volren.dataName = "aneurism";
		else if (c == 1)
			volren.dataName = "bonsai";
		else if (c == 2)
			volren.dataName = "bucky";
		else if (c == 3)
			volren.dataName = "foot";
		else if (c == 4)
			volren.dataName = "fuel";
		else if (c == 5)
			volren.dataName = "skull";

		// load data
		volren.data = parent.loadBytes(volren.dataName + ".raw");
		loadData();

		// update volume renderer
		parent.redraw();
	}

	// grab our controlp5 object
	public ControlP5 control() {
		return cp5;
	}

	// creates our window
	Controls(PApplet parent, VolumeRenderer volren) {
		this.parent = parent;
		this.volren = volren;

		Frame f = new Frame("Controls");
		f.add(this);

		init();

		f.setTitle("Control Panel");
		f.setSize(cWidth, cHeight);
		f.setLocation(100, 100);
		f.setResizable(false);
		f.setVisible(true);
	}

	// only update screen when necessary
	class RedrawListener implements CallbackListener {
		PApplet target;

		RedrawListener(PApplet target) {
			this.target = target;
		}

		public void controlEvent(CallbackEvent event) {
			if (event.getAction() == ControlP5.ACTION_BROADCAST)
				target.redraw();
		}
	}

	// set composite mode (removed)
	// public void setCompositeMode(int c){
	// volren.compositeMode = c;
	// parent.redraw();
	// }

	// set second transfer function color (removed)
	// public void setTFColor2(int c){
	// volren.tfColor2 = c;
	// parent.redraw();
	// }
}
