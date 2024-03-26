package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val mp = BackgroundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val titleTextView: TextView = findViewById(R.id.titleTextView)
        val dumbAiButton: Button = findViewById(R.id.dumbAiButton)
        val smartAiButton: Button = findViewById(R.id.smartAiButton)

        titleTextView.text = getString(R.string.title)

        dumbAiButton.setOnClickListener {
            val intent = Intent(this, TicTacToeActivity::class.java)
            intent.putExtra(getString(R.string.is_smart), false)
            startActivity(intent)
        }

        smartAiButton.setOnClickListener {
            val intent = Intent(this, TicTacToeActivity::class.java)
            intent.putExtra(getString(R.string.is_smart), true)
            startActivity(intent)
        }

        mp.start(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        mp.end()
    }
}