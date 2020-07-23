package com.codinghub.apps.codinghubdemo.model.objects.responses

import com.codinghub.apps.codinghubdemo.model.objects.face.Person

data class FaceResponse(val similars: List<Person>,
                        val ret: Int)


