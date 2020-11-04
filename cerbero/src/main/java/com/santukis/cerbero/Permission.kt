package com.santukis.cerbero

data class Permission(
        val permission: String,
        val message: String = "",
        val shouldShowMessage: Boolean = false
)