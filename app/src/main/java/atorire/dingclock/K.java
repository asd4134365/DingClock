package atorire.dingclock;

class K {
    static String TAG = "DingClockLogTAG";
    static class Intent{
        static String callClock = "callClock";// -1无，0，唤醒，1，程序打卡结束回调
        static String callClockTime = "callClockTime";// 打卡时间
    }
    static class Storage{
        static String data = "data";
        static String data_log = "log";
        static String data_time = "time";
    }
    static class LogCode{
        static int none = -1;
        static int fail = 0;
        static int success = 1;
        static int flowLog = 10;
    }
}
