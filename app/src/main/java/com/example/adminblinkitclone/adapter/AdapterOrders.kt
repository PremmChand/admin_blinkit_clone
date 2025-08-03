package com.example.adminblinkitclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.databinding.ItemViewOrdersBinding
import com.example.adminblinkitclone.models.OrderedItems


class AdapterOrders(context: Context, val orderItemViewClicked: (OrderedItems) -> Unit) : RecyclerView.Adapter<AdapterOrders.OrderViewHolder> (){
    class OrderViewHolder(val binding: ItemViewOrdersBinding) : ViewHolder(binding.root)


         private val diffUtil = object : DiffUtil.ItemCallback<OrderedItems>(){
            override fun areItemsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
                return  oldItem.orderId == newItem.orderId
            }

            override fun areContentsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
            return oldItem == newItem
            }

        }
        val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterOrders.OrderViewHolder {
        return OrderViewHolder(ItemViewOrdersBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AdapterOrders.OrderViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.binding.apply {
            tvOrderTitle.text = order.itemTitle
            tvOrderDate.text = order.itemDate
            tvOrderAmount.text = "₹${order.itemPrice.toString()}"

            when(order.itemStatus){
                0->{
                    tvOrderStatus.text = "Ordered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context,  R.color.yellow)
                }
                1->{
                    tvOrderStatus.text = "Received"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context,  R.color.blue)
                }
                2->{
                    tvOrderStatus.text = "Dispatched"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context,  R.color.dark_green)
                }
                3->{
                    tvOrderStatus.text = "Delivered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context,  R.color.deep_orange)
                }
            }

            holder.itemView.setOnClickListener{
                orderItemViewClicked(order)
            }
        }
    }

}