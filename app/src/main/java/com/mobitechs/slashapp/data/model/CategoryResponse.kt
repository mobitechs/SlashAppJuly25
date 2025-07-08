package com.mobitechs.slashapp.data.model


data class CategoryListResponse(
    val data: List<CategoryItem>,
    val success: Boolean
)
data class CategoryItem(
    val id: Int,
    val name: String,
    val icon: String?,
    val display_order: Int,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String
)

