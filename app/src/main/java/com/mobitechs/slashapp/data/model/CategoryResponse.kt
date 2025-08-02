package com.mobitechs.slashapp.data.model


data class CategoryListResponse(
    val data: List<CategoryItem>,
    val success: Boolean
)
data class CategoryItem(
    val id: String,
    val name: String,
    val icon: String?,
    val display_order: Int,
    val is_active: Int,
    val created_at: String?,
    val updated_at: String?
)

