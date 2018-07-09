package com.test.functiontese;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.test.functiontese.util.ImageFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 扫描身份证
 *
 * @author kaifa
 */
public class ScanActivity extends Activity {
    private ImageView back, position;// 返回和切换前后置摄像头
    private SurfaceView surface;
    private ImageButton shutter;// 快门
    private SurfaceHolder holder;
    static final String TESSBASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tesseract/";
    static final String DEFAULT_LANGUAGE = "id";
    static final String TESSDATA_PATH = TESSBASE_PATH + "tessdata/";
    private Camera camera;// 声明相机
    /**
     * 选取图片！
     */
    private boolean isChoice = true;

    TextView textView;
    int screenWidth = 0;
    int screenHeight = 0;
    int x, y, width, height;

    ImageView imageView;
    View xianView;

    //返回的窗体
    private String returnClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);// 没有标题
        // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        // 设置手机屏幕朝向，一共有7种
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_sacn);
        writeFile();
        WindowManager wm = this.getWindowManager();

        // 获得返回画面名称
        returnClass = getIntent().getStringExtra("class");

        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();

        //width = Dp2Px(this, 400);
        //height = Dp2Px(this, 253);
        width = 1200;
        height = 600;

        x = (screenWidth - width) / 2;
        y = (screenHeight - height) / 2;

        surface = (SurfaceView) findViewById(R.id.camera_surface);
        holder = surface.getHolder();// 获得句柄
        holder.addCallback(new Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                try {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    holder = null;
                    surface = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                if (camera == null) {
                    camera = Camera.open();
                    try {

                        camera.setPreviewDisplay(holder);
                        CameraConfigurationManager configManager = new CameraConfigurationManager(
                                getApplicationContext());
                        configManager.initFromCameraParameters(camera);
                        configManager.setDesiredCameraParameters(camera);
                        // camera.setDisplayOrientation(90);
                        camera.startPreview();
                        camera.setPreviewCallback(previewCallback);
                        camera.autoFocus(focusCallback);

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
                // TODO Auto-generated method stub

            }
        });// 添加回调
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// surfaceview不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前

        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                camera.autoFocus(new AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        // TODO Auto-generated method stub
                        if (success) {
                            Log.v("xingyun", "自动对焦成功");
                            isChoice = true;
                        }
                    }
                });
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView1);
        textView = (TextView) findViewById(R.id.textView1);
        xianView = findViewById(R.id.xian);
        setAnimation();
    }

    //创建目录和写入临时文件
    private void writeFile() {
        InputStream inputStream;
        try {
            inputStream = getResources().getAssets().open("id.traineddata");
            File file = new File(TESSDATA_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(
                    TESSDATA_PATH + "/id.traineddata");
            byte[] buffer = new byte[512];
            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            System.out.println("zzzzzzzzzz success"+ TESSDATA_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 拍照回调
     */
    PreviewCallback previewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if (isChoice) {
                new MyOrcTask().execute(data);
                isChoice = false;
            }

        }
    };

    Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case 1:
                    // 自动对焦~
                    try {
                        camera.autoFocus(focusCallback);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }

        }

        ;
    };

    AutoFocusCallback focusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            if (success) {
                handler.sendEmptyMessageDelayed(1, 1500);
            }
        }
    };

    /**
     * 图片解析的异步任务！
     *
     * @author kaifa
     */
    class MyOrcTask extends AsyncTask<byte[], Void, Void> {

        String text = "";

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(byte[]... params) {
            // TODO Auto-generated method stub
            byte[] data = params[0];
            //Log.v("xingyun", "看到了画面！" + data.length);
            Size size = camera.getParameters().getPreviewSize();
            //Log.v("xingyun", "Size is: w=" + size.width + "   h="+ size.height);
            try {
                YuvImage image = new YuvImage(data, ImageFormat.NV21,
                        size.width, size.height, null);
                if (image != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compressToJpeg(
                            new Rect(0, 0, size.width, size.height), 80, stream);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(
                            stream.toByteArray(), 0, stream.size());

                    System.out.println("zzzzzzzz x=" + x + " y=" + y + " w=" + width + " h=" + height + " bmpW=" + bitmap.getWidth() + " bmpH=" + bitmap.getHeight());
                    Log.v("xingyun", "x=" + x + " y=" + y + " w=" + width + " h=" + height + " bmpW=" + bitmap.getWidth() + " bmpH=" + bitmap.getHeight());
                    bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);

                    // 发送消息去解析
                    if (bitmap != null) {
                        isChoice = false;
                        bitmap = comp(bitmap);

                        bitmap = ImageFilter.grayScale(bitmap);

                        TessBaseAPI baseAPI = new TessBaseAPI();

                        // 初始化
                        baseAPI.init(TESSBASE_PATH, DEFAULT_LANGUAGE);

                        baseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);

                        baseAPI.setImage(bitmap);

                        text = baseAPI.getUTF8Text();

                        baseAPI.end();
                    }

                    stream.close();
                    Log.v("xingyun", "bitmao 大小 width：" + bitmap.getWidth()
                            + " height:" + bitmap.getHeight());
                }
            } catch (Exception ex) {
                Log.e("xingyun", "Error:" + ex.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            text.replaceAll("\n", "");
            text = text.trim();
            if (text.length() > 18) {
                String idcard = text.substring(text.length() - 18, text.length());
                //text = text.substring(text.length() - 18, text.length());
                if (IDcheckClassUtil.validateIdCard18(idcard)) {
                    try {
                        //Toast.makeText(ScanActivity.this, "成功！请核对", Toast.LENGTH_LONG).show();
                        isChoice = false;
                        textView.setText(idcard);

                        Intent intent = new Intent();
                        intent.setClassName(ScanActivity.this, returnClass);
                        intent.putExtra("idcard", idcard.toUpperCase()); // 将计算的值回传回去
                        setResult(RESULT_OK, intent);

                        finish(); // 结束当前的activity的生命周期
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    // Toast.makeText(ScanActivity.this, "就差一点点啦！", 0).show();
                    isChoice = true;
                }

            } else {
                // Toast.makeText(ScanActivity.this, "请再对齐一点点哦！", 0).show();
                // 继续去选择图片
                isChoice = true;
            }

        }
    }

    private Bitmap comp(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 400f;// 这里设置高度为800f
        float ww = 400f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 动画设置
     */
    void setAnimation() {

        Animation animation = new TranslateAnimation(0, 0, 0, height
                - Dp2Px(this, 3));
        animation.setDuration(5000);
        animation.setRepeatMode(Animation.REVERSE);// 设置反方向执行
        animation.setRepeatCount(100);
        xianView.setAnimation(animation);
        animation.startNow();
        ;

    }
}
