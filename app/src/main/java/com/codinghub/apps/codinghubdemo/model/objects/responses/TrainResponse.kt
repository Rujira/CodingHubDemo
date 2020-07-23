package com.codinghub.apps.codinghubdemo.model.objects.responses

import com.codinghub.apps.codinghubdemo.model.objects.face.Face

data class TrainResponse(val face: Face,
                         val ret: Int)