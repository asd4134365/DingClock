package atorire.dingclock;

import androidx.appcompat.app.AppCompatActivity;
import atorire.dingclock.bean.TimeBean;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DevicePolicyManager policyManager;
    private ComponentName adminReceiver;


    private Integer defaultTime[][] = {{8,30},{12,00},{12,10},{18,01}};
    private List<TimeBean> timeData = new ArrayList<>();
    private Calendar calendar = Calendar.getInstance();

    private TableLayout clockTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeData = getData();

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

        for(int i =0;i<timeData.size() || i<4;i++){
            createTable(i);
        }

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
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
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                String tmpS = "设置闹钟时间为" + Util.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + Util.format(calendar.get(Calendar.MINUTE));

                Log.e("tag","123123--》"+tmpS);

                policyManager.lockNow();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setData();
                setAlarmOn(getNearestTimeBean());
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarmOff();
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
     * @param mContext
     * @return boolean
     */
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        // TestService为对应的服务
        final String service = getPackageName() + "/" + DingClockService.class.getCanonicalName();
        Log.i(KEYS.TAG, "service:" + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(KEYS.TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(KEYS.TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(KEYS.TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.v(KEYS.TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(KEYS.TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(KEYS.TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }
    private void activeManage(){
        // 启动设备管理(隐式Intent) - 在AndroidManifest.xml中设定相应过滤器
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        //权限列表
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        //描述(additional explanation)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活后才能使用锁屏功能");
        startActivityForResult(intent, 0);
    }

    private void setAlarmOn(TimeBean timeBean){

        boolean isNextDay = false;
        // 打开界面-点击开始，不知道上次执行的时间
        // 取最近的
        TimeBean time = timeBean;
        if(time == null) {
            isNextDay = true;
            time = timeData.get(0);
        }
        calendar.setTimeInMillis(System.currentTimeMillis());
        if(isNextDay){
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }

        // TODO test 3s后执行打卡
//         calendar.add(Calendar.SECOND, 3);
        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Log.e("tag","next time: "+calendar.getTime());

        // 建立Intent和PendingIntent来调用目标组件
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        // 获取闹钟管理的实例
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        String msg = "设置闹钟时间为 "+Util.format(calendar.get(Calendar.MONTH)+1)+"-"+Util.format(calendar.get(Calendar.DAY_OF_MONTH))+" " +
                Util.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + Util.format(calendar.get(Calendar.MINUTE));

        Util.showToast(this, msg);
        callHome();
    }

    //获取存储的时间数据
    private List<TimeBean> getData(){
        List<TimeBean> timeList = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences(KEYS.Storage.data,Context.MODE_PRIVATE);
        String time = sp.getString(KEYS.Storage.data_time, null);
//        重制时间
//        String time =  null;
        if(time==null){
            for (Integer[] t : defaultTime){
                timeList.add(new TimeBean(t[0],t[1]));
            }
            sort(timeList);
            return timeList;
        }else{
            String timeArr[] = time.split(";");
            for(String timeItem : timeArr){
                if(timeItem.length()<=0)
                    continue;
                String timeItemArr[] = timeItem.split(":");
                timeList.add(new TimeBean(Integer.parseInt(timeItemArr[0].trim()), Integer.parseInt(timeItemArr[1].trim())));
            }
            sort(timeList);
            return timeList;
        }
    }

    private void setData(){
        String data = "";
        for(int i = 0; i<clockTable.getChildCount();i++){
            TableRow tr = (TableRow)clockTable.getChildAt(i);
            TextView tv = (TextView)tr.getChildAt(0);
            String text = tv.getText().toString();
            if("".equals(text))
                continue;
            data += text+";";
        }
        sort(timeData);
        SharedPreferences sp = getSharedPreferences(KEYS.Storage.data,Context.MODE_PRIVATE);
        sp.edit().putString(KEYS.Storage.data_time , data).commit();
    }

    /**
     * 创建界面定时工具
     * @param index
     */
    private void createTable(final int index){
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams rowLayout = new TableRow.LayoutParams();
        rowLayout.width = TableRow.LayoutParams.MATCH_PARENT;
        rowLayout.height = TableRow.LayoutParams.MATCH_PARENT;
        tableRow.setLayoutParams(rowLayout);

        TableRow.LayoutParams tv_layout = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2);

        TextView textView = new TextView(this);
        textView.setLayoutParams(tv_layout);

        if(index<timeData.size())
            textView.setText(Util.format(timeData.get(index).getHour())+":"+Util.format(timeData.get(index).getMinute()));
        else {
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
                setTime(tv, index);
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
            }
        });
        tableRow.addView(btn_del);

        clockTable.addView(tableRow);
    }

    // 设置时间
    private void setTime(final TextView tv, final Integer index){
        calendar.setTimeInMillis(System.currentTimeMillis());
        new TimePickerDialog(this,
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Integer timeArr[] = {hourOfDay,minute};
                    String timeStr = Util.format(hourOfDay)+":"+Util.format(minute);
                    timeData.remove(index);
                    timeData.add(new TimeBean(timeArr[0],timeArr[1]));
                    tv.setText(timeStr);
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    /**
     * 关闭定时
     * 删除定时任务
     */
    private void setAlarmOff(){
        Util.clearAlarm(this,AlarmReceiver.class,0);
        Util.showToast(this,"清除定时任务");
    }

    /**
     * 获取最近的时间
     * @return
     */
    private TimeBean getNearestTimeBean(){
        long timeMillies = System.currentTimeMillis();
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

    private String callClockTime = "";

    /**
     * 窗口恢复
     */
    @Override
    protected void onResume() {
        super.onResume();

        addLog2View();

        int callClock = -1;
        if(getIntent()!=null)
            callClock = getIntent().getIntExtra(KEYS.Intent.callClock, -1);

        Log.e(KEYS.TAG,"--->"+callClock);

        if(callClock==0){
            String callClockTime = getIntent().getStringExtra(KEYS.Intent.callClockTime);
            if(!this.callClockTime.equals(callClockTime)){// 时间
                this.callClockTime = callClockTime;

                // 清空旧定时任务
                Util.clearAlarm(this,AlarmReceiver.class,0);
                // 获取下一个定时任务时间
                TimeBean timeBean = getNearestTimeBean();
                // 设置下一个定时任务
                setAlarmOn(timeBean);
                // 启动钉钉
                Util.callDingDing(this);
            }
        }else if(callClock==1){
            // 2s后回到主页面, 并熄灭屏幕
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    callHome();
                    policyManager.lockNow();
                }
            }, 2*1000);
        }
        setIntent(null);
    }

    private void addLog2View(){
        DingClockApplication app = (DingClockApplication)getApplication();

        String[] logData = app.getLogData();
        if(logData!=null){
            LinearLayout ll = findViewById(R.id.log_scroll_layout);
            ll.removeAllViews();
            for (int i = logData.length-1; i >= 0; i--){
                TextView textView = new TextView(this);
                LinearLayout.LayoutParams tv_layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textView.setLayoutParams(tv_layout);
                textView.setText(logData[i]);
                ll.addView(textView);
            }
        }
    }

    public void callHome(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
        intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
        startActivity(intent);
    }

    /**
     * key排序，由大到小
     * @return
     */
    public static List<TimeBean> sort(List<TimeBean> list) {
        Collections.sort(list, new Comparator<TimeBean>() {
            public int compare(TimeBean o1, TimeBean o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return list;
    }


}
