package com.pnuema.android.foursite.mainscreen.models

import android.os.Parcel
import android.os.Parcelable

class LabeledLatLng() : Parcelable {

    var label: String? = null
    var lat: Double? = null
    var lng: Double? = null

    constructor(parcel: Parcel) : this() {
        label = parcel.readString()
        lat = parcel.readValue(Double::class.java.classLoader) as? Double
        lng = parcel.readValue(Double::class.java.classLoader) as? Double
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeValue(lat)
        parcel.writeValue(lng)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LabeledLatLng> {
        override fun createFromParcel(parcel: Parcel): LabeledLatLng {
            return LabeledLatLng(parcel)
        }

        override fun newArray(size: Int): Array<LabeledLatLng?> {
            return arrayOfNulls(size)
        }
    }

}
