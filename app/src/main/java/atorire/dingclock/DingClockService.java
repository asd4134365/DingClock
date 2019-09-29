package atorire.dingclock;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DingClockService extends AccessibilityService {
    private final String DingDingPackage = "com.alibaba.android.rimet";
    private final String QQPackage = "com.tencent.mobileqq";

    static final String QQ_TEXT_KEYs[][] = {{"0","截图"}};

    private String TAG = "tag";
    private static int step = -1;//-1 不执行 0 待命 1 工作页面 2 打卡页面 3 打卡 4 打卡结果 5 返回'我的'页面

    private int resultCode = -1;// -1 没返回，0 失败，1 成功

    public DingClockService() {  }

    public static void setStepReady(){
        step = 0;
    }
    private static void setStepNotReady(){
        step = -1;
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        final int eventType = accessibilityEvent.getEventType();
        String packageName = accessibilityEvent.getPackageName().toString();
        if(DingDingPackage.equals(packageName)){
            Log.d(TAG, "钉钉事件--->" + accessibilityEvent);


//            if(true)
//                return;
            // app窗口切换
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                step_test();

                if(step==0) {
//                    return;
                    step1_gotoWorkPage();
                }
                if(step==4){
                    step = -1;
                    step5_goBack2MinePage();
                }
                //通知栏事件
            }else if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                // app内窗口切换
            } else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
                Log.d(TAG, "钉钉事件className--"+step+"->" + accessibilityEvent.getClassName());

                String className = accessibilityEvent.getClassName().toString();
                if(step==1){
                    step = -1;
                    step2_gotoCheckInPage();
                }

                if(step==2){
                    step = -1;
                    step3_doCheckIn();
                }
                if(step==3){
                    step = -1;
                    step4_doGetResult();
                }
            }
        }else if(QQPackage.equals(packageName)){
            if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                List<CharSequence> texts = accessibilityEvent.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence t : texts) {
                        String text = String.valueOf(t);
                        for ( String[] kMap : QQ_TEXT_KEYs){
                            boolean isContains = false;
                            for (String k : kMap){
                                if (text.contains(k)) {
//                                    openNotification(event);
                                    Log.e(TAG,"截图～～");
                                    isContains = true;
                                    break;
                                }
                            }
                            if(isContains)
                                break;
                        }
                    }
                }
            }
        }
    }
    private void step1_gotoWorkPage(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("工作");
        for (AccessibilityNodeInfo n : list) {
            if("工作".equals(n.getContentDescription())){
                Log.d(TAG, "click【工作】"+n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                step = 1;
                break;
            }
        }
    }

    private void step2_gotoCheckInPage(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> allChildNodes = new ArrayList<>();

        findAllChildNodes(allChildNodes, nodeInfo);
        for (AccessibilityNodeInfo node : allChildNodes) {
            if(node!=null){
                CharSequence cs = node.getContentDescription();
                if("考勤打卡".equals(cs)){
                    Log.d(TAG,"click【考勤打卡】"+node);
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    step = 2;
                    break;
                }
            }
        }
        if(step!=2){
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    step2_gotoCheckInPage();
                }
            }, 5*1000);
        }
    }
    private void step3_doCheckIn(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> allChildNodes = new ArrayList<>();

        findAllChildNodes(allChildNodes, nodeInfo);

        for (AccessibilityNodeInfo node : allChildNodes) {
            if(node!=null &&
                    ("下班打卡".equals(node.getContentDescription()) || "上班打卡".equals(node.getContentDescription()))) {
                Log.d(TAG,"click【打卡】"+node);
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                step = 3;
                break;
            }
        }
        if(step!=3){
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    step3_doCheckIn();
                }
            }, 5*1000);
        }
    }
    private void step4_doGetResult(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        List<AccessibilityNodeInfo> allChildNodes = new ArrayList<>();

        findAllChildNodes(allChildNodes, nodeInfo);
        resultCode = -1;
        for (AccessibilityNodeInfo node : allChildNodes) {
//            Log.d(TAG,"==="+step+">>>"+node);
            if(node!=null){
                String description = node.getContentDescription()+"";
                if(resultCode!=-1){
                    Log.d(TAG,"gotResult【resultCode】"+resultCode);
                    step = 4;
                    break;
                }
                if(description.startsWith("确定要打") || "不打卡".equals(description)){
                    resultCode = 0;
                }else if("我知道了".equals(description)){
                    resultCode = 1;
                }
            }
        }

        if(step==4){
            // go back
            this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            Log.e(TAG,"result:"+resultCode);
        }else{
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    step4_doGetResult();
                }
            }, 5*1000);
        }
    }
    private void step5_goBack2MinePage(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("我的");
        for (AccessibilityNodeInfo n : list) {
            if("我的".equals(n.getContentDescription())){
                Log.d(TAG, "click【我的】"+n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                setStepNotReady();
                break;
            }
        }
        if(step==-1){
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    callSelf();
                }
            }, 2*1000);
        }
    }
    private void callSelf(){
        DingClockApplication app = (DingClockApplication)getApplication();
        app.addData(Util.getDateStr(),resultCode);

//        PackageManager packageManager = this.getPackageManager();
//        Intent intent= packageManager.getLaunchIntentForPackage("atorire.dingclock");
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.setPackage(null);
//        startActivity(intent);

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        i.putExtra("callClock", true);
        startActivity(i);

        // 1s后回到主页面
        new Handler().postDelayed(new Runnable(){
            public void run() {
                callHome();
            }
        }, 2*1000);
    }

    public void callHome(){
        this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    private void step_test(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        for(int i=0;i<nodeInfo.getChildCount();i++){
            Log.w(TAG,"---"+step+"--->>"+nodeInfo.getChild(i));
        };
        List<AccessibilityNodeInfo> allChildNodes = new ArrayList<>();

        findAllChildNodes(allChildNodes, nodeInfo);

        for (AccessibilityNodeInfo node : allChildNodes) {
//            Log.e(TAG,"==="+step+"--->>"+node);
//            if(node!=null &&
//                    ("下班打卡".equals(node.getContentDescription()) || "上班打卡".equals(node.getContentDescription()))) {
//                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            }
        }
    }
    @Override
    public void onInterrupt() {
        Util.showToast(this,"中断服务");
    }

    public List<AccessibilityNodeInfo> findAllChildNodes(List<AccessibilityNodeInfo> nodes, AccessibilityNodeInfo parentNode) {
        int nodeCount = parentNode.getChildCount();
        for (int i = 0; i < nodeCount; i++) {
            AccessibilityNodeInfo nodeinfo = parentNode.getChild(i);
            nodes.add(parentNode.getChild(i));
            if (nodeinfo != null) {
                int childCount = nodeinfo.getChildCount();
                if (childCount > 0) {
                    findAllChildNodes(nodes, nodeinfo);
                }
            }

        }
        return nodes;
    }
}
