package com.carlos.jpct;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.carlos.jpct.RenderView.AXIS;
import com.carlos.jpct.RenderView.CollisionListener;
import com.carlos.jpct.RenderView.OBJ_TYPES;
import com.carlos.jpct.joystick.JoystickMovedListener;
import com.carlos.jpct.joystick.JoystickView;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.BitmapHelper;

public class ViewerActivity extends Activity implements OnTouchListener, CollisionListener, JoystickMovedListener {

	private GLSurfaceView myGLView;
	private RenderView myRenderer;

	private JoystickView joystickView;

	private enum TRANSFORMATION {
		TRANSLATION("Translation"), ROTATION("Rotation");

		private String name;

		private TRANSFORMATION(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	private TRANSFORMATION currentTransformation = TRANSFORMATION.TRANSLATION;

	//collision attributes
	private float initialX;
	private float initialY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		this.createTextures();

		myGLView = (GLSurfaceView) this.findViewById(R.id.surfaceView);
		myRenderer = new RenderView(this);
		myRenderer.setCollisionListener(this);
		myGLView.setRenderer(myRenderer);
		myGLView.setOnTouchListener(this);

		this.joystickView = (JoystickView) this.findViewById(R.id.joystickView);
		this.joystickView.setOnJostickMovedListener(this);

		this.findViewById(R.id.buttonLoadObj2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myRenderer.addObject(ViewerActivity.this, OBJ_TYPES.CAMARO);
			}
		});

		this.findViewById(R.id.buttonLoadObj3).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myRenderer.addObject(ViewerActivity.this, OBJ_TYPES.POLICE_CAR);
			}
		});

		this.findViewById(R.id.buttonLoadObj4).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myRenderer.addObject(ViewerActivity.this, OBJ_TYPES.TRUCK);
			}
		});

		this.findViewById(R.id.buttonMode).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button button = (Button) findViewById(R.id.buttonMode);
				if (button.getText().equals(TRANSFORMATION.ROTATION.toString())) {
					button.setText(TRANSFORMATION.TRANSLATION.toString());
					currentTransformation = TRANSFORMATION.TRANSLATION;
				} else {
					button.setText(TRANSFORMATION.ROTATION.toString());
					currentTransformation = TRANSFORMATION.ROTATION;
				}
			}
		});

		this.findViewById(R.id.buttonHide).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.frameObjectOptions).setVisibility(View.GONE);
				myRenderer.unselectObject();
			}
		});

		this.findViewById(R.id.buttonRemove).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myRenderer.removeObject();
			}
		});

		this.findViewById(R.id.ButtonAxisZPlus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentTransformation == TRANSFORMATION.ROTATION) {
					myRenderer.applyRotation(AXIS.Z, 0.5f);
				} else {
					myRenderer.applyTranslation(0, 0, 10);
				}
			}
		});

		this.findViewById(R.id.ButtonAxisZLess).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentTransformation == TRANSFORMATION.ROTATION) {
					myRenderer.applyRotation(AXIS.Z, -0.5f);
				} else {
					myRenderer.applyTranslation(0, 0, -10);
				}
			}
		});

		this.findViewById(R.id.buttonScalePlus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myRenderer.increaseScale();
			}
		});

		this.findViewById(R.id.buttonScaleLess).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myRenderer.decreaseScale();
			}
		});

	}

	private void createTextures() {
		
		Texture texture = new Texture(BitmapHelper.convert(getResources().getDrawable(R.drawable.watertruck)));
		if(!TextureManager.getInstance().containsTexture("watertruck_texture")){
			TextureManager.getInstance().addTexture("watertruck_texture", texture);
		}

		if(!TextureManager.getInstance().containsTexture("camaro_texture")){
			texture = new Texture(BitmapHelper.convert(getResources().getDrawable(R.drawable.camaro2)));
			TextureManager.getInstance().addTexture("camaro_texture", texture);
		}

		if(!TextureManager.getInstance().containsTexture("police_car_texture")){
			texture = new Texture(BitmapHelper.convert(getResources().getDrawable(R.drawable.policecar)));
			TextureManager.getInstance().addTexture("police_car_texture", texture);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		myGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		myGLView.onResume();
	}

	protected boolean isFullscreenOpaque() {
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			initialX = event.getX();
			initialY = event.getY();
			myRenderer.projectRay((int) event.getX(), (int) event.getY());
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float currentX = event.getX();
			float currentY = event.getY();

			int diffX = (int) (currentX - initialX);
			int diffY = (int) (currentY - initialY);

			if (currentTransformation == TRANSFORMATION.ROTATION) {
				if (diffX > 0) {
					myRenderer.applyRotation(AXIS.X, 0.1f);
				} else if (diffX < 0) {
					myRenderer.applyRotation(AXIS.X, -0.1f);
				}

				if (diffY > 0) {
					myRenderer.applyRotation(AXIS.Y, 0.1f);
				} else if (diffY < 0) {
					myRenderer.applyRotation(AXIS.Y, -0.1f);
				}
			} else if (currentTransformation == TRANSFORMATION.TRANSLATION) {
				myRenderer.applyTranslation(diffX, diffY, 0);
			}
			initialX = currentX;

			initialY = currentY;
		}
		return true;
	}

	@Override
	public void onCollision() {
		this.findViewById(R.id.frameObjectOptions).setVisibility(View.VISIBLE);
	}

	@Override
	public void OnMoved(int pan, int tilt) {
		SimpleVector direction = new SimpleVector(pan * 1.0f, tilt * 1.0f, 0.0f);
		this.myRenderer.moveCamera(direction, 0.3f);
	}

	@Override
	public void OnReleased() {

	}

	@Override
	public void OnReturnedToCenter() {

	}
}