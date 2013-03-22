package pl.przemek.sudoq;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.google.analytics.tracking.android.EasyTracker;

public class SudoqActivity extends Activity {
	
	private SudoqView sudoqView;
	private int[][][] table;	//x,y -> coordinates z -> isInitial
	private int size = 9;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	public int getSize(){
		return size;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public int getTableValue(int i, int j){
		return table[i][j][0];
	}
	
	public void setTableValue(int i, int j, int value){
		this.table[i][j][0] = value;
	}
	
	public boolean isTableValueInitial(int i, int j){
		if(table[i][j][1] == 1 || table[i][j][1] == 3){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isTableValueError(int i, int j){
		if(table[i][j][1] == 2 || table[i][j][1] == 3){
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		String diff = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
		if(diff.equals("continue")){
			table = sudoqLoader();
			EasyTracker.getTracker().sendEvent("Game", "Continued", null, 1L);
		} else {
			table = createTableFromJson(diff);
			EasyTracker.getTracker().sendEvent("Game", "New", diff, 1L);
			sudoqSaver();
		}
		
		sudoqView = new SudoqView(this);
		setContentView(sudoqView);
		LayoutInflater factory = LayoutInflater.from(this);
  		View myView = factory.inflate(R.layout.keypad, null);
  		addContentView(myView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		sudoqView.requestFocus();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
		EasyTracker.getInstance().dispatch();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
		EasyTracker.getInstance().dispatch();
	}

	private void sudoqSaver(){
		try {
			oos = new ObjectOutputStream(openFileOutput("savedSudoq", Context.MODE_PRIVATE));
			oos.writeObject(table);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int[][][] sudoqLoader(){
		try {
			ois = new ObjectInputStream(openFileInput("savedSudoq"));
			int[][][] tmp = (int[][][]) ois.readObject();
			return tmp;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sudoq, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private int[][][] createTableFromJson(String fileName){
		int[][][] table = new int[this.size][this.size][2];
		BufferedReader reader = null;
		try {
			AssetManager assetManager = getAssets();
			reader = new BufferedReader(
					new InputStreamReader(
							assetManager.open(fileName + ".json")));
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			int n;
			while ((n = reader.read(buffer)) != -1){
				writer.write(buffer, 0, n);
			}
			String jsonString = writer.toString();
			JSONObject jsonPuzzles = new JSONObject(jsonString);
			int index;
			Random random = new Random();
			JSONArray jsonPuzzlesArray = jsonPuzzles.getJSONArray("puzzles");
			index = random.nextInt(jsonPuzzlesArray.length());
			JSONArray jsonArray = (JSONArray) jsonPuzzlesArray.get(index);
			for(int i = 0; i < this.size; i++){
				for (int j = 0; j < this.size; j++){
					table[i][j][0] = ((JSONArray)jsonArray.get(i)).getInt(j);
					if(table[i][j][0] != 0){
						table[i][j][1] = 1;
					} else {
						table[i][j][1] = 0;
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return table;
	}
	
	public void keypadClick(View view){
		if(((Button)view).getText().toString().equals("C")){
			sudoqView.saveNumber(0);
		} else {
			sudoqView.saveNumber(Integer.parseInt(((Button)view).getText().toString()));
		}
		sudoqSaver();
	}
	
	public void checkRow(int i){
		for(int k = 0; k < this.size - 1; k++){
			for(int l = k + 1; l < this.size; l++){
				if(table[i][l][0] != 0 && table[i][k][0] == table[i][l][0]){
					if(table[i][k][1] < 2){
						table[i][k][1] += 2;
					}
					if(table[i][l][1] < 2){
						table[i][l][1] += 2;
					}
				}
			}
		}
	}
	
	public void checkColumn(int i){
		for(int k = 0; k < this.size - 1; k++){
			for(int l = k + 1; l < this.size; l++){
				if(table[l][i][0] != 0 && table[k][i][0] == table[l][i][0]){
					if(table[k][i][1] < 2){
						table[k][i][1] += 2;
					}
					if(table[l][i][1] < 2){
						table[l][i][1] += 2;
					}
				}
			}
		}
	}
	
	public void checkBox(int i, int j){
		int smallSize = (int) Math.sqrt((double) this.size);
		int i1 = i / smallSize * smallSize;
		int j1 = j / smallSize * smallSize;
		for(int k = i1; k < i1 + smallSize; k++){
			for(int l = j1; l < j1 + smallSize; l++){
				for(int m = k; m < i1 + smallSize; m++){
					for(int n = j1; n < j1 + smallSize; n++){
						if(!(m == k && n <= l)){
							if(table[m][n][0] != 0 && table[k][l][0] == table[m][n][0]){
								if(table[k][l][1] < 2){
									table[k][l][1] += 2;
								}
								if(table[m][n][1] < 2){
									table[m][n][1] += 2;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void resetErrors(int i, int j){
		for(int k = 0; k < this.size; k++){
			if(table[i][k][1] > 1){
				table[i][k][1] -= 2;
			}
		}
		for(int k = 0; k < this.size; k++){
			if(table[k][j][1] > 1){
				table[k][j][1] -= 2;
			}
		}
		int smallSize = (int) Math.sqrt((double) this.size);
		int i1 = i / smallSize * smallSize;
		int j1 = j / smallSize * smallSize;
		for(int k = i1; k < i1 + smallSize; k++){
			for(int l = j1; l < j1 + smallSize; l++){
				if(table[k][l][1] > 1){
					table[k][l][1] -= 2;
				}
			}
		}
	}
	
	public void resetErrors(){
		for(int i = 0; i < this.size; i++){
			for(int j = 0; j < this.size; j++){
				if(table[i][j][1] > 1){
					table[i][j][1] -= 2;
				}
			}
		}
	}
}
