package com.codinghub.apps.codinghubdemo.model.objects.licenseplate

data class Candidate(val matches_template: Int,
                     val plate: String,
                     val confidence: Double)