package sch.id.smkn4palembang.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.databinding.ActivityEbookMenuBinding
import sch.id.smkn4palembang.adapter.ListEbookMenuAdapter

class EbookMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEbookMenuBinding
    private lateinit var listEbookMenuAdapter: ListEbookMenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEbookMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listEbookMenuAdapter = ListEbookMenuAdapter(applicationContext)
        listEbookMenuAdapter.setOnItemClickListener {
            Intent(Intent.ACTION_VIEW, Uri.parse(it)).apply { startActivity(this) }
        }

        binding.apply {
            recyclerview.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            val alphaAdapter = AlphaInAnimationAdapter(listEbookMenuAdapter)
            recyclerview.adapter = ScaleInAnimationAdapter(alphaAdapter).apply { setDuration(500) }
            recyclerview.setHasFixedSize(true)
        }
    }




}