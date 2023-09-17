package sch.id.smkn4palembang.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityOpacBinding

class OpacActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpacBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpacBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            socialSciencesCardview.setOnClickListener(::onClick)
            appliedScienceCardview.setOnClickListener(::onClick)
            religionSciencesCardview.setOnClickListener(::onClick)
            philosophyScienceCardview.setOnClickListener(::onClick)
            languageSciencesCardview.setOnClickListener(::onClick)
            kesustaraanScienceCardview.setOnClickListener(::onClick)
            computerSciencesCardview.setOnClickListener(::onClick)
            pureScienceCardview.setOnClickListener(::onClick)
            sportSciencesCardview.setOnClickListener(::onClick)
            artScienceCardview.setOnClickListener(::onClick)
        }
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.social_sciences_cardview -> {  }
            R.id.applied_science_cardview -> {  }
            R.id.religion_sciences_cardview -> {  }
            R.id.philosophy_science_cardview -> {  }
            R.id.language_sciences_cardview -> {  }
            R.id.kesustaraan_science_cardview -> {  }
            R.id.computer_sciences_cardview -> {  }
            R.id.pure_science_cardview -> {  }
            R.id.sport_sciences_cardview -> {  }
            R.id.art_science_cardview -> {  }
        }
    }

}