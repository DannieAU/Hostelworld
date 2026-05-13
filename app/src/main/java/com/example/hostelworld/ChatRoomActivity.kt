package com.example.hostelworld

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ChatRoomActivity : AppCompatActivity() {

    private val messages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val chatName = intent.getStringExtra("CHAT_NAME") ?: "Chat"

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarChatRoom)
        toolbar.title = chatName
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        // Mock some previous messages to make it look alive
        messages.add("System: Welcome to $chatName!")
        messages.add("Sarah: Hey everyone! Anyone want to grab dinner tonight?")

        val lvMessages = findViewById<ListView>(R.id.lvMessages)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
        lvMessages.adapter = adapter

        // Handle sending a new message
        val etInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSend = findViewById<Button>(R.id.btnSendMessage)

        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                messages.add("You: $text")
                adapter.notifyDataSetChanged() // Refresh list
                etInput.text.clear() // Clear input box
                lvMessages.setSelection(adapter.count - 1) // Scroll to bottom
            }
        }
    }
}