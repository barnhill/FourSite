package com.pnuema.android.foursite.mainscreen.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import com.pnuema.android.foursite.mainscreen.ui.models.ILocationResult

open class DiffCallback<T : ILocationResult>(private val oldList: List<T>, private val newList: List<T>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].areItemsSame(newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].areContentsSame(newList[newItemPosition])
    }
}