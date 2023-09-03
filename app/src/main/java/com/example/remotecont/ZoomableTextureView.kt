package com.example.remotecont

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.TextureView
import android.view.View

class ZoomableTextureView(context: Context, attrs: AttributeSet) : TextureView(context, attrs) {

    private var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private var minScaleFactor = 1.0f
    private var maxScaleFactor = 3.0f
    private var pivotX = 0f
    private var pivotY = 0f

    private var lastX = 0f
    private var lastY = 0f

    init {
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val dy = event.y - lastY

                translationX += dx
                translationY += dy

                lastX = event.x
                lastY = event.y
            }
        }
        return true
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(minScaleFactor, maxScaleFactor)

            pivotX = detector.focusX
            pivotY = detector.focusY

            scaleX = scaleFactor
            scaleY = scaleFactor

            return true
        }
    }
}

