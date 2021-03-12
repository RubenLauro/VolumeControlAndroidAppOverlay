package com.example.volumecontrollerapp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.view.*
import kotlinx.android.synthetic.main.layout_floating_widget.view.*


class VolumeControlService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val curVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val mParams: WindowManager.LayoutParams? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        }

        mParams?.let {
            it.x = 0
            it.y = 0
            it.gravity = Gravity.TOP or Gravity.START
        }

        mWindowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager?.addView(mFloatingView, mParams)

        mFloatingView?.let { it ->
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0.0
            var initialTouchY = 0.0
            it.root_container.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //remember the initial position
                        println("passei no action down")
                        mParams?.let {
                            initialX = it.x
                            initialY = it.y

                        }

                        //get the touch location
                        initialTouchX = event.rawX.toDouble()
                        initialTouchY = event.rawY.toDouble()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val Xdiff = (event.rawX - initialTouchX).toInt()
                        val Ydiff = (event.rawY - initialTouchY).toInt()
                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (it.hiddenCl.visibility == View.GONE)
                                it.hiddenCl.visibility = View.VISIBLE
                            else
                                it.hiddenCl.visibility = View.GONE
                        }
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mParams?.let {
                                it.x = initialX + (event.rawX - initialTouchX).toInt()
                                it.y = initialY + (event.rawY - initialTouchY).toInt()
                        }

                        //Update the layout with new X & Y coordinate
                        mWindowManager!!.updateViewLayout(mFloatingView, mParams)
                        true
                    }
                    else -> false
                }
            }

            it.volumePercTv.text = curVolume.toString()

            it.upBtn.setOnClickListener { _ ->
                // ADJUST_RAISE = Raise the volume, FLAG_SHOW_UI = show changes made to volume bar
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)

                val newCurVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                println("cur volume $newCurVolume")
                it.volumePercTv.text = newCurVolume.toString()
            }

            // At the click of downBtn
            it.downBtn.setOnClickListener { _ ->
                // ADJUST_LOWER = LOWER the volume, FLAG_SHOW_UI = show changes made to volume bar
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
                val newCurVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                println("cur volume $newCurVolume")
                it.volumePercTv.text = newCurVolume.toString()
            }

            it.closeBtn.setOnClickListener {
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)
    }
}