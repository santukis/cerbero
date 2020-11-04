package com.santukis.cerbero

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionsWizard(private val context: Context){

    private var permissions: List<Permission> = listOf()

    fun withPermissions(vararg permissions: String): PermissionsWizard {
        this.permissions = permissions.map { Permission(permission = it) }
        return this
    }

    fun withPermissions(vararg permissions: Permission): PermissionsWizard {
        this.permissions = permissions.toList()
        return this
    }

    fun check(onGranted: () -> Unit = {}, onPermissionsMissing: (List<Permission>) -> Unit = {}) {
        val uncheckedPermissions = permissions.filter { !checkPermission(it) }
        Companion.onGranted = onGranted
        Companion.onPermissionsMissing = onPermissionsMissing

        requestPermissionsIfNeeded(uncheckedPermissions)
    }

    fun release() {
        onGranted = null
        onPermissionsMissing = null
    }

    private fun isPermissionGranted(grantResult: Int): Boolean {
        return grantResult == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission(permission: Permission) = isPermissionGranted(ContextCompat.checkSelfPermission(context, permission.permission))

    private fun requestPermissionsIfNeeded(uncheckedPermissions: List<Permission>) {
        when {
            uncheckedPermissions.isEmpty() -> onGranted?.invoke()
            else -> requestPermissions(uncheckedPermissions)
        }
    }

    private fun requestPermissions(uncheckedPermissions: List<Permission>) {
        val check = Intent(context, RequestPermissionActivity::class.java)
        check.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        check.putStringArrayListExtra(RequestPermissionActivity.PERMISSIONS_TO_REQUEST_KEY, ArrayList(uncheckedPermissions.map { it.permission }))
        check.putStringArrayListExtra(RequestPermissionActivity.PERMISSIONS_MESSAGES_KEY, ArrayList(uncheckedPermissions.map { it.message }))
        context.startActivity(check)
    }

    companion object {
        var onGranted: (() -> Unit)? = null
        var onPermissionsMissing: ((List<Permission>) -> Unit)? = null
    }
}