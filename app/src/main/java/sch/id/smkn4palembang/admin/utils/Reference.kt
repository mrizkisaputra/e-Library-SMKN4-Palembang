package sch.id.smkn4palembang.admin.utils

import java.util.UUID


/**
 * ini adalah singleton object untuk membuat reference/folder didalam firebase storage
 */
object Reference {
    const val IMAGES_ROOT_REF: String = "images"
    const val BOOKS_CHILD_REF: String = "books"
    const val VISITOR_CHILD_REF: String = "visitors"
    const val VISITOR_COLLECTION: String = "visitors"
    const val MEMBERS_COLLECTION = "members"
    const val MEMBER_CHILD_REF: String = "members"
    val FILE_NAME_REF: String = "IMG${UUID.randomUUID()}.jpeg"
}