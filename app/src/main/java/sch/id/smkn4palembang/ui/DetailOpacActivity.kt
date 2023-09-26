package sch.id.smkn4palembang.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityDetailOpacBinding
import sch.id.smkn4palembang.member.ui.MemberLoginActivity
import sch.id.smkn4palembang.model.Book
import java.text.SimpleDateFormat
import java.util.Locale

class DetailOpacActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailOpacBinding
    private var book: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailOpacBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appbar.setNavigationOnClickListener { finish() }
        handleIntentData()

        binding.bookingBookButton.setOnClickListener {
            startActivity(Intent(this, MemberLoginActivity::class.java))
        }

        binding.shareWhatsappButton.setOnClickListener {
            val message = """
                Halo! Saya ingin merekomendasikan sebuah buku yang saya baca baru-baru ini. Judul bukunya adalah "[${book?.title}]" karya [${book?.publisher}]. Saya sangat terkesan dengan buku ini.

                Ringkasan singkat:
                ${book?.description}

                Jika Anda mencari bacaan yang menginspirasi atau menghibur, saya sarankan untuk mencoba membaca buku ini. Saya yakin Anda akan menikmatinya!

                Terima kasih dan selamat membaca!
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)

            startActivity(intent)

        }

        binding.shareFacebookButton.setOnClickListener {

        }

    }

    private fun handleIntentData() {
        val position = intent.getIntExtra(OpacActivity.EXTRA_POSITION, 0)
        book = intent.getParcelableExtra(OpacActivity.EXTRA_BOOK)

        book?.let { bindDetailCatalog(it, position) }
    }

    private fun bindDetailCatalog(book: Book, position: Int) {
        val date = book.timestamp?.toDate()
        val dateFormat = SimpleDateFormat("MMMM/dd/yyyy", Locale.getDefault()).format(date)

        binding.apply {

            Glide.with(this@DetailOpacActivity)
                .load(book.cover)
                .placeholder(R.drawable.default_book_cover)
                .into(detailCoverImageview)

            detailTitleTextview.text = book.title
            detailCategoryTextview.text = book.category
            detailIsbnTextview.text = book.isbn
            detailPublisherTextview.text = if (book.publisher.isNullOrBlank()) "-" else book.publisher
            detailLanguageTextview.text = if (book.language.isNullOrBlank()) "-" else book.language
            detailAvailabilityTextview.text = book.availability
            detailStockTextview.text = book.stock
            detailDescriptionTextview.text = if (book.description.isNullOrBlank()) "-" else book.description
            detailUploadTextview.text = dateFormat

        }
    }




}