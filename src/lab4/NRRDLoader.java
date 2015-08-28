package lab4;

import java.io.BufferedReader;
import java.io.IOException;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

public class NRRDLoader {
	private PApplet _parent;
	private int[] _dim;
	private int _maxValue;
	private int _minValue;
	private int _inputMAX;
	public int get_inputMAX() {
		return _inputMAX;
	}

	public void set_inputMAX(int _inputMAX) {
		this._inputMAX = _inputMAX;
	}

	private int[][] _dataRaw;
	
	public int get_maxValue() {
		return _maxValue;
	}
	
	public void set_maxValue(int _maxValue) {
		this._maxValue = _maxValue;
	}
	
	public int get_minValue() {
		return _minValue;
	}
	
	public void set_minValue(int _minValue) {
		this._minValue = _minValue;
	}
	public int[][] get_dataRaw() {
		return _dataRaw;
	}

	public void set_dataRaw(int[][] _dataRaw) {
		this._dataRaw = _dataRaw;
	}

	private float[][] _dataLinear = null;
	private float[][] _dataLinearNormalized = null;
	private float[][] _dataCubic = null;
	private float[][] _dataCubicNormalized = null;

	enum MODE {
		SELF, MAP
	}

	public NRRDLoader(PApplet p, String f) {
		_parent = p;

		load(f);
	}

