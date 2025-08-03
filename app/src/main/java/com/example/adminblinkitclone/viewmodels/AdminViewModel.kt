package com.example.adminblinkitclone.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.adminblinkitclone.models.CartProductTable
import com.example.adminblinkitclone.models.Orders
import com.example.adminblinkitclone.models.Product
import com.example.adminblinkitclone.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {

    private val _isImagesUploaded = MutableStateFlow(false)
    var isImagesUploaded : StateFlow<Boolean>  = _isImagesUploaded

    private val _downloadedUrls = MutableStateFlow<ArrayList<String?>>(arrayListOf())
    var  downloadedUrls : StateFlow<ArrayList<String?>> = _downloadedUrls

    private val _isProductSaved = MutableStateFlow(false)
    var isProductSaved : StateFlow<Boolean>  = _isProductSaved

    fun saveImageInDB(imageUri: ArrayList<Uri>){
        val downloadUrls = ArrayList<String?>()

        imageUri.forEach{uri ->
            val imageRef = FirebaseStorage.getInstance().reference.child(Utils.getCurrentUserId()).child("images")
                .child(UUID.randomUUID().toString())
            imageRef.putFile(uri).continueWithTask{
                imageRef.downloadUrl
            }.addOnCompleteListener{task->
                val url = task.result
                downloadUrls.add(url.toString())

                if(downloadUrls.size == imageUri.size){
                    _isImagesUploaded.value = true
                    _downloadedUrls.value = downloadUrls
                }
            }
        }
    }

    fun saveProduct(product: Product){
        FirebaseDatabase.getInstance().getReference("Admins").
        child("AllProducts/${product.productRandomId}").setValue(product)
            .addOnCompleteListener{
                FirebaseDatabase.getInstance().getReference("Admins").
                child("ProductCategory/${product.productCategory}/${product.productRandomId}").setValue(product)
                    .addOnCompleteListener{
                        FirebaseDatabase.getInstance().getReference("Admins").
                        child("ProductTypes/${product.productType}/${product.productRandomId}").setValue(product)
                            .addOnCompleteListener{
                                _isProductSaved.value = true
                            }

                    }
            }
    }

    fun fetchAllTheProduct(category: String) : Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for(product in snapshot.children){
                    val prod = product.getValue(Product ::class.java)

                    if(category == "All" || prod?.productCategory == category) {
                        products.add(prod!!)
                    }
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }

        db.addValueEventListener(eventListener)
        awaitClose{
            db.removeEventListener(eventListener)
        }
    }

    fun savingUpdatedProducts(product: Product){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductTypes/${product.productType}/${product.productRandomId}").setValue(product)


    }

    // orders
    fun getAllOrders() : Flow<List<Orders>> = callbackFlow{
        val db =   FirebaseDatabase.getInstance().getReference("Admins").child("Orders").orderByChild("orderStatus")

        val eventListener = object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for(orders in snapshot.children){
                    val order = orders.getValue(Orders::class.java)

                    if (order != null) {
                        orderList.add(order)
                    }
                }
                trySend(orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{db.removeEventListener(eventListener)}
    }

    fun getOrderedProducts(orderId: String) : Flow<List<CartProductTable>> = callbackFlow {
        val db =  FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId)

        val eventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                // trySend(order?.orderList!!)
                order?.orderList?.let {
                    Log.d("OrderDetailFlow", "Emitting ${it.size} items")
                    trySend(it)
                } ?: Log.d("OrderDetailFlow", "No products found")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseGetOrderedProducts", "Failed to load orders: ${error.message}")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{db.removeEventListener(eventListener)}

    }

    fun updateOrderStatus(orderId: String, status:Int){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId).child("orderStatus").setValue(status)
    }


    suspend fun sendNotification(
        orderId: String,
        title: String,
        message: String,
        context: Context
    ) {
        try {
            Log.d("Push Notification", "Fetching orderingUserId for orderId: $orderId")

            val userUidSnapshot = FirebaseDatabase.getInstance()
                .getReference("Admins")
                .child("Orders")
                .child(orderId)
                .child("orderingUserId")
                .get()
                .await()

            val userUid = userUidSnapshot.getValue(String::class.java)
            Log.d("Push Notification", "User UID: $userUid")

            if (userUid.isNullOrBlank()) {
                Log.e("Push Notification", "User UID is null or blank.")
                return
            }

            val tokenSnapshot = FirebaseDatabase.getInstance()
                .getReference("AllUsers")
                .child("Users")
                .child(userUid)
                .child("userToken")
                .get()
                .await()

            val userToken = tokenSnapshot.getValue(String::class.java)
            Log.d("Push Notification", "User Token: $userToken")

            if (userToken.isNullOrBlank()) {
                Log.e("Push Notification", "Token is null or blank, skipping notification")
                return
            }

            val functions = FirebaseFunctions.getInstance("us-central1")
            val data = hashMapOf(
                "title" to title,
                "body" to message,
                "token" to userToken
            )

            val result = functions
                .getHttpsCallable("sendNotification")
                .call(data)
                .await()

            Log.d("Push Notification", "Notification sent successfully: ${result.data}")

        } catch (e: Exception) {
            Log.e("Push Notification", "Exception: ${e.message}", e)
        }
    }

    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }

}