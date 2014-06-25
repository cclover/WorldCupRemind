package com.cc.worldcupremind.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.logic.MatchDataController;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;

public class ImageCreator {
	
	private static final String TAG = "ImageCreator";
	public static final String DATA_SECOND_STAGE_IMAGE = "secondstage.jpg";
	public static final String ACTION_CRATEA_IAMGE_DONE = "com.cc.worldcupremind.createimage";
	public static final String KEY_CRATEA_IAMGE_DONE = "com.cc.worldcupremind.createimage.key";
	private Context context;
	private Resources res;
	private MatchDataController controller;
	
	private int matchWidth = 0;
	private int matchHeight = 0;
	
	private int imageWidth = 0;
	private int imageHeight = 0;
	private int marginWidth = 0;
	private int marginHeight = 0;
	private SparseArray<Point> matchPointList;

	
	public ImageCreator(Context context) {
		this.context = context.getApplicationContext();
		this.res = this.context.getResources();
		this.controller = MatchDataController.getInstance();
		matchPointList = new SparseArray<Point>();
	}
	
	
	public Boolean createSecondStageImage(){
		
		//Get screen and image size
		DisplayMetrics display = res.getDisplayMetrics();
		int screenWidth = display.widthPixels;
		int screenHeight = display.heightPixels;
		LogHelper.d(TAG, String.format("screenWidth:%d,screenHeight:%d", screenWidth,screenHeight));
		imageWidth = screenWidth > screenHeight ? screenWidth : screenHeight;
		imageHeight = screenWidth > screenHeight ? screenHeight : screenWidth;
		LogHelper.d(TAG, String.format("imageWidth:%d,imageHeight:%d", imageWidth,imageHeight));
		
		//
		matchWidth = imageWidth/8;
		matchHeight = imageHeight/6;
		marginWidth = imageWidth/20;
		marginHeight = imageHeight/18;
		
		//8-A
		Point p49 = new Point(marginWidth, marginHeight);
		Point p50 = new Point(p49.x, p49.y + imageHeight + marginHeight);
		Point p53 = new Point(p49.x, p50.y + imageHeight + marginHeight*2);
		Point p54 = new Point(p49.x, p53.y + imageHeight + marginHeight);
		
		//8-B
		Point p51 = new Point(screenWidth -matchWidth - marginWidth, p49.y);
		Point p52 = new Point(p51.x, p50.y);
		Point p55 = new Point(p51.x, p53.y);
		Point p56 = new Point(p51.x, p54.y);
		
		//4-A
		
		//4-B
		
		
		
		try{
			//Load background image and clip size same to screen
			LogHelper.d(TAG, "Load background image as bitmap");
			//BitmapFactory.decodeResource will scale the picture
			Bitmap background  = BitmapFactory.decodeStream(res.openRawResource(R.drawable.bg_secondestage));
			LogHelper.d(TAG, String.format("background pic width:%d,height:%d", background.getWidth(), background.getHeight()));
			Bitmap secondStageBG = null;
			if(background.getWidth() > imageWidth || background.getHeight() > imageHeight){
				LogHelper.d(TAG, "Clip the bitmap size same to screen");
				Matrix matrix = new Matrix(); 
				matrix.postScale((float)imageWidth/background.getWidth() ,(float)imageHeight/background.getHeight());
				secondStageBG = Bitmap.createBitmap(background, 0, 0, background.getWidth(), background.getHeight(), matrix, true);
			}else{
				secondStageBG = background;
			}
			LogHelper.d(TAG, String.format("secondStageBG pic width:%d,height:%d", secondStageBG.getWidth(),secondStageBG.getHeight()));
			
			//Draw secondstage image
			Boolean ret = drawSecondStageImage(secondStageBG);
			LogHelper.d(TAG, String.format("drawSecondStageImage:%b", ret));
			return ret;
		}catch(Exception e){
			LogHelper.e(TAG, e);
			return false;
		}
	}
	
	

	private Boolean drawSecondStageImage(Bitmap bitmap){
	
		//Create canvas
		Log.d(TAG, "create bitmap object");
		Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas cv = new Canvas(canvasBitmap);  
		
		//Draw secondstage background
		cv.drawBitmap(bitmap, 0, 0, null);
		
		//Draw Match item area
		
		
		//Draw each match item detail
		

		//Save canvas
	    cv.save(Canvas.ALL_SAVE_FLAG);
	    cv.restore();
	    
	    //Recycle the resource
	    bitmap.recycle();
	    
	    //Save file to local
	    return saveImageToLocal(canvasBitmap);
	}


	private Boolean saveImageToLocal(Bitmap outputBitmap){
		  
	    Log.d(TAG, "saveImageToLocal");
	    
	    //Check local file
	    File picFile = context.getFileStreamPath(DATA_SECOND_STAGE_IMAGE);
	    if(picFile.exists()){
	    	Log.d(TAG, "Delete exist file");
	    	picFile.delete();
	    }
	    
	    //write the bitmap to local file
	    FileOutputStream picOutputStream = null; 
	    try {
	        picOutputStream = new FileOutputStream(picFile);
	        outputBitmap.compress(Bitmap.CompressFormat.JPEG, 90, picOutputStream);
	        picOutputStream.flush();
	        picOutputStream.close();
	        Log.d(TAG, "Save bitmap done!");
	     } catch (FileNotFoundException e) {
	    	 LogHelper.e(TAG, e);
	    	 return false;
	    } catch (IOException e) {
	    	 LogHelper.e(TAG, e);
	    	 return false;
	    }finally{
	    	outputBitmap.recycle(); //recyle the bitmap
	    	try {
				picOutputStream.close();
			} catch (IOException e) {
				 LogHelper.e(TAG, e);
		    	 return false;
			}
	    }
	    return true;
	}
}
