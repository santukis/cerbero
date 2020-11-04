package com.sample.cerbero

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.santukis.cerbero.Permission
import com.santukis.cerbero.PermissionsWizard
import com.santukis.cerbero.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permission_requested?.text =
            "${Manifest.permission.CAMERA}\n${Manifest.permission.ACCESS_FINE_LOCATION}\n${Manifest.permission.BLUETOOTH}"

        request?.setOnClickListener {
            PermissionsWizard(this)
                .withPermissions(
                    Permission(
                        permission = Manifest.permission.CAMERA,
                        message = "Optional message for Camera permission"
                    ),
                    Permission(
                        permission = Manifest.permission.ACCESS_FINE_LOCATION,
                        message = "Optional message for Location permission"
                    ),
                    Permission(
                        permission = Manifest.permission.BLUETOOTH,
                        message = "Optional message for Bluetooth permission"
                    )
                )
                .check(
                    onGranted = {
                        results?.text = "All permission granted"
                    },
                    onPermissionsMissing = { permissions ->
                        var message = ""

                        permissions.forEach { permission ->
                            message += "${permission.permission} denied \n"
                        }

                        results?.text = message
                    })
        }
    }
}