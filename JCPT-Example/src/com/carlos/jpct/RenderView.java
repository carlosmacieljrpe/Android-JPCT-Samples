package com.carlos.jpct;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.threed.jpct.Camera;
import com.threed.jpct.CollisionEvent;
import com.threed.jpct.CollisionListener;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class RenderView implements GLSurfaceView.Renderer, CollisionListener {
	private static final long serialVersionUID = 1L;

	public enum OBJ_TYPES {
		POLICE_CAR, TRUCK, CAMARO, TRACTOR
	}

	public enum AXIS {
		X, Y, Z
	}

	public interface CollisionListener {
		void onCollision();
	}

	private CollisionListener listener;

	private World myWorld;
	private FrameBuffer frameBuffer;

	private RGBColor backgroundBlue = new RGBColor(50, 50, 100);

	private Object3D selectedObj;
	private boolean fromUserAction;
	private boolean hasObjectsCollision;

	public RenderView(Context context) {
		myWorld = new World();
		myWorld.setAmbientLight(25, 25, 25);

		Light light = new Light(myWorld);
		light.setIntensity(250, 250, 250);
		light.setPosition(new SimpleVector(0, 0, -15));

		Camera cam = myWorld.getCamera();
		cam.moveCamera(Camera.CAMERA_MOVEOUT, 20);
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		if (frameBuffer != null) {
			frameBuffer.dispose();
		}
		frameBuffer = new FrameBuffer(gl, w, h);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}

	public void onDrawFrame(GL10 gl) {
		frameBuffer.clear(backgroundBlue);
		myWorld.renderScene(frameBuffer);
		myWorld.draw(frameBuffer);
		frameBuffer.display();
	}

	public void projectRay(int x, int y) {
		fromUserAction = true;
		SimpleVector ray = Interact2D.reproject2D3DWS(myWorld.getCamera(), frameBuffer, x, y).normalize();
		//this method below trigger the collision listener (onCollision)
		myWorld.calcMinDistanceAndObject3D(myWorld.getCamera().getPosition(), ray, 10000F);
		fromUserAction = false;
	}

	public void removeObject() {
		if (selectedObj != null) {
			myWorld.removeObject(selectedObj);
			selectedObj = null;
		}
	}

	public void addObject(Context context, OBJ_TYPES type) {

		Object3D newObject = null;
		if (type.ordinal() == OBJ_TYPES.TRUCK.ordinal()) {
			try {
				Object3D[] objectsArray2 = Loader.loadOBJ(context.getResources().getAssets().open("watertruck.obj"), context.getResources()
						.getAssets().open("watertruck.mtl"), 1.0f);
				newObject = Object3D.mergeAll(objectsArray2);
				newObject.setTexture("watertruck_texture");
				newObject.setOrigin(new SimpleVector(0, 0, 350));
				newObject.rotateZ(160.0f);
				newObject.setName("watertruck.obj");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (type.ordinal() == OBJ_TYPES.CAMARO.ordinal()) {
			try {
				Object3D[] objectsArray2 = Loader.loadOBJ(context.getResources().getAssets().open("camaro2.obj"), context.getResources()
						.getAssets().open("camaro2.mtl"), 1.0f);
				newObject = Object3D.mergeAll(objectsArray2);
				newObject.setTexture("camaro_texture");
				newObject.setName("camaro.obj");
				newObject.setOrigin(new SimpleVector(0, 0, 50));
				newObject.rotateY(160.0f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (type.ordinal() == OBJ_TYPES.POLICE_CAR.ordinal()) {
			try {
				Object3D[] objectsArray2 = Loader.loadOBJ(context.getResources().getAssets().open("policecar.obj"), context.getResources()
						.getAssets().open("policecar.mtl"), 1.0f);
				newObject = Object3D.mergeAll(objectsArray2);
				newObject.setTexture("police_car_texture");
				newObject.setName("police_car.obj");
				newObject.setOrigin(new SimpleVector(0, 0, 300));
				newObject.rotateZ(160.0f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (type.ordinal() == OBJ_TYPES.TRACTOR.ordinal()) {
			try {
				Object3D[] objectsArray = Loader.loadOBJ(context.getResources().getAssets().open("tractor.obj"), context.getResources()
						.getAssets().open("tractor.mtl"), 1.0f);
				newObject = Object3D.mergeAll(objectsArray);
				newObject.setTexture("tractor_texture");
				newObject.setName("tractor.obj");
				newObject.setOrigin(new SimpleVector(0, 0, 300));
				newObject.rotateZ(160.0f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		newObject.strip();
		newObject.build();
		newObject.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS | Object3D.COLLISION_CHECK_SELF);
		newObject.setCollisionOptimization(true);
		newObject.addCollisionListener(this);

		myWorld.addObject(newObject);
	}

	public void moveCamera(int mode) {
		myWorld.getCamera().moveCamera(mode, 10);
	}

	public void moveCamera(SimpleVector direction, float velocity) {
		myWorld.getCamera().moveCamera(direction, velocity);
	}

	public void applyTranslation(float incX, float incY, float incZ) {
		if (this.selectedObj != null) {
			SimpleVector objOrigin = this.selectedObj.getOrigin();
			float projectionFix = Math.max((-3 * myWorld.getCamera().getPosition().z / 2) - (objOrigin.z / 11), 1);

			incX = incX / projectionFix;
			incY = incY / projectionFix;

			float[] bBox = this.selectedObj.getMesh().getBoundingBox();
			this.selectedObj.checkForCollisionEllipsoid(new SimpleVector(incX, incY, incZ), new SimpleVector((bBox[1] - bBox[0]) / 2,
					(bBox[3] - bBox[2]) / 2, (bBox[5] - bBox[4]) / 2), 5);

			this.selectedObj.translate(incX, incY, incZ);
			if (hasObjectsCollision) {
				this.selectedObj.setTransparency(Object3D.TRANSPARENCY_MODE_ADD);
				hasObjectsCollision = false;
			} else {
				this.selectedObj.setTransparency(-1);
			}
		}
	}

	public void applyRotation(AXIS axis, float inc) {
		if (this.selectedObj != null) {
			if (axis == AXIS.X) {
				this.selectedObj.rotateX(inc);
			} else if (axis == AXIS.Y) {
				this.selectedObj.rotateY(inc);
			} else {
				this.selectedObj.rotateZ(inc);
			}
		}
	}

	public void increaseScale() {
		if (this.selectedObj != null) {
			this.selectedObj.setScale(3);
		}
	}

	public void decreaseScale() {
		if (this.selectedObj != null) {
			this.selectedObj.setScale(1);
		}
	}

	public void unselectObject() {
		this.selectedObj = null;
	}

	@Override
	public void collision(CollisionEvent colEvent) {
		if (fromUserAction) {
			this.selectedObj = colEvent.getObject();
			if (this.listener != null) {
				this.listener.onCollision();
			}
		} else {
			this.hasObjectsCollision = true;
		}
	}

	@Override
	public boolean requiresPolygonIDs() {
		return false;
	}

	public void setCollisionListener(CollisionListener listener) {
		this.listener = listener;
	}
}
