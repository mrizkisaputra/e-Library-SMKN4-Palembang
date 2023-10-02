package sch.id.smkn4palembang.model

import com.google.firebase.firestore.FieldValue

data class Visitor(
    val photo: String? = null,
    val name: String? = null,
    val id: String? = null,
    val role: String? = null,
    val visitingTime: String? = null,
    val timetamp: Long? = null
)