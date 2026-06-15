package com.example.bubblesample

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class BubbleActivity : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var chatScroll: ScrollView
    private lateinit var editMessage: EditText
    private lateinit var btnSendMsg: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bubble)

        chatContainer = findViewById(R.id.chat_container)
        chatScroll = findViewById(R.id.chat_scroll)
        editMessage = findViewById(R.id.edit_message)
        btnSendMsg = findViewById(R.id.btn_send_msg)

        val chatPartnerName = intent.getStringExtra("chat_partner_name") ?: "サポートボット"
        findViewById<TextView>(R.id.chat_partner_name).text = chatPartnerName

        // 初期メッセージの表示
        addMessageBubble("こんにちは！こちらはバブル画面です。何かご質問はありますか？", isUser = false)

        btnSendMsg.setOnClickListener {
            val text = editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                // ユーザーのメッセージを追加
                addMessageBubble(text, isUser = true)
                editMessage.text.clear()

                // ボットの自動返信を1秒後にシミュレート
                Handler(Looper.getMainLooper()).postDelayed({
                    addMessageBubble("「$text」と送信されましたね！バブルデモは正常に動作しています。", isUser = false)
                }, 1000)
            }
        }
    }

    private fun addMessageBubble(message: String, isUser: Boolean) {
        val bubbleBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 32f
            setColor(if (isUser) Color.parseColor("#4CAF50") else Color.parseColor("#2E2E2E"))
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = if (isUser) Gravity.END else Gravity.START
            topMargin = 8
            bottomMargin = 8
            leftMargin = if (isUser) 80 else 16
            rightMargin = if (isUser) 16 else 80
        }

        val textView = TextView(this).apply {
            text = message
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(32, 20, 32, 20)
            background = bubbleBackground
            this.layoutParams = layoutParams
        }

        chatContainer.addView(textView)

        // 追加後に一番下までスクロール
        chatScroll.post {
            chatScroll.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}
