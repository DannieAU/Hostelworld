package com.example.hostelworld

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long
)

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatName: String
    private lateinit var adapter: MessageAdapter
    private val messagesList = mutableListOf<ChatMessage>()
    private var currentUserName = "Traveler"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        chatName = intent.getStringExtra("CHAT_NAME") ?: "General Chat"

        findViewById<TextView>(R.id.tvChatRoomTitle).text = chatName

        // --- NEW: BACK BUTTON LOGIC ---
        val btnBackChat = findViewById<TextView>(R.id.btnBackChat)
        btnBackChat.setOnClickListener {
            finish() // Closes the chat room and reveals the list behind it!
        }

        val rvMessages = findViewById<RecyclerView>(R.id.rvMessages)
        val etInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSend = findViewById<Button>(R.id.btnSendMessage)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvMessages.layoutManager = layoutManager
        adapter = MessageAdapter(messagesList)
        rvMessages.adapter = adapter

        fetchCurrentUserName()
        listenForMessages()

        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etInput.text.clear()
            }
        }
    }

    private fun fetchCurrentUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            currentUserName = doc.getString("name") ?: "Traveler"
        }
    }

    private fun listenForMessages() {
        db.collection("chats").document(chatName).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading messages.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    messagesList.clear()
                    for (doc in snapshot.documents) {
                        val senderId = doc.getString("senderId") ?: ""
                        val senderName = doc.getString("senderName") ?: "Unknown"
                        val text = doc.getString("text") ?: ""
                        val time = doc.getLong("timestamp") ?: 0L
                        messagesList.add(ChatMessage(senderId, senderName, text, time))
                    }
                    adapter.notifyDataSetChanged()

                    if (messagesList.isNotEmpty()) {
                        findViewById<RecyclerView>(R.id.rvMessages).scrollToPosition(messagesList.size - 1)
                    }
                }
            }
    }

    private fun sendMessage(text: String) {
        val uid = auth.currentUser?.uid ?: return

        val msgData = hashMapOf(
            "senderId" to uid,
            "senderName" to currentUserName,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("chats").document(chatName).collection("messages").add(msgData)
    }

    inner class MessageAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvChatSender)
            val tvText: TextView = view.findViewById(R.id.tvChatText)
            val tvTime: TextView = view.findViewById(R.id.tvChatTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val msg = messages[position]
            holder.tvName.text = msg.senderName
            holder.tvText.text = msg.text

            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            holder.tvTime.text = sdf.format(Date(msg.timestamp))

            if (msg.senderId == auth.currentUser?.uid) {
                holder.tvName.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                holder.tvName.text = "Me"
            } else {
                holder.tvName.setTextColor(android.graphics.Color.parseColor("#7C3AED"))
            }
        }

        override fun getItemCount() = messages.size
    }
}