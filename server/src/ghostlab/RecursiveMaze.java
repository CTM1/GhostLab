package ghostlab;

import java.util.Random;



public class RecursiveMaze implements LabyrInterface {

    private boolean grid[][];
    private int width;
    private int height;
    private Random rand = new Random();

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
        this.grid = new boolean[height][width];
        this.width = width;
        this.height = height;

        //fill outer walls
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                if (y == 0 || y == height-1 || x == 0 || x == width-1)
                    grid[y][x] = true;
            }
        }

        recursiveChamber(grid, 1, 1, width-2, height-2);
    }

    public char getWidth() {
        return (char) Integer.toUnsignedLong(width);
    }

    public char getHeight() {
        return (char) Integer.toUnsignedLong(height);
    }

    public int[] emptyPlace() {
        int tries = 0;
        while(tries < (width*height)*2) {
            int x = rand.nextInt(0, width);
            int y = rand.nextInt(0, height);
            if (!grid[y][x])
                return new int[]{x, y};
            tries++;
        }
        return null;
    }

    public int tryMove(int x, int y, int direction, int distance) {
        int npX = x;
        int npY = y;
        int wx = 0;
        int wy = 0;
        switch(direction) {
            case 0:
                wy = -1;
                break;
            case 1:
                wy = 1;
                break;
            case 2:
                wx = -1;
                break;
            case 3:
                wx = 1;
                break;
        }
        for(int i=0; i<distance; i++) {
            if (grid[npY+wy][npX+wx])
                return i;
            npX += wx;
            npY += wy;
        }

        return distance;
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

    public boolean[][] getSurface() {
       return grid;
    }
}
