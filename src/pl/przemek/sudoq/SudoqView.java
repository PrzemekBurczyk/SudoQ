package pl.przemek.sudoq;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.view.MotionEvent;
import android.view.View;

public class SudoqView extends View {

	private final SudoqActivity sudoqActivity;
	
	private float width;    // width of one tile
	//private float height;   // height of one tile
	private int selX;       // X index of selection
	private int selY;       // Y index of selection
	private final Rect selRect = new Rect();
	
	private Paint background;
	private Paint dark;
	private Paint light;
	private Paint selection;
	private Paint foreground;
	private Paint transparent;
	private Paint errorPaint;
	private Paint initialErrorPaint;
	
	public SudoqView(Context context) {
		super(context);
		this.sudoqActivity = (SudoqActivity) context;
		setFocusable(true);
		setFocusableInTouchMode(true);
		background = new Paint();
  		background.setColor(getResources().getColor(R.color.white));
  		dark = new Paint();
  		dark.setColor(getResources().getColor(R.color.black));
  		light = new Paint();
  		light.setColor(getResources().getColor(R.color.grey));
  		selection = new Paint();
  		selection.setColor(getResources().getColor(R.color.selection));
  		transparent = new Paint();
  		transparent.setColor(getResources().getColor(R.color.initial));
  		errorPaint = new Paint();
  		errorPaint.setColor(getResources().getColor(R.color.error));
  		initialErrorPaint = new Paint();
  		initialErrorPaint.setColor(getResources().getColor(R.color.initialError));
  		foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
  		selX = 0;
  		selY = 0;
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w / Float.valueOf(sudoqActivity.getSize());
		//height = h / Float.valueOf(sudoqActivity.getSize());
		getRect(selX, selY, selRect);
		super.onSizeChanged(w, w, oldw, oldh);
	}
	
	private void getRect(int x, int y, Rect rect) {
		rect.set((int) (x * width), (int) (y * width), (int) (x * width + width), (int) (y * width + width));
	}
	
	private Rect getNewRect(int x, int y){
		Rect tmp = new Rect((int) (x * width), (int) (y * width), (int) (x * width + width), (int) (y * width + width));
		return tmp;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, getWidth(), getWidth(), background);
  		for (int i = 0; i < sudoqActivity.getSize(); i++) {
  			canvas.drawLine(0, i * width, getWidth(), i * width, light);
  			canvas.drawLine(i * width, 0, i * width, getWidth(), light);
  		}
  		for (int i = 0; i < sudoqActivity.getSize(); i++) {
  			if (i % Math.sqrt(sudoqActivity.getSize()) != 0)
  				continue;
  			canvas.drawLine(0, i * width, getWidth(), i * width, dark);
  			canvas.drawLine(0, i * width - 1, getWidth(), i * width - 1, dark);		//bold horizontal
		    canvas.drawLine(i * width, 0, i * width, getWidth(), dark);
		    canvas.drawLine(i * width - 1, 0, i * width - 1, getWidth(), dark);			//bold vertical
  		}
  		canvas.drawLine(0, sudoqActivity.getSize() * width, getWidth(), sudoqActivity.getSize() * width, dark);  //border on bottom
  		foreground.setColor(getResources().getColor(R.color.black));
  		foreground.setStyle(Style.FILL);
  		foreground.setTextSize(width * 0.75f);
  		foreground.setTextAlign(Paint.Align.CENTER);
  		FontMetrics fm = foreground.getFontMetrics();
  		// Centering in X: use alignment (and X at midpoint)
  		float x = width / 2;
  		// Centering in Y: measure ascent/descent first
  		float y = width / 2 - (fm.ascent + fm.descent) / 2;
  		for (int i = 0; i < sudoqActivity.getSize(); i++) {
  			for (int j = 0; j < sudoqActivity.getSize(); j++) {
  				if(this.sudoqActivity.isTableValueInitial(i, j)){
					canvas.drawRect(getNewRect(j, i), transparent);
				}
  				if(this.sudoqActivity.getTableValue(i, j) != 0){
  					canvas.drawText(Integer.valueOf(this.sudoqActivity.getTableValue(i, j)).toString(), j * width + x, i * width + y, foreground);
  				}
  				if(this.sudoqActivity.isTableValueError(i, j)){
  					if(this.sudoqActivity.isTableValueInitial(i, j)){
  						canvas.drawRect(getNewRect(j, i), initialErrorPaint);
  					} else {
  	  					canvas.drawRect(getNewRect(j, i), errorPaint);
  					}
  				}
  			}
  		}
  		// Draw the selection
  		canvas.drawRect(selRect, selection);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN || event.getY() > getWidth())
			return super.onTouchEvent(event);
		
		select((int) (event.getX() / width), (int) (event.getY() / width));
		//this.sudoqActivity.showKeypadOrError(selX, selY);
		return true;
	}
	
	private void select(int x, int y) {
		invalidate(selRect);
		selX = Math.min(Math.max(x, 0), sudoqActivity.getSize() - 1);
      	selY = Math.min(Math.max(y, 0), sudoqActivity.getSize() - 1);
      	getRect(selX, selY, selRect);
      	invalidate(selRect);
	}
	
	public void saveNumber(int value){		//saves number to current selection and checks for errors
		if(!this.sudoqActivity.isTableValueInitial(selY, selX)){
			this.sudoqActivity.setTableValue(selY, selX, value);
			checkForErrors();
			invalidate();
		}
	}
	
	public void checkForErrors(){
		this.sudoqActivity.resetErrors();
		for(int i = 0; i < this.sudoqActivity.getSize(); i++){
			this.sudoqActivity.checkRow(i);
			this.sudoqActivity.checkColumn(i);
		}
		int smallSize = (int) Math.sqrt((double) this.sudoqActivity.getSize());
		for(int i = 0; i < smallSize; i++){
			for(int j = 0; j < smallSize; j++){
				this.sudoqActivity.checkBox(i * smallSize, j * smallSize);
			}
		}
	}
	
}
