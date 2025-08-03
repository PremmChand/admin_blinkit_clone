package com.example.adminblinkitclone.models


data class CartProductTable(

    var productId: String = "random",
    var productTitle: String? = null,
    var productQuantity: String? = null,
    var productPrice: String? = null,
    var productCount: Int? = null,
    var productStock: Int? = null,
    var productImage: String? = null,
    var productCategory: String? = null,
    var adminUid: String? = null,
    var productType: String? = null,
)




