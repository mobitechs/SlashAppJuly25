package com.mobitechs.slashapp.data.model

data class Pagination(
    val current_page: Int,
    val per_page: Int,
    val total_count: Int,
    val total_pages: Int,
    val total_results: Int
)