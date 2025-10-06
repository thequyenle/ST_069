package net.android.st069_fakecallphoneprank.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.receivers.FakeCallReceiver

class FakeCallScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule a fake call using AlarmManager for exact timing
     */
    fun scheduleFakeCall(fakeCall: FakeCall) {
        val intent = Intent(context, FakeCallReceiver::class.java).apply {
            action = "TRIGGER_FAKE_CALL"
            putExtra("FAKE_CALL_ID", fakeCall.id)
            putExtra("NAME", fakeCall.name)
            putExtra("PHONE_NUMBER", fakeCall.phoneNumber)
            putExtra("AVATAR", fakeCall.avatar)
            putExtra("VOICE_TYPE", fakeCall.voiceType)
            putExtra("DEVICE_TYPE", fakeCall.deviceType)
            putExtra("TALK_TIME", fakeCall.talkTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            fakeCall.id.toInt(), // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate trigger time
        val triggerTime = fakeCall.scheduledTime

        try {
            // Use exact alarm for precise timing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            Log.d("FakeCallScheduler", "Scheduled fake call ID: ${fakeCall.id} at $triggerTime")
        } catch (e: SecurityException) {
            Log.e("FakeCallScheduler", "Permission denied for exact alarm", e)
            // Fallback to inexact alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Cancel scheduled fake call
     */
    fun cancelFakeCall(fakeCallId: Long) {
        val intent = Intent(context, FakeCallReceiver::class.java).apply {
            action = "TRIGGER_FAKE_CALL"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            fakeCallId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("FakeCallScheduler", "Cancelled fake call ID: $fakeCallId")
    }

    /**
     * Reschedule all active fake calls (e.g., after device reboot)
     */
    fun rescheduleAllActiveCalls(fakeCalls: List<FakeCall>) {
        val currentTime = System.currentTimeMillis()

        fakeCalls.forEach { fakeCall ->
            if (fakeCall.isActive && fakeCall.scheduledTime > currentTime) {
                scheduleFakeCall(fakeCall)
            }
        }
    }
}