package com.test.functiontese.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.test.functiontese.R;
import com.test.functiontese.util.FileUtil;
import com.test.functiontese.util.ImageFilter;


/**
 * Created by gxj on 2018/2/18 11:46.
 * 拍照界面
 */
public class CameraActivity extends Activity implements View.OnClickListener {
    //中文字典表
    static final String TESSBASE_PATH = Environment.getExternalStorageDirectory() + "/tesseract";
//    static final String TESSBASE_PATH = Environment.getExternalStorageDirectory() + "/";

    //识别语言英文
    static final String DEFAULT_LANGUAGE = "chi_sim";
    //中文
//    static final String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * 身份证正面
     */
    public final static int TYPE_ID_CARD_FRONT = 1;
    /**
     * 身份证反面
     */
    public final static int TYPE_ID_CARD_BACK = 2;

    public final static int REQUEST_CODE = 0X13;

    private CustomCameraPreview customCameraPreview;
    private View containerView;
    private ImageView cropView;
    private View optionView;
    private String content;
    private int type;

    /**
     * 跳转到拍照页面
     */
    public static void navToCamera(Context context, int type) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra("type", type);
        ((Activity) context).startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", 0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_camera);

        customCameraPreview = (CustomCameraPreview) findViewById(R.id.camera_surface);
        containerView = findViewById(R.id.camera_crop_container);
        cropView = (ImageView) findViewById(R.id.camera_crop);
        optionView = findViewById(R.id.camera_option);

        //获取屏幕最小边，设置为cameraPreview较窄的一边
        float screenMinSize = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        //根据screenMinSize，计算出cameraPreview的较宽的一边，长宽比为标准的16:9
        float maxSize = screenMinSize / 9.0f * 16.0f;
        RelativeLayout.LayoutParams layoutParams;

        layoutParams = new RelativeLayout.LayoutParams((int) maxSize, (int) screenMinSize);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        customCameraPreview.setLayoutParams(layoutParams);

        float height = (int) (screenMinSize * 0.75);
        float width = (int) (height * 75.0f / 47.0f);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams((int) width, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams((int) width, (int) height);
        containerView.setLayoutParams(containerParams);
        cropView.setLayoutParams(cropParams);
        switch (type) {
            case TYPE_ID_CARD_FRONT:
                cropView.setImageResource(R.mipmap.camera_front);
                break;
            case TYPE_ID_CARD_BACK:
                cropView.setImageResource(R.mipmap.camera_back);
                break;
        }

        customCameraPreview.setOnClickListener(this);
        findViewById(R.id.camera_close).setOnClickListener(this);
        findViewById(R.id.camera_take).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_surface:
                customCameraPreview.focus();
                break;
            case R.id.camera_close:
                finish();
                break;
            case R.id.camera_take:
                takePhoto();
        }
    }

    private void takePhoto() {
        optionView.setVisibility(View.GONE);
        customCameraPreview.setEnabled(false);
        customCameraPreview.takePhoto(new Camera.PictureCallback() {
            public void onPictureTaken(final byte[] data, final Camera camera) {
                //子线程处理图片，防止ANR
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = null;
                        if (data != null) {
                            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            camera.stopPreview();
                        }
                        if (bitmap != null) {
                            //计算裁剪位置
                            float left = ((float) containerView.getLeft() - (float) customCameraPreview.getLeft()) / (float) customCameraPreview.getWidth();
                            float top = (float) cropView.getTop() / (float) customCameraPreview.getHeight();
                            float right = (float) containerView.getRight() / (float) customCameraPreview.getWidth();
                            float bottom = (float) cropView.getBottom() / (float) customCameraPreview.getHeight();

                            //裁剪及保存到文件
                            Bitmap resBitmap = Bitmap.createBitmap(bitmap,
                                    (int) (left * (float) bitmap.getWidth()),
                                    (int) (top * (float) bitmap.getHeight()),
                                    (int) ((right - left) * (float) bitmap.getWidth()),
                                    (int) ((bottom - top) * (float) bitmap.getHeight()));
                            //新加功能
                            FileUtil.saveBitmap(resBitmap);
                            localre(resBitmap);
                            if (!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                            if (!resBitmap.isRecycled()) {
                                resBitmap.recycle();
                            }

                            //拍照完成，返回对应图片路径
                            Intent intent = new Intent();
                            intent.putExtra("result", FileUtil.getImgPath());
                            intent.putExtra("string",content);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        return;
                    }
                }).start();
            }
        });
    }

    private String localre (Bitmap bm){
        content = "";
        bm = bm.copy(Bitmap.Config.ARGB_8888,true);
        TessBaseAPI baseAPI = new TessBaseAPI();
        baseAPI.init(TESSBASE_PATH, DEFAULT_LANGUAGE);
//        baseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"); // 识别白名单
//        baseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"\\|~`,./<>?"); // 识别黑名单
        baseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
//        bm =  ImageFilter.gray2Binary(bm);// 图片二值化
//        bm =  ImageFilter.grayScaleImage(bm);// 图片灰度
        baseAPI.setImage(bm);
        content = baseAPI.getUTF8Text();
//        content = content.substring(content.length()-18,content.length());
//        String str = "";
//        for (int i=0;i<content.length();i++){
//            if ((content.charAt(i)>=48 && content.charAt(i)<=57)||
//                    (content.charAt(i)>=65 && content.charAt(i)<=90)){
//                str += content.charAt(i);
//            }
//        }
//        Log.e("===========>",str);
//        content = str;
        baseAPI.clear();
        baseAPI.end();
        return content;
   }

}
