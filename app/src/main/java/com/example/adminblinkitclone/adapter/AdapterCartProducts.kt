package com.example.adminblinkitclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.adminblinkitclone.databinding.ItemViewCartProductsBinding
import com.example.adminblinkitclone.models.CartProductTable

class AdapterCartProducts : RecyclerView.Adapter<AdapterCartProducts.CartProductsViewHolder> (){
    class CartProductsViewHolder(val binding: ItemViewCartProductsBinding): ViewHolder(binding.root) {

    }

    val diffUtil = object: DiffUtil.ItemCallback<CartProductTable>(){
        override fun areItemsTheSame(
            oldItem: CartProductTable,
            newItem: CartProductTable
        ): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(
            oldItem: CartProductTable,
            newItem: CartProductTable
        ): Boolean {
         return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductsViewHolder {
        return  CartProductsViewHolder(ItemViewCartProductsBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    override fun getItemCount(): Int {
     return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CartProductsViewHolder, position: Int) {
        val product = differ.currentList[position]

        holder.binding.apply {
            Glide.with(holder.itemView).load(product.productImage).into(ivProductImage)
            tvProdcutTitle.text = product.productTitle
            tvProdcutQuantity.text = product.productQuantity
            tvProdcutPrice.text = product.productPrice
            tvProdcutCount.text = product.productCount.toString()
        }
    }
}