package com.pnuema.android.foursite.mainscreen.models

import android.os.Parcel
import android.os.Parcelable

class Icon() : Parcelable {

    var prefix: String? = null
    var suffix: String? = null

    constructor(parcel: Parcel) : this() {
        prefix = parcel.readString()
        suffix = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(prefix)
        parcel.writeString(suffix)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Icon> {
        override fun createFromParcel(parcel: Parcel): Icon {
            return Icon(parcel)
        }

        override fun newArray(size: Int): Array<Icon?> {
            return arrayOfNulls(size)
        }
    }

}
