package com.pnuema.android.codingchallenge.common

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.pnuema.android.codingchallenge.R

class LightGreyDividerDecoration(context: Context, orientation: Int) : DividerItemDecoration(context, orientation) {
    init {
        ContextCompat.getDrawable(context, R.drawable.grey_divider)?.let {
            setDrawable(it)
        }
    }
}