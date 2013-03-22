package pl.przemek.sudoq;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.analytics.tracking.android.EasyTracker;
//import com.google.analytics.tracking.android.GoogleAnalytics;
//import com.google.analytics.tracking.android.Tracker;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "pl.przemek.sudoq.MESSAGE";
	private String difficulty;
	
	//private Tracker myTracker;
	//private GoogleAnalytics myAnalytics;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		this.difficulty = settings.getString("difficulty", "easy");
		/*Button button = (Button) findViewById(R.id.button1);
		button.setEnabled(true);
		try {
			openFileInput("savedSudoq");
		} catch (FileNotFoundException e) {
			button.setEnabled(false);
		}*/
		//myAnalytics = GoogleAnalytics.getInstance(this);
		//myTracker = myAnalytics.getTracker("UA-38473230-1");
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Button button = (Button) findViewById(R.id.button1);
		button.setEnabled(true);
		try {
			openFileInput("savedSudoq");
		} catch (FileNotFoundException e) {
			button.setEnabled(false);
		}
		EasyTracker.getInstance().activityStart(this);
		EasyTracker.getInstance().dispatch();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
		EasyTracker.getInstance().dispatch();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		if (this.difficulty.equals("easy")){
			menu.findItem(R.id.easy).setChecked(true); 
		} else if (this.difficulty.equals("medium")){
			menu.findItem(R.id.medium).setChecked(true);
		} else if (this.difficulty.equals("hard")){
			menu.findItem(R.id.hard).setChecked(true);
		}
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
		switch (item.getItemId()) {
		case R.id.easy:
            item.setChecked(true);
            editor.putString("difficulty", "easy");
            editor.commit();
            this.difficulty = "easy";
			return true;
		case R.id.medium:
            item.setChecked(true);
            editor.putString("difficulty", "medium");
            editor.commit();
            this.difficulty = "medium";
			return true;
		case R.id.hard:
            item.setChecked(true);
            editor.putString("difficulty", "hard");
            editor.commit();
            this.difficulty = "hard";
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void continueGame(View view){
		Intent intent = new Intent(this, SudoqActivity.class);
		String message = "continue";
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}
	
	public void newGame(View view){
		Intent intent = new Intent(this, SudoqActivity.class);
		String message = new String(this.difficulty);
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}

	public void about(View view){
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
}
