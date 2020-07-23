package com.codinghub.apps.codinghubdemo.model.objects.requests


data class LivenessRequest(val database_image_content: String,
                           val database_image_type: Int,
                           val query_image_package: String,
                           val query_image_package_return_image_list: Boolean,
                           val query_image_package_check_same_person: Boolean,
                           val auto_rotate_for_database: Boolean,
                           val true_negative_rate: Double)

//"database_image_content":"/9j/...(BASE64-encoded JPG net-patterned photos)",
// "database_image_type": 101,
//"query_image_package":"Ymv..( BASE64-encoded encrypted package)",
// "query_image_package_return_image_list":true,
// "query_image_package_check_same_person":true,
// "auto_rotate_for_database":true,
//"true_negative_rate": "99.99"