	public void load(String f) {
		try {
			dataInit(f, 7);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int dataWidth() {
		return _dim[0];
	}

	public int dataHeight() {
		return _dim[1];
	}

	public float[][] normalize(float[][] dest, float[][] src, int min, int max) {
		if (src == null)
			System.exit(0);
		if (dest == null)
			dest = new float[src.length][src[0].length];

		for (int y = 0; y < dest[0].length; ++y) {
			for (int x = 0; x < dest.length; ++x) {
				dest[x][y] = PApplet.map(src[x][y], min, max, 0, 1.0f);
			}
		}
		

		return dest;
	}

	private void dataInit(String f, int n) throws IOException {
		_maxValue = Integer.MIN_VALUE;
		_minValue = Integer.MAX_VALUE;
		BufferedReader _reader = _parent.createReader(f);
		String line = "";
		if (readHeader(_reader, n)) {
			_reader.reset();
			// column major read in data
			for (int y = 0; y < _dim[1]; ++y) {
				for (int x = 0; x < _dim[0]; ++x) {
					line = _reader.readLine();
					_dataRaw[x][y] = Integer.parseInt(line);
					_maxValue = _maxValue < _dataRaw[x][y]
							? _dataRaw[x][y]
							: _maxValue;
					_minValue = _minValue > _dataRaw[x][y]
							? _dataRaw[x][y]
							: _minValue;
				}
			}
		}
		if (_maxValue > 0xff) set_inputMAX(0xffff);
		else set_inputMAX(0xff);
	}

	private boolean readHeader(BufferedReader f, int n) throws IOException {
		String line = "";
		for (int i = 0; i < n - 1; ++i) {
			if (line != null) {
				line = f.readLine();
				if (line != null && line.contains("dimension:")) {
					String[] pieces = PApplet.split(line, ' ');
					if (2 != Integer.parseInt(pieces[1]))
						return false;
					_dim = new int[2];
				}
				if (line != null && line.contains("sizes:")) {
					String[] pieces = PApplet.split(line, ' ');
					_dim[0] = Integer.parseInt(pieces[1]);
					_dim[1] = Integer.parseInt(pieces[2]);
					_dataRaw = new int[_dim[0]][_dim[1]];
				}
			}
			f.mark(i);
		}
		return true;
	}

	PImage bufferImage(float[][] dataNormalized, int minC, int maxC) {
		PImage image = _parent.createImage(dataNormalized.length, 
				dataNormalized[0].length, PConstants.RGB);
		_parent.colorMode(PConstants.RGB);
		image.loadPixels();
		for (int x = 0; x < image.width; x++) {
			for (int y = 0; y < image.height; y++) {
				image.set(x, y, _parent.color(_parent.lerpColor(minC, maxC,
						dataNormalized[x][y])));
			}
		}
		image.updatePixels();
		_parent.colorMode(PConstants.HSB);
		return image;
	}

	public float[][] get_dataCubic() {
		return _dataCubic;
	}

	public void set_dataCubic(float[][] _dataCubic) {
		this._dataCubic = _dataCubic;
	}

	public float[][] get_dataLinear() {
		return _dataLinear;
	}

	public void set_dataLinear(float[][] _dataLinear) {
		this._dataLinear = _dataLinear;
	}

	public float[][] get_dataCubicNormalized() {
		return _dataCubicNormalized;
	}

	public void set_dataCubicNormalized(float[][] _dataCubicNormalized) {
		this._dataCubicNormalized = _dataCubicNormalized;
	}

	public float[][] get_dataLinearNormalized() {
		return _dataLinearNormalized;
	}

	public void set_dataLinearNormalized(float[][] _dataLinearNormalized) {
		this._dataLinearNormalized = _dataLinearNormalized;
	}
	
	public float[][] linearScale(int[][] src, int w, int h) {
		if (src == null) {
			System.out.println("no source data");
			System.exit(0);
		}
		if (_dataLinear == null)
			_dataLinear = new float[w][h];

		for (int i = 0; i < _dataLinear.length; ++i) {
			for (int j = 0; j < _dataLinear[0].length; ++j) {
				float xx = PApplet.map(i, 0, _dataLinear.length, 0,
						src.length - 1);
				float yy = PApplet.map(j, 0, _dataLinear[0].length, 0,
						src[0].length - 1);
				int x0 = PApplet.floor(xx);
				int x1 = PApplet.ceil(xx);
				int y0 = PApplet.floor(yy);
				int y1 = PApplet.ceil(yy);
				xx -= x0;
				yy -= y0;
				float color1 = PApplet.lerp(src[x0][y0], src[x1][y0], xx);
				float color2 = PApplet.lerp(src[x0][y1], src[x1][y1], xx);
				_dataLinear[i][j] = PApplet.lerp(color1, color2, yy);
			}
		}
		return _dataLinear;
	}

	public float[][] cubicScale(int[][] src, int w, int h) {
		if (src == null) {
			System.out.println("no source data");
			System.exit(0);
		}
		if (get_dataCubic() == null)
			set_dataCubic(new float[w][h]);

		double a00, a01, a02, a03;
		double a10, a11, a12, a13;
		double a20, a21, a22, a23;
		double a30, a31, a32, a33;
		

		for (int j = 0; j < get_dataCubic()[0].length; ++j) {
			for (int i = 0; i < get_dataCubic().length; ++i) {
				float xx = PApplet.map(i, 0, get_dataCubic().length-1, 0,
						src.length - 1);
				float yy = PApplet.map(j, 0, get_dataCubic()[0].length-1, 0,
						src[0].length - 1);
				int x0 = PApplet.floor(xx);
				int y0 = PApplet.floor(yy);
				xx -= x0;
				yy -= y0;

				int px0 = x0 - 1, px1 = x0, px2 = x0 + 1, px3 = x0 + 2;
				int py0 = y0 - 1, py1 = y0, py2 = y0 + 1, py3 = y0 + 2;
				if (px0 < 0)
					px0 = 0;
				if (py0 < 0)
					py0 = 0;
				if (px2 > src.length-1)
					px2 = src.length-1;
				if (px3 > src.length-1)
					px3 = src.length-1;
				if (py2 > src[0].length-1)
					py2 = src[0].length-1;
				if (py3 > src[0].length-1)
					py3 = src[0].length-1;

				a00 = src[px1][py1];
				a01 = -.5 * src[px1][py0] + .5 * src[px1][py2];
				a02 = src[px1][py0] - 2.5 * src[px1][py1] + 2 * src[px1][py2]
						- .5 * src[px1][py3];
				a03 = -.5 * src[px1][py0] + 1.5 * src[px1][py1] - 1.5
						* src[px1][py2] + .5 * src[px1][py3];
				a10 = -.5 * src[px0][py1] + .5 * src[px2][py1];
				a11 = .25 * src[px0][py0] - .25 * src[px0][py2] - .25
						* src[px2][py0] + .25 * src[px2][py2];
				a12 = -.5 * src[px0][py0] + 1.25 * src[px0][py1]
						- src[px0][py2] + .25 * src[px0][py3] + .5
						* src[px2][py0] - 1.25 * src[px2][py1] + src[px2][py2]
						- .25 * src[px2][py3];
				a13 = .25 * src[px0][py0] - .75 * src[px0][py1] + .75
						* src[px0][py2] - .25 * src[px0][py3] - .25
						* src[px2][py0] + .75 * src[px2][py1] - .75
						* src[px2][py2] + .25 * src[px2][py3];
				a20 = src[px0][py1] - 2.5 * src[px1][py1] + 2 * src[px2][py1]
						- .5 * src[px3][py1];
				a21 = -.5 * src[px0][py0] + .5 * src[px0][py2] + 1.25
						* src[px1][py0] - 1.25 * src[px1][py2] - src[px2][py0]
						+ src[px2][py2] + .25 * src[px3][py0] - .25
						* src[px3][py2];
				a22 = src[px0][py0] - 2.5 * src[px0][py1] + 2 * src[px0][py2]
						- .5 * src[px0][py3] - 2.5 * src[px1][py0] + 6.25
						* src[px1][py1] - 5 * src[px1][py2] + 1.25
						* src[px1][py3] + 2 * src[px2][py0] - 5 * src[px2][py1]
						+ 4 * src[px2][py2] - src[px2][py3] - .5
						* src[px3][py0] + 1.25 * src[px3][py1] - src[px3][py2]
						+ .25 * src[px3][py3];
				a23 = -.5 * src[px0][py0] + 1.5 * src[px0][py1] - 1.5
						* src[px0][py2] + .5 * src[px0][py3] + 1.25
						* src[px1][py0] - 3.75 * src[px1][py1] + 3.75
						* src[px1][py2] - 1.25 * src[px1][py3] - src[px2][py0]
						+ 3 * src[px2][py1] - 3 * src[px2][py2] + src[px2][py3]
						+ .25 * src[px3][py0] - .75 * src[px3][py1] + .75
						* src[px3][py2] - .25 * src[px3][py3];
				a30 = -.5 * src[px0][py1] + 1.5 * src[px1][py1] - 1.5
						* src[px2][py1] + .5 * src[px3][py1];
				a31 = .25 * src[px0][py0] - .25 * src[px0][py2] - .75
						* src[px1][py0] + .75 * src[px1][py2] + .75
						* src[px2][py0] - .75 * src[px2][py2] - .25
						* src[px3][py0] + .25 * src[px3][py2];
				a32 = -.5 * src[px0][py0] + 1.25 * src[px0][py1]
						- src[px0][py2] + .25 * src[px0][py3] + 1.5
						* src[px1][py0] - 3.75 * src[px1][py1] + 3
						* src[px1][py2] - .75 * src[px1][py3] - 1.5
						* src[px2][py0] + 3.75 * src[px2][py1] - 3
						* src[px2][py2] + .75 * src[px2][py3] + .5
						* src[px3][py0] - 1.25 * src[px3][py1] + src[px3][py2]
						- .25 * src[px3][py3];
				a33 = .25 * src[px0][py0] - .75 * src[px0][py1] + .75
						* src[px0][py2] - .25 * src[px0][py3] - .75
						* src[px1][py0] + 2.25 * src[px1][py1] - 2.25
						* src[px1][py2] + .75 * src[px1][py3] + .75
						* src[px2][py0] - 2.25 * src[px2][py1] + 2.25
						* src[px2][py2] - .75 * src[px2][py3] - .25
						* src[px3][py0] + .75 * src[px3][py1] - .75
						* src[px3][py2] + .25 * src[px3][py3];

				double x2 = xx * xx;
				double x3 = x2 * xx;
				double y2 = yy * yy;
				double y3 = y2 * yy;

				get_dataCubic()[i][j] = (float) ((a00 + a01 * yy + a02 * y2 + a03
						* y3)
						+ (a10 + a11 * yy + a12 * y2 + a13 * y3)
						* xx
						+ (a20 + a21 * yy + a22 * y2 + a23 * y3) * x2 + (a30
						+ a31 * yy + a32 * y2 + a33 * y3)
						* x3);
			}
		}
		return get_dataCubic();
	}
}
