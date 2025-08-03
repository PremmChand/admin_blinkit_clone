package com.example.adminblinkitclone.models

data class Orders(
    val orderId : String? = null,
    val orderList : List<CartProductTable>? = null,
    val userAddress : String? = null,
    val orderStatus : Int? = null,
    val orderDate : String? = null,
    val orderingUserId : String? = null,
)
