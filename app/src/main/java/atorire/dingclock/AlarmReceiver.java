package atorire.dingclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "on receiver [" + Util.getDateStr() + "]");

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("callClock",true);
        i.putExtra("callClockTime",Util.getDateStr());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(i);
    }
}
