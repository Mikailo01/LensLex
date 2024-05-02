package com.bytecause.lenslex.models

import java.lang.Exception

sealed interface SimpleResult {
    data object OnSuccess : SimpleResult
    data class OnFailure(val exception: Exception?) : SimpleResult
}