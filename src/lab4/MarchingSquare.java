package lab4;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import lab4.IsoCell.side;
import processing.core.PApplet;

public class MarchingSquare {
	private PApplet _parent;
	private LinkedList<LinkedList<myPath>> _isoLines;
	private final double epsilon = 1e-10;
	private float _scaleX = 1;
	private float _scaleY = 1;

	double[][] _dataBound;
	double[] _isos;

	MarchingSquare(PApplet p, float[][] data, double[] isos) {
		if (p == null || data == null) {
			System.out.println("check input pointers.");
			System.exit(1);
		}

		_parent = p;
		_isos = isos;

		double[][] ddata = new double[data[0].length][data.length];
		for (int x = 0; x < ddata.length; ++x)
			for (int y = 0; y < ddata[0].length; ++y)
				ddata[ddata.length - 1 - x][y] = (double) data[y][x];

		boundData(ddata, _isos);
		_isoLines = new LinkedList<LinkedList<myPath>>();
		setScaleX(_dataBound.length);
		setScaleY(_dataBound[0].length);
	}

	MarchingSquare(PApplet p, int[][] data, double[] isos) {
		if (p == null || data == null) {
			System.out.println("check input pointers.");
			System.exit(1);
		}

		_parent = p;
		_isos = isos;

		double[][] ddata = new double[data[0].length][data.length];
		for (int x = 0; x < ddata.length; ++x)
			for (int y = 0; y < ddata[0].length; ++y)
				ddata[ddata.length - 1 - x][y] = (double) data[y][x];

		boundData(ddata, _isos);
		// for (double[] row : _dataBound){
		// for (double d : row)
		// PApplet.print(d, PConstants.TAB);
		// PApplet.println();
		// }
		_isoLines = new LinkedList<LinkedList<myPath>>();
		setScaleX(_dataBound.length);
		setScaleY(_dataBound[0].length);
	}
	public void render(int color) {
		for (LinkedList<myPath> list : _isoLines)
			for (myPath path : list)
				path.render(color);
	}
	public void render(int[] color) {
		if (color.length < _isoLines.size())
			PApplet.println("# iso lines more than colors given");
		for (int i = 0; i < _isoLines.size(); ++i) {
			for (myPath path : _isoLines.get(i))
				path.render(color[i]);
		}
	}

	public void setScaleX(float scale) {
		_scaleX = scale;
	}
	public void setScaleY(float scale) {
		_scaleY = scale;
	}

	public void make() {
		for (int i = 0; i < _isos.length; ++i) {
			IsoCell[][] cellGrid = computeGrid(_dataBound, _isos[i]);
			_isoLines.addLast(computeIsoLines(cellGrid, _dataBound, _isos[i]));
		}
	}

	public void clear() {
		_isoLines.clear();
	}

	public LinkedList<myPath> computeIsoLines(IsoCell[][] cellGrid,
			double[][] data, double iso) {
		int r, c;
		int rows = cellGrid.length;
		int cols = cellGrid[0].length;
		LinkedList<myPath> isoLines = new LinkedList<myPath>();

		for (r = 0; r < rows; ++r)
			for (c = 0; c < cols; ++c)
				interpolateCrossing(cellGrid, _dataBound, r, c, iso);

		for (r = 0; r < rows; ++r)
			for (c = 0; c < cols; ++c) {
				isoLines.addLast(new myPath(_parent));

				if (cellGrid[r][c].getCheckSum() % 5 != 0) {
					isoSubpath(cellGrid, r, c, isoLines.getLast());
				}
			}

		return isoLines;
	}

	private void isoSubpath(IsoCell[][] cellGrid, int r, int c, myPath isoPoints) {

		float s = _scaleX / (float) cellGrid[0].length;
		float sx0 = -s;
		float sx1 = s + _scaleX;
		float sy0 = s + _scaleY;
		float sy1 = -s;

		// to determine the ambiguous situation
		side prevSide = side.NONE;
		IsoCell start = cellGrid[r][c];
		Point2D pt = start.normalizedPointCCW(start.firstSideCCW(prevSide));
		double x = c + pt.getX();
		double y = r + pt.getY();
		isoPoints.addPoint(
				PApplet.map((float) x, 0, cellGrid[0].length, sx0, sx1),
				PApplet.map((float) y, 0, cellGrid.length, sy0, sy1));
		pt = start.normalizedPointCCW(start.secondSideCCW(prevSide));
		double xPrev = c + pt.getX();
		double yPrev = r + pt.getY();

		if (Math.abs(x - xPrev) > epsilon && Math.abs(y - yPrev) > epsilon) {
			isoPoints.addPoint(
					PApplet.map((float) x, 0, cellGrid[0].length, sx0, sx1),
					PApplet.map((float) y, 0, cellGrid.length, sy0, sy1));
		}

		prevSide = start.nextCellCCW(prevSide);

		switch (prevSide) {
			case BOTTOM :
				r -= 1;
				break;
			case LEFT :
				c -= 1;
				break;
			case RIGHT :
				c += 1;
				break;
			case TOP :
				r += 1;
				break;
			default :
				break;
		}

		start.clearIso(prevSide); // Erase this isoline.

		IsoCell curCell = cellGrid[r][c];
		while (curCell != start) {
			pt = curCell.normalizedPointCCW(curCell.secondSideCCW(prevSide));
			x = c + pt.getX();
			y = r + pt.getY();
			if (Math.abs(x - xPrev) > epsilon && Math.abs(y - yPrev) > epsilon) {
				isoPoints
						.addPoint(PApplet.map((float) x, 0, cellGrid[0].length,
								sx0, sx1), PApplet.map((float) y, 0,
								cellGrid.length, sy0, sy1));
			}
			xPrev = x;
			yPrev = y;
			prevSide = curCell.nextCellCCW(prevSide);
			switch (prevSide) {
				case BOTTOM :
					r -= 1;
					break;
				case LEFT :
					c -= 1;
					break;
				case RIGHT :
					c += 1;
					break;
				case TOP :
					r += 1;
				default :
					break;
			}
			curCell.clearIso(prevSide); // Erase this isoline.
			curCell = cellGrid[r][c];
		}
	}

