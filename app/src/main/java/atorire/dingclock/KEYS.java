package atorire.dingclock;

public class KEYS {
    public static String TAG = "DingClockLogTAG";
    public static class Intent{
        public static String callClock = "callClock";// -1无，0，唤醒，1，程序打卡结束回调
        public static String callClockTime = "callClockTime";// 打卡时间
    }
    public static class Storage{
        public static String data = "data";
        public static String data_log = "log";
        public static String data_time = "time";
    }
}
