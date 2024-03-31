package com.bytecause.lenslex.util

fun String.capital(): String = this.replaceFirstChar { it.uppercase() }