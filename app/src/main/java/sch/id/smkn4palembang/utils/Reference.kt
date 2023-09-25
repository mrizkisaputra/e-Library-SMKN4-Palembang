package sch.id.smkn4palembang.utils



/**
 * ini adalah singleton object untuk membuat reference/folder didalam firebase storage
 */
object Reference {
    const val IMAGES_ROOT_REF: String = "images"
    const val BOOKS_CHILD_REF: String = "books"
    const val VISITOR_CHILD_REF: String = "visitors"
    const val MEMBER_CHILD_REF: String = "members"

    const val VISITOR_COLLECTION: String = "visitors"
    const val MEMBERS_COLLECTION = "members"
    const val BORROWING_COLLECTION = "borrowing"
    const val BORROWING_HISTORY_COLLECTION = "borrowing_history"
    const val BOOKS_COLLECTION: String = "books"

}