	private void interpolateCrossing(IsoCell[][] cellGrid, double[][] data,
			int r, int c, double threshold) {

		double a, b;

		IsoCell cell = cellGrid[r][c];
		double ll = data[r][c];
		double lr = data[r][c + 1];
		double ul = data[r + 1][c];
		double ur = data[r + 1][c + 1];

		// Left side of iso cell.
		switch (cell.getCheckSum()) {
			case 1 :
			case 3 :
			case 5 :
			case 7 :
			case 8 :
			case 10 :
			case 12 :
			case 14 :
				a = ll;
				b = ul;
				cell.setLeft((threshold - a) / (b - a)); // frac from LL
				break;
			default :
				break;
		}

		// Bottom side of iso cell.
		switch (cell.getCheckSum()) {
			case 1 :
			case 2 :
			case 5 :
			case 6 :
			case 9 :
			case 10 :
			case 13 :
			case 14 :
				a = ll;
				b = lr;
				cell.setBottom((threshold - a) / (b - a)); // frac from LL
				break;
			default :
				break;
		}

		// Top side of iso cell.
		switch (cell.getCheckSum()) {
			case 4 :
			case 5 :
			case 6 :
			case 7 :
			case 8 :
			case 9 :
			case 10 :
			case 11 :
				a = ul;
				b = ur;
				cell.setTop((threshold - a) / (b - a)); // frac from UL
				break;
			default :
				break;
		}

		// Right side of iso cell.
		switch (cell.getCheckSum()) {
			case 2 :
			case 3 :
			case 4 :
			case 5 :
			case 10 :
			case 11 :
			case 12 :
			case 13 :
				a = lr;
				b = ur;
				cell.setRight((threshold - a) / (b - a)); // frac from LR
				break;
			default :
				break;
		}
	}

	private IsoCell[][] computeGrid(double[][] data, double iso) {
		int cols = data.length;
		int rows = data[0].length;
		IsoCell[][] cellGrid = new IsoCell[cols - 1][rows - 1];

		// iterate through the bound data from lower-left corner
		// and assign values according to significance of bits
		int c, r;
		for (c = 0; c < cols - 1; ++c)
			for (r = 0; r < rows - 1; ++r)
				cellGrid[c][r] = computeCell(data, c, r, iso);

		return cellGrid;
	}

	private IsoCell computeCell(double[][] data, int c, int r, double iso) {
		IsoCell cell = new IsoCell();

		int ll = data[c][r] > iso ? 0 : 1;
		int lr = data[c][r + 1] > iso ? 0 : 2;
		int ur = data[c + 1][r + 1] > iso ? 0 : 4;
		int ul = data[c + 1][r] > iso ? 0 : 8;

		// 0x00001000 || 0x00000100 || 0x00000010 || 0x00000001
		int nInfo = ur | ul | ll | lr;

		cell.setFlipped(false);
		if (nInfo == 5 || nInfo == 10) {
			double center = (data[c][r] + data[c][r + 1] + data[c + 1][r + 1] + data[c + 1][r]) / 4;
			if (nInfo == 5 && center > iso)
				cell.setFlipped(true);
			else if (nInfo == 10 && center < iso)
				cell.setFlipped(true);
		}

		cell.setCheckSum(nInfo);

		return cell;
	}

	// bound original matrix with a small value so that marching squares will
	// never go off the data
	private double[][] boundData(double[][] dataOrigin, double[] _isoValues) {
		int cols = dataOrigin.length;
		int rows = dataOrigin[0].length;

		double min = Double.MAX_VALUE;
		for (Double d : _isoValues)
			min = Math.min(d.doubleValue(), min);
		min -= min * 0.5;

		_dataBound = new double[cols + 2][rows + 2];
		int r, c;
		for (c = 1; c < cols + 1; ++c)
			for (r = 1; r < rows + 1; ++r)
				_dataBound[c][r] = dataOrigin[c - 1][r - 1];

		for (r = 0; r < rows + 2; ++r) {
			_dataBound[0][r] = min;
			_dataBound[cols + 1][r] = min;
		}
		for (c = 0; c < cols + 2; ++c) {
			_dataBound[c][0] = min;
			_dataBound[c][rows + 1] = min;
		}

		return _dataBound;
	}
}
