package sch.id.smkn4palembang.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val searchTitle: String? = null,
    val documentID: String? = null,
    val cover: String? = null,
    val title: String? = null,
    val publisher: String? = null,
    val isbn: String? = null,
    val language: String? = null,
    val category: String? = null,
    val stock: String? = null,
    val availability: String? = null,
    val description: String? = null,
    val timestamp: Timestamp? = null
) : Parcelable
