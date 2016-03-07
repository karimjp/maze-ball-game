package jana.karim.hw4;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import jana.karim.hw4.MazeObject;
import jana.karim.hw4.MazeObject.Type;
import jana.karim.hw4.MainActivity;

public class DrawingView extends View {

	private List<MazeObject> mazeObstacles = new ArrayList<MazeObject>();
	private Maze maze;
	private MazeObject marble = null;
	private MazeObject goal = null;
	private MainActivity mainActivity = (MainActivity) getContext();

	Bitmap bitmap;
	float x = 0;
	float y = 0;
	// private MazeObject selected;
	private RectF rect = new RectF();

	public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// TODO Auto-generated constructor stub
	}

	public DrawingView(Context context) {
		super(context);

	}

	public void init() {

		bitmap = BitmapFactory
				.decodeResource(getResources(), R.drawable.marble);
		Bitmap scaledMarble = scaleBitmapToMazeCell(bitmap);
		marble = new MazeObject(Type.MARBLE, scaledMarble);

		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.goal);
		Bitmap scaledFloppyDisk = scaleBitmapToMazeCell(bitmap);
		goal = new MazeObject(Type.GOAL, scaledFloppyDisk);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);

		float viewX = 0;
		float viewY = 0;

		if (marble == null) {
			// Program is starting: Marble will be set along with maze and
			// objects
			initMaze();
			init();
			maze.setMazeLayoutObjects(canvas, viewX, viewY, mazeObstacles);
			startMarblePosition(canvas);
			setMarbleGoal(canvas);
			rect.set(marble.getBounds());

		} else {
			// Program was running: redraw

			maze.drawMaze(canvas, viewX, viewY);
			marble.drawMazeObject(canvas, marble.getBounds());
			goal.drawMazeObject(canvas, goal.getBounds());

			if (marble.getBounds().intersect(goal.getBounds())) {
				Log.d("INTERSECTION", "Goal has been reach");
				mainActivity.winVibration();
				mainActivity.wonGameOption();
			}
			if (findIntersection(marble).getType() == Type.OBSTACLE) {
				Log.d("INTERSECTION", "Obstacle found. Game over");
				mainActivity.loseVibration();
				mainActivity.gameOverOption();
			}
		}

	}

	public void moveMarble(float[] accelerometer, float[] magnetometer) {
		float ax = accelerometer[0] * 10;
		float ay = accelerometer[1] * 10;
		float mx = magnetometer[0];
		float my = magnetometer[1];

		// Y coordinate
		float bottom = marble.getBounds().bottom;
		// Y coordinate
		float top = marble.getBounds().top;
		// X coordinate
		float left = marble.getBounds().left;
		// X coordinate
		float right = marble.getBounds().right;

		float b = bottom + ax + mx;
		float t = top + ax + mx;
		float l = left + ay + my;
		float r = right + ay + my;

		marble.setBounds(new RectF(l, t, r, b));
		MazeObject intersecting = findIntersection(marble);
		if (intersecting.getType() == Type.WALL) {
			marble.setBounds(new RectF(left, top, right, bottom));
		}

		// enforceWallBoundaries(b, t, l, r);

		// marble.setBounds(new RectF(ax, ay, ax+marble.getBounds().width(),
		// ay+marble.getBounds().height()));;
	}

	/*
	 * public void enforceWallBoundaries(float b, float t, float l, float r){
	 * 
	 * MazeObject intersected = findIntersection(marble); Log.d("wall intesect",
	 * "intersection Type:" +intersected.getType()+""); if(intersected.getType()
	 * == Type.WALL){ RectF intersectedCoords = intersected.getBounds(); RectF
	 * marbleCoords = marble.getBounds(); if(intersectedCoords.left <
	 * marbleCoords.left ){ Log.d("wall intesect", "left"); //wall has to be
	 * less because is on the left most side float offset =
	 * intersectedCoords.left - marbleCoords.left;
	 * 
	 * l=l - offset; r=r - offset;
	 * 
	 * } if(intersectedCoords.right > marbleCoords.right ){
	 * Log.d("wall intesect", "right"); //wall has to be greater because is on
	 * the right most side float offset = intersectedCoords.right -
	 * marbleCoords.right; l=l - offset; r=r - offset;
	 * 
	 * } if(intersectedCoords.top < marbleCoords.top ){ Log.d("wall intesect",
	 * "top"); //wall has to be less because top should be below marble float
	 * offset = intersectedCoords.top - marbleCoords.top; t=t - offset; b=b -
	 * offset;
	 * 
	 * } if(intersectedCoords.bottom> marbleCoords.bottom ){
	 * Log.d("wall intesect", "bottom"); //wall has to be greater because bottom
	 * should be above marble float offset = intersectedCoords.bottom -
	 * marbleCoords.bottom; t=t - offset; b=b - offset; } marble.setBounds(new
	 * RectF(l, t, r, b)); } }
	 */
	public void resetView() {
		invalidate();
	}

	private Bitmap scaleBitmapToMazeCell(Bitmap bitmap) {
		/*
		 * Scales the any bitmap to the measurements of the maze cells in the
		 * screen.
		 */
		int w = Math.round(maze.getCellWidth());
		int h = Math.round(maze.getCellHeight());
		Bitmap scaled = Bitmap.createScaledBitmap(bitmap, w, h, true);
		return scaled;

	}

	private void startMarblePosition(Canvas canvas) {
		/*
		 * Sets the robot pacman in the maze array starting position.
		 */
		for (int i = mazeObstacles.size() - 1; i >= 0; i--) {
			MazeObject mazeObject = mazeObstacles.get(i);
			if (mazeObject.getProperty() == "STARTING_POSITION") {
				marble.drawMazeObject(canvas, mazeObject.getBounds());
			}
		}
	}

	private void setMarbleGoal(Canvas canvas) {
		/*
		 * Sets the floppy disk in the maze array finish position.
		 */
		for (int i = mazeObstacles.size() - 1; i >= 0; i--) {
			MazeObject mazeObject = mazeObstacles.get(i);
			if (mazeObject.getProperty() == "FINISH_POSITION") {
				goal.drawMazeObject(canvas, mazeObject.getBounds());
			}
		}

	}

	private MazeObject findIntersection(MazeObject movingMazeObject) {
		float left = movingMazeObject.getBounds().left;
		float top = movingMazeObject.getBounds().top;
		float right = movingMazeObject.getBounds().right;
		float bottom = movingMazeObject.getBounds().bottom;
		for (int i = mazeObstacles.size() - 1; i >= 0; i--) {
			MazeObject mazeObject = mazeObstacles.get(i);
			if (mazeObject.getBounds().intersects(left, top, right, bottom)) {
				return mazeObject;
			}
		}
		return movingMazeObject;

	}

	public boolean isMarbleInit() {
		if (marble == null)
			return false;
		return true;

	}

	public void initMaze() {
		/*
		 * Allows to change the Maze Layout.
		 */

		// 0 == floor, 1 == wall, 2 == obstacles
		// 10 == starting position, 20 == finish
		int[][] mazeArray = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
				{ 1, 10, 0, 0, 0, 0, 0, 2, 0, 1 },
				{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				{ 1, 0, 0, 0, 2, 0, 0, 0, 0, 1 },
				{ 1, 0, 2, 0, 0, 0, 2, 0, 0, 1 },
				{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				{ 1, 1, 1, 1, 1, 20, 1, 1, 1, 1 },

		};

		Bitmap[] bitmaps = {
				BitmapFactory.decodeResource(getResources(), R.drawable.floor),
				BitmapFactory.decodeResource(getResources(), R.drawable.wall),
				BitmapFactory.decodeResource(getResources(),
						R.drawable.obstacles) };

		// Chance the 480 and 320 to match the screen size of your device
		maze = new Maze(bitmaps, mazeArray, 10, 9, getWidth(), getHeight());

	}

}