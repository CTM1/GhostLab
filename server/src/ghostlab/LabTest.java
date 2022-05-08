package ghostlab;

public class LabTest {
	public static void main(String[] args) {
		Maze x = new Maze(50,50);
		boolean[][] xx =  x.getSurface();
		for(boolean[] l: xx) {
			for (boolean z:l) {
				if(z) {
					System.out.print("1");
				} else {
					System.out.print("0");
				}
			}
			System.out.println();
		}
		System.out.format("Dimensions: %d:%d\n", xx.length, xx[0].length);
	}
}
