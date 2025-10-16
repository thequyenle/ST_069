package net.android.st069_fakecallphoneprank.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import net.android.st069_fakecallphoneprank.activity.IncomingCallActivity

class FakeCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "TRIGGER_FAKE_CALL" -> {
                Log.d("FakeCallReceiver", "Fake call triggered!")

                // Get data from intent
                val fakeCallId = intent.getLongExtra("FAKE_CALL_ID", -1)
                val name = intent.getStringExtra("NAME") ?: "Unknown"
                val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
                val avatar = intent.getStringExtra("AVATAR")
                val voiceType = intent.getStringExtra("VOICE_TYPE")
                val deviceType = intent.getStringExtra("DEVICE_TYPE")
                val talkTime = intent.getIntExtra("TALK_TIME", 15)

                // DEBUG: Log avatar path
                Log.d("FakeCallReceiver", "Avatar received: '$avatar'")
                if (avatar != null) {
                    val avatarFile = java.io.File(avatar)
                    Log.d("FakeCallReceiver", "Avatar file exists: ${avatarFile.exists()}")
                }

                // Launch incoming call activity
                val callIntent = Intent(context, IncomingCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("FAKE_CALL_ID", fakeCallId)
                    putExtra("NAME", name)
                    putExtra("PHONE_NUMBER", phoneNumber)
                    putExtra("AVATAR", avatar)
                    putExtra("VOICE_TYPE", voiceType)
                    putExtra("DEVICE_TYPE", deviceType)
                    putExtra("TALK_TIME", talkTime)
                }

                Log.d("FakeCallReceiver", "Starting IncomingCallActivity with avatar: '$avatar'")
                context.startActivity(callIntent)
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("FakeCallReceiver", "Device rebooted - rescheduling calls")
                // Reschedule all active calls after reboot
                // This will be handled in a separate service
            }
        }
    }
}