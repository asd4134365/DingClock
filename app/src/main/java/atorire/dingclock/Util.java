package atorire.dingclock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.PowerManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import atorire.dingclock.bean.TimeBean;

public class Util {
    /**
     * 清除闹铃
     * @param context context
     * @param receiver receiver
     * @param requestCode code
     */
    static void clearAlarm(Context context, Class<?> receiver, int requestCode){
        Intent intent = new Intent(context, receiver);

        // FLAG_NO_CREATE表示如果描述的pi不存在，则返回null，而不是创建它。
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        if(pendingIntent!=null) {
            pendingIntent.cancel();
            // 获取闹钟管理实例
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            // 取消
            am.cancel(pendingIntent);
        }
    }

    static String getDateStr(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
    /**
     * 格式化字符串7:3-->07:03
     * @param x 数字
     * @return 数字格式化补0
     */
    public static String format(int x) {
        String s = "" + x;
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    static List<TimeBean> sort(List<TimeBean> list) {
        Collections.sort(list, new Comparator<TimeBean>() {
            public int compare(TimeBean o1, TimeBean o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return list;
    }

    static void showToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    static void callDingDing(Context context){
        String dingPackageName = "com.alibaba.android.rimet";
        DingClockService.setStepReady();
        try{
            PackageInfo pi = context.getPackageManager().getPackageInfo(dingPackageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null ) {
                dingPackageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ComponentName cn = new ComponentName(dingPackageName, className);

                intent.setComponent(cn);
                context.startActivity(intent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 屏幕唤醒
     * @param context context
     */
    static void wakeUpAndUnlock(Context context){
        //屏锁管理器
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.SCREEN_BRIGHT_WAKE_LOCK,"DingClock:bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }


    static String[] getLog(Activity a){
        DingClockApplication app = (DingClockApplication)a.getApplication();
        return app.getLogData();
    }
    static void doLog(Service s, String data, int resultCode){
        DingClockApplication app = (DingClockApplication)s.getApplication();
        doLog(app,data,resultCode);
    }
    static void doLog(Activity a, String data, int resultCode){
        DingClockApplication app = (DingClockApplication)a.getApplication();
        doLog(app,data,resultCode);
    }
    private static void doLog(DingClockApplication app, String data, int resultCode){
        app.addData(data, resultCode);
    }

}
