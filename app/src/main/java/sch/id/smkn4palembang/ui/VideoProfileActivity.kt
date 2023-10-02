package sch.id.smkn4palembang.ui

import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityVideoProfileBinding

class VideoProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val video = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/x1lQXC14Dzk?si=uehdnWx8MFBugnOy\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>"

        binding.apply {
            videoProfileWebView.loadData(video, "text/html", "utf-8")
            videoProfileWebView.settings.javaScriptEnabled = true
            videoProfileWebView.webChromeClient = WebChromeClient()
        }

    }

}