package com.immon.truckorbit.data.models

import java.util.UUID

data class LatLngModel(
    var id: String = UUID.randomUUID().toString(),
    var latitude: Double? = null,
    var longitude: Double? = null
) {
    constructor() : this(latitude = null, longitude = null)
}