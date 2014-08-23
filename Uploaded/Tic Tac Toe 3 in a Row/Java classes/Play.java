package com.tic.tac.toe.three.row;

import java.util.Random;

import com.tictactoe.three.row.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class Play extends Activity {

	TicArea ticArea;
	SharedPreferences mSharedPreferences;
	private static final String PREFS = "prefs";
	private static final String PREF_GAMEMODE = "name";
	private static final String PREF_BOARD = "board"; //was planning to save the board, but nah
	
	/* float variables: values different for each device!!!
	 * WIDTH, HEIGHT: screen dimensions
	 * SQUARE, LINEWIDTH: board dimensions
	 * TITLEBAR_HEIGHT: used to re-calculate button dimensions, prevents buttons from leaking out of the screen
	 * BOARD_AREA_HEIGHT: distance between gameModeTextRect and clear/changemode buttons, used to center board on screen
	 * GAP_TOP_TO_BOARD: distance between gameModeTextRect and board lines
	 * 
	 */
	float WIDTH, HEIGHT, SQUARE, LINEWIDTH, TITLEBAR_HEIGHT, BOARD_AREA_HEIGHT, GAP_TOP_TO_BOARD;
	int[] board;
	int emptyCells;
	int turn;
	String player1Mark;
	String player2Mark;
	String GAMEMODE; //"2P" or "AI"
	boolean GAMEOVER;
	AI ai;
	boolean aiThinking; //touching rectangles does nothing when this is set to TRUE (ai is thinking his move) (only required if there is Thread.sleep(1000) in aiGamePlay runnable)
	Handler myHandler = new Handler();
	private Runnable aiGamePlay = new Runnable() {
		
		@Override
		public void run() {
			if (GAMEMODE.equals("AI") && !GAMEOVER && turn == 2) {
				aiThinking = true; //not used if the code beneath is commented out
//				ticArea.invalidate();
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//				}
				int move = ai.generateMove(board);
				Log.d("AI moved here:", "" + move);
				rectangles[move].status = player2Mark;
				for (MyRect r : rectangles) {
					r.color = Color.TRANSPARENT;
				}
				rectangles[move].color = Color.YELLOW;
				board[move] = 2;
				emptyCells--;
				gameOver(checkWin());
				turn = 1;
				ticArea.invalidate();
			}
			myHandler.postDelayed(this, 0);
		}
	};
	Random rnd = new Random();
	
	Bitmap o;
	Bitmap x;
	Paint linePnt = new Paint();
	Paint rectPnt = new Paint();
	Paint textPnt = new Paint();
	MyRect[] rectangles;
	MyRect r1, r2, r3, r4, r5, r6, r7, r8, r9, gameModeTextRect, clearButton, changeModeButton;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//hide the ActionBar
		if(Build.VERSION.SDK_INT >= 11)
			getActionBar().hide();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //fix vertical screen
		Display display = getWindowManager().getDefaultDisplay();
		WIDTH = display.getWidth();
		HEIGHT = display.getHeight();
		SQUARE = WIDTH / 5;
		LINEWIDTH = SQUARE / 10;
		//get height of the title bar (title bar height must be considered in calculating exact dimensions of rects)
		TITLEBAR_HEIGHT = 0;
	    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	    if (resourceId > 0) {
	        TITLEBAR_HEIGHT = getResources().getDimensionPixelSize(resourceId);
	        Toast.makeText(this, "title bar: " + TITLEBAR_HEIGHT, Toast.LENGTH_SHORT).show();
	    }
	    
		mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
		GAMEMODE = mSharedPreferences.getString(PREF_GAMEMODE, "AI");
		ai = new AI();
		aiThinking = false;
		
		o = BitmapFactory.decodeResource(getResources(), R.drawable.o);
		x = BitmapFactory.decodeResource(getResources(), R.drawable.x);
		resetBoard();
		ticArea = new TicArea(this);
		setContentView(ticArea);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		myHandler.postDelayed(aiGamePlay, 0);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		myHandler.removeCallbacks(aiGamePlay);
	}

	public void resetBoard() {
		gameModeTextRect = new MyRect(LINEWIDTH, LINEWIDTH, WIDTH - LINEWIDTH, SQUARE - LINEWIDTH);
		gameModeTextRect.color = Color.GRAY;
		clearButton = new MyRect(SQUARE/2, HEIGHT - SQUARE - 2*LINEWIDTH - TITLEBAR_HEIGHT, WIDTH / 2 - LINEWIDTH, HEIGHT - 2*LINEWIDTH - TITLEBAR_HEIGHT);
		clearButton.color = Color.DKGRAY;
		changeModeButton = new MyRect(WIDTH / 2 + LINEWIDTH, HEIGHT - SQUARE - 2*LINEWIDTH - TITLEBAR_HEIGHT, WIDTH - SQUARE/2, HEIGHT - 2*LINEWIDTH - TITLEBAR_HEIGHT);
		changeModeButton.color = Color.DKGRAY;
		
		BOARD_AREA_HEIGHT = clearButton.top - gameModeTextRect.bottom;
	    Toast.makeText(this, "Board area: " + BOARD_AREA_HEIGHT, Toast.LENGTH_SHORT).show();
		GAP_TOP_TO_BOARD = (BOARD_AREA_HEIGHT - 3*SQUARE) / 2;
		Toast.makeText(this, "Board to top area: " + GAP_TOP_TO_BOARD, Toast.LENGTH_SHORT).show();
		
		r1 = new MyRect(SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD, 2*SQUARE - LINEWIDTH/2, gameModeTextRect.bottom + SQUARE - LINEWIDTH/2 + GAP_TOP_TO_BOARD);
		r2 = new MyRect(2*SQUARE + LINEWIDTH/2, gameModeTextRect.bottom + GAP_TOP_TO_BOARD, 3*SQUARE - LINEWIDTH/2, gameModeTextRect.bottom + SQUARE - LINEWIDTH/2 + GAP_TOP_TO_BOARD);
		r3 = new MyRect(3*SQUARE + LINEWIDTH/2, gameModeTextRect.bottom + GAP_TOP_TO_BOARD, 4*SQUARE, gameModeTextRect.bottom + SQUARE - LINEWIDTH/2 + GAP_TOP_TO_BOARD);
		r4 = new MyRect(SQUARE, gameModeTextRect.bottom + SQUARE + LINEWIDTH/2 + GAP_TOP_TO_BOARD, 2*SQUARE - LINEWIDTH/2, gameModeTextRect.bottom + 2*SQUARE - LINEWIDTH/2 + GAP_TOP_TO_BOARD);
		r5 = new MyRect(2*SQUARE + LINEWIDTH/2, gameModeTextRect.bottom + SQUARE + LINEWIDTH/2 + GAP_TOP_TO_BOARD, 3*SQUARE - LINEWIDTH/2, gameModeTextRect.bottom + 2*SQUARE - LINEWIDTH/2 + GAP_TOP_TO_BOARD);
		r6 = new MyRect(3*SQUARE + LINEWIDTH/2, gameModeTextRect.bottom + SQUARE + LINEWIDTH/2 + GAP_TOP_TO_BOARD, 4*SQUARE, gameModeTextRect.bottom + 2*SQUARE - LINEWIDTH/2 + GAP_TOP_TO_BOARD);
		r7 = new MyRect(SQUARE, gameModeTextRect.bottom + 2*SQUARE + LINEWIDTH/2 + GAP_TOP_TO_BOARD, 2*SQUARE - LINEWIDTH/2, gameModeTextRect.bottom + 3*SQUARE + GAP_TOP_TO_BOARD);
		r8 = new MyRect(2*SQUARE + LINEWIDTH/2, gameModeTextRect.bottom + 2*SQUARE + LINEWIDTH/2 + GAP_TOP_TO_BOARD, 3*SQUARE - LINEWIDTH/2, gameModeTextRect.bottom + 3*SQUARE + GAP_TOP_TO_BOARD);
		r9 = new MyRect(3*SQUARE + LINEWIDTH/2, gameModeTextRect.bottom + 2*SQUARE + LINEWIDTH/2 + GAP_TOP_TO_BOARD, 4*SQUARE, gameModeTextRect.bottom + 3*SQUARE + GAP_TOP_TO_BOARD);
		
		rectangles = new MyRect[]{r1, r2, r3, r4, r5, r6, r7, r8, r9};
		board = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
		turn = rnd.nextInt(2) + 1; //1 or 2
		if(rnd.nextInt(2) + 1 == 1) {
			player1Mark = "O";
			player2Mark = "X";
		} else {
			player1Mark = "X";
			player2Mark = "O";
		}
		GAMEOVER = false;
		emptyCells = 9;
	}

	public int checkWin() {
		GAMEOVER = true;
		if(board[0] == turn && board[1] == turn && board[2] == turn) //check 3 lines across
			return 1;
		if(board[3] == turn && board[4] == turn && board[5] == turn)
			return 2;
		if(board[6] == turn && board[7] == turn && board[8] == turn)
			return 3;
		if(board[0] == turn && board[3] == turn && board[6] == turn) //check 3 lines down
			return 4;
		if(board[1] == turn && board[4] == turn && board[7] == turn)
			return 5;
		if(board[2] == turn && board[5] == turn && board[8] == turn)
			return 6;
		if(board[0] == turn && board[4] == turn && board[8] == turn) //check diagonals
			return 7;
		if(board[2] == turn && board[4] == turn && board[6] == turn)
			return 8;
		if(emptyCells == 0) //if nobody won but all cells are filled
			return 9;
		GAMEOVER = false;
		return 0; //game not finished
	}
	
	public void gameOver(int checkWin) {
		if(checkWin == 0)
			return;
		switch (checkWin) {
		case 1:
			r1.color = Color.RED;
			r2.color = Color.RED;
			r3.color = Color.RED;
			break;
		case 2:
			r4.color = Color.RED;
			r5.color = Color.RED;
			r6.color = Color.RED;
			break;
		case 3:
			r7.color = Color.RED;
			r8.color = Color.RED;
			r9.color = Color.RED;
			break;
		case 4:
			r1.color = Color.RED;
			r4.color = Color.RED;
			r7.color = Color.RED;
			break;
		case 5:
			r2.color = Color.RED;
			r5.color = Color.RED;
			r8.color = Color.RED;
			break;
		case 6:
			r3.color = Color.RED;
			r6.color = Color.RED;
			r9.color = Color.RED;
			break;
		case 7:
			r1.color = Color.RED;
			r5.color = Color.RED;
			r9.color = Color.RED;
			break;
		case 8:
			r3.color = Color.RED;
			r5.color = Color.RED;
			r7.color = Color.RED;
			break;
		default:
			break;
		}
		Intent winIntent = new Intent(this, WinDialog.class);
		if(checkWin == 9)
			winIntent.putExtra("turn", 999); //full but no wins
		else
			winIntent.putExtra("turn", turn); //someone won the game
		winIntent.putExtra("player1Mark", player1Mark);
		startActivity(winIntent);
	}
	
	class TicArea extends View implements OnTouchListener {

		public TicArea(Context context) {
			super(context);
			this.setOnTouchListener(this);
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			int action = event.getAction();
			
			//surf
			if(action == MotionEvent.ACTION_MOVE) {
				for (int i = 0; i < 9; i++) {
					if(rectangles[i].contains((int)x, (int)y) && board[i] == 0)
						rectangles[i].color = Color.GRAY; //hover on rect
					else
						rectangles[i].color = Color.TRANSPARENT; //move away
					invalidate();
				}
				if(clearButton.contains((int)x, (int)y)) {
					clearButton.color = Color.GRAY;
					invalidate();
				} else {
					clearButton.color = Color.DKGRAY;
					invalidate();
				}
				if(changeModeButton.contains((int)x, (int)y)) {
					changeModeButton.color = Color.GRAY;
					invalidate();
				} else {
					changeModeButton.color = Color.DKGRAY;
					invalidate();
				}
			}
			//select
			if(action == MotionEvent.ACTION_UP) {
				if (!GAMEOVER) {
					for (int i = 0; i < 9; i++) {
						if (rectangles[i].contains((int) x, (int) y)) {
							rectangles[i].color = Color.TRANSPARENT;
							if (turn == 1 && board[i] == 0) { //if player 1's turn
								rectangles[i].status = player1Mark;
								board[i] = 1;
								emptyCells--;
								gameOver(checkWin());
								turn = 2;
								myHandler.postDelayed(aiGamePlay, 0);
							} else if (turn == 2 && board[i] == 0) { //if player 2's turn
								rectangles[i].status = player2Mark;
								board[i] = 2;
								emptyCells--;
								gameOver(checkWin());
								turn = 1;
								myHandler.postDelayed(aiGamePlay, 0);
							}
						}
					}
				}
				//clear button
				clearButton.color = Color.DKGRAY;
				if(clearButton.contains((int)x, (int)y))
					resetBoard();
				//change mode button
				changeModeButton.color = Color.DKGRAY;
				if(changeModeButton.contains((int)x, (int)y)) {
					if(GAMEMODE.equals("2P"))
						GAMEMODE = "AI";
					else
						GAMEMODE = "2P";
					//save recent game mode
					SharedPreferences.Editor e = mSharedPreferences.edit();
					e.putString(PREF_GAMEMODE, GAMEMODE);
					e.commit(); //don't forget to commit!
					
					resetBoard();
				}
				invalidate();
			}
			return true;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(Color.LTGRAY);
			linePnt.setColor(Color.DKGRAY);
			linePnt.setAntiAlias(true);
			linePnt.setStrokeWidth(LINEWIDTH);
			linePnt.setDither(true);
			linePnt.setStyle(Style.STROKE);
			linePnt.setStrokeJoin(Paint.Join.ROUND);
			linePnt.setStrokeCap(Paint.Cap.ROUND);
			
			canvas.drawLine(2*SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD, 2*SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD + 3*SQUARE, linePnt);
			canvas.drawLine(3*SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD, 3*SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD + 3*SQUARE, linePnt);
			canvas.drawLine(SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD + SQUARE, 4*SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD + SQUARE, linePnt);
			canvas.drawLine(SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD + 2*SQUARE, 4*SQUARE, gameModeTextRect.bottom + GAP_TOP_TO_BOARD + 2*SQUARE, linePnt);
			
			for (MyRect r : rectangles) {
				rectPnt.setColor(r.color);
				canvas.drawRect(r, rectPnt);
				
				if(r.status.equals("O")) {
					canvas.drawBitmap(o, r.centerX() - o.getWidth() / 2, r.centerY() - o.getHeight() / 2, linePnt);
				} else if(r.status.equals("X")) {
					canvas.drawBitmap(x, r.centerX() - o.getWidth() / 2, r.centerY() - o.getHeight() / 2, linePnt);
				}
			}
			//set text pnt
			textPnt.setColor(Color.WHITE);
			textPnt.setStrokeWidth(LINEWIDTH/5);
			
			//game mode text rect
			rectPnt.setColor(gameModeTextRect.color);
			canvas.drawRoundRect(gameModeTextRect, SQUARE/4, SQUARE/4, rectPnt);
			textPnt.setTextSize(SQUARE - 2*LINEWIDTH);
			canvas.drawText(GAMEMODE + " mode", gameModeTextRect.left + SQUARE/4, gameModeTextRect.bottom - LINEWIDTH, textPnt);
			
			//clear button
			rectPnt.setColor(clearButton.color);
			canvas.drawRoundRect(clearButton, SQUARE/3, SQUARE/3, rectPnt);
			textPnt.setTextSize(SQUARE/3 * 2);
			canvas.drawText("Clear", clearButton.left + clearButton.width() / 8, clearButton.bottom - clearButton.height() / 8, textPnt);
			
			//change mode button
			rectPnt.setColor(changeModeButton.color);
			canvas.drawRoundRect(changeModeButton, SQUARE/3, SQUARE/3, rectPnt);
			if(GAMEMODE.equals("2P"))
				canvas.drawText("vs AI", changeModeButton.left + changeModeButton.width() / 8, changeModeButton.bottom - changeModeButton.height() / 8, textPnt);
			else
				canvas.drawText("2P", changeModeButton.left + changeModeButton.width() / 8, changeModeButton.bottom - changeModeButton.height() / 8, textPnt);
		}
	}
}