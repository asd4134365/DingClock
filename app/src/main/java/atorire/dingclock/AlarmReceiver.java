package atorire.dingclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(KEYS.TAG, "on receiver [" + Util.getDateStr() + "]");

        Util.wakeUpAndUnlock(context);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(KEYS.Intent.callClock,0);
        i.putExtra(KEYS.Intent.callClockTime, Util.getDateStr());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(i);
    }
}
