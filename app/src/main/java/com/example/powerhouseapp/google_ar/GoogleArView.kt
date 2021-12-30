/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.powerhouseapp.google_ar

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.opengl.GLException
import android.opengl.GLSurfaceView
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.powerhouseapp.R
import com.example.powerhouseapp.camera.CameraViewModel
import com.example.powerhouseapp.google_ar.common.helpers.SnackbarHelper
import com.example.powerhouseapp.google_ar.common.helpers.TapHelper
import com.example.powerhouseapp.utils.Constants
import com.google.ar.core.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL10


class GoogleArView(val activity: GoogleArActivity, viewModel: CameraViewModel) :
    DefaultLifecycleObserver {
    val root = View.inflate(activity, R.layout.activity_main, null)!!
    val surfaceView = root.findViewById<GLSurfaceView>(R.id.surfaceview)!!
    private var snapshotBitmap: Bitmap? = null
    val photoButton = root.findViewById<AppCompatButton>(R.id.cameraCaptureButton).apply {
        setOnClickListener { _ ->
            animateFlash()
            Toast.makeText(
                activity,
                activity.getString(R.string.depth_upload_started),
                Toast.LENGTH_LONG
            )
                .show()
            captureBitmap(object : BitmapReadyCallbacks {
                override fun onBitmapReady(bitmap: Bitmap?) {
                    val tempUri = bitmap?.let { getImageUri(activity.applicationContext, it) }
                    val finalFile = File(getRealPathFromURI(tempUri))
                    viewModel.apiManager.uploadImage(Constants.EMAIL_ID, finalFile, true)
                }
            })
        }
    }
    val settingsButton =
        root.findViewById<Button>(R.id.settings_button).apply {
            setOnClickListener { v ->
                PopupMenu(activity, v).apply {
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.depth_settings -> launchDepthSettingsMenuDialog()
                            R.id.instant_placement_settings -> launchInstantPlacementSettingsMenuDialog()
                            else -> null
                        } != null
                    }
                    inflate(R.menu.settings_menu)
                    show()
                }
            }
        }

    private fun animateFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            root.postDelayed({
                root.foreground = ColorDrawable(Color.WHITE)
                root.postDelayed({
                    root.foreground = null
                }, 50)
            }, 100)
        }
    }

    private val session
        get() = activity.arCoreSessionHelper.session

    val snackBarHelper = SnackbarHelper()
    val tapHelper = TapHelper(activity).also { surfaceView.setOnTouchListener(it) }

    override fun onResume(owner: LifecycleOwner) {
        surfaceView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        surfaceView.onPause()
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (activity.contentResolver != null) {
            val cursor: Cursor? =
                uri?.let { activity.contentResolver.query(it, null, null, null, null) }
            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        Log.d(TAG, "real depth image path: $path")
        return path
    }

    fun showOcclusionDialogIfNeeded() {
        val session = session ?: return
        val isDepthSupported = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)
        if (!activity.depthSettings.shouldShowDepthEnableDialog() || !isDepthSupported) {
            return
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.options_title_with_depth)
            .setMessage(R.string.depth_use_explanation)
            .setPositiveButton(R.string.button_text_enable_depth) { _, _ ->
                activity.depthSettings.setUseDepthForOcclusion(true)
            }
            .setNegativeButton(R.string.button_text_disable_depth) { _, _ ->
                activity.depthSettings.setUseDepthForOcclusion(false)
            }
            .show()
    }

    private fun launchInstantPlacementSettingsMenuDialog() {
        val resources = activity.resources
        val strings = resources.getStringArray(R.array.instant_placement_options_array)
        val checked = booleanArrayOf(activity.instantPlacementSettings.isInstantPlacementEnabled)
        AlertDialog.Builder(activity)
            .setTitle(R.string.options_title_instant_placement)
            .setMultiChoiceItems(strings, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton(R.string.done) { _, _ ->
                val session = session ?: return@setPositiveButton
                activity.instantPlacementSettings.isInstantPlacementEnabled = checked[0]
                activity.configureSession(session)
            }
            .show()
    }

    /** Shows checkboxes to the user to facilitate toggling of depth-based effects. */
    private fun launchDepthSettingsMenuDialog() {
        val session = session ?: return

        // Shows the dialog to the user.
        val resources: Resources = activity.resources
        val checkboxes =
            booleanArrayOf(
                activity.depthSettings.useDepthForOcclusion(),
                activity.depthSettings.depthColorVisualizationEnabled()
            )
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            // With depth support, the user can select visualization options.
            val stringArray = resources.getStringArray(R.array.depth_options_array)
            AlertDialog.Builder(activity)
                .setTitle(R.string.options_title_with_depth)
                .setMultiChoiceItems(stringArray, checkboxes) { _, which, isChecked ->
                    checkboxes[which] = isChecked
                }
                .setPositiveButton(R.string.done) { _, _ ->
                    activity.depthSettings.setUseDepthForOcclusion(checkboxes[0])
                    activity.depthSettings.setDepthColorVisualizationEnabled(checkboxes[1])
                }
                .show()
        } else {
            // Without depth support, no settings are available.
            AlertDialog.Builder(activity)
                .setTitle(R.string.options_title_without_depth)
                .setPositiveButton(R.string.done) { _, _ -> /* No settings to apply. */ }
                .show()
        }
    }

    private fun captureBitmap(bitmapReadyCallbacks: BitmapReadyCallbacks) {
        surfaceView.queueEvent(Runnable {
            val egl = EGLContext.getEGL() as EGL10
            val gl = egl.eglGetCurrentContext().gl as GL10
            snapshotBitmap =
                createBitmapFromGLSurface(0, 0, surfaceView.getWidth(), surfaceView.getHeight(), gl)
            activity.runOnUiThread(Runnable { bitmapReadyCallbacks.onBitmapReady(snapshotBitmap) })
        })
    }

    // from other answer in this question
    private fun createBitmapFromGLSurface(x: Int, y: Int, w: Int, h: Int, gl: GL10): Bitmap? {
        val bitmapBuffer = IntArray(w * h)
        val bitmapSource = IntArray(w * h)
        val intBuffer: IntBuffer = IntBuffer.wrap(bitmapBuffer)
        intBuffer.position(0)
        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer)
            var offset1: Int
            var offset2: Int
            for (i in 0 until h) {
                offset1 = i * w
                offset2 = (h - i - 1) * w
                for (j in 0 until w) {
                    val texturePixel = bitmapBuffer[offset1 + j]
                    val blue = texturePixel shr 16 and 0xff
                    val red = texturePixel shl 16 and 0x00ff0000
                    val pixel = texturePixel and -0xff0100 or red or blue
                    bitmapSource[offset2 + j] = pixel
                }
            }
        } catch (e: GLException) {
            Log.e("GoogleArView", "createBitmapFromGLSurface: " + e.message, e)
            return null
        }
        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888)
    }

    private interface BitmapReadyCallbacks {
        fun onBitmapReady(bitmap: Bitmap?)
    }
}
