package e.roman.minesweeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val playBtn = findViewById<Button>(R.id.play_btn)
        playBtn.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        val autoPlayButton = findViewById<Button>(R.id.auto_play_btn)
        autoPlayButton.setOnClickListener { startActivity(Intent(this, AutoPlayActivity::class.java)) }
    }
}