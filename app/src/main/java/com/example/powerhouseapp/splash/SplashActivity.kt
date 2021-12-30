package com.example.powerhouseapp.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.powerhouseapp.R
import com.example.powerhouseapp.camera.CameraActivity
import com.example.powerhouseapp.databinding.ActivitySplashBinding
import com.example.powerhouseapp.utils.Constants
import com.example.powerhouseapp.utils.PowerHouseUtility
import dagger.hilt.android.AndroidEntryPoint

private const val PERMISSION_REQUEST_CODE = 100
private const val TAG = "SplashActivity"

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private val viewModel by viewModels<SplashViewModel>()
    private lateinit var mBinding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initUI()

        registerUserConfigObserver()

        getUserConfig()
    }

    private fun initUI() {
        mBinding.retryTextView.setOnClickListener {
            mBinding.retryTextView.visibility = INVISIBLE
            getUserConfig()
        }
    }

    private fun registerUserConfigObserver() {
        viewModel.apiManager.userConfigResponse.observe(this@SplashActivity) {
            mBinding.progressBar.visibility = INVISIBLE
            Log.d(TAG, "$it")
            when (it.code) {
                1 -> {
                    checkAndAskPermissions()
                }
                else -> {
                    Toast.makeText(
                        this,
                        getString(R.string.some_error_on_server),
                        Toast.LENGTH_SHORT
                    ).show()
                    mBinding.retryTextView.visibility = VISIBLE
                }
            }

        }
    }

    private fun getUserConfig() {
        if (PowerHouseUtility.checkNetworkConnectivity(this)) {
            viewModel.getUserConfig(Constants.EMAIL_ID)
            mBinding.progressBar.visibility = VISIBLE
        } else {
            mBinding.retryTextView.visibility = VISIBLE
            Toast.makeText(
                this,
                getString(R.string.no_internet),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkAndAskPermissions() {
        val writeStorage = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val readStorage = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val cameraPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val permissionList = mutableListOf<String>()

        if (!writeStorage) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readStorage) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!cameraPermission) {
            permissionList.add(Manifest.permission.CAMERA)
        }

        if (permissionList.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionList.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            redirectToCameraScreen()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var isDenied = false
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    isDenied = true
                }
            }
            if (isDenied) {
                checkAndAskPermissions()
            } else {
                redirectToCameraScreen()
            }
        }
    }

    private fun redirectToCameraScreen() {
        val intent = Intent(this@SplashActivity, CameraActivity::class.java)
        startActivity(intent)
        finish()
    }

}