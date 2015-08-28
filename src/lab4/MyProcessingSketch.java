package lab4;

import java.util.LinkedList;
import java.util.List;

import processing.core.*;

public class MyProcessingSketch extends PApplet {
	private static final long serialVersionUID = 1L;

	// window
	int image_width = 800;
	int image_height = 800;
	// colors
	COLORS colors;
	int colorMin, colorMax;
	int[] linesColors;
	// font
	List<PFont> fontList;
	// data
	NRRDLoader loader;
	String _fileName = "../brain.nrrd";
	// images
	PImage[] images;
	int selectedImage;
	// iso
	MarchingSquare msRaw, msLinear, msCubic, msCurrent;
	double isoValues[];
	boolean drawIsos = false;
	// widgets
	ButtonSets interpolations;
	boolean bilinear;
	ButtonSets dataUsing;
	boolean raw;
	ColorPicker[] cp;
	ButtonSets isos;
	int selectedIso;

	public void setup() {
		colors = new COLORS(this);
		colorMin = colors.BLACK;
		colorMax = colors.WHITE;
		fontInit();
		dataInit();
		imgInit();
		widInit(image_width, 0);
		msInit();
		size(image_width + 340, image_height, JAVA2D);
	}

	public void draw() {
		noStroke();
		fill(colors.LIGHTGREY);
		rect(0, 0, width, height);
		// draw image
		image(images[selectedImage], 0, 0);
		// image frame
		noFill();
		strokeWeight(1.5f);
		stroke(colors.MIDGREY);
		/*****************************************************/
		if (drawIsos)
			msCurrent.render(linesColors);
		/*****************************************************/
		fill(colors.bupu1);
		rect(image_width, 0, 340, 380);
		drawPickers(image_width+20);
		fill(colors.DARKGREY);
		textAlign(LEFT, TOP);
		textFont(fontList.get(1));
		textSize(16);
		text("Click the Legends to Change Colors.", image_width + 15, 355);
		/*****************************************************/
		fill(colors.bupu2);
		rect(image_width, 380, 340, 80);
		dataUsing.render();
		fill(colors.DARKGREY);
		textAlign(LEFT, TOP);
		textFont(fontList.get(1));
		textSize(16);
		text("Data Used to Generate Isocontours.", image_width + 15, 435);
		/*****************************************************/
		fill(colors.bupu3);
		rect(image_width, 460, 340, 80);
		interpolations.render();
		fill(colors.DARKGREY);
		textAlign(LEFT, TOP);
		textFont(fontList.get(1));
		textSize(16);
		text("Image Interpolation Method.", image_width + 15, 515);
		/*****************************************************/
		fill(colors.bupu4);
		rect(image_width, 540, 340, 260);
		fill(colors.bupu2);
		textAlign(LEFT, TOP);
		textFont(fontList.get(1));
		int inc =image_width + 15;
		textSize(16);
		text("c : toggle iso contours", inc, 550);
		textSize(18);
		text("Min Value: ", inc, 570);
		inc += 110;
		text(loader.get_minValue(), inc, 570);
		inc += 50;
		text("Min Value: ", inc, 570);
		inc += 110;
		text(loader.get_maxValue(), inc, 570);
		inc = image_width + 15; 
		textSize(16);
		text("-/= : decrease/increase selected iso value ", inc, 740);
		text("use legend to change the color of selected", inc, 760);
		text("isoline ", inc, 780);
		isos.render();
		stroke(colors.LIGHTGREY);
		fill(linesColors[0]);
		rect(inc+60, 605, 60, 30);
		fill(linesColors[1]);
		rect(inc+60, 655, 60, 30);
		fill(linesColors[2]);
		rect(inc+60, 705, 60, 30);
		fill(colors.LIGHTGREY);
		textAlign(LEFT, CENTER);
		text(String.format("%.2f", isoValues[0]), inc+130, 615);
		text(String.format("%.2f", isoValues[1]), inc+130, 665);
		text(String.format("%.2f", isoValues[2]), inc+130, 715);
		
	}

