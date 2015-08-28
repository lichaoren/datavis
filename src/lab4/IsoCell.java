package lab4;

import java.awt.geom.Point2D;

public class IsoCell {
	enum side {
		LEFT, RIGHT, TOP, BOTTOM, NONE
	};

	boolean flipped;
	int CheckSum;
	double left, right, top, bottom;

	public IsoCell() {
		flipped = false;
	}

	public boolean isFlipped() {
		return flipped;
	}

	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}

	public int getCheckSum() {
		return CheckSum;
	}

	public void setCheckSum(int CheckSum) {
		this.CheckSum = CheckSum;
	}

	public Point2D normalizedPointCCW(side cellSide) {
		switch (cellSide) {
		case BOTTOM:
			return new Point2D.Double(bottom, 0);
		case LEFT:
			return new Point2D.Double(0, left);
		case RIGHT:
			return new Point2D.Double(1, right);
		case TOP:
			return new Point2D.Double(top, 1);
		default:
			return null;
		}
	}

	public side firstSideCCW(side prev) {

		switch (CheckSum) {
		case 1:
		case 3:
		case 7:
			return side.LEFT;
		case 2:
		case 6:
		case 14:
			return side.BOTTOM;
		case 4:
		case 12:
		case 13:
			return side.RIGHT;
		case 8:
		case 9:
		case 11:
			return side.TOP;
		case 5:
			switch (prev) {
			case LEFT:
				return side.RIGHT;
			case RIGHT:
				return side.LEFT;
			default:
				System.out.println(getClass() + ".firstSideCCW: case 5!");
				System.exit(1);
			}
		case 10:
			switch (prev) {
			case BOTTOM:
				return side.TOP;
			case TOP:
				return side.BOTTOM;
			default:
				System.out.println(getClass() + ".firstSideCCW: case 10!");
				System.exit(1);
			}
		default:
			System.out.println(getClass() + ".firstSideCCW: default!");
			System.exit(1);
		}
		return null;
	}

	public side secondSideCCW(side prev) {

		switch (CheckSum) {
		case 8:
		case 12:
		case 14:
			return side.LEFT;
		case 1:
		case 9:
		case 13:
			return side.BOTTOM;
		case 2:
		case 3:
		case 11:
			return side.RIGHT;
		case 4:
		case 6:
		case 7:
			return side.TOP;
		case 5:
			switch (prev) {
			case LEFT: // Normal case 5.
				return flipped ? side.BOTTOM : side.TOP;
			case RIGHT: // Normal case 5.
				return flipped ? side.TOP : side.BOTTOM;
			default:
				System.out.println(getClass() + ".secondSideCCW: case 5!");
				System.exit(1);
			}
		case 10:
			switch (prev) {
			case BOTTOM: // Normal case 10
				return flipped ? side.RIGHT : side.LEFT;
			case TOP: // Normal case 10
				return flipped ? side.LEFT : side.RIGHT;
			default:
				System.out.println(getClass() + ".secondSideCCW: case 10!");
				System.exit(1);
			}
		default:
			System.out.println(getClass()
					+ ".secondSideCCW: shouldn't be here!  CheckSum = "
					+ CheckSum);
			System.exit(1);
			return side.NONE;
		}
	}

	public side nextCellCCW(side prev) {
		return secondSideCCW(prev);
	}

	public void clearIso(side prev) {
		switch (CheckSum) {
		case 0:
		case 5:
		case 10:
		case 15:
			break;
		default:
			CheckSum = 15;
		}
	}

	public double getLeft() {
		return left;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getRight() {
		return right;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public double getTop() {
		return top;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getBottom() {
		return bottom;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}
}