package com.example.adminblinkitclone.utils

import android.widget.Filter
import com.example.adminblinkitclone.adapter.ProductAdapter
import com.example.adminblinkitclone.models.Product
import java.util.Locale

class FilteringProducts (
    val adapter: ProductAdapter,
            val filter : ArrayList<Product>
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()

       if(!constraint.isNullOrEmpty()){
           val filteredList = ArrayList<Product>()
           val query = constraint.toString().trim().uppercase(Locale.getDefault()).split( " ")

           for(products in filter){
               if(query.any{
                       products.productTitle?.uppercase(Locale.getDefault())?.contains(it) == true ||
                       products.productCategory?.uppercase(Locale.getDefault())?.contains(it) == true ||
                       products.productPrice?.toString()?.uppercase(Locale.getDefault())?.contains(it) == true ||
                       products.productType?.uppercase(Locale.getDefault())?.contains(it) == true

               }){
                   filteredList.add(products)
               }
           }
           results.values  = filteredList
           results.count = filteredList.size

       }else{
        results.values  = filter
           results.count = filter.size
       }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.differ.submitList(results?.values as ArrayList<Product>)
    }

}