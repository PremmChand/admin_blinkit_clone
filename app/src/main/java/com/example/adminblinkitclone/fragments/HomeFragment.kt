package com.example.adminblinkitclone.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminblinkitclone.activity.AuthMainActivity
import com.example.adminblinkitclone.utils.Constants
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.adapter.CategoriesAdapter
import com.example.adminblinkitclone.adapter.ProductAdapter
import com.example.adminblinkitclone.databinding.EditProductLayoutBinding
import com.example.adminblinkitclone.databinding.FragmentHomeBinding
import com.example.adminblinkitclone.models.Categories
import com.example.adminblinkitclone.models.Product
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import com.example.adminblinkitclone.utils.Utils
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    private val viewModel :AdminViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private  lateinit var productAdapter: ProductAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        setCategories()
        onLogOut()
        searchProducts()
        getAllTheProducts("All")

        return binding.root
    }

    private fun onLogOut() {
        binding.tbHomeFragment.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.menuLogout ->{
                    logOutUser()
                    true
                }

                else -> {false}
            }
        }
    }

    private fun logOutUser() {
            val builder = AlertDialog.Builder(requireContext())
            val alertDialog = builder.create()
            builder.setTitle("Log out")
                .setMessage("Do you want to log out ?")
                .setPositiveButton("Yes"){_,_ ->
                    viewModel.logOutUser()
                    startActivity(Intent(requireContext(), AuthMainActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("No"){_,_ ->
                    alertDialog.dismiss()
                }
                .show()
                .setCancelable(false)

    }

    private fun searchProducts() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                productAdapter.filter.filter(query)
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }


    private fun getAllTheProducts(category: String) {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchAllTheProduct(category).collect{

                if(it.isEmpty()){
                    binding.rvProducts.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                }else{
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                 productAdapter = ProductAdapter(::onEditButtonClicked)
                binding.rvProducts.adapter = productAdapter
                productAdapter.differ.submitList(it)

                productAdapter.originalList = it as ArrayList<Product>

                binding.shimmerViewContainer.visibility = View.GONE
            }

        }
    }

    private fun setCategories() {
        val categoryList = ArrayList<Categories>()

        for (i in 0 until Constants.allProductCategoryIcon.size) {
            categoryList.add(Categories(Constants.allProductsCategory[i], Constants.allProductCategoryIcon[i]))
        }

        binding.rvCategories.adapter = CategoriesAdapter(categoryList, ::onCategoryClicked)
    }

    private fun onCategoryClicked(categories:Categories){
    getAllTheProducts(categories.category)
    }
    @SuppressLint("SuspiciousIndentation")
    private fun onEditButtonClicked(product:Product){
    val editProduct = EditProductLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        editProduct.apply {
            etProductTitle.setText(product.productTitle)
            etProductQuantity.setText(product.productQuantity.toString())
            etProductUnit.setText(product.productUnit)
            etProductPrice.setText(product.productPrice.toString())
            etProductStock.setText(product.productStock.toString())
            etProductCategory.setText(product.productCategory)
            etProductType.setText(product.productType)
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(editProduct.root)
            .create()
        alertDialog.show()

        editProduct.btnEdit.setOnClickListener{
            editProduct.apply {
                etProductTitle.isEnabled = true
                etProductQuantity.isEnabled = true
                etProductUnit.isEnabled = true
                etProductPrice.isEnabled = true
                etProductStock.isEnabled = true
                etProductCategory.isEnabled = true
                etProductType.isEnabled = true
            }
        }

        setAutoCompeteTextViews(editProduct)

        editProduct.btnSave.setOnClickListener{
           lifecycleScope.launch {
               product.productTitle = editProduct.etProductTitle.text.toString()
               product.productQuantity = editProduct.etProductQuantity.text.toString().toInt()
               product.productUnit = editProduct.etProductUnit.text.toString()
               product.productPrice = editProduct.etProductPrice.text.toString().toInt()
               product.productStock = editProduct.etProductStock.text.toString().toInt()
               product.productCategory = editProduct.etProductCategory.text.toString()
               product.productType = editProduct.etProductType.text.toString()

               viewModel.savingUpdatedProducts(product)
           }
            Utils.showToast(requireContext(),"Saved changes!")
            alertDialog.dismiss()
        }
    }

    private fun setAutoCompeteTextViews(editProduct: EditProductLayoutBinding) {
        val units = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allunitsOfProducts)
        val category =
            ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductsCategory)
        val producType =
            ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductType)

        editProduct.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(producType)
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStatusBarColor()
    }


    private  fun setStatusBarColor(){
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor =statusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility =View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}