package atorire.dingclock;

class K {
    static final String DingDingPackage = "com.alibaba.android.rimet";
    static final String QQPackage = "com.tencent.mobileqq";
    static String TAG = "DingClockLogTAG";
    // intent跳转所带参数
    static class Intent{
        /**
         * 程序回调的key
         */
        static String callClock = "callClock";
        /**
         * 未知操作唤起主程序
         */
        static int callClock_None = -1;// -1无，0，唤醒，1，程序打卡结束回调
        /**
         * 开始打卡唤醒主程序
         */
        static int callClock_Wakeup = 0;
        /**
         * 程序打卡结束回调主程序
         */
        static int callClock_Recall = 1;
        static String isGetLogData = "isGetLogData";// 打卡时间
    }
    //存储的key及内容
    static class Storage{
        static String data = "data";
        static String data_log = "log";
        static String data_time = "time";
    }
    // 日志code
    static class LogCode{
        static int none = -1;
        static int fail = 0;
        static int success = 1;
        static int flowLog = 10;
    }
    //qq的标识
    static class QQSendCode{
        static int none = -1;
        static int getLog = 0;
        static int doCheckIn = 1;
    }
}
