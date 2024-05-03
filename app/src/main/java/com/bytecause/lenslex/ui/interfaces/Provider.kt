package com.bytecause.lenslex.ui.interfaces

sealed interface Provider {
    data object Google : Provider
    data object Email : Provider
}