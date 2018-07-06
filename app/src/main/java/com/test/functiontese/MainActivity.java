package com.test.functiontese;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.test.functiontese.camera.CameraActivity;
import com.test.functiontese.tools.BarChartManager;
import com.test.functiontese.tools.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private TextView time;
    private BarChart mBar;
    private Button mSb;
    private ImageView imageView;
    /**
     * 统计区域
     */
    private String[] strX = new String[]{"城东", "城中", "城南", "开发区", "顾高", "蒋垛", "大伦", "张甸", "梁徐", "白米", "淤溪", "桥头", "溱潼", "娄庄", "沈高", "兴太", "俞垛", "华港", "姜水", "溱水"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        time = (TextView) findViewById(R.id.time);
        mSb = findViewById(R.id.sb);
        imageView = (ImageView) findViewById(R.id.main_image);

        mSb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(CameraActivity.TYPE_ID_CARD_FRONT);
            }
        });
        Utils.initDatePicker(MainActivity.this,time,false);
        mBar  = findViewById(R.id.bar_chart);
        //设置x轴的标题
        ArrayList<String> xLabels = new ArrayList<>();
        for (int i = 0; i < strX.length; i++) {
            xLabels.add(strX[i]);
        }
        System.out.print(strX);
        //设置x轴的数据
        ArrayList<Float> xValues = new ArrayList<>();
        for (int i = 0; i < strX.length; i++) {
            xValues.add((float) i);
        }


        //设置y轴的数据()
        List<Float> yValues1 = new ArrayList<>();
        for (int j = 0; j < strX.length; j++) {
            yValues1.add((float) (Math.random() * 35));
        }

        //设置y轴的数据()
        List<Float> yValues2 = new ArrayList<>();
        for (int j = 0; j < strX.length; j++) {
            yValues2.add((float) (Math.random() * 35));
        }

        //设置y轴的数据()
        List<Float> yValues3 = new ArrayList<>();
        for (int j = 0; j <= strX.length; j++) {
            yValues3.add((float) (Math.random() * 40));
        }
        BarChartManager bm = new BarChartManager();
        bm.setTwoBarChart(mBar, xLabels, yValues1, yValues2, "", "");
        mBar.invalidate();
        Matrix mMatrix = new Matrix();
        mMatrix.postScale(2.5f, 1f);
        mBar.getViewPortHandler().refresh(mMatrix, mBar, false);
        mBar.animateY(800);


    }
    /**
     * 拍摄证件照片
     *
     * @param type 拍摄证件类型
     */
    private void takePhoto(int type) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x12);
            return;
        }
        CameraActivity.navToCamera(this, type);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == CameraActivity.REQUEST_CODE) {
            //获取文件路径，显示图片
            if (data != null) {
                String path = data.getStringExtra("result");
                String text = data.getStringExtra("string");
//                String name = text.substring(text.indexOf("姓名")+2,text.indexOf("性别"));
//                String xb = text.substring(text.indexOf("性别")+2,text.indexOf("民族"));
//                String mz = text.substring(text.indexOf("民族")+2,text.indexOf("出生"));
////                String cs = text.substring(text.indexOf("出生")+2,text.indexOf("住址"));
//                String zz = text.substring(text.indexOf("住址")+2,text.indexOf("日")+1);
//                String sfz = text.substring(text.indexOf("号码")+2,text.length());
                Pattern pattern = Pattern.compile("[0-9]*");
                Matcher isNum = pattern.matcher(data.getStringExtra("string").substring(text.length()-18,text.length()));
                if (!isNum.matches()){
                    //反的
                    time.setText(data.getStringExtra("string").substring(0,18));

                }else {
                    time.setText(data.getStringExtra("string").substring(text.length()-18,text.length()));

                }

                if (!TextUtils.isEmpty(path)) {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(path));
                }
            }
        }
    }
}
