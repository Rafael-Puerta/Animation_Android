package com.rafael_puerta.first_animation;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;


public class firstAnimation extends ApplicationAdapter implements ApplicationListener {

	// Constant rows and columns of the sprite sheet
	private static final int FRAME_COLS = 3, FRAME_ROWS = 1;
	private WebSocket socket;
	private String address = "localhost";
	private int port = 8888,posX=50;
	private Rectangle up, down, left, right, fire;
	private int SCR_WIDTH=800,SCR_HEIGHT=480;
	private OrthographicCamera camera;
	protected  int IDLE=0, UP=1, DOWN=2, LEFT=3, RIGHT=4;
	private float lastSend = 0;
	// Objects used
	Animation<TextureRegion> walkAnimation; // Must declare frame type (TextureRegion)
	Texture walkSheet;
	SpriteBatch spriteBatch;

	// A variable for tracking elapsed time for the animation
	float stateTime;

	@Override
	public void create() {
		camera=new OrthographicCamera();
		camera.setToOrtho(false,800,480);
		// Load the sprite sheet as a Texture
		walkSheet = new Texture(Gdx.files.internal("beatRecortado.png"));
		// facilities per calcular el "touch"
		up = new Rectangle(0, SCR_HEIGHT*2/3, SCR_WIDTH, SCR_HEIGHT/3);
		down = new Rectangle(0, 0, SCR_WIDTH, SCR_HEIGHT/3);
		left = new Rectangle(0, 0, SCR_WIDTH/3, SCR_HEIGHT);
		right = new Rectangle(SCR_WIDTH*2/3, 0, SCR_WIDTH/3, SCR_HEIGHT);
		//TEST WEBSOCKET
		if( Gdx.app.getType()== Application.ApplicationType.Android )
			// en Android el host és accessible per 10.0.2.2
			address = "10.0.2.2";
		socket = WebSockets.newSocket(WebSockets.toWebSocketUrl(address, port));
		socket.setSendGracefully(false);
		socket.addListener((WebSocketListener) new MyWSListener());
		socket.connect();
		socket.send("Enviar dades");
		//END TEST
		// Use the split utility method to create a 2D array of TextureRegions. This is
		// possible because this sprite sheet contains frames of equal size and they are
		// all aligned.
		TextureRegion[][] tmp = TextureRegion.split(walkSheet,
				walkSheet.getWidth() / FRAME_COLS,
				walkSheet.getHeight() / FRAME_ROWS);

		// Place the regions into a 1D array in the correct order, starting from the top
		// left, going across first. The Animation constructor requires a 1D array.
		TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		int index = 0;
		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				walkFrames[index++] = tmp[i][j];
			}
		}

		// Initialize the Animation with the frame interval and array of frames
		walkAnimation = new Animation<TextureRegion>(0.105f, walkFrames);

		// Instantiate a SpriteBatch for drawing and reset the elapsed animation
		// time to 0
		spriteBatch = new SpriteBatch();
		stateTime = 0f;

	}

	@Override
	public void render() {

		int move= virtual_joystick_control();
		if(move==LEFT){
			posX += -1;
		}else if (move==RIGHT){
			posX += 1;
		}

		if( stateTime-lastSend > 1.0f ) {
			lastSend = stateTime;
			socket.send("Enviar dades");
		}

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear screen
		stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time

		// Get current frame of animation for the current stateTime
		TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true);
		spriteBatch.begin();
		spriteBatch.draw(currentFrame, posX, 50,400,400); // Draw current frame at (50, 50)
		spriteBatch.end();
	}

	@Override
	public void dispose() { // SpriteBatches and Textures must always be disposed
		spriteBatch.dispose();
		walkSheet.dispose();
	}
	class MyWSListener implements WebSocketListener {

		@Override
		public boolean onOpen(WebSocket webSocket) {
			System.out.println("Opening...");
			return false;
		}

		@Override
		public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
			System.out.println("Closing...");
			return false;
		}

		@Override
		public boolean onMessage(WebSocket webSocket, String packet) {
			System.out.println("Message:");
			return false;
		}

		@Override
		public boolean onMessage(WebSocket webSocket, byte[] packet) {
			System.out.println("Message:");
			return false;
		}

		@Override
		public boolean onError(WebSocket webSocket, Throwable error) {
			System.out.println("ERROR:"+error.toString());
			return false;
		}
	}
	protected int virtual_joystick_control() {
		// iterar per multitouch
		// cada "i" és un possible "touch" d'un dit a la pantalla
		for(int i=0;i<10;i++)
			if (Gdx.input.isTouched(i)) {
				Vector3 touchPos = new Vector3();
				touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
				// traducció de coordenades reals (depen del dispositiu) a 800x480
				camera.unproject(touchPos);
				if (up.contains(touchPos.x, touchPos.y)) {
					return UP;
				} else if (down.contains(touchPos.x, touchPos.y)) {
					return DOWN;
				} else if (left.contains(touchPos.x, touchPos.y)) {
					return LEFT;
				} else if (right.contains(touchPos.x, touchPos.y)) {
					return RIGHT;
				}
			}
		return IDLE;
	}
}
