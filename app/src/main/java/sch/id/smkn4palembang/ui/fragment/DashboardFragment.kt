package sch.id.smkn4palembang.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.FragmentDashboardBinding
import sch.id.smkn4palembang.ui.EbookMenuActivity
import sch.id.smkn4palembang.ui.OpacActivity

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private fun initImageSlider() {
        binding.apply {
            viewFlipper.isAutoStart = true
            viewFlipper.flipInterval = 4300
            viewFlipper.setInAnimation(requireActivity(), android.R.anim.slide_in_left)
            viewFlipper.setOutAnimation(requireActivity(), android.R.anim.slide_out_right)
            viewFlipper.startFlipping()
        }
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.epaper_cardview -> {
                val uri: Uri? = Uri.parse("https://www.tribunnews.com/epaper")
                Intent(Intent.ACTION_VIEW, uri).apply { startActivity(this) }
            }

            R.id.ebook_carview -> {
                Intent(requireActivity(), EbookMenuActivity::class.java).apply { startActivity(this) }
            }

            R.id.literacy_cardview -> {
                val uri: Uri? = Uri.parse("https://literasidigital.id/buku")
                Intent(Intent.ACTION_VIEW, uri).also { startActivity(it) }
            }

            R.id.comic_education_cardview -> {
                val uri: Uri? = Uri.parse("https://komik.pendidikan.id/online/")
                Intent(Intent.ACTION_VIEW, uri).also { startActivity(it) }
            }

            R.id.comic_international_cardview -> {
                val uri: Uri? = Uri.parse("https://www.letsreadasia.org/")
                Intent(Intent.ACTION_VIEW, uri).also { startActivity(it) }
            }

            R.id.short_story_cardview -> {
                val uri: Uri? = Uri.parse("https://www.republika.co.id/kanal/puisi-sastra/cerpen")
                Intent(Intent.ACTION_VIEW, uri).apply { startActivity(this) }
            }

            R.id.web_school_cardview -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.smkn4palembang.sch.id"))
                startActivity(intent)
            }

            R.id.opac_cardview -> {
                Intent(requireActivity(), OpacActivity::class.java).apply { startActivity(this) }
            }

            R.id.video_profile_cardview -> {  }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initImageSlider()
        binding.apply {
            epaperCardview.setOnClickListener(::onClick)
            ebookCarview.setOnClickListener(::onClick)
            literacyCardview.setOnClickListener(::onClick)
            comicEducationCardview.setOnClickListener(::onClick)
            comicInternationalCardview.setOnClickListener(::onClick)
            shortStoryCardview.setOnClickListener(::onClick)
            webSchoolCardview.setOnClickListener(::onClick)
            opacCardview.setOnClickListener(::onClick)
            videoProfileCardview.setOnClickListener(::onClick)

            membersAreaButton.setOnClickListener {
                AlertDialog.Builder(requireActivity()).apply {
                    setMessage("Masih dalam tahap Pengembangan !!!")
                    setCancelable(true)
                }.create().show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}