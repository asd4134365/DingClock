package atorire.dingclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import atorire.dingclock.bean.TimeBean;

public class Util {

    static void clearAlarm(Context context, Class<?> receiver, int requestCode){
        Intent intent = new Intent(context, receiver);

        // FLAG_NO_CREATE表示如果描述的pi不存在，则返回null，而不是创建它。
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        if(pendingIntent!=null) {
            pendingIntent.cancel();
            // 获取闹钟管理实例
            AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            // 取消
            am.cancel(pendingIntent);
        }
    }

    public static String getDateStr(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sdf.format(new Date());
    }
    /**
     * 格式化字符串7:3-->07:03
     * @param x x
     * @return
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
}
