package com.cc.worldcupremind.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.MatchesModel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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
	private ResourceHelper resourceHelper;
	
	private int bgWidth = 0;
	private int bgHeight = 0;
	private float itemWidth = 0;
	private float itemHeight = 0;
	private float marginWidth = 0;
	private float marginHeight = 0;
	private float flagWidth = 0;
	private float flagHight = 0;
	private SparseArray<DrawMatchModel> drawMatchesList;
	private SparseArray<MatchesModel> matchList;
	
	public ImageCreator(Context context) {
		this.context = context.getApplicationContext();
		this.res = this.context.getResources();
		this.controller = MatchDataController.getInstance();
		drawMatchesList = new SparseArray<DrawMatchModel>();
		matchList = controller.getMatchesData();
		resourceHelper = controller.gerResourceHelper();
	}
	
	
	public Boolean createSecondStageImage(){
		
		LogHelper.d(TAG, "createSecondStageImage");
		
		//Get screen and image size
		DisplayMetrics display = res.getDisplayMetrics();
		int screenWidth = display.widthPixels;
		int screenHeight = display.heightPixels;
		LogHelper.d(TAG, String.format("screenWidth:%d,screenHeight:%d", screenWidth,screenHeight));
		bgWidth = screenWidth > screenHeight ? screenWidth : screenHeight;
		bgHeight = screenWidth > screenHeight ? screenHeight : screenWidth;
		LogHelper.d(TAG, String.format("imageWidth:%d,imageHeight:%d", bgWidth,bgHeight));
		
		//calculate the position
		generateDrawMatchesList();
		
		try{
			//Load background image and clip size same to screen
			LogHelper.d(TAG, "Load background image as bitmap");
			//BitmapFactory.decodeResource will scale the picture
			Bitmap background = BitmapFactory.decodeStream(res.openRawResource(R.drawable.bg_secondestage));
			LogHelper.d(TAG, String.format("background pic width:%d,height:%d", background.getWidth(), background.getHeight()));
			Bitmap secondStageBG = null;
			if(background.getWidth() > bgWidth || background.getHeight() > bgHeight){
				LogHelper.d(TAG, "Clip the bitmap size same to screen");
				Matrix matrix = new Matrix(); 
				matrix.postScale((float)bgWidth/background.getWidth() ,(float)bgHeight/background.getHeight());
				secondStageBG = Bitmap.createBitmap(background, 0, 0, background.getWidth(), background.getHeight(), matrix, true);
				background.recycle();
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
	
	private void generateDrawMatchesList(){
		
		LogHelper.d(TAG, "calculatePosition");
		
		//Get item and margin size
		itemWidth = bgWidth/7;
		itemHeight = bgHeight/6;
		marginWidth = bgWidth/40;
		marginHeight = bgHeight/18;
		flagWidth = itemWidth/4;
		flagHight = itemHeight/4;
		
		//left 8
		PointF p49 = new PointF(marginWidth, marginHeight);
		PointF p50 = new PointF(p49.x, p49.y + itemHeight + marginHeight);
		PointF p53 = new PointF(p49.x, p50.y + itemHeight + marginHeight*2);
		PointF p54 = new PointF(p49.x, p53.y + itemHeight + marginHeight);
		
		//right 8
		PointF p51 = new PointF(bgWidth - itemWidth - marginWidth, p49.y);
		PointF p52 = new PointF(p51.x, p50.y);
		PointF p55 = new PointF(p51.x, p53.y);
		PointF p56 = new PointF(p51.x, p54.y);
		
		//left 4
		PointF p57 = new PointF(p49.x + itemWidth + marginWidth, 
				p49.y + itemHeight/2 + marginHeight/2);
		PointF p58 = new PointF(p57.x,
				p53.y + itemHeight/2 + marginHeight/2);
		
		//right 4
		PointF p59 = new PointF(p51.x - marginWidth - itemWidth , p57.y);
		PointF p60 = new PointF(p59.x, p58.y);
		
		//left 2
		PointF p61 = new PointF(p57.x - marginWidth/2 + itemWidth/2,
				p50.y + itemHeight/2 + marginHeight);
		
		//right 2
		PointF p62 = new PointF(p59.x - itemWidth/2 + marginWidth/2, p61.y);
		
		//final 2
		PointF p64 = new PointF(bgWidth/2 - itemWidth/2, p61.y);
		PointF p63 = new PointF(p64.x, p64.y + itemHeight + marginHeight*2);
		
		//Add to list
		SparseArray<PointF> pointList = new SparseArray<PointF>();
		pointList.put(49, p49);
		pointList.put(50, p50);
		pointList.put(51, p51);
		pointList.put(52, p52);
		pointList.put(53, p53);
		pointList.put(54, p54);
		pointList.put(55, p55);
		pointList.put(56, p56);
		pointList.put(57, p57);
		pointList.put(58, p58);
		pointList.put(59, p59);
		pointList.put(60, p60);
		pointList.put(61, p61);
		pointList.put(62, p62);
		pointList.put(63, p63);
		pointList.put(64, p64);
		
		//create draw match mode list
		for(int i = 0; i < pointList.size(); i++){
			PointF p = pointList.valueAt(i);
			int matchNo = pointList.keyAt(i);
			RectF rect = new RectF(p.x, p.y, p.x + itemWidth, p.y + itemHeight);
			MatchesModel model = matchList.get(matchNo);
			drawMatchesList.put(matchNo, new DrawMatchModel(rect, model));
		}
	}

	private Boolean drawSecondStageImage(Bitmap bitmap){
	
		LogHelper.d(TAG, "drawSecondStageImage");
		
		//Create canvas
		Log.d(TAG, "create bitmap object");
		Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas cv = new Canvas(canvasBitmap);  
		
		//Draw secondstage background
		Log.d(TAG, "Draw secondstage background");
		cv.drawBitmap(bitmap, 0, 0, null);
		
		//Draw Match item
		drawMatchDetail(cv);

		//Save canvas
		Log.d(TAG, "Save canvas");
	    cv.save(Canvas.ALL_SAVE_FLAG);
	    cv.restore();
	    
	    //Recycle the resource
	    bitmap.recycle();
	    
	    //Save file to local
	    Boolean ret = saveImageToLocal(canvasBitmap);
		System.gc(); //gc the bitmap
		return ret;
	}
	
	private void drawMatchDetail(Canvas cv){
		
		Log.d(TAG, "drawMatchDetail");
		for(int i = 0; i < drawMatchesList.size(); i++){

			DrawMatchModel model = drawMatchesList.valueAt(i);
			Log.d(TAG, "drawMatchDetail NO: " + model.model.getMatchNo());
			
			//draw area
			Paint pa = new Paint();
			pa.setColor(res.getColor(R.color.lightgray));
			cv.drawRect(model.rect, pa);
			
			//draw flag
			Bitmap flag1 = getFlagBitmap(model.model.getTeam1Code());
			Bitmap flag2 = getFlagBitmap(model.model.getTeam2Code());
			cv.drawBitmap(flag1, model.pointFlag1.x, model.pointFlag1.y, null);
			cv.drawBitmap(flag2, model.pointFlag2.x, model.pointFlag2.y, null);
			
			
			//draw time
			
			//draw score
			
			//draw name
		}
	}
	
	
	private void drawMatchLine(Canvas cv){
		
		
	}
	
	private Bitmap getFlagBitmap(String teamCode){
		
		Bitmap flag = BitmapFactory.decodeStream(res.openRawResource(
				resourceHelper.getFlagResourceIDByCode(teamCode)));	
		Matrix matrix = new Matrix(); 
		matrix.postScale((float)flagWidth/flag.getWidth() ,(float)flagHight/flag.getHeight());
		Bitmap sacleFlag = Bitmap.createBitmap(flag, 0, 0, flag.getWidth(), flag.getHeight(), matrix, true);
		flag.recycle();
		return sacleFlag;	
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
	
	
	class DrawMatchModel{
		
		public RectF rect;
		public MatchesModel model;
		public PointF pointFlag1;
		public PointF pointFlag2;
		
		public DrawMatchModel(RectF r, MatchesModel model){
			this.rect = r;
			this.model = model;
			
			//calculate flag pos
			pointFlag1 = new PointF(r.left + flagWidth/10, r.top + r.height()/2 - flagHight/2);
			pointFlag2 = new PointF(r.right - flagWidth - flagWidth/10, pointFlag1.y);
		}
	}
}
