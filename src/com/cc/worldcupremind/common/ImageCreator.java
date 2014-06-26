package com.cc.worldcupremind.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.MatchStatus;
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
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
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
	private float lineWidth = 0;
	private SparseArray<DrawMatchModel> drawMatchesList;
	private ArrayList<DrawLineModel> drawLinesList;
	private SparseArray<MatchesModel> matchList;
	
	private Boolean needDrawLine;
	private Boolean needDrawArea;
	
	public ImageCreator(Context context) {
		this.context = context.getApplicationContext();
		this.res = this.context.getResources();
		this.controller = MatchDataController.getInstance();
		drawMatchesList = new SparseArray<DrawMatchModel>();
		drawLinesList = new ArrayList<DrawLineModel>();
		resourceHelper = controller.gerResourceHelper();
		needDrawLine = false;
		needDrawArea = false;
	}
	
	
	public Boolean createSecondStageImage(){
		
		LogHelper.d(TAG, "createSecondStageImage");
		
		//Get resource
		matchList = controller.getMatchesData();
		
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
		
		if(needDrawLine){
			drawMatchLine(cv);
		}

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
			Log.d(TAG, "drawMatchDetail NO: " + model.model.getMatchNo() + "--" + model.model.getTeam1Code() + ":" +  model.model.getTeam2Code());
			
			if(needDrawArea){
				//draw area
				Paint pa = new Paint();
				if(model.model.getMatchNo() == 64){
					pa.setColor(res.getColor(R.color.match_area_final));
				}else if(model.model.getMatchNo() == 63){
					pa.setColor(res.getColor(R.color.match_area_16));
				}else if(model.model.getMatchNo() >= 61){
					pa.setColor(res.getColor(R.color.match_area_2));	
				}else{
					pa.setColor(res.getColor(R.color.match_area_16));
				}
				cv.drawRect(model.rect, pa);
			}
			
			//draw flag
			Bitmap flag1 = getFlagBitmap(model.model.getTeam1Code());
			Bitmap flag2 = getFlagBitmap(model.model.getTeam2Code());
			cv.drawBitmap(flag1, model.pointFlag1.x, model.pointFlag1.y, null);
			cv.drawBitmap(flag2, model.pointFlag2.x, model.pointFlag2.y, null);
			
			//draw name
			Paint paText = new Paint();
			paText.setColor(Color.BLACK);
			paText.setTextSize(ResourceHelper.dip2px(context, 12));
			paText.setTypeface(Typeface.DEFAULT_BOLD);
			String name1 = controller.getTeamNationalName(model.model.getTeam1Code());
			String name2 = controller.getTeamNationalName(model.model.getTeam2Code());
			Rect txtSize1 = getTextSize(paText, name1);
			Rect txtSize2 = getTextSize(paText, name2);
			cv.drawText(name1, model.pointFlag1.x + flagWidth/2 - txtSize1.width()/2  ,
					model.pointFlag1.y + flagHight + txtSize1.height() + flagHight/2, paText);
			cv.drawText(name2, model.pointFlag2.x + flagWidth/2 - txtSize2.width()/2 , 
					model.pointFlag2.y + flagHight + txtSize2.height() + flagHight/2, paText);
			
			//draw score
			Paint paScore = new Paint();
			paScore.setTextSize(ResourceHelper.dip2px(context, 12));
			paScore.setTypeface(Typeface.DEFAULT_BOLD);
			String score = "";
			if(model.model.getMatchStatus() == MatchStatus.MATCH_STATUS_WAIT_START){
				score = model.model.getMatchTime().getTimeString();
				paScore.setColor(res.getColor(R.color.gray));
			}else{
				score = String.format("%d:%d", model.model.getTeam1Score(), model.model.getTeam2Score());
				paScore.setColor(res.getColor(R.color.score));
			}
			Rect scoreSize = getTextSize(paScore, score);
			cv.drawText(score, 
					model.rect.left + itemWidth/2 - scoreSize.width()/2 , 
					model.rect.top + itemHeight/2 + scoreSize.height()/2,
					paScore);
			
			//draw score
			
		}
	}
	
	private float getTextWidth(Paint paint, String str) {  
		float iRet = 0;  
	    if (str != null && str.length() > 0) {  
	        int len = str.length();  
	        float[] widths = new float[len];  
	        paint.getTextWidths(str, widths);  
	        for (int j = 0; j < len; j++) {  
	            iRet += widths[j];  
	        }  
	    }  
	    return iRet;  
	}  
	
	
	private Rect getTextSize(Paint paint, String str){
		
		Rect rect = new Rect();  
		paint.getTextBounds(str, 0, str.length(), rect);  
		int w = rect.width();  
		int h = rect.height(); 
		return rect;
	}

	
	private void drawMatchLine(Canvas cv){
		
		LogHelper.d(TAG, "drawMatchLine");
		Paint pa = new Paint();
		pa.setColor(Color.WHITE);
		pa.setAlpha(180);
		pa.setStrokeWidth(lineWidth);
		for (DrawLineModel model : drawLinesList){
			cv.drawLine(model.start.x, model.start.y, model.end.x, model.end.y, pa);
		}
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
	    	Log.d(TAG, "The file exist");
	    	if(picFile.delete()){
		    	Log.d(TAG, "Delete exist file");
	    	}
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
	
	private void generateDrawMatchesList(){
		
		LogHelper.d(TAG, "calculatePosition");
		
		//Get item and margin size
		itemWidth = bgWidth/7;
		itemHeight = bgHeight/6;
		marginWidth = bgWidth/40;
		marginHeight = bgHeight/18;
		flagWidth = itemWidth/4;
		flagHight = itemHeight/4;
		lineWidth = ResourceHelper.dip2px(context, 3);//3dp
		
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

		if(!needDrawLine){
			return;
		}
		
		//left point
		PointF lp1 = new PointF(p49.x + itemWidth, p49.y + itemHeight/3);
		PointF lp2 = new PointF(lp1.x, p50.y + itemHeight*2/3);
		PointF lp3 = new PointF(lp1.x, p53.y + itemHeight/3);
		PointF lp4 = new PointF(lp1.x, p54.y + itemHeight*2/3);
		PointF lp5 = new PointF(p57.x + itemWidth/2, lp1.y);
		PointF lp6 = new PointF(lp5.x, p57.y);
		PointF lp7 = new PointF(lp5.x, p57.y + itemHeight);
		PointF lp8 = new PointF(lp5.x, lp2.y);
		PointF lp9 = new PointF(lp5.x, lp3.y);
		PointF lp10 = new PointF(lp5.x, p58.y);
		PointF lp11 = new PointF(lp5.x, p58.y + itemHeight);
		PointF lp12 = new PointF(lp5.x, lp4.y);
		PointF lp13 = new PointF(p57.x + itemWidth, p57.y + itemHeight/2);
		PointF lp14 = new PointF(lp13.x, p58.y + itemHeight/2);
		PointF lp15 = new PointF(p61.x + itemWidth*3/4, lp13.y);
		PointF lp16 = new PointF(lp15.x, p61.y);
		PointF lp17 = new PointF(lp15.x, p61.y + itemHeight);
		PointF lp18 = new PointF(lp15.x, lp14.y);
		PointF lp19 = new PointF(p61.x + itemWidth, p61.y + itemHeight/2);
		PointF lp20 = new PointF(p64.x, lp19.y);
		
		//right point
		PointF rp1 = new PointF(p52.x, lp1.y);
		PointF rp2 = new PointF(rp1.x, lp2.y);
		PointF rp3 = new PointF(rp1.x, lp3.y);
		PointF rp4 = new PointF(rp1.x, lp4.y);
		PointF rp5 = new PointF(p59.x + itemWidth/2, lp5.y);
		PointF rp6 = new PointF(rp5.x, lp6.y);
		PointF rp7 = new PointF(rp5.x, lp7.y);
		PointF rp8 = new PointF(rp5.x, lp8.y);
		PointF rp9 = new PointF(rp5.x, lp9.y);
		PointF rp10 = new PointF(rp5.x, lp10.y);
		PointF rp11 = new PointF(rp5.x, lp11.y);
		PointF rp12 = new PointF(rp5.x, lp12.y);
		PointF rp13 = new PointF(p59.x, lp13.y);
		PointF rp14 = new PointF(p59.x, lp14.y);
		PointF rp15 = new PointF(p62.x + itemWidth/4, lp15.y);
		PointF rp16 = new PointF(rp15.x, lp16.y);
		PointF rp17 = new PointF(rp15.x, lp17.y);
		PointF rp18 = new PointF(rp15.x, lp18.y);
		PointF rp19 = new PointF(p62.x, lp19.y);
		PointF rp20 = new PointF(p64.x + itemWidth, lp20.y);
		
		//create left line
		DrawLineModel lline1 = new DrawLineModel(lp1, lp5);
		DrawLineModel lline2 = new DrawLineModel(lp5, lp6);
		DrawLineModel lline3 = new DrawLineModel(lp2, lp8);
		DrawLineModel lline4 = new DrawLineModel(lp8, lp7);
		DrawLineModel lline5 = new DrawLineModel(lp3, lp9);
		DrawLineModel lline6 = new DrawLineModel(lp9, lp10);
		DrawLineModel lline7 = new DrawLineModel(lp4, lp12);
		DrawLineModel lline8 = new DrawLineModel(lp12, lp11);
		DrawLineModel lline9 = new DrawLineModel(lp13, lp15);
		DrawLineModel lline10 = new DrawLineModel(lp15, lp16);
		DrawLineModel lline11 = new DrawLineModel(lp14, lp18);
		DrawLineModel lline12 = new DrawLineModel(lp18, lp17);
		DrawLineModel lline13 = new DrawLineModel(lp19,lp20);

		//create right line
		DrawLineModel rline1 = new DrawLineModel(rp1, rp5);
		DrawLineModel rline2 = new DrawLineModel(rp5, rp6);
		DrawLineModel rline3 = new DrawLineModel(rp2, rp8);
		DrawLineModel rline4 = new DrawLineModel(rp8, rp7);
		DrawLineModel rline5 = new DrawLineModel(rp3, rp9);
		DrawLineModel rline6 = new DrawLineModel(rp9, rp10);
		DrawLineModel rline7 = new DrawLineModel(rp4, rp12);
		DrawLineModel rline8 = new DrawLineModel(rp12, rp11);
		DrawLineModel rline9 = new DrawLineModel(rp13, rp15);
		DrawLineModel rline10 = new DrawLineModel(rp15, rp16);
		DrawLineModel rline11 = new DrawLineModel(rp14, rp18);
		DrawLineModel rline12 = new DrawLineModel(rp18, rp17);
		DrawLineModel rline13 = new DrawLineModel(rp19,rp20);
		
		drawLinesList.add(lline1);
		drawLinesList.add(lline2);
		drawLinesList.add(lline3);
		drawLinesList.add(lline4);
		drawLinesList.add(lline5);
		drawLinesList.add(lline6);
		drawLinesList.add(lline7);
		drawLinesList.add(lline8);
		drawLinesList.add(lline9);
		drawLinesList.add(lline10);
		drawLinesList.add(lline11);
		drawLinesList.add(lline12);
		drawLinesList.add(lline13);
		
		drawLinesList.add(rline1);
		drawLinesList.add(rline2);
		drawLinesList.add(rline3);
		drawLinesList.add(rline4);
		drawLinesList.add(rline5);
		drawLinesList.add(rline6);
		drawLinesList.add(rline7);
		drawLinesList.add(rline8);
		drawLinesList.add(rline9);
		drawLinesList.add(rline10);
		drawLinesList.add(rline11);
		drawLinesList.add(rline12);
		drawLinesList.add(rline13);
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
			pointFlag1 = new PointF(r.left + flagWidth/8, r.top + r.height()/2 - flagHight/2);
			pointFlag2 = new PointF(r.right - flagWidth - flagWidth/8, pointFlag1.y);
		}
	}
	
	class DrawLineModel{
		PointF start;
		PointF end;
		
		public DrawLineModel(PointF start, PointF end){
			this.start = start;
			this.end = end;
		}
	}
	
}
