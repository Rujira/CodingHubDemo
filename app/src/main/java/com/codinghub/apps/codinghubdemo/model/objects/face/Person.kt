package com.codinghub.apps.codinghubdemo.model.objects.face

data class Person(val face_image_id: String,
                  val image: String,
                  val name: String,
                  val person_id: String,
                  val similarity: Double)
