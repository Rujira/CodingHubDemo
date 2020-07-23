package com.codinghub.apps.codinghubdemo.model.objects.licenseplate

data class Plate(val plate: String,
                 val confidence: Double,
                 val region_confidence: Int,
                 val region: String,
                 val candidates: List<Candidate>,
                 val vehicle: Vehicle
)

