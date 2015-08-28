package test;

import processing.core.*;

import controlP5.*;

public class sketch extends PApplet {
	private static final long serialVersionUID = 1L;

	int bgrdcolor = 0;
	// CP5 vars
	ControlP5 cp5;

	Chart myChart;
	Canvas myCanvas;
	RadioButton rbts;
	TransferFuncGui tfg;

	int[] ys;
	int yr;

	public void setup() {
		size(512, 300, P2D);
		smooth();
		cp5 = new ControlP5(this);
		ControlP5.printPublicMethodsFor(Chart.class);
		myChart = cp5.addChart("hello")
		             .setPosition(50, 50)
		             .setSize(200, 200)
		             .setRange(-20, 100)
		             .setView(Chart.BAR);

		myChart.getColor().setBackground(color(255, 25));

		myChart.addDataSet("world");
		myChart.setColors("world", color(255, 0, 255), color(255, 0, 0));
		myChart.setData("world", new float[20]);

		myChart.addDataSet("earth");
		myChart.setColors("earth", color(255), color(0, 255, 0));
		myChart.setData("earth", new float[4]);

		rbts = cp5.addRadioButton("radioButton")
		          .setPosition(20, 160)
		          .setSize(40, 20)
		          .setColorForeground(color(120))
		          .setColorActive(color(255))
		          .setColorLabel(color(255))
		          .setItemsPerRow(5)
		          .setSpacingColumn(30)
		          .addItem("50", 1)
		          .addItem("100", 2)
		          .addItem("150", 3)
		          .addItem("200", 4)
		          .addItem("250", 5);

		for (Toggle t : rbts.getItems()) {
			t.captionLabel().setColorBackground(color(255, 80));
			t.captionLabel().style().moveMargin(-7, 0, 0, -3);
			t.captionLabel().style().movePadding(7, 0, 0, 3);
			t.captionLabel().style().backgroundWidth = 45;
			t.captionLabel().style().backgroundHeight = 13;
		}
		// myCanvas = new

		ys = new int[256];
	}

	public void draw() {
		background(bgrdcolor);
		// unshift: add data from left to right (first in)
		myChart.unshift("world", (sin(frameCount * 0.05f) * 10));

		// push: add data from right to left (last in)
		myChart.push("earth", (sin(frameCount * 0.1f) * 10));
		stroke(244);
		noFill();
		if (mouseX >= 50 && mouseX <= 305) {
			if (dmouseY > 0) {
				yr = mouseY;
				ys[mouseX - 50] = yr;
			}
		}
		beginShape();
		int x = 50;
		for (int a : ys)
			vertex((int) x++, (int) a);
		endShape(LINES);
	}

	public void mouseDragged() {

		// int X0, X1, Y0, Y1;
		// if (mouseX >= pmouseX) {
		// X0 = pmouseX;
		// Y0 = pmouseY;
		//
		// }
		// else {
		// int X0 = mouseX > pmouseX ? pmouseX : mouseX;
		// int X1 = mouseX <= pmouseX ? pmouseX : mouseX;
		// float t =
		// ((float)mouseY-(float)pmouseY)/((float)mouseX-(float)pmouseX);
		// for (int x = X0; x <= X1; ++x) {
		// ys[x-50] = (int)(x*t);
		// }
		// }
	}

	public void keyPressed() {
		switch (key) {
		case ('0'):
			rbts.deactivateAll();
			break;
		case ('1'):
			rbts.activate(0);
			break;
		case ('2'):
			rbts.activate(1);
			break;
		case ('3'):
			rbts.activate(2);
			break;
		case ('4'):
			rbts.activate(3);
			break;
		case ('5'):
			rbts.activate(4);
			break;
		}
	}

	public void controlEvent(ControlEvent theEvent) {
		if (theEvent.isFrom(rbts)) {
			print("got an event from " + theEvent.getName() + "\t");
			for (int i = 0; i < theEvent.getGroup().getArrayValue().length; i++) {
				print((int) (theEvent.getGroup().getArrayValue()[i]));
			}
			println("\t " + theEvent.getValue());
			bgrdcolor = color((int) (theEvent.group().value() * 50), 0, 0);
		}
	}

	class TransferFuncGui {
		final float Ymin, Ymax;
		final float Xmin, Xmax;

