package lab4;

import java.util.LinkedList;

import processing.core.PApplet;
import processing.core.PConstants;

class myPoint2d {
	float x, y;

	public myPoint2d(float x1, float y1) {
		x = x1;
		y = y1;
	}

	public myPoint2d(double x1, double y1) {
		x = (float) x1;
		y = (float) y1;
	}
}

class myPath {
	LinkedList<myPoint2d> points;
	PApplet _parent;

	public myPath(PApplet p) {
		_parent = p;
		points = new LinkedList<myPoint2d>();
	}

	public void addPoint(float x, float y) {
		points.addLast(new myPoint2d(x, y));
	}

	public void addPoint(double x, double y) {
		points.addLast(new myPoint2d(x, y));
	}

	public void render(int color) {
		_parent.stroke(color);
		_parent.noFill();
		this._parent.beginShape();
		for (myPoint2d p : points)
			this._parent.vertex(p.x, p.y);
		this._parent.endShape(PConstants.CLOSE);
	}
}

class COLORS {
	private PApplet _parent;
	final public int BLACK;
	final public int DARKGREY;
	final public int MIDGREY;
	final public int LIGHTGREY;
	final public int WHITE;
	final public int bupu1;
	final public int bupu2;
	final public int bupu3;
	final public int bupu4;
	final public int bupu5;
	final public int rdbu1;
	final public int rdbu2;

	public COLORS(PApplet p) {
		_parent = p;

		_parent.colorMode(PConstants.RGB);
		BLACK = _parent.color(0);
		DARKGREY = _parent.color(64);
		MIDGREY = _parent.color(192);
		LIGHTGREY = _parent.color(244);
		WHITE = _parent.color(255);
		bupu1 = _parent.color(237,248,251);
		bupu2 = _parent.color(179,205,227);
		bupu3 = _parent.color(140,150,198);
		bupu4 = _parent.color(136,86,167);
		bupu5 = _parent.color(129,15,124);
		rdbu1 = _parent.color(214, 96, 77);
		rdbu2 = _parent.color(67, 147, 195);
	}
}

class CubicInterpolator
{
	public static double getValue (double[] p, double x) {
		return p[1] + 0.5 * x*(p[2] - p[0] + x*(2.0*p[0] - 5.0*p[1] + 4.0*p[2] - p[3] + x*(3.0*(p[1] - p[2]) + p[3] - p[0])));
	}
}

class BicubicInterpolator extends CubicInterpolator
{
	private double[] arr = new double[4];

	public double getValue (double[][] p, double x, double y) {
		arr[0] = getValue(p[0], y);
		arr[1] = getValue(p[1], y);
		arr[2] = getValue(p[2], y);
		arr[3] = getValue(p[3], y);
		return getValue(arr, x);
	}
}

class TricubicInterpolator extends BicubicInterpolator
{
	private double[] arr = new double[4];

	public double getValue (double[][][] p, double x, double y, double z) {
		arr[0] = getValue(p[0], y, z);
		arr[1] = getValue(p[1], y, z);
		arr[2] = getValue(p[2], y, z);
		arr[3] = getValue(p[3], y, z);
		return getValue(arr, x);
	}
}