	public void mouseClicked() {
		for (ColorPicker c : cp)
			c.mouseClicked();
		interpolations.mouseClicked();
		selectedImage = interpolations.selectedIndex();
		println(selectedImage);
		dataUsing.mouseClicked();
		switch (dataUsing.selectedIndex()) {
			case 0 :
				msCurrent = msRaw;
				break;
			case 1:
				msCurrent = msLinear;
				break;
			case 2:
				msCurrent = msCubic;
				break;
			default :
				break;
		}
		isos.mouseClicked();
		selectedIso = isos.selectedIndex();
		if (drawIsos) {
			msCurrent.clear();
			msCurrent.make();
		}
	}
	
	public void mousePressed() {
		for (ColorPicker c : cp)
			c.mousePressed();
	}
	
	public void mouseReleased() {
		for (ColorPicker c : cp)
			c.mouseReleased();
	}
	
	public void mouseDragged() {
		for (ColorPicker c : cp)
			c.mouseDragged();
		updateColors();
	}
	
	private void updateColors(){
		colorMin = cp[0].getActiveColor();
		colorMax = cp[1].getActiveColor();
		if (drawIsos)
			linesColors[selectedIso] = cp[2].getActiveColor();
	}
	
	public void keyPressed() {
		switch (key) {
			case '-' :
				isoValues[selectedIso] -= 0.01*loader.get_maxValue();
				break;
			case '=' :
				isoValues[selectedIso] += 0.01*loader.get_maxValue();
				break;
			case 'c' :
				drawIsos = !drawIsos;
				break;
			default :
				break;
		}
		
		if (drawIsos) {
			msCurrent.clear();
			msCurrent.make();
		}
	}
	
	private void drawPickers(int x) {
		cp[0].draw();
		cp[1].draw();
		cp[2].draw();
		textSize(18);
		fill(colors.DARKGREY);
		int xx = image_width + 50;
		textAlign(LEFT, TOP);
		text("Min Valur Color", xx, 270);
		text("Max Valur Color", xx, 300);
		text("Iso Lines Color", xx, 330);

		reColorImage();
	}

	void reColorImage() {
		if (selectedImage == 0)
			images[selectedImage] = loader.bufferImage(loader.get_dataLinearNormalized(),
					colorMin, colorMax);
		else 
			images[selectedImage] = loader.bufferImage(loader.get_dataCubicNormalized(),
					colorMin, colorMax);
	}

	void reColorLines() {
		for (int i = 0; i < linesColors.length; ++i) {
			double norm = (isoValues[i] / loader.get_maxValue())*0.7+0.3;
			linesColors[i] = color(
					hue(cp[2].getActiveColor()),
					(int)(saturation(cp[2].getActiveColor()) * norm),
					(int)(brightness(cp[2].getActiveColor()) * norm) 
					);
		}
	}
	
	private void fontInit() {
		fontList = new LinkedList<PFont>();
		fontList.add(createFont("Ubuntu", 14));
		fontList.add(createFont("Ubuntu Bold", 14));
		fontList.add(createFont("Ubuntu Mono Bold", 14));
	}
	private void dataInit() {
		loader = new NRRDLoader(this, _fileName);
	}

	private void msInit() {
		isoValues = new double[]{0.3*loader.get_maxValue(), 
				0.5*loader.get_maxValue(),
				0.8*loader.get_maxValue()};
		linesColors = new int[isoValues.length];
		msRaw = new MarchingSquare(this, loader.get_dataRaw(), isoValues);
		msRaw.setScaleX((float)image_width);
		msRaw.setScaleY((float)image_height);
		msRaw.make();
		msLinear = new MarchingSquare(this, loader.get_dataLinear(), isoValues);
		msLinear.setScaleX((float)image_width);
		msLinear.setScaleY((float)image_height);
		msLinear.make();
		msCubic = new MarchingSquare(this, loader.get_dataCubic(), isoValues);
		msCubic.setScaleX((float)image_width);
		msCubic.setScaleY((float)image_height);
		msCubic.make();
		msCurrent = msRaw;
	}

