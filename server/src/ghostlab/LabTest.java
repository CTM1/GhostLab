package ghostlab;

public class LabTest {
  public static void main(String[] args) {
    Maze x = new Maze(120, 120);

    boolean[][] xx = x.getSurface();

    for (boolean[] l : xx) {
      for (boolean c : l) {
        System.out.print(c ? " " : "x");
      }
      System.out.println();
    }
  }
}
