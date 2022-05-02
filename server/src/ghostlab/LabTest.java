package ghostlab;

public class LabTest {
    public static void main(String[] args) {
        //Labyrinth lab = new Labyrinth(80, 100);
        //for (boolean[] row : lab.getSurface()) {
        //    for(boolean c : row) {
        //        System.out.print(c ? " " : "#");
        //    }
        //    System.out.println();
        //}
	PerfectLabyrinth l = new PerfectLabyrinth(25);
	System.out.println(l.draw());
    }
}
