package com.hoody.commonbase.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * 可以放大、缩小、剪裁图片输出成文件或bitmap的控件
 * @author 纪广兴 
 * @since 创建时间：2013-10-22 上午10:52:44
 * 
 */
public class CropImageView extends ImageView {

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCropView();
	}
	public CropImageView(Context context) {
		super(context);
		initCropView();
	}
	//===========================================================================
	private float outputScale =1.0f;	//输出框的缩放系数，默认如果设置的输出框大于控件自身的80%的宽高，则需要缩放一下显示，真实截图时按真实大小截取
	private int mOutWidth,mOutHeight;	//保存外部的设置值
	int frameWidth = 0;
	int frameHeight = 0;
	
	Paint paint = new Paint();
	Paint errTxtPaint;
	
    private static Point leftTop, rightBottom, center;

    @Override
    protected void onDraw(Canvas canvas)
    {
    	if(!TextUtils.isEmpty(errorHintTxt)){
    		if(errTxtPaint==null){
    			errTxtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);                 
    			errTxtPaint.setColor(Color.GRAY);                 
    			errTxtPaint.setTextSize(24);                                   
    		}
    		FontMetrics fm = errTxtPaint.getFontMetrics();
			int textLen = (int) errTxtPaint.measureText(errorHintTxt);
			int textLeft = (getWidth() - textLen ) / 2; 
			int textTop = (int) (getHeight() - getWidth() + 40 + Math.abs(fm.ascent) / 2); 
    		canvas.drawText(errorHintTxt, textLeft, textTop, errTxtPaint);
    	}else{
        	//下面画裁剪框
        	if(leftTop.equals(0, 0))
                resetPoints(true);	//画的时候再计算控件大小，则有值，初始化时计算无效
        	//根据缩放系统和用户拖动，画设置的图片
        	try {
            	super.onDraw(canvas);
            	if(maskBmp==null){
            		maskBmp = getFrameMaskBitmap(new Rect(leftTop.x,leftTop.y,rightBottom.x,rightBottom.y));
            	}
            	if(maskBmp!=null){
            		canvas.drawBitmap(maskBmp, 0, 0, null);	//如果蒙板图片创建成功，则画蒙板图片指示选框大小位置
            	}else{
                    canvas.drawRect(leftTop.x, leftTop.y, rightBottom.x, rightBottom.y, paint); //如果因内存等原因没创建成功，则画简易指示
            	}
			} catch (Throwable e) {
				setImageDrawable(null);
				e.printStackTrace();
			}
    	}
    }
    private Bitmap maskBmp;		//遮罩用图片，和用户设置的选框大小相关
  //获得圆角图片的方法   
    public Bitmap getFrameMaskBitmap(Rect frameRect){   
    	try {
            Bitmap output = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);   
            Canvas canvas = new Canvas(output);   
            Paint paint3 = new Paint();
    		paint3.setColor(Color.argb(128, 0, 0, 0));
    		canvas.drawRect(new Rect(0,0,getWidth(),getHeight()), paint3);
    		
            final int color = 0xff424242;   
            final Paint paint = new Paint();   
        
            paint.setAntiAlias(true);   
            canvas.drawARGB(0, 0, 0, 0);   
            paint.setColor(color);   
       
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));   
            paint.setColor(Color.argb(0, 0, 0, 0));
            canvas.drawRect(frameRect, paint);
            
            Paint storePaint = new Paint();
            storePaint.setStyle(Style.STROKE);
            storePaint.setColor(Color.rgb(0x46, 0xb4, 0xe7));
            storePaint.setStrokeWidth(3);
            canvas.drawRect(frameRect, storePaint);
            return output;   
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
    }   
    //----------------------------------------------------------------------------------------
 	Matrix imgMatrix = new Matrix(); // 定义图片的变换矩阵

 	private static final int NONE = 0;
 	private static final int DRAG = 1; // 拖动操作
 	private static final int ZOOM = 2; // 放大缩小操作
 	private int mode = NONE; // 当前模式


 	private float primaryW; // 原图宽度
 	private float primaryH; // 原图高度

 	private Matrix savedMatrix = new Matrix();

    private float maxScale =3.0f;  //保存初始化的缩放系数，再操作也不能大于此值
    private float miniScale =0.2f;  //保存初始化的缩放系数，再操作也不能小于此值
 	private float scale;
 	private float scaleY;
 	private float scaleX;
 	
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist;
	private boolean isFirst = false;
	private float tempWidth, tempHeight, imageWidth, imageHeight;
	float[]	values = new float[9];
	private boolean isMaxScale,isMinScale;
    
	public void setImageRotate(boolean isLeft) {
		if(srcBmp!=null){
			float saveRotate = 0f;
			if(isLeft){
				saveRotate =-90;
			}else{
				saveRotate = 90;
			}
			Matrix matrix = new Matrix();
		    matrix.postRotate(saveRotate); //旋转只是图片，不需要在这里指定中心点。
		    Bitmap rotateBmp = null;
		    try {
			    rotateBmp = Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.getWidth(), srcBmp.getHeight(), matrix, true);
			    if(rotateBmp!=srcBmp){
					srcBmp.recycle();	//旋转后返回了新bmp对象，则释放老图内存。
					srcBmp = null;
			    }
			    if(rotateBmp!=null){
				    setCropAndBmpSize(mOutWidth, mOutHeight, rotateBmp.getWidth(), rotateBmp.getHeight());	//这里设置框的大小和图的大小，图按实现加载的获取，指定的只是个范围期望。

				   	resetPoints(false);	//目前不复位计算，旋转后，图片不能位于选框中，所以必须先旋转，后缩放

					setImageBitmap(rotateBmp);     
			    }else{
			    	setErrorHint("创建旋转图片出错了！");
			    }   
			} catch (Throwable e) {
				e.printStackTrace();				
			}
		}		
	}
	public void setImageZoom(boolean isOut) {
		if(imgMatrix!=null){
			if(isOut){
				if(!isMaxScale){
					imgMatrix.postScale(1.1f, 1.1f,center.x,center.y);
					isMinScale = false;
				}
			}else{
				if(!isMinScale){
					imgMatrix.postScale(0.9f,0.9f,center.x,center.y);
					isMaxScale = false;
				}
			}
			checkScaleLimit();
			checkTransLimit();
			this.setImageMatrix(imgMatrix);
		}		
	}

	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		ImageView view = this;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(imgMatrix);			//在触摸开始前，保存当前Matrix的镜像值。在身拖放或缩放时，用saveMatrix的值先复位ImgMatrix，再执行postXXXX操作
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			isMaxScale = false;	//复位到限值标记，允许在限值向回缩放
			isMinScale = false;
			if (isFirst) {
				tempWidth *= scale;
				tempHeight *= scale;

				if (tempWidth < imageWidth || tempHeight < imageHeight) {
					imgMatrix.postScale(imageWidth / tempWidth, imageHeight
							/ tempHeight, mid.x, mid.y);

					tempWidth = imageWidth;
					tempHeight = imageHeight;
				}

			}
			isFirst = false;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(imgMatrix);//在触摸开始前，保存当前Matrix的镜像值。在身拖放或缩放时，用saveMatrix的值先复位ImgMatrix，再执行postXXXX操作
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				imgMatrix.set(savedMatrix);	//先恢复先前保存的Matrix值
				float tranX = event.getX() - start.x;
				float tranY = event.getY() - start.y;
				imgMatrix.postTranslate(tranX,tranY);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					imgMatrix.set(savedMatrix);//先恢复先前保存的Matrix值
					scale = newDist / oldDist;	
					if(!isMaxScale&&!isMinScale){    //没到达最大或最小限值时，可以二指缩放继续操作，
						imgMatrix.postScale(scale, scale, mid.x, mid.y);
						isFirst = true;						
					}else if(isMaxScale){
						imgMatrix.setScale(maxScale, maxScale, mid.x, mid.y);
					}else if(isMinScale){
						imgMatrix.setScale(miniScale, miniScale, mid.x, mid.y);
					}
				}
			}

			break;
		} 
		if(mode!=NONE){
			checkTransLimit();
			checkScaleLimit();
		}
		view.setImageMatrix(imgMatrix);	//在图片上应用Matrix新值
		return true;
    }
	
	/**
	 * 检查并修正缩放与平移限值
	 */
	private void checkScaleLimit() {
		if(srcBmp==null){
			return;
		}
		imgMatrix.getValues(values); 
		if(values[0]>maxScale){  //x,y缩放超过10倍，则不再放大
			isMaxScale = true;	//设置限值标记，不再允许放大或缩小
			values[0] = maxScale;
		}else if(values[0]<miniScale){  //x,y缩放超过10倍，则不再放大
			isMinScale = true;	//设置限值标记，不再允许放大或缩小
			values[0] = miniScale;
		}
		if(values[4]>maxScale){  //x,y缩放小于0.2，则不再缩小
			values[4] = maxScale;
		}else if(values[4]<miniScale){  //x,y缩放小于0.2，则不再缩小
			values[4] = miniScale;
		}
        imgMatrix.setValues(values);
	}
	/**
	 * 检查并修正缩放与平移限值
	 */
	private void checkTransLimit() {
		if(srcBmp==null){
			return;
		}
		imgMatrix.getValues(values); 
		float leftHideWidth = primaryW*values[0]-rightBottom.x;  //图片原始宽度*缩放系统-右侧左移边界位置
		if(values[2]>leftTop.x){	//向右移不能超过剪裁框的左边
			values[2] = leftTop.x;        	
		}else if(values[2]<-leftHideWidth){  //向左移不能超过剪裁框的右边
			values[2] = -leftHideWidth;
		}
		float TopHideHeight = primaryH*values[4]-rightBottom.y;
		if(values[5]>leftTop.y){	//向下移不能超过剪裁框的顶
			values[5] = leftTop.y;
		}else if(values[5]<-TopHideHeight){
			values[5] = -TopHideHeight;
		}
		imgMatrix.setValues(values);
	}
	
	private float spacing(MotionEvent event) {
		try {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return (float) Math.sqrt(x * x + y * y);
		}catch (IllegalArgumentException e){

		}
		return 0f;
	}

	private void midPoint(PointF point, MotionEvent event) {
		try {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}catch (IllegalArgumentException e){

		}
	}
	
    private void initCropView() {
		this.setScaleType(ScaleType.MATRIX);
		scaleX = 1.0f;  
        scaleY = 1.0f;  
        miniScale = 0.8f;  

		this.setImageMatrix(imgMatrix);
		
    	paint.setColor(Color.YELLOW);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(5);  
        leftTop = new Point();
        rightBottom = new Point();
        center = new Point();
    }

    public void setCropAndBmpSize(int outW,int outH,int bmpW,int bmpH){
    	mOutWidth = outW;	//设置剪裁区域宽，高
    	mOutHeight= outH;
    	primaryW = bmpW;	//设置原图宽、高
    	primaryH = bmpH;
    }
    //isRotate 该参数临时处理 旋转后getMeasuredHeight 值更变导致保存选择框与图片可拖拽区域错位
    public void resetPoints(boolean isRotate) {
        outputScale = 1.0f;		//目前因计算复杂，不支持选框缩放显示。
    	frameWidth = (int) (mOutWidth*outputScale);	//
    	frameHeight = (int) (mOutHeight*outputScale);
		//isRotate = false 为旋转状态
		if(isRotate){
			center.set(getMeasuredWidth()/2, getMeasuredHeight()/2);	//保存选择框在屏幕上的坐标
			leftTop.set((getMeasuredWidth()-frameWidth)/2,(getMeasuredHeight()-frameHeight)/2);
		}
        rightBottom.set(leftTop.x+frameWidth, leftTop.y+frameHeight);
        
    	scaleX = (float) frameWidth / primaryW;  	//获取真实图片初始化缩小到框显示大小的缩放比例
        scaleY = (float) frameHeight / primaryH;  
        miniScale = scaleX > scaleY ? scaleX : scaleY;	//等比例缩放图片进行显示   ,保存缩小用系数    
        
        if(miniScale>1.0f){
        	maxScale = miniScale*2f;	//如果选择的图片还不如用户的选框大，则不能再放大图片，只能填充选框大小后进行截取
        }else{
        	int bmpLongEdge = (int) Math.max(primaryW, primaryH);	//取原图最长的一边。
        	int frameLongEdge = Math.max(frameWidth, frameHeight);	//取原图最长的一边。
        	maxScale = Math.min(4000, bmpLongEdge/miniScale)/frameLongEdge;	//动态计算最大放大倍数，防止超过内存，不能截图
        	maxScale = Math.min(5f, maxScale); //计算后也要防止不能放大超过5倍
        	if(maxScale<2.0f){
        		maxScale = 2.0f;
        	}
        }
        
        float transX = leftTop.x/miniScale;
        float transY = leftTop.y/miniScale;
        if(primaryW*miniScale>frameWidth){
        	transX = transX - (primaryW*miniScale-frameWidth)/2/miniScale;  //设置缩小后的图片超出框宽度的部分，居中显示
        }
        if(primaryH*miniScale>frameHeight){
        	transY = transY - (primaryH*miniScale-frameHeight)/2/miniScale;	////设置缩小后的图片超出框宽度的部分，居中显示
        }
		imgMatrix.setTranslate(transX, transY);	//设置初始位置时，要除以系统，才能开始时把图片移到框的位置
		imgMatrix.postScale(miniScale, miniScale, 0, 0);
		this.setImageMatrix(imgMatrix);	//设置到图片显示控制中
    }
    
    public byte[] getCropImage(){
    	if(srcBmp==null){
    		return null;
    	}
    	ByteArrayOutputStream stream =null;
    	Bitmap dstBmp = null;
	    Bitmap cropped = null;	    
    	float[]	values = new float[9];
    	imgMatrix.getValues(values); 
    	float scrImgLeft = values[2];	//当前显示图片的左上角x坐标
    	float scrImgTop  = values[5];
    	float curImgScale= values[0]; 	//获取当前图片缩放系数
    	int x = (int) (leftTop.x-scrImgLeft);
    	int y = (int) (leftTop.y-scrImgTop);
    	int cropWidth = rightBottom.x-leftTop.x;
    	int cropHeight = rightBottom.y-leftTop.y;
	    try {
	    	//缩小选框，从srcbmp上截取图片，然后再放大到指定选框大小。比较省内存
			if(Math.abs(1.0f-curImgScale)<0.000001f){
				if((cropWidth+x)>srcBmp.getWidth()){	//防止计算误差造成设置的宽、高超过原图大小，报错，所以这里判断修正一下计算的宽、高值
			    	cropWidth = (int) (srcBmp.getWidth() -x);
			    	cropWidth = Math.min(srcBmp.getWidth(), Math.abs(cropWidth));
			    }
			    if((cropHeight+y)>srcBmp.getHeight()){
			    	cropHeight = (int) (srcBmp.getHeight()-y);
			    	cropHeight = Math.min(srcBmp.getHeight(), Math.abs(cropHeight));
			    }
				cropped = Bitmap.createBitmap(srcBmp,(int)x,(int)y,cropWidth,cropHeight); //1.0原图标准大小显示时，直接从原图上截取选框区域，生成cropped图像
			}else{
				x = (int) (x / curImgScale);	//按原图的缩放系数，转换选框大小，实现同比例在原图上截取。
				y = (int) (y / curImgScale);
				cropWidth = (int) (cropWidth/curImgScale);
				cropHeight = (int) (cropHeight / curImgScale);
			    dstBmp = Bitmap.createBitmap(srcBmp,(int)x,(int)y,cropWidth,cropHeight); //从原图上截取缩放处理的选区图片。
				cropped = Bitmap.createScaledBitmap(dstBmp, mOutWidth, mOutHeight, true);  //把原图上截取到的小图，再放大到截取框大小。返回给用户
				if(dstBmp!=cropped){
					dstBmp.recycle();
					dstBmp = null;
				}
			}
		    stream = new ByteArrayOutputStream();
		    cropped.compress(Bitmap.CompressFormat.PNG, 100, stream);
		    cropped.recycle();
		    cropped = null;
		    byte[] resultBytes = stream.toByteArray();		//此方法是复制数据到新内存，所以后面要释放流的内存占用
			stream.close();
		    stream = null;
		    return resultBytes;	//返回截取的图片bytes数组值
		} catch (Throwable e) {
	    	e.printStackTrace();
		} finally{
			if(dstBmp!=null){
				dstBmp.recycle();
				dstBmp = null;
			}
			if(cropped!=null){
				cropped.recycle();
				cropped = null;
			}
			if(stream!=null){
				try {
					stream.close();
				} catch (IOException e) {
				}
				stream = null;
			}
		}
	    return null;
    }
    
    private Bitmap srcBmp=null;
	private String errorHintTxt;
    //这里保存设置到控件src属性的bitmap对象，用于其它过程中操作。
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		if(srcBmp!=null&&(!srcBmp.isRecycled())){
			srcBmp.recycle();
			srcBmp = null;
		}
		srcBmp = bm;
		checkTransLimit();
		checkScaleLimit();
	}
	
	public void setErrorHint(String errHint) {
		this.errorHintTxt = errHint;
	}

    
}
