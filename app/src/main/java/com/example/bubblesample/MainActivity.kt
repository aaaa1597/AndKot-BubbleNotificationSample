package com.example.bubblesample

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val CHANNEL_ID = "bubble_chat_channel"
        const val NOTIFICATION_ID = 1001
        const val SHORTCUT_ID = "chat_shortcut_bot"
        const val PERMISSION_REQUEST_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        val btnSendBubble = findViewById<Button>(R.id.btn_send_bubble)
        btnSendBubble.setOnClickListener {
            if (checkNotificationPermission()) {
                sendBubbleNotification()
            } else {
                requestNotificationPermission()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Bubble Chat"
            val descriptionText = "Notifications for bubble chat demonstration"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setAllowBubbles(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendBubbleNotification()
            } else {
                Toast.makeText(this, "通知の権限が拒否されました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendBubbleNotification() {
        val context = this

        val targetIntent = Intent(context, BubbleActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("chat_partner_name", "サポートボット")
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val bubbleIntent = PendingIntent.getActivity(context, 0, targetIntent, flags)

        val chatPartnerIcon = IconCompat.createWithResource(context, R.mipmap.ic_launcher)
        val chatPartner = Person.Builder()
            .setName("サポートボット")
            .setIcon(chatPartnerIcon)
            .setImportant(true)
            .build()

        val category = "com.example.bubblesample.category.CHAT"
        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
            .setCategories(setOf(category))
            .setIntent(Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
            })
            .setLongLived(true)
            .setShortLabel("サポートボット")
            .setPerson(chatPartner)
            .setIcon(chatPartnerIcon)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)

        val bubbleMetadata = NotificationCompat.BubbleMetadata.Builder(
            bubbleIntent,
            chatPartnerIcon
        )
            .setDesiredHeight(600)
            .setAutoExpandBubble(true)
            .setSuppressNotification(true)
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setBubbleMetadata(bubbleMetadata)
            .setShortcutId(SHORTCUT_ID)
            .addPerson(chatPartner)
            .setStyle(
                NotificationCompat.MessagingStyle(chatPartner)
                    .addMessage("こんにちは！バブルが開きました。メッセージを送ってみてください！", System.currentTimeMillis(), chatPartner)
            )
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            Toast.makeText(this, "通知送信に必要な権限がありません", Toast.LENGTH_SHORT).show()
        }
    }
}
