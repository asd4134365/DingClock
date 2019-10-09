package atorire.dingclock;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class DingClockApplication extends Application {

//    private List<String> logData = new ArrayList<>();



    public String[] getLogData() {
        SharedPreferences sp = getSharedPreferences(KEYS.Storage.data, Context.MODE_PRIVATE);
        String logStr = sp.getString(KEYS.Storage.data_log, null);

        return logStr!=null?logStr.split(","):null;
    }

    public void addData(String resultTime, int resultCode){
        if(resultCode!=-1){
            String data = "";
            if(resultCode==0){
                data = "【" + resultTime + "】打卡失败";
            }else if(resultCode==1){
                data = "【" + resultTime + "】打卡成功";
            }

            SharedPreferences sp = getSharedPreferences(KEYS.Storage.data, Context.MODE_PRIVATE);
            String logStr = sp.getString(KEYS.Storage.data_log, null);

            if(logStr==null || logStr.length()<=0){
                logStr = data;
            }else{
                logStr += "," + data;
            }

            if(logStr.split(",").length>100){
                // 删掉最早的一条数据
                logStr = logStr.substring(logStr.indexOf(",") + 1);
//                logData.remove(0);
//                logData.remove(0);
            }
            sp.edit().putString("log", logStr).commit();



//            SharedPreferences sp = getSharedPreferences("data",Context.MODE_PRIVATE);


//            logData = new ArrayList<>();
//
//            if(logData.size()>100)
//                logData.remove(0);
//            for (String log : logArr){
//                logData.add(log);
//            }
////            logData = new ArrayList<>();
//
//            logData.add(data);
        }
    }

}
