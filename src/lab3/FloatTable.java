package lab3;
import java.util.*;

import processing.core.*;

public class FloatTable {
	PApplet parent;
	int rowCount;
	int columnCount;
	float[][] data;
	String[] rowNames;
	String[] columnNames;
	Map<String, ArrayList<Integer>> manufactures; // brand name to indices
	Map<String, ArrayList<String>> models;  //brand name to models

	FloatTable(PApplet p, String filename) {
		parent = p;
		String[] rows = parent.loadStrings(filename);

		String[] columns = PApplet.split(rows[0], PConstants.TAB);
		columnNames = PApplet.subset(columns, 1); // upper-left corner ignored
		scrubQuotes(columnNames);
		columnCount = columnNames.length;

		rowNames = new String[rows.length - 1];
		data = new float[rows.length - 1][];
		
		manufactures = new HashMap<String, ArrayList<Integer>>();
		models = new HashMap<String, ArrayList<String>>();

		// start reading at row 1, because the first row was only the column
		// headers
		for (int i = 1; i < rows.length; i++) {
			if (PApplet.trim(rows[i]).length() == 0) {
				continue; // skip empty rows
			}
			if (rows[i].startsWith("#")) {
				continue; // skip comment lines
			}

			// split the row on the tabs
			String[] pieces = PApplet.split(rows[i], PConstants.TAB);
			scrubQuotes(pieces);

			// copy row title
			rowNames[rowCount] = pieces[0];
			// copy data into the table starting at pieces[1]
			data[rowCount] = PApplet.parseFloat(PApplet.subset(pieces, 1));

			// increment the number of valid rows found so far
			rowCount++;
		}
		// resize the 'data' array as necessary
		data = (float[][]) PApplet.subset(data, 0, rowCount);
	}
	
	public void genDict() {
		String first;
		for(int row = 0; row < rowCount; ++row) {
			first =PApplet.split(rowNames[row], ' ')[0];
			if (!manufactures.containsKey(first)) {
				manufactures.put(first, new ArrayList<Integer>());
				manufactures.get(first).add(row);
			}				
			else
				manufactures.get(first).add(row);
			
			if (!models.containsKey(first)) {
				models.put(first, new ArrayList<String>());
				models.get(first).add(rowNames[row].trim());
			}				
			else {
				models.get(first).add(rowNames[row].trim());
			}
		}
	}

	public void scrubQuotes(String[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].length() > 2) {
				// remove quotes at start and end, if present
				if (array[i].startsWith("\"") && array[i].endsWith("\"")) {
					array[i] = array[i].substring(1, array[i].length() - 1);
				}
			}
			// make double quotes into single quotes
			array[i] = array[i].replaceAll("\"\"", "\"");
		}
	}

	public int getRowCount() {
		return rowCount;
	}

	public String getRowName(int rowIndex) {
		return rowNames[rowIndex];
	}

	public String[] getRowNames() {
		return rowNames;
	}

	// Find a row by its name, returns -1 if no row found.
	// This will return the index of the first row with this name.
	// A more efficient version of this function would put row names
	// into a Hashtable (or HashMap) that would map to an integer for the
	// row.
	public int getRowIndex(String name) {
		for (int i = 0; i < rowCount; i++) {
			if (rowNames[i].equals(name)) {
				return i;
			}
		}
		// println("No row named '" + name + "' was found");
		return -1;
	}

	// technically, this only returns the number of columns
	// in the very first row (which will be most accurate)
	public int getColumnCount() {
		return columnCount;
	}

	public String getColumnName(int colIndex) {
		return columnNames[colIndex];
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public float getFloat(int rowIndex, int col) {
		// Remove the 'training wheels' section for greater efficiency
		// It's included here to provide more useful error messages

		// begin training wheels
		if ((rowIndex < 0) || (rowIndex >= data.length)) {
			throw new RuntimeException("There is no row " + rowIndex);
		}
		if ((col < 0) || (col >= data[rowIndex].length)) {
			throw new RuntimeException("Row " + rowIndex
					+ " does not have a column " + col);
		}
		// end training wheels

		return data[rowIndex][col];
	}

	public boolean isValid(int row, int col) {
		if (row < 0)
			return false;
		if (row >= rowCount)
			return false;
		// if (col >= columnCount) return false;
		if (col >= data[row].length)
			return false;
		if (col < 0)
			return false;
		return !Float.isNaN(data[row][col]);
	}

	public float getColumnMin(int col) {
		float m = Float.MAX_VALUE;
		for (int i = 0; i < rowCount; i++) {
			if (!Float.isNaN(data[i][col])) {
				if (data[i][col] < m) {
					m = data[i][col];
				}
			}
		}
		return m;
	}

	public float getColumnMax(int col) {
		float m = -Float.MAX_VALUE;
		for (int i = 0; i < rowCount; i++) {
			if (isValid(i, col)) {
				if (data[i][col] > m) {
					m = data[i][col];
				}
			}
		}
		return m;
	}

	public float getRowMin(int row) {
		float m = Float.MAX_VALUE;
		for (int i = 0; i < columnCount; i++) {
			if (isValid(row, i)) {
				if (data[row][i] < m) {
					m = data[row][i];
				}
			}
		}
		return m;
	}

	public float getRowMax(int row) {
		float m = -Float.MAX_VALUE;
		for (int i = 1; i < columnCount; i++) {
			if (!Float.isNaN(data[row][i])) {
				if (data[row][i] > m) {
					m = data[row][i];
				}
			}
		}
		return m;
	}

	public float getTableMin() {
		float m = Float.MAX_VALUE;
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++) {
				if (isValid(i, j)) {
					if (data[i][j] < m) {
						m = data[i][j];
					}
				}
			}
		}
		return m;
	}

	public float getTableMax() {
		float m = -Float.MAX_VALUE;
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++) {
				if (isValid(i, j)) {
					if (data[i][j] > m) {
						m = data[i][j];
					}
				}
			}
		}
		return m;
	}
}
