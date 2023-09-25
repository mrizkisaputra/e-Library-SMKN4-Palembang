package sch.id.smkn4palembang.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import sch.id.smkn4palembang.databinding.ActivityDetailOpacBinding

class DetailOpacActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailOpacBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         binding = ActivityDetailOpacBinding.inflate(layoutInflater)
         setContentView(binding.root)

    }
}