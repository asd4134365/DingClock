package atorire.dingclock;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class DingClockApplication extends Application {

//    private List<String> logData = new ArrayList<>();



    public String[] getLogData() {
        SharedPreferences sp = getSharedPreferences(K.Storage.data, Context.MODE_PRIVATE);
        String logStr = sp.getString(K.Storage.data_log, null);

        return logStr!=null?logStr.split(","):null;
    }

    public void addData(String resultData, int resultCode){
        if(resultCode != -1){
            String data = "";
            if(resultCode== K.LogCode.fail){
                data = "["+Util.getDateStr()+"]【打卡日志】打卡失败（提示早退卡，故不打卡）";
            }else if(resultCode== K.LogCode.success){
                data = "["+Util.getDateStr()+"]【打卡日志】打卡成功";
            }else if(resultCode== K.LogCode.flowLog){
                data = "["+Util.getDateStr()+"]【流程日志】" + resultData + "";
            }

            SharedPreferences sp = getSharedPreferences(K.Storage.data, Context.MODE_PRIVATE);
            String logStr = sp.getString(K.Storage.data_log, null);

            if(logStr==null || logStr.length()<=0){
                logStr = data;
            }else{
                logStr += "," + data;
            }

            // 存储日志 100条上限
            if(logStr.split(",").length>100){
                // 删掉最早的一条数据
                logStr = logStr.substring(logStr.indexOf(",") + 1);
            }
            sp.edit().putString("log", logStr).apply();

        }
    }
}
