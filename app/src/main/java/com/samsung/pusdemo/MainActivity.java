package com.samsung.pusdemo;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mBatInfoReceiver;

    // 统计时间
    private String countDate;
    private int unlockCount;
    private int screenOn;

    private List<App> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        countDate = preferences.getString("count_date", millisecondToTime(System.currentTimeMillis(), "yyyy-MM-dd"));
        unlockCount = preferences.getInt("unlock_count", 0);
        screenOn = preferences.getInt("screen_on", 0);
        // 隔天清零
        if (!countDate.equals(millisecondToTime(System.currentTimeMillis(), "yyyy-MM-dd"))) {
            Log.e("clear", "隔天清零~~~~~~~~~~~~~~~~");
            countDate = millisecondToTime(System.currentTimeMillis(), "yyyy-MM-dd");
            unlockCount = 0;
            screenOn = 0;
        }

        // 两个方法就行，一个判断是否有这个模块，一个获取所有应用记录启动和使用时间的信息，foreach遍历匹配包名就行
        if (hasModule(this)) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            if (!granted) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivityForResult(intent, 1);
            }

            if (getUsageStats(this) == null) {
                toast("没有记录啊");
            } else {
                toast("该有记录啊");
                // 1. 手机总计使用时间
                long runTime = SystemClock.elapsedRealtime();
                String info = "设备已开机 : " + millisecondToTime(runTime, "HH:mm:ss")
                        + "\n------------------------------------------------------------------";
                Drawable icon = (Drawable) getResources().getDrawable(R.drawable.time);
                App app = new App(info, icon);
                appList.add(app);
                Log.e("time ", info);
                // 3. 各App使用时间
                List<UsageStats> queryUsageStats = getUsageStats(this);
                PackageManager pm = getPackageManager();
                int seconds = 0;

                for (UsageStats usageStats : queryUsageStats) {
//                    if (usageStats.getPackageName().equals("你需要查找的应用的包名")) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(usageStats.getPackageName(), PackageManager.GET_META_DATA);
                        info = "包名 : " + usageStats.getPackageName() + "\n" +
                                "应用名 ： " + pm.getApplicationLabel(appInfo) + "\n" +
                                "使用频次 : " + usageStats.getClass().getDeclaredField("mLaunchCount").getInt(usageStats) + " 次\n" +
                                "第一次变化该包对应数据的时间 : " + millisecondToTime(usageStats.getFirstTimeStamp(), "yyyy-MM-dd HH:mm:ss") + "\n" +
                                "最近一次变化该包对应数据的时间: " + millisecondToTime(usageStats.getLastTimeStamp(), "yyyy-MM-dd HH:mm:ss") + "\n" +
                                "最后一次使用时间 : " + millisecondToTime(usageStats.getLastTimeUsed(), "yyyy-MM-dd HH:mm:ss") + "\n" +
                                "在前台的总时间 : " + usageStats.getTotalTimeInForeground() / 1000 + "秒"
                                + "\n-----------------------------------";
                        icon = pm.getApplicationIcon(appInfo);
                        seconds += usageStats.getTotalTimeInForeground() / 1000;
                        app = new App(info, icon);
                        appList.add(app);
//                        Log.e("my ", info);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
//                    }
                }
                // 2. 解锁次数/亮屏时间
                final IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_USER_PRESENT);   // 屏幕解锁广播
//                filter.addAction(Intent.ACTION_SCREEN_OFF);     // 屏幕灭屏广播
                filter.addAction(Intent.ACTION_SCREEN_ON);      // 屏幕亮屏广播
                mBatInfoReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(final Context context, final Intent intent) {
                        String action = intent.getAction();
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        if (Intent.ACTION_USER_PRESENT.equals(action)) {
                            unlockCount += 1;
                            editor.putInt("unlock_count", unlockCount).apply();
                            Log.e("user present", "解锁 " + unlockCount);
                        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                            screenOn += 1;
                            editor.putInt("screen_on", screenOn).apply();
                            Log.e("screen on", "亮屏 " + screenOn);
                        }
                    }
                };
                registerReceiver(mBatInfoReceiver, filter);
                info = "设备解锁次数 : " + unlockCount + " 次"
                        + "\n设备亮屏次数 : " + screenOn + " 次"
                        + "\n设备亮屏使用 : " + seconds + " 秒"
                        + "\n------------------------------------------------------------------";
                icon = (Drawable) getResources().getDrawable(R.drawable.lock);
                app = new App(info, icon);
                appList.add(app);
                Log.e("bright ", info);
                // 列表展示
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(layoutManager);
                AppAdapter adapter = new AppAdapter(appList);
//                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }
        } else {
            toast("小于5.0，没这个功能");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatInfoReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            if (!granted) {
                Toast.makeText(this, "请开启该权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 判断是否5.0以上系统有这个功能
     * @return
     */
    public static boolean hasModule(Activity act) {
        PackageManager packageManager = act.getApplicationContext().getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     *  5.0以上系统获取应用使用情况 若支持则返回所有应用记录的数据，若无则返回null
     * @return
     */
    public static List<UsageStats> getUsageStats(Activity act){
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager)act.getApplicationContext().getSystemService("usagestats");
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,ts-200000, ts);
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return null;
        }
        return queryUsageStats;
    }

    public static String millisecondToTime(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
