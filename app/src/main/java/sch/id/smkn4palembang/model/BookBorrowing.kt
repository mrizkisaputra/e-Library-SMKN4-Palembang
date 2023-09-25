package sch.id.smkn4palembang.model

data class BorrowingBook(
    val name: String? = null,
    val id: String? = null,
    val contact: String? = null,
    val title: String? = null,
    val isbn: String? = null,
    val condition: String? = null,
    val borrowingDate: String? = null,
    val returnDate: String? = null,
    val dateTime: Any? = null
)