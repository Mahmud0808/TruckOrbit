package com.immon.truckorbit.data.models

import com.google.android.gms.maps.model.LatLng
import com.immon.truckorbit.data.enums.DrivingStatusModel
import java.util.UUID

data class TruckModel(
    var id: String = UUID.randomUUID().toString(),
    var truckName: String,
    var licenseNo: String,
    var currentDriver: UserModel? = null,
    var drivingStatus: DrivingStatusModel = DrivingStatusModel.STOPPED,
    var location: LatLng? = null
) {
    constructor() : this(truckName = "", licenseNo = "")
}