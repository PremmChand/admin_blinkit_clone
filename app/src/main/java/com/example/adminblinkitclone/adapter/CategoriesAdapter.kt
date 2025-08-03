package com.example.adminblinkitclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminblinkitclone.databinding.ItemViewProductCategoryBinding
import com.example.adminblinkitclone.models.Categories

class CategoriesAdapter(
    private val categoryList: List<Categories>,
    val onCategoryClicked: (Categories) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(val binding: ItemViewProductCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemViewProductCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        with(holder.binding) {
            ivCategoryImage.setImageResource(category.icon)
            tvCategoryTitle.text = category.category
        }

        holder.itemView.setOnClickListener{
            onCategoryClicked(category)
        }
    }

}