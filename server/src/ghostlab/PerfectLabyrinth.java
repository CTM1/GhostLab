package ghostlab;

/**
 * Generates a perfect labyrinth, thus a connex one.
 * Uses binary tree algo
 */
import java.util.Random;

public class PerfectLabyrinth implements LabyrInterface {
	private char w;
	private char h;

	/** A cell of the labyrinth
	 */
	private class Cell {
		public boolean north = false; // true means passage open
		public boolean south = false;
		public boolean east = false;
		public boolean west = false;
	}

	public int tryMove(int x, int y, int dir, int dis) {
		return 0; // TODO
	}

	// TODO getSurface

	public PerfectLabyrinth(int width, int height) {
		width = Math.min(1000, width);
		height = Math.min(1000, height);

		w = (char)Integer.toUnsignedLong(width);
		h = (char)Integer.toUnsignedLong(height);

		Cell[][] surface = new Cell[height][width];

		boolean goingNorth = false;
		boolean goingWest = false;
		Random rd = new Random();

		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				goingNorth = false;
				goingWest = false;
				if (y > 0 && x > 0) {
					goingNorth = rd.nextBoolean();
					goingWest = !goingNorth;
				} if (y > 0) {
					goingWest = true;
				} else if (x > 0) {
					goingNorth = true;
				}


				if (goingNorth) {
					surface[x][y].north = true;
					surface[x][y+1].south = true;
				}
				if (goingWest) {
					surface[x][y].west = true;
					surface[x+1][y].east = true;
				}
			}
		}
	}


	public char getWidth() { return w; }
	public char getHeight() { return h; }
}
