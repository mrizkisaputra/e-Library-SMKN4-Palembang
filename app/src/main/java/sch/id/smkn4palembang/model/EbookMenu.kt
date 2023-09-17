package sch.id.smkn4palembang.model

import androidx.annotation.DrawableRes

data class EbookMenu(
    @DrawableRes val imageResourceId: Int,
    val subtitle: String,
    val url: String
)