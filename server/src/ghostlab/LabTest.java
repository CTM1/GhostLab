package ghostlab;

public class LabTest {
  public static void main(String[] args) {
    Maze x = new Maze(12, 12);

    boolean[][] xx = x.getSurface();

    for (boolean[] l : xx) {
      for (boolean c : l) {
        System.out.print(c ? " " : "x");
      }
      System.out.println();
    }
  }
}
