package com.android.rumahsehatmannawasalwa.ui.model

import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking

sealed class BookingUiModel {
    data class Item(val booking: ApiBooking) : BookingUiModel()
    data class Header(val date: String) : BookingUiModel()
}
