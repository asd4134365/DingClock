package atorire.dingclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(K.TAG, "on receiver [" + Util.getDateStr() + "]");
        Util.callSelfAndDoCheckIn(context,false);
    }
}
