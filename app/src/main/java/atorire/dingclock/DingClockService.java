package atorire.dingclock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class DingClockService extends AccessibilityService {
    private final String DingDingPackage = "com.alibaba.android.rimet";
    private final String QQPackage = "com.tencent.mobileqq";

    static final String QQ_TEXT_KEYs[][] = {{"0","获取打卡日志","1"}};

    private static int step = -1;//-1 不执行 0 待命 1 工作页面 2 打卡页面 3 打卡 4 打卡结果 5 返回'我的'页面

    private int resultCode = K.LogCode.none;

    public DingClockService() {  }

    public static void setStepReady(){
        step = 0;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        final int eventType = accessibilityEvent.getEventType();
        String packageName = accessibilityEvent.getPackageName().toString();
        if(DingDingPackage.equals(packageName)){
            Log.d(K.TAG, "钉钉事件--->" + accessibilityEvent);

            // app窗口切换
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                step_test();
                if(step==0) {// 前往【工作】标签页
                    step1_gotoWorkPage();
                }
                if(step==4){// 返回【我的】页面
                    step = -1;
                    step5_goBack2MinePage();
                }
                //通知栏事件
            }else if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                // app内窗口切换
            } else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
                if(step==1){// 前往【打卡】页面
                    step = -1;
                    step2_gotoCheckInPage();
                }

                if(step==2){// 点击【打卡】
                    step = -1;
                    step3_doCheckIn();
                }
                if(step==3){// 获取点击打卡后的返回信息，通过判断是早退打卡还是正常打卡，得知是否正常打卡
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
                                    Log.e(K.TAG,"截图～～");
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
            Log.w(K.TAG, "rootWindow为空");
            return;
        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("工作");
        for (AccessibilityNodeInfo n : list) {
            if("工作".equals(n.getContentDescription()+"")){
                Log.d(K.TAG, "click【工作】"+n);
                Util.doLog(this, "点击【工作】标签页", K.LogCode.flowLog);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                step = 1;
                break;
            }
        }
    }

    private void step2_gotoCheckInPage(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(K.TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> allChildNodes = new ArrayList<>();

        findAllChildNodes(allChildNodes, nodeInfo);
        for (AccessibilityNodeInfo node : allChildNodes) {
            if(node!=null){
                Log.w(K.TAG,"1==="+node+"");
                if("考勤打卡".equals(node.getContentDescription()+"")){
                    Log.d(K.TAG,"click【考勤打卡】"+node);
                    Util.doLog(this, "点击【考勤打卡】", K.LogCode.flowLog);
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
            Log.w(K.TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> allChildNodes = new ArrayList<>();

        findAllChildNodes(allChildNodes, nodeInfo);

        for (AccessibilityNodeInfo node : allChildNodes) {
            if(node!=null &&
                    ("下班打卡".equals(node.getContentDescription()+"") || "上班打卡".equals(node.getContentDescription()+""))) {
                Log.d(K.TAG,"click【打卡】"+node);
                Util.doLog(this, "点击【打卡】按钮", K.LogCode.flowLog);
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
        } else {
            // 获取之前的打卡日志
            String logData = "";
            for (int i=0;i<allChildNodes.size();i++){
                AccessibilityNodeInfo node = allChildNodes.get(i);

                if(node!=null){
                    String contentDescription = node.getContentDescription()+"";
                    if("打卡时间".equals(contentDescription)){
                        String lastContentDescription = "";
                        String nextContentDescription = "";
                        if(allChildNodes.get(i-1)!=null)
                            lastContentDescription = allChildNodes.get(i-1).getContentDescription()+"";
                        if(allChildNodes.get(i+1)!=null)
                            nextContentDescription = allChildNodes.get(i+1).getContentDescription()+"";
                        logData += lastContentDescription+"("+contentDescription+nextContentDescription+");";
                    }
                }
            }
            Log.e(K.TAG,logData);
        }
    }
    private void step4_doGetResult(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(K.TAG, "rootWindow为空");
            return;
        }

        List<AccessibilityNodeInfo> allChildNodes = new ArrayList<>();

        findAllChildNodes(allChildNodes, nodeInfo);
        resultCode = K.LogCode.none;
        for (AccessibilityNodeInfo node : allChildNodes) {
//            Log.d(TAG,"==="+step+">>>"+node);
            if(node!=null){
                String description = node.getContentDescription()+"";
                if(resultCode!= K.LogCode.none){
                    Log.d(K.TAG,"gotResult【resultCode】"+resultCode);
                    step = 4;
                    break;
                }
                if(description.startsWith("确定要打") || "不打卡".equals(description)){
                    resultCode = K.LogCode.fail;
                }else if("我知道了".equals(description)){
                    resultCode = K.LogCode.success;
                }
            }
        }

        if(step==4){
            Util.doLog(this, "", resultCode);
            // go back
            this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            Log.e(K.TAG,"result:"+resultCode);
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
            Log.w(K.TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("我的");
        for (AccessibilityNodeInfo n : list) {
            if("我的".equals(n.getContentDescription())){
                Log.d(K.TAG, "click【我的】"+n);
                Util.doLog(this, "返回【我的】标签页", K.LogCode.flowLog);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                step = -1;
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

//        PackageManager packageManager = this.getPackageManager();
//        Intent intent= packageManager.getLaunchIntentForPackage("atorire.dingclock");
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.setPackage(null);
//        startActivity(intent);

        Util.doLog(this, "唤起DingClock主程序并息屏", K.LogCode.flowLog);
        // 唤起主程序
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(K.Intent.callClock,1);
        startActivity(i);

        // 2s后回到主页面
//        new Handler().postDelayed(new Runnable(){
//            public void run() {
//                callHome();
//            }
//        }, 2*1000);
    }

//    public void callHome(){
//        this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
//    }

    private void step_test(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(K.TAG, "rootWindow为空");
            return;
        }

        for(int i=0;i<nodeInfo.getChildCount();i++){
            Log.w(K.TAG,"---"+step+"--->>"+nodeInfo.getChild(i));
        }
        List<AccessibilityNodeInfo> allChildNodes = new ArrayList<>();

        findAllChildNodes(allChildNodes, nodeInfo);

        for (AccessibilityNodeInfo node : allChildNodes) {
            Log.e(K.TAG,"==="+step+"--->>"+node);
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


    private List<AccessibilityNodeInfo> findAllChildNodes(List<AccessibilityNodeInfo> nodes, AccessibilityNodeInfo parentNode) {
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
