package com.example.powerhouseapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.powerhouseapp.databinding.ActivitySplashBinding

private const val SPLASH_TIME = 2000L


class CameraActivity : AppCompatActivity() {

    private val mHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    private lateinit var activityCameraBinding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        /*activityCameraBinding.fragmentContainer.postDelayed({
            activityCameraBinding.fragmentContainer.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)*/
    }

    companion object {
        /** Combination of all flags required to put activity into immersive mode */
        const val FLAGS_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        /** Milliseconds used for UI animations */
        const val ANIMATION_FAST_MILLIS = 50L
        const val ANIMATION_SLOW_MILLIS = 100L
        private const val IMMERSIVE_FLAG_TIMEOUT = 500L
    }


/*
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash)

    mHandler.postDelayed(Runnable {
        val intent = Intent(this@CameraActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }, SPLASH_TIME)
}

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)

    }
*/

}