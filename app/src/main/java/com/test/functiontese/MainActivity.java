package com.test.functiontese;

import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.test.functiontese.tools.BarChartManager;
import com.test.functiontese.tools.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView time;
    private BarChart mBar;
    /**
     * 统计区域
     */
    private String[] strX = new String[]{"城东", "城中", "城南", "开发区", "顾高", "蒋垛", "大伦", "张甸", "梁徐", "白米", "淤溪", "桥头", "溱潼", "娄庄", "沈高", "兴太", "俞垛", "华港", "姜水", "溱水"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        time = (TextView) findViewById(R.id.time);
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
}
