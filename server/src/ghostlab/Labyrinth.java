package ghostlab;

import java.util.Random;

/**
 * Labyrinth.java
 * A naive but usable LabyrInterface implementation
 *
 * @since 04.04.2022
 * @author whogives4fuck
 */

public class Labyrinth implements LabyrInterface {
	// The labyrinth properly said, true means cell, false means wall
	private boolean[][] surface;

	public Labyrinth(int width, int height) {
		surface = new boolean[width][height];

		// all of surface is inaccessible at first
		// as the default value of boolean is false.
		
		// after that, we dig-a-dig a hole
		Random r = new Random();

		int x = r.nextInt(width + 1);
		int y = r.nextInt(height+1);
		int maxDiggs = r.nextInt(width*height);
		int digged = 0;

		int newX = x;
		int newY = y;
		int dir =  0;
		while(digged < maxDiggs) {
			dir = r.nextInt(4);
			switch (dir) {
				case 0:
					newX++;break;
				case 1:
					newX--; break;
				case 2:
					newY--; break;
				case 3:
					newY++; break;
			}
			
			// validate move
			if (newX >= 0 && newX < width
				&& newY < height && newY >= 0) {
				surface[newX][newY] = true;
				x = newX;
				y = newY;
				digged++;
			} else {
				newX = x;
				newY = y;
			}
		}
	}

	public static LabyrInterface CTOR(int width, int height) {
		return new Labyrinth(width, height);
	}

	public int tryMove(int x, int y, int direction, int distance) {
		int moved = 0;
		while (moved < distance && surface[x][y]) {
			switch (direction) {
				case 0:
					x++;break;
				case 1:
					x--; break;
				case 2:
					y--; break;
				case 3:
					y++; break;
			}
		}
		return moved;
	}

}