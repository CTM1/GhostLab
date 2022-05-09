package ghostlab;

import java.util.Random;



public class RecursiveMaze {

    boolean grid[][];

    private void recursiveChamber(boolean grid[][], int startX, int startY, int endX, int endY) {

        int chamberSizeX = endX-startX + 1;
        int chamberSizeY = endY-startY + 1;
        if (chamberSizeX == 2 && chamberSizeY == 2)
            return;
        if (chamberSizeX < 2 || chamberSizeY < 2) {
            // System.out.println(String.format("Aborting (%d, %d) to (%d, %d)", startX, startY, endX, endY));
            return;
        }
            
        // System.out.println(String.format("=============== (%d, %d) to (%d, %d)\n"+this, startX, startY, endX, endY));
        Random rand = new Random();
        boolean horizontal;
        if (chamberSizeX < chamberSizeY)
            horizontal = true;
        else if (chamberSizeY < chamberSizeX)
            horizontal = false;
        else
            horizontal = rand.nextInt(1, 3) == 1;

        boolean wallselected=false;
        if (horizontal) {
            // System.out.println("Horizontal wall");
            int wallY = -1;
            int tries = 0;
            while (!wallselected) {
                if (tries > 100*chamberSizeY) {
                    // System.out.println(String.format("Couldn't find hwall for (%d, %d) to (%d, %d)", startX, startY, endX, endY));
                    return;
                }
                    
                wallY = rand.nextInt(startY+1, endY);
                if (grid[wallY][startX-1] && grid[wallY][endX+1])
                    wallselected = true;
                tries++;
            }
                
            int holeX = rand.nextInt(startX, endX+1);
            // System.out.println("Hole at x="+holeX);
            for(int i=0; i<chamberSizeX; i++)
                grid[wallY][startX+i] = (startX+i != holeX);
            recursiveChamber(grid, startX, startY, endX, wallY-1);
            recursiveChamber(grid, startX, wallY+1, endX, endY);
        } else {
            // System.out.println("Vertical wall");
            int wallX = -1;
            int tries = 0;
            while (!wallselected) {
                if (tries > 100*chamberSizeX) {
                    // System.out.println(String.format("Couldn't find vwall for (%d, %d) to (%d, %d)", startX, startY, endX, endY));
                    return;
                }
                wallX = rand.nextInt(startX+1, endX);
                if (grid[startY-1][wallX] && grid[endY+1][wallX])
                    wallselected = true;
                tries++;
            }
            int holeY = rand.nextInt(startY, endY+1);
            // System.out.println("Hole at y="+holeY);
            for(int i=0; i<chamberSizeY; i++)
                grid[startY+i][wallX] = (startY+i != holeY);
            recursiveChamber(grid, startX, startY, wallX-1, endY);
            recursiveChamber(grid, wallX+1, startY, endX, endY);
        }
        
    }

    public RecursiveMaze(int width, int height) {
        grid = new boolean[height][width];

        //fill outer walls
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                if (y == 0 || y == height-1 || x == 0 || x == width-1)
                    grid[y][x] = true;
            }
        }

        recursiveChamber(grid, 1, 1, width-2, height-2);
    }

    public void draw() {
        System.out.println(this);
    }

    public String toString() {
        String out = "";
        for(boolean[] line : this.grid) {
            for(boolean cell : line) {
                out += cell ? "#" : " ";
            }
            out += "\n";
        }
        return out;
    }
}
