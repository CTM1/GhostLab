package ghostlab;

import java.util.Random;

/**
 * Labyrinth.java A naive but usable LabyrInterface implementation
 *
 * @since 04.04.2022
 * @author whogives4fuck
 */
public class Labyrinth implements LabyrInterface {
  // The labyrinth properly said, true means cell, false means wall
  private boolean[][] surface;
  private char w;
  private char h;
  private int width;
  private int length;
  Random r;

  public char getWidth() {
    return w;
  }

  public char getHeight() {
    return h;
  }

  public int[] emptyPlace() {
    int x;
    int y;
    do {
      x = r.nextInt(width);
      y = r.nextInt(length);
    } while (!surface[x][y]);

    return new int[] {x, y};
  }

  public Labyrinth(int width, int height) {
    r = new Random();
    width = Math.min(1000, width);
    height = Math.min(1000, height);

    w = (char) Integer.toUnsignedLong(width);
    h = (char) Integer.toUnsignedLong(height);

    surface = new boolean[width][height];

    // all of surface is inaccessible at first
    // as the default value of boolean is false.

    // after that, we dig-a-dig a hole

    int x = r.nextInt(width + 1);
    int y = r.nextInt(height + 1);
    int maxDiggs = r.nextInt(width * height);
    int digged = 0;

    int newX = x;
    int newY = y;
    int dir = 0;
    while (digged < maxDiggs) {
      dir = r.nextInt(4);
      switch (dir) {
        case 0:
          newX++;
          break;
        case 1:
          newX--;
          break;
        case 2:
          newY--;
          break;
        case 3:
          newY++;
          break;
      }

      // validate move
      if (newX >= 0 && newX < width && newY < height && newY >= 0) {
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

  public boolean[][] getSurface() {
    return this.surface;
  }

  public static LabyrInterface CTOR(int width, int height) {
    return new Labyrinth(width, height);
  }

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
}
