package atorire.dingclock.bean;

import androidx.annotation.NonNull;
import atorire.dingclock.Util;

public class TimeBean {
    private int hour,minute;

    public TimeBean(int hour, int minute) {
        super();
        this.hour = hour;
        this.minute = minute;
    }
    public int getHour() {
        return hour;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public int getMinute() {
        return minute;
    }
    public void setMinute(int minute) {
        this.minute = minute;
    }

    @NonNull
    @Override
    public String toString() {
        return Util.format(this.hour)+":"+Util.format(this.minute);
    }
}
