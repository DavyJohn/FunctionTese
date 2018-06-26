package com.test.functiontese.tools;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.test.functiontese.widget.CustomDatePicker;

public class Utils {
    Context context;
    public Utils(Context context){
        this.context = context;
    }
    public static  void  initDatePicker(final Context context, final TextView tv, boolean isInit){
        if(isInit){
            tv.setText(DateUtils.getNowMin());
        }
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String now = DateUtils.getNowMin();
                    if(DateUtils.isValidDateMi(tv.getText().toString())) {
                        now = tv.getText().toString();
                    }
                    final CustomDatePicker startT = new CustomDatePicker(context, new CustomDatePicker.ResultHandler() {
                        @Override
                        public void handle(String time) { // 回调接口，获得选中的时间
                            // times = time;
                            tv.setText(time);
                        }
                    }, "2010-01-01 00:00", "2099-01-01 00:00"); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
                    startT.showSpecificTime(true); // 显示时和分
                    startT.setIsLoop(true); // 允许循环滚动
                    startT.show(now);
                }catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

}
