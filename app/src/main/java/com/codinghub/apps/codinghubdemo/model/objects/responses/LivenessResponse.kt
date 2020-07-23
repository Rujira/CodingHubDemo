package com.codinghub.apps.codinghubdemo.model.objects.responses


data class LivenessResponse(val rtn: Int,
                            val message: String,
                            val pair_verify_result: Int,
                            val pair_verify_similarity: Double)
