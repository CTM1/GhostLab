package ghostlab;

import java.util.ArrayList;
import java.util.Random;

/**
 * A just decent enough implentation of the binary tree maze generation algorithm.
 *
 * <p>Since we need to have a boolean array, we create a cell by having packs of 3*3 booleans, like
 * so:
 *
 * <pre>
 *          * <north> *
 *      <east> <cell> <west>
 *           * <south> *
 * </pre>
 *
 * <p>Additional cells (represented as '*' in the schema) are carved or not depending on the two
 * nearby walls.
 */
public class Maze implements LabyrInterface {
  private boolean[][] surface;
  private int width;
  private int height;
  private Random random;

  // constructor
  public Maze(int xDimension, int yDimension) {
    random = new Random();

    width = roundUpToMultipleOf3(xDimension); // rounding
    height = roundUpToMultipleOf3(yDimension);
    surface = new boolean[width][height];

    // init of surface
    for (int i = 0; i < xDimension; i++) {
      surface[i] = new boolean[yDimension];
      for (int j = 0; j < yDimension; j++) {
        surface[i][j] = false;
      }
    }

    // the generation properly said
    int dir = 0; // 0 = North, 1 = East
    ArrayList<Integer> dirs;
    // we jump from cell to cell, not from case to case
    for (int x = 1; x < width - 1; x += 2) {
      for (int y = 1; y < height - 1; y += 2) {
        // Logger.log("cell (%d, %d)\n", x, y);
        dirs = new ArrayList<Integer>();
        dirs.add(-1);
        // find direction in which to carve
        if (x != 1) { // can go North
          dirs.add(0);
        }
        if (y != 1) { // Can go east
          dirs.add(1);
        }

        dir = dirs.get(random.nextInt(dirs.size()));

        if (dir == 0) { // carving North
          surface[x][y] = true;
          surface[x + 1][y] = true;
          if (x + 3 < width) {
            surface[x + 2][y] = true;
            surface[x + 3][y] = true;
          }
        } else if (dir == 1) { // carving east
          surface[x][y] = true;
          surface[x][y - 1] = true;
          if (y - 3 > 0) {
            surface[x][y - 2] = true;
            surface[x][y - 3] = true;
          }
        }
      }
    }
  }

  @Override
  public int tryMove(int x, int y, int direction, int distance) {
    int moved = 0;
    while (moved < distance && surface[x][y]) {
      switch (direction) {
        case 0:
          x++;
          break;
        case 1:
          x--;
          break;
        case 2:
          y--;
          break;
        case 3:
          y++;
          break;
      }
    }
    return moved;
  }

  @Override
  public int[] emptyPlace() {
    int x;
    int y;
    do {
      x = random.nextInt(width);
      y = random.nextInt(height);
    } while (!surface[x][y]);

    return new int[] {x, y};
  }

  @Override
  public char getHeight() {
    return (char) Integer.toUnsignedLong(height);
  }

  @Override
  public char getWidth() {
    return (char) Integer.toUnsignedLong(width);
  }

  /** Returns the maze in a two dimensionnal boolean array */
  public boolean[][] getSurface() {
    return surface;
  }

  /** Name says it all, used to get usable Cells */
  private int roundUpToMultipleOf3(int x) {
    return ((int) Math.ceil(x / 3)) * 3;
  }
}
