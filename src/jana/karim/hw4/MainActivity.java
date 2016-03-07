package jana.karim.hw4;

/**
 * @author karim jana
 * Game Description:
 A Maze with the Maze Objects: Goal, Obstacles, Ball, Wall, Floor

 Roles:
 Goal: If Marble reaches coordinates then win game

 Obstacles: If Marble coordinates are same as obstacle coordinates then terminate game.

 Wall: Does not allow Marble to cross it 

 Floor: Provides the coordinates of the obstacles, wall, and goal 

 Marble: Will be moved based on accelerometer and magnetometer input.

 Defining the program:

 Win game:
 - Vibrates with win vibration pattern
 - Shows dialog with positive message and ask user to restart game or quit application.


 Terminate Game:
 - Vibrates with lose vibration pattern
 - Shows dialog with supportive message and ask the user to restart game or quit game.

 Do Not Cross Wall Boundaries:
 - Ignores further movement in the direction of the wall. 

 Move Marble:
 - Accelerate in the direction that user is pointing. Adds acceleration 
 if dropping down due to magnetometer increase.

 NOTES:
 Code from hw3 was reused and modified.
 MazeObject.java
 Maze.java
 DrawingView.java
 * 
 */

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout.LayoutParams;

public class MainActivity extends Activity {
	private DrawingView drawingView;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private Vibrator vibrator;
	private long[] loseVibrationPattern = { 0, 200 };
	private long[] winVibrationPattern = { 0, 50, 50, 200 };
	private float[] acceleration = new float[3];
	private float[] geomagnetic = new float[3];
	private final int MOVING_AVG_POINTS = 4;
	private List<float[]> accelerationRawPoints;
	private List<float[]> magnetometerRawPoints;
	private float[] filteredAcceleration;
	private float[] filteredMagnetometer;

	SensorEventListener accelListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(final SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				System.arraycopy(event.values, 0, geomagnetic, 0, 3);
				magnetometerRawPoints.add(geomagnetic);

			} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				System.arraycopy(event.values, 0, acceleration, 0, 3);

				accelerationRawPoints.add(acceleration);

			}
			// use 4 points to calculate the moving avg
			if (updateAvailable()) {
				updateMarbleValues();
				if (drawingView.isMarbleInit()) {

					drawingView.moveMarble(filteredAcceleration,
							filteredMagnetometer);
					drawingView.resetView();

				}
				// remove raw readings added to calculate mv avg.
				clearSensorFilterBuffers();
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	private boolean updateAvailable() {
		boolean accelerometer_ready = (accelerationRawPoints.size() >= MOVING_AVG_POINTS);
		boolean magnetometer_ready = (magnetometerRawPoints.size() >= MOVING_AVG_POINTS);
		if (accelerometer_ready && magnetometer_ready)
			return true;
		return false;
	}

	// updates clean values for the marble to move in canvas
	private void updateMarbleValues() {
		filteredAcceleration = movingAverageFilter(accelerationRawPoints);
		filteredMagnetometer = movingAverageFilter(magnetometerRawPoints);

	}

	// remove raw readings added to calculate mv avg.
	private void clearSensorFilterBuffers() {
		accelerationRawPoints.clear();
		magnetometerRawPoints.clear();
	}

	// filtering routine for sensor values
	public float[] movingAverageFilter(List<float[]> points) {
		final int AXIS_NUM = 3;
		float[] sum = new float[AXIS_NUM];
		float[] avg = new float[AXIS_NUM];
		// initialize each axis sum to 0
		for (int i = 0; i < AXIS_NUM; i++) {
			sum[i] = 0;
			avg[i] = 0;
		}
		// reset to last 3 read values
		if (points.size() > MOVING_AVG_POINTS) {
			int elementsToRemove = points.size() - MOVING_AVG_POINTS;
			while (elementsToRemove != 0) {
				int index = elementsToRemove - 1;
				points.remove(index);
				elementsToRemove--;
			}
		}

		// for each set of reading (point)
		for (float[] arrayValue : points) {
			// calculate the sum for each axis
			int index = 0;
			for (float axis : arrayValue) {
				sum[index] += axis;
				index++;
			}
		}
		// calculates the moving avg for each axis
		int axisIndex = 0;
		for (float axisSumValue : sum) {
			// Log.d("AXIS SUM VALUE", axisSumValue+" i:"+ axisIndex);
			avg[axisIndex] = axisSumValue / MOVING_AVG_POINTS;
			axisIndex++;

		}

		return avg;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
			// setup sensor
			accelerometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		} else {
			// exit 
			finish();
		}
		if (sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size() != 0) {
			// setup sensor
			magnetometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		} else {
			// exit 
			finish();
		}
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		accelerationRawPoints = new ArrayList<float[]>();
		magnetometerRawPoints = new ArrayList<float[]>();
		filteredAcceleration = new float[3];
		filteredMagnetometer = new float[3];
		drawingView = new DrawingView(this);
		addContentView(drawingView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(accelListener, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(accelListener, magnetometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		sensorManager.unregisterListener(accelListener);
		super.onPause();
	}

	// invokes win vibration pattern
	public void winVibration() {
		vibrator.vibrate(winVibrationPattern, -1);
	}

	// invokes lose vibration pattern
	public void loseVibration() {
		vibrator.vibrate(loseVibrationPattern, -1);
	}

	// win message dialog
	/* http://www.mkyong.com/android/android-alert-dialog-example/ */
	public void wonGameOption() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Congratulations!");
		alert.setMessage("You survived the black holes! Play again?");

		alert.setPositiveButton("Play Again",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						Intent intent = getIntent();
						finish();
						startActivity(intent);
					}
				});

		alert.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alert.create();
		// show it
		alertDialog.show();
	}

	// lose message dialog
	/* http://www.mkyong.com/android/android-alert-dialog-example/ */
	public void gameOverOption() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("GameOver");
		alert.setMessage("You lost this game. Try again?");

		alert.setPositiveButton("Try Again",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						Intent intent = getIntent();
						finish();
						startActivity(intent);
					}
				});

		alert.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alert.create();
		// show it
		alertDialog.show();
	}
}
