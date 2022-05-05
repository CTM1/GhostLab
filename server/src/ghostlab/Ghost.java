package ghostlab;

public class Ghost {
  int x;
  int y;

  public Ghost(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void moveGhost(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
