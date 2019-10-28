package atorire.dingclock;

import androidx.appcompat.app.AppCompatActivity;
import atorire.dingclock.bean.TimeBean;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DevicePolicyManager policyManager;
    private ComponentName adminReceiver;

    private TableLayout clockTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 时间list
        List<TimeBean> timeData = getData();

        //获取设备管理服务
        policyManager = (DevicePolicyManager)  getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminReceiver = new ComponentName(this, ScreenOffAdminReceiver.class);
        if (!policyManager.isAdminActive(adminReceiver)) {
            Util.showToast(this,"需要开启设备管理服务");
            activeManage();
        }

        if (!isAccessibilitySettingsOn(this)) {
            Util.showToast(this,"需要开启辅助功能，找到DingClock，点击开启");
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }

        Button btnCancel,btnStart, btnTest;
        clockTable = findViewById(R.id.clock_table);
        btnStart = findViewById(R.id.btn_start);
        btnCancel = findViewById(R.id.btn_cancel);
        btnTest = findViewById(R.id.testbtn);

        for(int i =0; i<timeData.size() || i<4; i++){
            createTable(timeData.get(i));
        }

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                calendar.add(Calendar.SECOND, 3);
//                calendar.set(Calendar.HOUR_OF_DAY, 12);
//                calendar.set(Calendar.MINUTE, 01);
//                calendar.set(Calendar.SECOND, 0);
//                calendar.set(Calendar.MILLISECOND, 0);
                // 建立Intent和PendingIntent来调用目标组件
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

                // 获取闹钟管理的实例
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                // am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// sdk>=23
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// sdk<23
                    am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
                String tmpS = "设置闹钟时间为" + Util.formatInteger(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + Util.formatInteger(calendar.get(Calendar.MINUTE));

                Log.e("tag","123123--》"+tmpS);

                policyManager.lockNow();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlarmOn();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.clearAlarm(MainActivity.this);
                Util.showToast(MainActivity.this,"清除定时任务");
            }
        });
    }

    /**
     * 检测辅助功能是否开启<br>
     * 方 法 名：isAccessibilitySettingsOn <br>
     * 创 建 人 <br>
     * 创建时间：2016-6-22 下午2:29:24 <br>
     * 修 改 人： <br>
     * 修改日期： <br>
     * @param mContext c
     * @return boolean
     */
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        // TestService为对应的服务
        final String service = getPackageName() + "/" + DingClockService.class.getCanonicalName();
        Log.i(K.TAG, "service:" + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(K.TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(K.TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(K.TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.v(K.TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(K.TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(K.TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }
    /**
     * 开启设备管理
     */
    private void activeManage(){
        // 启动设备管理(隐式Intent) - 在AndroidManifest.xml中设定相应过滤器
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        //权限列表
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        //描述(additional explanation)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活后才能使用锁屏功能");
        startActivityForResult(intent, 0);
    }

    /**
     * 添加日志到控件
     */
    private void addLog2View(){
        String[] logData = Util.getLog(this);
        if(logData!=null){
            LinearLayout ll = findViewById(R.id.log_scroll_layout);
            ll.removeAllViews();
            for (int i = logData.length-1; i >= 0; i--){
                TextView textView = new TextView(this);
                LinearLayout.LayoutParams tv_layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textView.setLayoutParams(tv_layout);
                textView.setText(logData[i]);
                if((logData[i]+"").indexOf("【打卡日志】")>0){

                    textView.setTextColor(Color.parseColor("#00ADAD"));
//                            getResources().getColor(R.color.colorCheckInLogTxt,null));
                }
                ll.addView(textView);
            }
        }
    }

    /**
     * 创建界面定时工具
     * @param timeData data
     */
    private void createTable(TimeBean timeData){
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams rowLayout = new TableRow.LayoutParams();
        rowLayout.width = TableRow.LayoutParams.MATCH_PARENT;
        rowLayout.height = TableRow.LayoutParams.MATCH_PARENT;
        tableRow.setLayoutParams(rowLayout);

        TableRow.LayoutParams tv_layout = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2);

        TextView textView = new TextView(this);
        textView.setLayoutParams(tv_layout);

        if(timeData!=null) {
            textView.setText(String.format(getResources().getString(R.string.timeFormat),Util.formatInteger(timeData.getHour()),Util.formatInteger(timeData.getMinute())));
        } else {
            textView.setText(null);
        }
        tableRow.addView(textView);

        TableRow.LayoutParams layout = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button btn_set = new Button(this);
        btn_set.setLayoutParams(layout);
        btn_set.setText(R.string.btn_set);
        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TableRow tr = (TableRow)view.getParent();
                TextView tv = (TextView)tr.getChildAt(0);
                setTime(tv);
            }
        });
        tableRow.addView(btn_set);

        Button btn_del = new Button(this);
        btn_del.setLayoutParams(layout);
        btn_del.setText(R.string.btn_del);
        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TableRow tr = (TableRow)view.getParent();
                TextView tv = (TextView)tr.getChildAt(0);
                tv.setText("");
                // 更新并保存数据
                saveTimeData();
            }
        });
        tableRow.addView(btn_del);

        clockTable.addView(tableRow);
    }

    private void setAlarmOn(){
        List<TimeBean> timeData = getData();
        boolean isNextDay = false;
        // 打开界面-点击开始，不知道上次执行的时间
        // 取最近的
        TimeBean time = getNearestTimeBean();
        if(time == null) {
            isNextDay = true;
            time = timeData.get(0);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if(isNextDay){
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // Test
        // calendar.add(Calendar.MINUTE, 01);
        Log.d(K.TAG,"next time: "+calendar.getTime());

        // 建立Intent和PendingIntent来调用目标组件
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        // 获取闹钟管理的实例
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        // am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        String msg = "设置闹钟时间为 "+Util.formatInteger(calendar.get(Calendar.MONTH)+1)+"-"+Util.formatInteger(calendar.get(Calendar.DAY_OF_MONTH))+" " +
                Util.formatInteger(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + Util.formatInteger(calendar.get(Calendar.MINUTE));

        Util.showToast(this, msg);
        Util.callHome(this);
    }

    //获取存储的时间数据
    private List<TimeBean> getData(){
        Integer[][] defaultTime = {{8,30},{12,0},{12,10},{18,0}};

        List<TimeBean> timeList = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences(K.Storage.data,Context.MODE_PRIVATE);
        String time = sp.getString(K.Storage.data_time, null);
        // for test 重制时间
        // String time =  null;
        if(time==null){
            for (Integer[] t : defaultTime){
                timeList.add(new TimeBean(t[0],t[1]));
            }
        }else{
            String[] timeArr = time.split(";");
            for(String timeItem : timeArr){
                if(timeItem.length()<=0)
                    continue;
                String[] timeItemArr = timeItem.split(":");
                timeList.add(new TimeBean(Integer.parseInt(timeItemArr[0].trim()), Integer.parseInt(timeItemArr[1].trim())));
            }
        }
        Util.sort(timeList);
        return timeList;
    }

    /**
     * 保存时间
     */
    private void saveTimeData(){
        StringBuilder data = new StringBuilder();
        for(int i = 0; i<clockTable.getChildCount();i++){
            TableRow tr = (TableRow)clockTable.getChildAt(i);
            TextView tv = (TextView)tr.getChildAt(0);
            String text = tv.getText().toString();
            if("".equals(text))
                continue;
            text = text + ";";
            data.append(text);
        }
        SharedPreferences sp = getSharedPreferences(K.Storage.data,Context.MODE_PRIVATE);
        sp.edit().putString(K.Storage.data_time , data.toString()).apply();
    }


    /**
     * 设置时间
     * @param tv textview
     */
    private void setTime(final TextView tv){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        new TimePickerDialog(this,
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    // 更新控件文本
                    String timeStr = Util.formatInteger(hourOfDay)+":"+Util.formatInteger(minute);
                    tv.setText(timeStr);
                    // 更新并保存数据
                    saveTimeData();
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    /**
     * 获取最近的时间
     * @return bean
     */
    private TimeBean getNearestTimeBean(){
        List<TimeBean> timeData = getData();
        long timeMillies = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillies);
        TimeBean timeBean = null;
        for(TimeBean tb : timeData){
            calendar.set(Calendar.HOUR_OF_DAY, tb.getHour());
            calendar.set(Calendar.MINUTE, tb.getMinute());
            if(timeMillies < calendar.getTimeInMillis()){
                timeBean = tb;
                break;
            }
        }
        return timeBean;
    }

    /**
     * 窗口恢复
     */
    @Override
    protected void onResume() {
        super.onResume();

        addLog2View();

        int callClock = K.Intent.callClock_None;
        if(getIntent()!=null)
            callClock = getIntent().getIntExtra(K.Intent.callClock, K.Intent.callClock_None);

        // 执行打卡
        if(callClock==K.Intent.callClock_Wakeup){
            // 清空旧定时任务
            Util.clearAlarm(this);
            // 获取下一个定时任务时间, 设置下一个定时任务
            setAlarmOn();
            boolean isGetLogData = getIntent().getBooleanExtra(K.Intent.isGetLogData, false);
            if(isGetLogData)
                Util.doLog(this,"=====开始获取日志=====", K.LogCode.flowLog);
            else
                Util.doLog(this,"=====开始打卡=====", K.LogCode.flowLog);

            // 启动钉钉
            Util.callDingDing(this, isGetLogData);
        // 打卡完毕回调
        }else if(callClock==K.Intent.callClock_Recall){
            Util.callHome(this);

            Util.doLog(MainActivity.this,"=====打卡结束=====", K.LogCode.flowLog);
            // 2s后回到主页面, 并熄灭屏幕
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    policyManager.lockNow();
                }
            }, 2*1000);
        }
        setIntent(null);
    }



}
