package com.bytecause.lenslex.ui.interfaces

sealed interface CredentialType {
    data object AccountLink : CredentialType
    data object Username : CredentialType
    data object Email : CredentialType
    data object Password : CredentialType
}