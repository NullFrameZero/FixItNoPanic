package com.example.fixitnopanic

data class Country(
    val name: String,
    val code: String,
    val flag: Int,
    val isDefault: Boolean = false
)