		float YvalMax, YvalMin;
		float XvalMax, XvalMin;

		float currMouseX;
		boolean pressed;

		final int size = 255;
		float[] targetArray;
		int[] target;

		boolean type;

		int[] red;
		int[] green;
		int[] blue;

		TransferFuncGui(float xmi, float xma, float ymi, float yma, float vxmi,
		        float vxma, float vymi, float vyma, float[] t, int[] r,
		        int[] g, int[] b) {
			Xmin = xmi;
			Ymin = ymi;
			Xmax = xma;
			Ymax = yma;
			XvalMin = vxmi;
			XvalMax = vxma;
			YvalMin = vymi;
			YvalMax = vyma;
			targetArray = t;
			red = r;
			green = g;
			blue = b;
			pressed = false;
			type = false;
		}

		TransferFuncGui(float xmi, float xma, float ymi, float yma, float vxmi,
		        float vxma, float vymi, float vyma, int[] t, int[] r, int[] g,
		        int[] b) {
			Xmin = xmi;
			Ymin = ymi;
			Xmax = xma;
			Ymax = yma;
			XvalMin = vxmi;
			XvalMax = vxma;
			YvalMin = vymi;
			YvalMax = vyma;
			target = t;
			red = r;
			green = g;
			blue = b;
			pressed = false;
			type = true;
		}

		void drawLines() {
			beginShape();
			float x, y;
			noFill();
			stroke(100);
			strokeWeight(1.5f);
			smooth(8);
			for (int i = 0; i < size; ++i) {
				x = getX((float) i);
				if (type)
					y = getY(target[i]);
				else
					y = getY(targetArray[i]);
				vertex(x, y);
			}
			endShape();
		}

		float getY(float v) {
			if (v > YvalMax)
				v = YvalMax;
			else if (v < YvalMin)
				v = YvalMin;
			return map(v, YvalMin, YvalMax, Ymax, Ymin);
		}

		float getX(float v) {
			if (v > XvalMax)
				v = XvalMax;
			else if (v < XvalMin)
				v = XvalMin;
			return map(v, XvalMin, XvalMax, Xmin, Xmax);
		}

		float getXval(float v) {
			if (v > Xmax)
				v = Xmax;
			else if (v < Xmin)
				v = Xmin;
			return map(v, Xmin, Xmax, XvalMin, XvalMax);
		}

		float getYval(float v) {
			if (v > Ymax)
				v = Ymax;
			else if (v < Ymin)
				v = Ymin;
			return map(v, Ymin, Ymax, YvalMax, YvalMin);
		}

		public void draw() {
			drawLines();
		}

		public void mousePressed() {
			if (mouseX >= Xmin && mouseX <= Xmax && mouseY >= Ymin
			        && mouseY <= Ymax && mouseButton == LEFT
			        && mouseEvent.getCount() == 1) {
				currMouseX = mouseX;
				pressed = true;
				if (type)
					target[(int) getXval((float) mouseX)] = (int) getYval(mouseY);
				else
					targetArray[(int) getXval((float) mouseX)] = getYval(mouseY);
				// updateColor((int) getXval((float) mouseX));
			}

			// options.mousePressed();
		}

		public void mouseDragged() {
			// if(mouseX >= Xmin && mouseX <= Xmax && mouseY >= Ymin && mouseY
			// <= Ymax && mouseButton == LEFT){
			// currMouseX = mouseX;
			// targetArray[(int)getXval(currMouseX)] = getYval(mouseY);
			// updateColor((int)getXval(currMouseX));
			// // if((int)getXval(currMouseX)+1 <= 255)
			// targetArray[(int)getXval(currMouseX)+1] = getYval(mouseY);
			// // if((int)getXval(currMouseX)-1 >= 0)
			// targetArray[(int)getXval(currMouseX)-1] = getYval(mouseY);
			// }
			if (pressed) {
				currMouseX = mouseX;
				if (type)
					target[(int) getXval((float) mouseX)] = (int) getYval(mouseY);
				else
					targetArray[(int) getXval((float) mouseX)] = getYval(mouseY);
				// updateColor((int) getXval((float) mouseX));
			}
		}

		public void mouseReleased() {
			pressed = false;
		}
	};

}
