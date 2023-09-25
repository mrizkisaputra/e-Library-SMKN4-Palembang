package sch.id.smkn4palembang.model

data class BookBorrowing(
    val documentID: String? = null,
    val name: String? = null,
    val id: String? = null,
    val contact: String? = null,
    val title: String? = null,
    val isbn: String? = null,
    val condition: String? = null,
    val borrowingDate: String? = null,
    val returnDate: String? = null,
    val dateTime: Any? = null,
    val timestamp: Long? = null,
    var isExpanded: Boolean = false
)