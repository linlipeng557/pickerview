package com.lp.pickerview_demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lp.pickerview.DatePickerView;

public class MainActivity extends AppCompatActivity {

    DatePickerView datePickerView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tv);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datePickerView == null) {
                    datePickerView = new DatePickerView.Builder(MainActivity.this).setType(DatePickerView.Type.YMD).show();
                    datePickerView.setOnSelectedDateTimeListener(new DatePickerView.OnSelectedDateTimeListener() {
                        @Override
                        public void onSelectedDateTime(int y, int m, int d, int h, int min) {
                            textView.setText(String.format("%d-%02d-%02d %02d:%02d", y, m, d, h, min));
                            Log.d("MainActivity", String.format("%d-%02d-%02d %02d:%02d", y, m, d, h, min));
                        }
                    });
                } else {
                    datePickerView.show();
                }
            }
        });
    }


}
