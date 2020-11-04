package com.santukis.cerbero

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import java.util.concurrent.atomic.AtomicInteger

@TargetApi(Build.VERSION_CODES.M)
internal class RequestPermissionActivity : Activity(), ActivityCompat.OnRequestPermissionsResultCallback {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        requestRequiredPermissions()
    }

    private fun requestRequiredPermissions() {
        val permissions = getPermissions(intent)

        val permissionsWithRationalMessage = permissions.filter { it.message.isNotEmpty() &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, it.permission) }

        when {
            permissionsWithRationalMessage.isNotEmpty() -> showPermissionMessages(permissionsWithRationalMessage, permissions)
            else -> requestPermissions(permissions)
        }
    }

    private fun requestPermissions(permissions: List<Permission>) {
        ActivityCompat.requestPermissions(this, permissions.map { it.permission }.toTypedArray(), REQUEST_PERMISSIONS_CODE)
    }

    private fun showPermissionMessages(permissionsWithMessage: List<Permission>, allPermission: List<Permission>) {
        val watchedMessages = AtomicInteger(0)

        permissionsWithMessage.forEach {  permission ->
            AlertDialog.Builder(this)
                    .setMessage(permission.message)
                    .setOnDismissListener {
                        if (watchedMessages.incrementAndGet() == permissionsWithMessage.size) {
                            requestPermissions(allPermission)
                        }
                    }
                    .show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val deniedPermissions = mutableListOf<Permission>()

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            permissions.forEach {
                if (grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_DENIED) {
                    deniedPermissions.add(Permission(
                            permission = it,
                            shouldShowMessage = ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                    ))
                }
            }
        }

        checkPermissionsAndFinish(deniedPermissions)
    }

    private fun checkPermissionsAndFinish(deniedPermissions: MutableList<Permission>) {
        when {
            deniedPermissions.isEmpty() -> PermissionsWizard.onGranted?.invoke()
            else -> PermissionsWizard.onPermissionsMissing?.invoke(deniedPermissions)
        }

        finish()
    }

    private fun getPermissions(intent: Intent): List<Permission> {
        val permissions = mutableListOf<Permission>()
        val requestedPermissions = intent.getStringArrayListExtra(PERMISSIONS_TO_REQUEST_KEY)
        val messages = intent.getStringArrayListExtra(PERMISSIONS_MESSAGES_KEY)

        requestedPermissions?.forEachIndexed { index, permission ->
            try {
                permissions.add(Permission(permission = permission, message = messages?.get(index) ?: ""))

            } catch (exception: Exception) {
                permissions.add(Permission(permission = permission))
            }
        }

        return permissions
    }

    companion object {
        const val PERMISSIONS_TO_REQUEST_KEY = "PERMISSIONS_TO_REQUEST"
        const val PERMISSIONS_MESSAGES_KEY = "PERMISSIONS_MESSAGES"
        const val REQUEST_PERMISSIONS_CODE = 1010
    }
}