	private void widInit(int x, int y) {
		cp = new ColorPicker[3];
		cp[0] = new ColorPicker(this);
		cp[0].setSelectorPos(x + 15, y + 270);
		cp[0].setPickerX(x + 10, y + 10);
		cp[0].setActiveColor(color(0x00));
		cp[1] = new ColorPicker(this);
		cp[1].setSelectorPos(x + 15, y + 300);
		cp[1].setPickerX(x + 10, y + 10);
		cp[1].setActiveColor(color(0xff));
		cp[2] = new ColorPicker(this);
		cp[2].setSelectorPos(x + 15, y + 330);
		cp[2].setPickerX(x + 10, y + 10);
		cp[2].setActiveColor(color(0xff, 0xff, 0));
		
		dataUsing = new ButtonSets(this);
		dataUsing.addButton(x+15, y+390, 60, 40, "Raw");
		dataUsing.get_buttonList().getLast()._textSize = 18;
		dataUsing.get_buttonList().getLast()._labelFont = fontList.get(2);
		dataUsing.addButton(x+85, y+390, 90, 40, "BiLinear");
		dataUsing.get_buttonList().getLast()._textSize = 18;
		dataUsing.get_buttonList().getLast()._labelFont = fontList.get(2);
		dataUsing.addButton(x+185, y+390, 80, 40, "BiCubic");
		dataUsing.get_buttonList().getLast()._textSize = 18;
		dataUsing.get_buttonList().getLast()._labelFont = fontList.get(2);

		interpolations = new ButtonSets(this);
		interpolations.addButton(x+15, y+470, 120, 40, "BiLinear");
		interpolations.get_buttonList().getLast()._textSize = 20;
		interpolations.get_buttonList().getLast()._labelFont = fontList.get(2);
		interpolations.addButton(x+140, y+470, 120, 40, "BiCubic");
		interpolations.get_buttonList().getLast()._textSize = 20;
		interpolations.get_buttonList().getLast()._labelFont = fontList.get(2);

		isos = new ButtonSets(this);
		isos.addButton(x+15, y+600, 40, 40, "#1");
		isos.get_buttonList().getLast()._textSize = 16;
		isos.get_buttonList().getLast()._labelFont = fontList.get(2);
		isos.addButton(x+15, y+650, 40, 40, "#2");
		isos.get_buttonList().getLast()._textSize = 16;
		isos.get_buttonList().getLast()._labelFont = fontList.get(2);
		isos.addButton(x+15, y+700, 40, 40, "#3");
		isos.get_buttonList().getLast()._textSize = 16;
		isos.get_buttonList().getLast()._labelFont = fontList.get(2);
		selectedIso = 0;
	}

	private void imgInit() {
		image_width = ceil((float) image_height * (float) loader.dataWidth()
				/ (float) loader.dataHeight());
		loader.linearScale(loader.get_dataRaw(), image_width, image_height);
		images = new PImage[2];
		loader.set_dataLinearNormalized(
				loader.normalize(null, loader.get_dataLinear(), 0, loader.get_inputMAX()));
		images[0] = loader.bufferImage(loader.get_dataLinearNormalized(),
				colors.BLACK, colors.WHITE);
		selectedImage = 0;
		loader.cubicScale(loader.get_dataRaw(), image_width, image_height);
		loader.set_dataCubicNormalized(
				loader.normalize(null, loader.get_dataCubic(), 0, loader.get_inputMAX()));
		images[1] = loader.bufferImage(loader.get_dataCubicNormalized(),
				colors.BLACK, colors.WHITE);
	}
}