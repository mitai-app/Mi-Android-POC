package io.vonley.mi.ui.main.ftp.adapters

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

//Felt cute might delete later
class FTPFileTouchListener : RecyclerView.OnItemTouchListener {

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}