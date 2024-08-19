package com.example.mobiledevproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmSetupUtility {

    public static void scheduleHabitResetAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Daily reset at midnight
        Calendar dailyCalendar = Calendar.getInstance();
        dailyCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dailyCalendar.set(Calendar.MINUTE, 0);
        dailyCalendar.set(Calendar.SECOND, 0);
        if (dailyCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            dailyCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        Intent dailyIntent = new Intent(context, HabitResetReceiver.class);
        dailyIntent.setAction("RESET_DAILY_HABITS");
        PendingIntent dailyPendingIntent = PendingIntent.getBroadcast(context, 0, dailyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dailyCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, dailyPendingIntent);

        // Weekly reset at midnight going from Sunday to Monday
        Calendar weeklyCalendar = Calendar.getInstance();
        weeklyCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        weeklyCalendar.set(Calendar.HOUR_OF_DAY, 0);
        weeklyCalendar.set(Calendar.MINUTE, 0);
        weeklyCalendar.set(Calendar.SECOND, 0);
        if (weeklyCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            weeklyCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        Intent weeklyIntent = new Intent(context, HabitResetReceiver.class);
        weeklyIntent.setAction("RESET_WEEKLY_HABITS");
        PendingIntent weeklyPendingIntent = PendingIntent.getBroadcast(context, 1, weeklyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, weeklyCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, weeklyPendingIntent);

        // Monthly reset at midnight on the first day of the month
        Calendar monthlyCalendar = Calendar.getInstance();
        monthlyCalendar.set(Calendar.DAY_OF_MONTH, 1);
        monthlyCalendar.set(Calendar.HOUR_OF_DAY, 0);
        monthlyCalendar.set(Calendar.MINUTE, 0);
        monthlyCalendar.set(Calendar.SECOND, 0);
        if (monthlyCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            monthlyCalendar.add(Calendar.MONTH, 1);
        }
        Intent monthlyIntent = new Intent(context, HabitResetReceiver.class);
        monthlyIntent.setAction("RESET_MONTHLY_HABITS");
        PendingIntent monthlyPendingIntent = PendingIntent.getBroadcast(context, 2, monthlyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, monthlyCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 30, monthlyPendingIntent);
    }
}

