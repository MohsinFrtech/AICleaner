package com.example.aicleaner.ui.activities

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.aicleaner.R
import com.example.aicleaner.databinding.ActivitySplashBinding
import com.example.aicleaner.utils.CustomDialogue
import com.example.aicleaner.utils.interfaces.DialogListener
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

///Home Screen ....
class SplashScreen : AppCompatActivity(), DialogListener {

    private var bindingHome: ActivitySplashBinding? = null
    private var permissionCount = 0
    private val readImagePermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            var count = 0
            for (i in results) {
                if (i.value == true) {
                    count++
                }
            }
            if (count == results.size) {
                moveToMainScreen()
            } else {
//                    moveToMainScreen()
            }
//            isGranted: Boolean ->
//            if (isGranted) {
////                bindingHome?.notificationLayout?.visibility = View.GONE
//                // Permission is granted. Continue the action or workflow in your
//                moveToMainScreen()
//            } else {
////                bindingHome?.notificationLayout?.visibility = View.VISIBLE
//
//            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingHome = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        //Initialize firebase instance...
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

    }


    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed(
            {
                emulatorCheck()
            },
            2000
        )
    }

    private fun emulatorCheck() {
        lifecycleScope.launch {
            //val state = emulatorDetector.getState()
            //getDeviceStateDescription(state)

            try {
                val isEmulator: Boolean by lazy {
                    // Android SDK emulator
                    return@lazy ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                            && Build.FINGERPRINT.endsWith(":user/release-keys")
                            && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith("sdk_gphone_") && Build.BRAND == "google"
                            && Build.MODEL.startsWith("sdk_gphone_"))
                            //
                            || Build.FINGERPRINT.startsWith("generic")
                            || Build.FINGERPRINT.startsWith("unknown")
                            || Build.MODEL.contains("google_sdk")
                            || Build.MODEL.contains("Emulator")
                            || Build.MODEL.contains("Android SDK built for x86")
                            //bluestacks
                            || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(
                        Build.MANUFACTURER,
                        ignoreCase = true
                    ) //bluestacks
                            || Build.MANUFACTURER.contains("Genymotion")
                            || Build.HOST.startsWith("Build") //MSI App Player
                            || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                            || Build.PRODUCT == "google_sdk"
                            || Build.FINGERPRINT.contains("generic")
                            // another Android SDK emulator check
                            )
                }

                getDeviceStateDescription(isEmulator)

            } catch (e: Exception) {
                Log.d("Exception", "" + e.message)

            }

        }
    }

    private fun getDeviceStateDescription(state: Boolean) {
        //if (state is DeviceState.Emulator) {
        if (state) {

            CustomDialogue(this).showDialog(
                this, "Alert!", "Please use application on real device",
                "", "Ok", "baseValue"
            )
        } else {
            checkStoragePermission()
        }
    }

    //Function to take storage permission....
    private fun checkStoragePermission() {

//        when {
//            ContextCompat.checkSelfPermission(
//                this,
//                readImagePermission
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                // You can use the API that requires the permission.
//                moveToMainScreen()
//            }
//
//            shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)
//            -> {
////                bindingHome?.notificationLayout?.visibility = View.VISIBLE
//
//            }
//
//            else -> {
//                // You can directly ask for the permission.
//                // The registered ActivityResultCallback gets the result of this request.
//                makePermission()
//            }
//
//        }
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (
                    ContextCompat.checkSelfPermission(
                        this,
                        READ_MEDIA_IMAGES
                    ) == PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_VIDEO
                            ) == PERMISSION_GRANTED
                    )
        ) {
            moveToMainScreen()
            // Full access on Android 13 (API level 33) or higher
        } else if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(
                this,
                READ_MEDIA_VISUAL_USER_SELECTED
            ) == PERMISSION_GRANTED
        ) {
            moveToMainScreen()
            // Partial access on Android 14 (API level 34) or higher
        } else if (ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        ) {
            moveToMainScreen()
            // Full access up to Android 12 (API level 32)
        } else {
            makePermission()
            // Access denied
        }

    }

    private fun makePermission() {


        if (permissionCount > 3) {
//                bindingHome?.notificationLayout?.visibility= View.GONE

            moveToMainScreen()

        } else if (permissionCount == 2) {
//                bindingHome?.notificationLayout?.visibility= View.GONE

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts(
                "package",
                packageName, null
            )
            intent.data = uri
            startActivity(intent)
        } else {
//            requestPermissionLauncher.launch(arrayOf())
////            requestPermissionLauncher.launch(
////                readImagePermission
////            )

            // Permission request logic
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                requestPermissionLauncher.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO))
            } else {
                requestPermissionLauncher.launch(arrayOf(READ_EXTERNAL_STORAGE))
            }
        }

        permissionCount++


    }

    override fun onDestroy() {
        super.onDestroy()
    }


    private fun moveToMainScreen() {
        bindingHome?.homeAnimLayout?.visibility = View.GONE
        if (isDeviceRooted()) {
            CustomDialogue(this).showDialog(
                this, "Alert!", "Please use application on real device",
                "", "Ok", "baseValue"
            )
        } else {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun isDeviceRooted(): Boolean {
        return checkForSuFile() || checkForSuCommand() ||
                checkForSuperuserApk() || checkForBusyBoxBinary() || checkForMagiskManager()
    }

    private fun checkForSuCommand(): Boolean {
        return try {
            // check if the device is rooted
            val file = File("/system/app/Superuser.apk")
            if (file.exists()) {
                return true
            }
            val command: Array<String> = arrayOf("/system/xbin/which", "su")
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            if (reader.readLine() != null) {
                return true
            }
            return false
        } catch (e: Exception) {
            false
        }
    }

    private fun checkForSuFile(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    private fun checkForSuperuserApk(): Boolean {
        val packageName = "eu.chainfire.supersu"
        val packageManager = packageManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                true
            } else {
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                true
            }

        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun checkForMagiskManager(): Boolean {
        val packageName = "com.topjohnwu.magisk"
        val packageManager = packageManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                true
            } else {
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                true
            }

        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun checkForBusyBoxBinary(): Boolean {
        val paths = arrayOf("/system/bin/busybox", "/system/xbin/busybox", "/sbin/busybox")
        try {
            for (path in paths) {
                val process = Runtime.getRuntime().exec(arrayOf("which", path))
                if (process.waitFor() == 0) {
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    override fun onPositiveDialogText(key: String) {
        finishAffinity()
    }

    override fun onNegativeDialogText(key: String) {
        finishAffinity()
    }


}