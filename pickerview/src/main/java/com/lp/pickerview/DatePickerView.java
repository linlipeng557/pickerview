package com.lp.pickerview;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lp.pickerview.dialog.AbsBottomDialog;
import com.lp.pickerview.widget.WheelView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatePickerView extends AbsBottomDialog implements View.OnClickListener {

    WheelView wv1;
    WheelView wv2;
    WheelView wv3;
    WheelView wv4;
    WheelView wv5;
    TextView cancelTV;
    TextView submitTv;
    OnSelectedDateTimeListener onSelectedDateTimeListener;

    boolean[] types = null;
    DParams dParams;

    public DatePickerView(Context context) {
        super(context);
    }

    private void setdParams(DParams dParams) {
        this.dParams = dParams;
    }

    @Override
    public int getLayoutId() {
        return R.layout.picker_date;
    }

    @Override
    public void initView() {
        initWV();
        cancelTV = findViewById(R.id.cancel_Tv);
        submitTv = findViewById(R.id.submit_Tv);
        cancelTV.setOnClickListener(this);
        submitTv.setOnClickListener(this);

    }

    @Override
    public void initData() {
        initWVData();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cancel_Tv) {
            dismiss();
        } else if (i == R.id.submit_Tv) {
            if (onSelectedDateTimeListener != null) {
                onSelectedDateTimeListener.onSelectedDateTime(types[0] ? (wv1.getCurrentPosition() + dParams.startYear) : 0,
                        types[1] ? (wv2.getCurrentPosition() + 1) : 0,
                        types[2] ? (wv3.getCurrentPosition() + 1) : 0,
                        types[3] ? wv4.getCurrentPosition() : 0,
                        types[4] ? wv5.getCurrentPosition() : 0);
            }
            dismiss();
        }
    }


    public void setOnSelectedDateTimeListener(OnSelectedDateTimeListener onSelectedDateTimeListener) {
        this.onSelectedDateTimeListener = onSelectedDateTimeListener;
    }

    private void initWV() {
        if (dParams == null) {
            dParams = DParams.getDefault().init();
        }
        types = dParams.type.booleans;
        wv1 = findViewById(R.id.wv1);
        wv2 = findViewById(R.id.wv2);
        wv3 = findViewById(R.id.wv3);
        wv4 = findViewById(R.id.wv4);
        wv5 = findViewById(R.id.wv5);

        if (types[0]) {
            wv1.setVisibility(View.VISIBLE);
            wv1.setOnSelectPickerListener(onSelectPickerListener);
        } else {
            wv1.setVisibility(View.GONE);
        }
        if (types[1]) {
            wv2.setVisibility(View.VISIBLE);
            wv2.setOnSelectPickerListener(onSelectPickerListener);
        } else {
            wv2.setVisibility(View.GONE);
        }
        if (types[2]) {
            wv3.setVisibility(View.VISIBLE);
        } else {
            wv3.setVisibility(View.GONE);
        }
        if (types[3]) {
            wv4.setVisibility(View.VISIBLE);
        } else {
            wv4.setVisibility(View.GONE);
        }
        if (types[4]) {
            wv5.setVisibility(View.VISIBLE);
        } else {
            wv5.setVisibility(View.GONE);
        }
    }

    private void initWVData() {
        if (types[0])
            wv1.setList(getNumber(dParams.startYear, dParams.yearCount));
        if (types[1])
            wv2.setList(getNumber02(1, 12));
        if (types[3])
            wv4.setList(getNumber02(0, 24));
        if (types[4])
            wv5.setList(getNumber02(0, 60));

        wv1.post(new Runnable() {
            @Override
            public void run() {
                if (types[0])
                    wv1.setCurrentPosition(dParams.year - dParams.startYear);
                if (types[1])
                    wv2.setCurrentPosition(dParams.month);

                if (types[2]) {
                    wv3.setList(getNumber02(1, getDays()));
                    wv3.setCurrentPosition(dParams.day - 1);
                }
                if (types[3])
                    wv4.setCurrentPosition(dParams.hour);
                if (types[4])
                    wv5.setCurrentPosition(dParams.min);
            }
        });

        wv1.setOnSelectPickerListener(onSelectPickerListener);
        wv2.setOnSelectPickerListener(onSelectPickerListener);
        wv1.setOnSelectPickerListener(new WheelView.OnSelectPickerListener() {
            @Override
            public void onSelect(View view, int poistion, Object o) {
                Log.d("WheelView", "poistion:" + poistion + "  Object:" + o);
            }
        });
    }


    WheelView.OnSelectPickerListener onSelectPickerListener = new WheelView.OnSelectPickerListener() {
        @Override
        public void onSelect(View view, int poistion, Object o) {
            wv3.setList(getNumber02(1, getDays()));
        }
    };

    private int getDays() {
        int year = wv1.getCurrentPosition() + 1999;
        int m = wv2.getCurrentPosition() + 1;
        if (m == 2) {
            if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                return 29;
            } else {
                return 28;
            }
        } else {
            return days[m - 1];
        }
    }

    static final int[] days = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};


    private static List<String> getNumber(int start, int total) {
        List<String> strings = new ArrayList<>(total);
        for (int i = start; i < start + total; i++) {
            strings.add(String.valueOf(i));
        }
        return strings;
    }

    private static List<String> getNumber02(int start, int total) {
        List<String> strings = new ArrayList<>(total);
        for (int i = start; i < start + total; i++) {
            strings.add(String.format("%02d", i));
        }
        return strings;
    }


    public static class Builder {
        DParams dParams;
        Context context;
        DatePickerView datePickerView;
        OnSelectedDateTimeListener onSelectedDateTimeListener;

        public Builder(Context context) {
            dParams = new DParams();
            this.context = context;
        }

        public Builder setType(Type type) {
            dParams.type = type;
            return this;
        }

        public Builder setStartYear(int startYear) {
            dParams.startYear = startYear;
            return this;
        }

        public Builder setYearCount(int count) {
            dParams.yearCount = count;
            return this;
        }


        public Builder setOnSelectedDateTimeListener(OnSelectedDateTimeListener onSelectedDateTimeListener) {
            this.onSelectedDateTimeListener = onSelectedDateTimeListener;
            return this;
        }
        public Builder setDateTime(Integer y, Integer m, Integer d, Integer h, Integer min) {
            if (y != null && y != 0)
                dParams.year = y;
            if (m != null && m != 0)
                dParams.month = m;
            if (d != null && d != 0)
                dParams.day = d;
            if (h != null)
                dParams.hour = h;
            if (min != null)
                dParams.min = min;
            return this;
        }

        private void create() {
            datePickerView = new DatePickerView(context);
            datePickerView.setdParams(dParams.init());
            datePickerView.setOnSelectedDateTimeListener(onSelectedDateTimeListener);
        }

        public DatePickerView show() {
            create();
            datePickerView.show();
            return datePickerView;
        }

    }

    private static class DParams {
        Type type = Type.YMDHM;
        Integer startYear = null;
        int yearCount = 100;
        Integer year = null;
        Integer month = null;
        Integer day = null;
        Integer hour = null;
        Integer min = null;

        static DParams getDefault() {
            return new DParams();
        }

        DParams init() {
            Calendar cal = null;
            if (year == null || month == null || day == null || hour == null || min == null) {
                cal = Calendar.getInstance();
            }
            if (cal == null) {
                return this;
            }
            if (year == null) {
                year = cal.get(Calendar.YEAR);
            }
            if (month == null) {
                month = cal.get(Calendar.MONTH);
            }
            if (day == null) {
                day = cal.get(Calendar.DATE);
            }
            if (hour == null) {
                hour = cal.get(Calendar.HOUR_OF_DAY);
            }
            if (min == null) {
                min = cal.get(Calendar.MINUTE);
            }
            if (startYear == null) {
                startYear = year - (yearCount / 2);
            }
            if (year > (startYear + yearCount)) {
                year = startYear + yearCount;
            }
            return this;
        }
    }

    public enum Type {
        YMDHM(true, true, true, true, true),
        MD(false, true, true, false, false),
        YMD(true, true, true, false, false),
        HM(false, false, false, true, true),
        MDHM(false, true, true, true, true);

        boolean[] booleans = new boolean[5];

        Type(boolean b0, boolean b1, boolean b2, boolean b3, boolean b4) {
            booleans[0] = b0;
            booleans[1] = b1;
            booleans[2] = b2;
            booleans[3] = b3;
            booleans[4] = b4;
        }
    }

    public interface OnSelectedDateTimeListener {
        void onSelectedDateTime(int y, int m, int d, int h, int min);
    }

}
