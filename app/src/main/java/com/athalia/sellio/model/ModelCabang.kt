package com.athalia.sellio.model

import android.os.Parcel
import android.os.Parcelable

data class ModelCabang(
    var idCabang: String = "",
    var namaCabang: String = "",
    var alamatCabang: String = "",
    var noTelp: String = "",
    var statusCabang: String = "1"
) : Parcelable {

    constructor(parcel: Parcel) : this(
        idCabang = parcel.readString() ?: "",
        namaCabang = parcel.readString() ?: "",
        alamatCabang = parcel.readString() ?: "",
        noTelp = parcel.readString() ?: "",
        statusCabang = parcel.readString() ?: "1"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idCabang)
        parcel.writeString(namaCabang)
        parcel.writeString(alamatCabang)
        parcel.writeString(noTelp)
        parcel.writeString(statusCabang)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelCabang> {
        override fun createFromParcel(parcel: Parcel): ModelCabang {
            return ModelCabang(parcel)
        }

        override fun newArray(size: Int): Array<ModelCabang?> {
            return arrayOfNulls(size)
        }
    }
}