package sch.id.smkn4palembang.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import sch.id.smkn4palembang.databinding.ActivityOpacBinding

class OpacActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpacBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpacBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}