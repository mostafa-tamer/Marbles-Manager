package com.example.barcodereader.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicBoolean


class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    var lock = MutableLiveData(Lock(false))

    override fun setOnClickListener(l: OnClickListener?) {

        super.setOnClickListener {
            if (!lock.value!!.status) {
                l?.onClick(this)
            }
        }

    }

    fun lockButton() {
        lock.value!!.status = (true)
    }

    fun unlockButton() {
        lock.value!!.status = (false)
    }
}

class CustomImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    var lock = MutableLiveData(Lock(false))

    override fun setOnClickListener(l: OnClickListener?) {

        super.setOnClickListener {
            if (!lock.value!!.status) {
                l?.onClick(this)
            }
        }

    }

    fun lockButton() {
        lock.value!!.status = (true)
    }

    fun unlockButton() {
        lock.value!!.status = (false)
    }
}