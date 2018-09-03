<pre data-anchor-id="1lou"><code>allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}


dependencies {
        implementation 'com.github.linlipeng557:pickerview:0.0.5'
}


现在只有一个时间选择器，其他功能后续加。
//DatePickerView.Type.YMDHM     年月日时分
//DatePickerView.Type.MD        月日
//DatePickerView.Type.YMD       年月日
//DatePickerView.Type.HM        时分
//DatePickerView.Type.MDHM      年月时分


new DatePickerView.Builder(MainActivity.this).setType(DatePickerView.Type.YMDHM).setOnSelectedDateTimeListener(new DatePickerView.OnSelectedDateTimeListener() {
                    @Override
                    public void onSelectedDateTime(int i, int i1, int i2, int i3, int i4) {
                            //对应年月日时分
                    }
                }).show();

</code></pre>
