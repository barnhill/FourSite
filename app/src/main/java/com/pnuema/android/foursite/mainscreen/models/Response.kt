package com.pnuema.android.foursite.mainscreen.models

import android.os.Parcel
import android.os.Parcelable

class Response() : Parcelable {
    var venues: ArrayList<Venue> = arrayListOf()

    constructor(parcel: Parcel) : this() {
        parcel.readTypedList(venues, Venue)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(venues)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Response> {
        override fun createFromParcel(parcel: Parcel): Response {
            return Response(parcel)
        }

        override fun newArray(size: Int): Array<Response?> {
            return arrayOfNulls(size)
        }
    }
}
