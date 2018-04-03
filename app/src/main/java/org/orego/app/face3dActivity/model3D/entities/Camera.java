package org.orego.app.face3dActivity.model3D.entities;

import android.opengl.Matrix;
import android.util.Log;

public class Camera {

	public float xPos, yPos; // Camera position.
	public float zPos;
	public float xView, yView, zView; // Look at position.
	public float xUp, yUp, zUp; // Up direction.


	private final BoundingBox boundingBox = new BoundingBox("scene",-20,20,-20,20,-20,20);

	private float[] matrix = new float[16];
	private float[] buffer = new float[12 + 12 + 16 + 16];
	private long animationCounter;
	private Object[] lastAction;
	private boolean changed = false;

	public Camera() {
		this(0, 0, 6, 0, 0, -1, 0, 1, 0);
	}

	private Camera(float xPos, float yPos, float zPos, float xView, float yView, float zView, float xUp, float yUp,
				   float zUp) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.zPos = zPos;
		this.xView = xView;
		this.yView = yView;
		this.zView = zView;
		this.xUp = xUp;
		this.yUp = yUp;
		this.zUp = zUp;
	}



	public synchronized void animate(){
		if (lastAction == null || animationCounter == 0){
			lastAction = null;
			animationCounter = 100;
			return;
		}
		String method = (String) lastAction[0];
		if (method.equals("translate")){
			float dX = (Float) lastAction[1];
			float dY = (Float) lastAction[2];
			translateCameraImpl(dX*animationCounter/100, dY*animationCounter/100);
		} else if (method.equals("rotate")){
			float rotZ = (Float)lastAction[1];
			RotateImpl(rotZ/100*animationCounter);
		}
		animationCounter--;
	}

	public synchronized void MoveCameraZ(float direction){
		if (direction == 0) return;
		MoveCameraZImpl(direction);
		lastAction = new Object[]{"zoom",direction};
	}
	private void MoveCameraZImpl(float direction) {

		float xLookDirection, yLookDirection, zLookDirection;


		xLookDirection = xView - xPos;
		yLookDirection = yView - yPos;
		zLookDirection = zView - zPos;

		float dp = Matrix.length(xLookDirection, yLookDirection, zLookDirection);
		xLookDirection /= dp;
		yLookDirection /= dp;
		zLookDirection /= dp;

		UpdateCamera(xLookDirection, yLookDirection, zLookDirection, direction);
	}

	private void UpdateCamera(float xDir, float yDir, float zDir, float dir) {

		Matrix.setIdentityM(matrix, 0);
		Matrix.translateM(matrix, 0, xDir * dir, yDir * dir, zDir * dir);

		Matrix.multiplyMV(buffer, 0, matrix, 0, getLocationVector(), 0);
		Matrix.multiplyMV(buffer, 4, matrix, 0, getLocationViewVector(), 0);
		Matrix.multiplyMV(buffer, 8, matrix, 0, getLocationUpVector(), 0);

		if (isOutOfBounds(buffer)) return;

		xPos = buffer[0] / buffer[3];
		yPos = buffer[1] / buffer[3];
		zPos = buffer[2] / buffer[3];
		xView = buffer[4] / buffer[7];
		yView = buffer[5] / buffer[7];
		zView = buffer[6] / buffer[7];
		xUp = buffer[8] / buffer[11];
		yUp = buffer[9] / buffer[11];
		zUp = buffer[10] / buffer[11];

		pointViewToOrigin();

		setChanged(true);
	}

	private void pointViewToOrigin(){
		xView = -xPos;
		yView = -yPos;
		zView = -zPos;
		float length = Matrix.length(xView, yView, zView);
		xView /= length;
		yView /= length;
		zView /= length;
	}

	private boolean isOutOfBounds(float[] buffer) {
		if (boundingBox.outOfBound(buffer[0] / buffer[3],buffer[1] / buffer[3],buffer[2] / buffer[3])){
			Log.i("Camera", "Out of scene bounds");
			return true;
		}
		return false;
	}

	public synchronized void translateCamera(float dX, float dY) {
		Log.d("Camera","translate:"+dX+","+dY);
		if (dX == 0 && dY == 0) return;
		translateCameraImpl(dX, dY);
		lastAction = new Object[]{"translate",dX, dY};
	}

	private void translateCameraImpl(float dX, float dY) {
		float vlen;

		float xLook, yLook, zLook;
		xLook = xView - xPos;
		yLook = yView - yPos;
		zLook = zView - zPos;
		vlen = Matrix.length(xLook, yLook, zLook);
		xLook /= vlen;
		yLook /= vlen;
		zLook /= vlen;

		float xArriba, yArriba, zArriba;
		xArriba = xUp - xPos;
		yArriba = yUp - yPos;
		zArriba = zUp - zPos;

		vlen = Matrix.length(xArriba, yArriba, zArriba);
		xArriba /= vlen;
		yArriba /= vlen;
		zArriba /= vlen;

		float xRight, yRight, zRight;
		xRight = (yLook * zArriba) - (zLook * yArriba);
		yRight = (zLook * xArriba) - (xLook * zArriba);
		zRight = (xLook * yArriba) - (yLook * xArriba);

		vlen = Matrix.length(xRight, yRight, zRight);
		xRight /= vlen;
		yRight /= vlen;
		zRight /= vlen;

		xArriba = (yRight * zLook) - (zRight * yLook);
		yArriba = (zRight * xLook) - (xRight * zLook);
		zArriba = (xRight * yLook) - (yRight * xLook);

		vlen = Matrix.length(xArriba, yArriba, zArriba);
		xArriba /= vlen;
		yArriba /= vlen;
		zArriba /= vlen;

		float[] coordinates = new float[] { xPos, yPos, zPos, 1, xView, yView, zView, 1, xUp, yUp, zUp, 1 };

		if (dX != 0 && dY != 0) {

			xRight *= dY;
			yRight *= dY;
			zRight *= dY;
			xArriba *= dX;
			yArriba *= dX;
			zArriba *= dX;

			float rotX, rotY, rotZ;
			rotX = xRight + xArriba;
			rotY = yRight + yArriba;
			rotZ = zRight + zArriba;
			vlen = Matrix.length(rotX, rotY, rotZ);
			rotX /= vlen;
			rotY /= vlen;
			rotZ /= vlen;
			createRotationMatrixAroundVector(buffer, vlen, rotX, rotY, rotZ);
		}
		else if (dX != 0){
			createRotationMatrixAroundVector(buffer, dX, xArriba, yArriba, zArriba);
		}
		else{
			createRotationMatrixAroundVector(buffer, dY, xRight, yRight, zRight);
		}
		multiplyMMV(buffer, buffer, coordinates);

		if (isOutOfBounds(buffer)) return;

		xPos = buffer[0] / buffer[3];
		yPos = buffer[1] / buffer[3];
		zPos = buffer[2] / buffer[3];
		xView = buffer[4] / buffer[4 + 3];
		yView = buffer[4 + 1] / buffer[4 + 3];
		zView = buffer[4 + 2] / buffer[4 + 3];
		xUp = buffer[8] / buffer[8 + 3];
		yUp = buffer[8 + 1] / buffer[8 + 3];
		zUp = buffer[8 + 2] / buffer[8 + 3];

		setChanged(true);

	}

	private static void createRotationMatrixAroundVector(float[] matrix, float angle, float x, float y,
														 float z) {
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		float cos_1 = 1 - cos;

		// @formatter:off
		matrix[24]=cos_1*x*x + cos     ;    	matrix[24 +1 ]=cos_1*x*y - z*sin   ;   matrix[24 +2 ]=cos_1*z*x + y*sin   ;   matrix[24 +3]=0   ;
		matrix[24 +4 ]=cos_1*x*y + z*sin   ;  	matrix[24 +5 ]=cos_1*y*y + cos     ;   matrix[24 +6 ]=cos_1*y*z - x*sin   ;   matrix[24 +7]=0   ;
		matrix[24 +8 ]=cos_1*z*x - y*sin   ;  	matrix[24 +9 ]=cos_1*y*z + x*sin   ;   matrix[24 +10]=cos_1*z*z + cos    ;   matrix[24 +11]=0  ;
		matrix[24 +12]=0           		 ;      matrix[24 +13]=0          		  ;   matrix[24 +14]=0          		  ;   matrix[24 +15]=1  ;
		
		// @formatter:on
	}

	private static void multiplyMMV(float[] result, float[] matrix, float[] vector4Matrix) {
		for (int i = 0; i < vector4Matrix.length / 4; i++) {
			Matrix.multiplyMV(result, (i * 4), matrix, 24, vector4Matrix, (i * 4));
		}
	}

	private float[] getLocationVector() {
		return new float[] { xPos, yPos, zPos, 1f };
	}

	private float[] getLocationViewVector() {
		return new float[] { xView, yView, zView, 1f };
	}

	private float[] getLocationUpVector() {
		return new float[] { xUp, yUp, zUp, 1f };
	}

	public boolean hasChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public String toString() {
		return "Camera [xPos=" + xPos + ", yPos=" + yPos + ", zPos=" + zPos + ", xView=" + xView + ", yView=" + yView
				+ ", zView=" + zView + ", xUp=" + xUp + ", yUp=" + yUp + ", zUp=" + zUp + "]";
	}

	public synchronized void Rotate(float rotViewerZ) {
		if (rotViewerZ == 0) return;
		RotateImpl(rotViewerZ);
		lastAction = new Object[]{"rotate",rotViewerZ};
	}

	private void RotateImpl(float rotViewerZ) {
		if (Float.isNaN(rotViewerZ)) {
			Log.w("Rot", "NaN");
			return;
		}
		float xLook = xView - xPos;
		float yLook = yView - yPos;
		float zLook = zView - zPos;
		float vlen = Matrix.length(xLook, yLook, zLook);
		xLook /= vlen;
		yLook /= vlen;
		zLook /= vlen;

		createRotationMatrixAroundVector(buffer, rotViewerZ, xLook, yLook, zLook);
		float[] coordinates = new float[] { xPos, yPos, zPos, 1, xView, yView, zView, 1, xUp, yUp, zUp, 1 };
		multiplyMMV(buffer, buffer, coordinates);

		xPos = buffer[0];
		yPos = buffer[1];
		zPos = buffer[2];
		xView = buffer[4];
		yView = buffer[4 + 1];
		zView = buffer[4 + 2];
		xUp = buffer[8];
		yUp = buffer[8 + 1];
		zUp = buffer[8 + 2];

		setChanged(true);
	}


}
