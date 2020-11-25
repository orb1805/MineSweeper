package e.roman.minesweeper

import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import kotlin.concurrent.fixedRateTimer

class MyPinchListener(private val drawer: Drawer) : SimpleOnScaleGestureListener() {

    var scale = 1f

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scale *= detector.scaleFactor
        if (scale < 1f)
            scale = 1f
        drawer.draw(scale)
        return true
    }
}