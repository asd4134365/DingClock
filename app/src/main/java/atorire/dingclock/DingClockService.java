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

    static final Integer QQ_TEXT_KEYs[] = {K.QQSendCode.getLog, K.QQSendCode.doCheckIn};

    private static int step = -1;//-1 不执行 0 待命 1 工作页面 2 打卡页面 3 打卡 4 打卡结果 5 返回'我的'页面
    private static boolean isGetLogAction = false;// 当前操作是否是qq获取日志
    private int resultCode = K.LogCode.none;

    private StringBuilder checkInLogData = null;

    public DingClockService() {  }

    public static void setStepReady(boolean isGetLogAction){
        DingClockService.step = 0;
        DingClockService.isGetLogAction = isGetLogAction;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        final int eventType = accessibilityEvent.getEventType();
        String packageName = accessibilityEvent.getPackageName().toString();
        if(K.DingDingPackage.equals(packageName)){
//            Log.d(K.TAG, "钉钉事件--->" + accessibilityEvent);
            // app窗口切换
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if(step==0) {// 前往【工作】标签页
                    step1_gotoWorkPage();
                }
                if(step==4){// 返回【我的】页面
                    step = -1;
                    step5_goBack2MinePage();
                }
                //通知栏事件
//            }else if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
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
                    if(!isGetLogAction){
                        step4_doGetResult();
                    }
                }
            }
        }else if(K.QQPackage.equals(packageName)){
            if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                int qqNotificationTag = K.QQSendCode.none;
                List<CharSequence> texts = accessibilityEvent.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence t : texts) {
                        String text = String.valueOf(t);
                        Log.e(K.TAG, "===>"+text);
                        boolean isContains = false;
                        for ( int kMap : QQ_TEXT_KEYs){
//                            String QQUser = "Atori.re";
//                            if (text.contains(QQUser) && text.contains(kMap[0])) {
                            if (text.contains(kMap+"")) {
//                                    openNotification(event);
                                Log.d(K.TAG,"收到QQ命令："+kMap);
                                isContains = true;
                                qqNotificationTag = kMap;
                                break;
                            }
                        }
                        if(isContains)
                            break;
                    }
                }

                if(qqNotificationTag==K.QQSendCode.doCheckIn){// 执行打卡
                    Util.doLog(this, "=====QQ调起-打卡=====", K.LogCode.flowLog);
                    Util.callSelfAndDoCheckIn(this,false);
                }else if(qqNotificationTag==K.QQSendCode.getLog){// 获取打卡数据
                    Util.doLog(this, "=====QQ调起-获取打卡数据=====", K.LogCode.flowLog);
                    Util.callSelfAndDoCheckIn(this, true);
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
        for (final AccessibilityNodeInfo node : allChildNodes) {
            if(node!=null){
                if("考勤打卡".equals(node.getContentDescription()+"")){
                    Log.d(K.TAG,"click【考勤打卡】"+node);
                    Util.doLog(this, "点击【考勤打卡】", K.LogCode.flowLog);
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }, 1000);

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

        for (final AccessibilityNodeInfo node : allChildNodes) {
            if(node!=null &&
                    ("下班打卡".equals(node.getContentDescription()+"") || "上班打卡".equals(node.getContentDescription()+""))) {
                if(!isGetLogAction){
                    Log.d(K.TAG,"click【打卡】"+node);
                    Util.doLog(this, "点击【打卡】按钮", K.LogCode.flowLog);

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }, 1000);
                }
                step = 3;
                break;
            }
        }

        checkInLogData = new StringBuilder();
        if(step!=3){
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    step3_doCheckIn();
                }
            }, 5*1000);
        } else {
            if(isGetLogAction) {
                // 获取之前的打卡日志
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
                            String logData = lastContentDescription + "(" + contentDescription + nextContentDescription + ");";
                            checkInLogData.append(logData);
                        }
                    }
                }
                this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                step5_goBack2MinePage();
            }
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
            Log.d(K.TAG,"result:"+resultCode);
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
        if(!isGetLogAction){
            if(step==-1){
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        Util.doLog(DingClockService.this, "唤起DingClock主程序并息屏", K.LogCode.flowLog);
                        Util.callSelfWhenCheckInFinish(DingClockService.this);
                    }
                }, 2*1000);
            }
        }else{
            this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            Util.doLog(DingClockService.this, "打卡记录:"+checkInLogData.toString(), K.LogCode.flowLog);
            Log.e(K.TAG,"call QQ 返回数据");
            Log.e(K.TAG,checkInLogData.toString());
            // TODO call QQ 返回数据
        }
    }


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
