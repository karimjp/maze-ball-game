/*
 * The basic version of this Maze class is from:
 * http://stackoverflow.com/questions/20747138/android-drawing-a-maze-to-canvas-with-smooth-character-movement
 */
package jana.karim.hw4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import jana.karim.hw4.MazeObject;
import jana.karim.hw4.MazeObject.Type;

public class Maze {
	private RectF drawRect = new RectF();
	private Bitmap[] bitmaps;
	private int[][] tileType;
	private final int STARTING_POSITION = 10;
	private final int FINISH_POSITION = 20;

	private List<Integer> mazeValues = new ArrayList<Integer>(Arrays.asList(0,
			1, 2, STARTING_POSITION, FINISH_POSITION));

	private float screenWidth, screenHeight;

	/**
	 * Initialize a new maze.
	 * 
	 * @param wallBitmap
	 *            The desired bitmaps for the floors and walls
	 * @param isWall
	 *            The wall data array. Each true value in the array represents a
	 *            wall and each false represents a gap
	 * @param xCellCountOnScreen
	 *            How many cells are visible on the screen on the x axis
	 * @param yCellCountOnScreen
	 *            How many cells are visible on the screen on the y axis
	 * @param screenWidth
	 *            The screen width
	 * @param screenHeight
	 *            The screen height
	 */
	public Maze(Bitmap[] bitmaps, int[][] tileType, float xCellCountOnScreen,
			float yCellCountOnScreen, float screenWidth, float screenHeight) {
		this.bitmaps = bitmaps;
		this.tileType = tileType;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		drawRect.set(0, 0, screenWidth / xCellCountOnScreen, screenHeight
				/ yCellCountOnScreen);
	}

	/**
	 * Get the type of the cell. x and y values are not coordinates!
	 * 
	 * @param x
	 *            The x index of the cell
	 * @param y
	 *            The y index of the cell
	 * @return The cell type
	 */
	public int getType(int x, int y) {
		if (y < tileType.length && x < tileType[y].length)
			return tileType[y][x];
		return 0;
	}

	public float getCellWidth() {
		return drawRect.width();
	}

	public float getCellHeight() {
		return drawRect.height();
	}

	/**
	 * Draws the maze. View coordinates should have positive values.
	 * 
	 * @param canvas
	 *            Canvas for the drawing
	 * @param viewX
	 *            The x coordinate of the view
	 * @param viewY
	 *            The y coordinate of the view
	 */
	public void drawMaze(Canvas canvas, float viewX, float viewY) {
		int tileX = 0;
		int tileY = 0;
		float xCoord = -viewX;
		float yCoord = -viewY;

		while (tileY < tileType.length && yCoord <= screenHeight) {
			// Begin drawing a new column
			tileX = 0;
			xCoord = -viewX;

			while (tileX < tileType[tileY].length && xCoord <= screenWidth) {
				// Check if the tile is not null
				if (mazeValues.contains(tileType[tileY][tileX])) {

					// This tile is not null, so check if it has to be drawn
					if (xCoord + drawRect.width() >= 0
							&& yCoord + drawRect.height() >= 0) {

						// The tile actually visible to the user, so draw it
						drawRect.offsetTo(xCoord, yCoord); // Move the rectangle
															// to the
															// coordinates
						if (STARTING_POSITION == tileType[tileY][tileX]) {
							tileType[tileY][tileX] = 0;
						}
						if (FINISH_POSITION == tileType[tileY][tileX]) {
							tileType[tileY][tileX] = 0;
						}

						canvas.drawBitmap(bitmaps[tileType[tileY][tileX]],
								null, drawRect, null);
					}
				}

				// Move to the next tile on the X axis
				tileX++;
				xCoord += drawRect.width();
			}

			// Move to the next tile on the Y axis
			tileY++;
			yCoord += drawRect.height();
		}
	}

	public int[][] getTileType() {
		return tileType;
	}

	public void setTileType(int[][] tileType) {
		this.tileType = tileType;
	}

	public void setMazeLayoutObjects(Canvas canvas, float viewX, float viewY,
			List<MazeObject> mazeObjects) {
		int tileX = 0;
		int tileY = 0;
		float xCoord = -viewX;
		float yCoord = -viewY;
		MazeObject mazeObject = null;
		while (tileY < tileType.length && yCoord <= screenHeight) {
			// Begin drawing a new column
			tileX = 0;
			xCoord = -viewX;

			while (tileX < tileType[tileY].length && xCoord <= screenWidth) {
				// Check if the tile is not null
				boolean c = mazeValues.contains(tileType[tileY][tileX]);
				Log.d("TEST", c + "");
				if (mazeValues.contains(tileType[tileY][tileX])) {

					// This tile is not null, so check if it has to be drawn
					if (xCoord + drawRect.width() >= 0
							&& yCoord + drawRect.height() >= 0) {

						// The tile actually visible to the user, so draw it
						drawRect.offsetTo(xCoord, yCoord); // Move the rectangle
															// to the
															// coordinates
						switch (tileType[tileY][tileX]) {
						case 0:
							// Log.d("MAZE","Setting Floor");
							mazeObject = new MazeObject(Type.FLOOR,
									bitmaps[tileType[tileY][tileX]]);
							mazeObject.setMazeArrayX(tileX);
							mazeObject.setMazeArrayY(tileY);
							break;
						case 1:
							// Log.d("MAZE","Ignore Setting Red wall as a MazeObject");
							// canvas.drawBitmap(bitmaps[tileType[tileY][tileX]],
							// null, drawRect, null);
							mazeObject = new MazeObject(Type.WALL,
									bitmaps[tileType[tileY][tileX]]);
							mazeObject.setMazeArrayX(tileX);
							mazeObject.setMazeArrayY(tileY);
							break;
						case 2:
							// Log.d("MAZE","Setting Purple Wall");
							mazeObject = new MazeObject(Type.OBSTACLE,
									bitmaps[tileType[tileY][tileX]]);
							mazeObject.setMazeArrayX(tileX);
							mazeObject.setMazeArrayY(tileY);
							break;
						case STARTING_POSITION:
							// Log.d("MAZE","Setting Starting Point");
							tileType[tileY][tileX] = 0;
							mazeObject = new MazeObject(Type.FLOOR,
									bitmaps[tileType[tileY][tileX]]);
							mazeObject.setMazeArrayX(tileX);
							mazeObject.setMazeArrayY(tileY);
							mazeObject.setProperty("STARTING_POSITION");
							break;
						case FINISH_POSITION:
							// Log.d("MAZE","Setting Goal");
							tileType[tileY][tileX] = 0;
							mazeObject = new MazeObject(Type.FLOOR,
									bitmaps[tileType[tileY][tileX]]);
							mazeObject.setMazeArrayX(tileX);
							mazeObject.setMazeArrayY(tileY);
							mazeObject.setProperty("FINISH_POSITION");
							break;

						}
						if (mazeObject != null) {
							mazeObject.drawMazeObject(canvas, drawRect);

							mazeObjects.add(mazeObject);

						}

					}
				}

				// Move to the next tile on the X axis
				tileX++;
				xCoord += drawRect.width();
			}

			// Move to the next tile on the Y axis
			tileY++;
			yCoord += drawRect.height();
		}
	}
}