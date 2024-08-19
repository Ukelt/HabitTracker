package com.example.mobiledevproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class NotificationSetupUtility {

    public static void scheduleNotificationAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Daily notification at 8PM
        Calendar dailyCalendar = Calendar.getInstance();
        dailyCalendar.set(Calendar.HOUR_OF_DAY, 20);
        dailyCalendar.set(Calendar.MINUTE, 0);
        dailyCalendar.set(Calendar.SECOND, 0);
        if (dailyCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            dailyCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        Intent dailyIntent = new Intent(context, NotificationReceiver.class);
        dailyIntent.setAction("DAILY_NOTIFICATION");
        PendingIntent dailyPendingIntent = PendingIntent.getBroadcast(context, 3, dailyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dailyCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, dailyPendingIntent);

        // Weekly notification on Sunday at 7PM
        Calendar weeklyCalendar = Calendar.getInstance();
        weeklyCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        weeklyCalendar.set(Calendar.HOUR_OF_DAY, 19);
        weeklyCalendar.set(Calendar.MINUTE, 0);
        weeklyCalendar.set(Calendar.SECOND, 0);
        if (weeklyCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            weeklyCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        Intent weeklyIntent = new Intent(context, NotificationReceiver.class);
        weeklyIntent.setAction("WEEKLY_NOTIFICATION");
        PendingIntent weeklyPendingIntent = PendingIntent.getBroadcast(context, 4, weeklyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, weeklyCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, weeklyPendingIntent);

        // Monthly notification on 28th at 12AM
        Calendar monthlyCalendar = Calendar.getInstance();
        monthlyCalendar.set(Calendar.DAY_OF_MONTH, 28);
        monthlyCalendar.set(Calendar.HOUR_OF_DAY, 0);
        monthlyCalendar.set(Calendar.MINUTE, 0);
        monthlyCalendar.set(Calendar.SECOND, 0);
        if (monthlyCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            monthlyCalendar.add(Calendar.MONTH, 1);
        }
        Intent monthlyIntent = new Intent(context, NotificationReceiver.class);
        monthlyIntent.setAction("MONTHLY_NOTIFICATION");
        PendingIntent monthlyPendingIntent = PendingIntent.getBroadcast(context, 5, monthlyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, monthlyCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 30, monthlyPendingIntent);
    }
}

