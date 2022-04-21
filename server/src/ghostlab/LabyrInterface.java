package ghostlab;

/**
 * LabyrInterface.java
 * Interface that describes the things a Labyrinth must do.
 *
 * @since 04.04.2022
 * @author ekip
 */

public interface LabyrInterface {
	/**
	 * Basically a factory, returns a well built Labyrinth
	 *
	 * @return A well built Labyrinth of specified width and height.
	 * @param width The width of the Labyrinth to build.
	 * @param height The height of the Labyrinth to build.
	 */
	public static LabyrInterface CTOR(int width, int height) {
		return null;
	}

	/**
	 * This method takes the coordinates of a player, a direction, a
	 * distance, and returns either the max distance the player could
	 * cover before meeting a wall, or the distance they wanted to cover.
	 *
	 * @return The distance covered before stopping or meeting a wall
	 * @param x The x position of the player before moving
	 * @param y The y position of the player before moving
	 * @param direction The direction to move to. (0->up, 1->bottom, 2->left, 3->right)
	 * @param distance The max distance covered before stopping.
	 */
	public int tryMove(int x, int y, int direction, int distance);

	public char getWidth();
	public char getHeight();
}
