package com.example.adminblinkitclone.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminblinkitclone.activity.AdminMainActivity
import com.example.adminblinkitclone.utils.Constants
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.adapter.AdapterSelectedImage
import com.example.adminblinkitclone.databinding.FragmentAddProductBinding
import com.example.adminblinkitclone.models.Product
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import com.example.adminblinkitclone.utils.Utils
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {

    private val viewModel: AdminViewModel by viewModels()
    private lateinit var binding: FragmentAddProductBinding
    private val imageUris: ArrayList<Uri> = arrayListOf()

    private val selectedImage =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { listOfUri ->
            val fiveImage = listOfUri.take(5)
            imageUris.clear()
            imageUris.addAll(fiveImage)

            binding.rvProductImage.adapter = AdapterSelectedImage(imageUris)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        setAutoCompeteTextViews()

        onImageSelectClicked()

        onAddButtonClicked()
        return binding.root
    }

    private fun onAddButtonClicked() {
        binding.btnAddProduct.setOnClickListener {
            Utils.showDialog(requireContext(), "Uploading images...")
            val productTitle = binding.productTitle.text.toString()
            val productQuantity = binding.productQuantity.text.toString()
            val productUnit = binding.productUnit.text.toString()
            val productPrice = binding.productPrice.text.toString()
            val productStock = binding.productStock.text.toString()
            val productCategory = binding.productCategory.text.toString()
            val productType = binding.productType.text.toString()

            if (productTitle.isEmpty() || productQuantity.isEmpty() || productUnit.isEmpty() ||
                productPrice.isEmpty() || productStock.isEmpty() || productCategory.isEmpty() || productType.isEmpty()
            ) {
                Utils.apply {
                    hideDialog()
                    showToast(requireContext(), "Empty fields are not allowed")
                }
            } else if (imageUris.isEmpty()) {
                Utils.apply {
                    hideDialog()
                    showToast(requireContext(), "Please upload some images")
                }
            } else {
                val product = Product(
                    productTitle = productTitle,
                    productQuantity = productQuantity.toInt(),
                    productUnit = productUnit,
                    productPrice = productPrice.toInt(),
                    productStock = productStock.toInt(),
                    productCategory = productCategory,
                    productType = productType,
                    itemCount = 0,
                    adminUid = Utils.getCurrentUserId(),
                    productRandomId = Utils.getRandomId()
                )

                saveImage(product)
            }
        }
    }

    private fun saveImage(product: Product) {
        viewModel.saveImageInDB(imageUris)

        lifecycleScope.launch {
            viewModel.isImagesUploaded.collect {
                if (it) {
                    Utils.apply {
                        hideDialog()
                        showToast(requireContext(), "image saved")
                    }
                    getUrls(product)
                }
            }
        }
    }

    private fun getUrls(product: Product) {
        Utils.showDialog(requireContext(), "Publishing products...")
        lifecycleScope.launch {
            viewModel.downloadedUrls.collect {
                val urls = it
                product.productImageUris = urls
                saveProduct(product)
            }
        }
    }

    private fun saveProduct(product: Product) {
        viewModel.saveProduct(product)
        lifecycleScope.launch {
            viewModel.isProductSaved.collect {
                if (it) {
                    Utils.hideDialog()
                    startActivity(Intent(requireContext(), AdminMainActivity::class.java))
                    Utils.showToast(requireContext(),"Your product is live")
                }
            }
        }
    }


    private fun onImageSelectClicked() {
        binding.btnSelectImage.setOnClickListener {
            selectedImage.launch("image/*")
        }

    }

    private fun setAutoCompeteTextViews() {
        val units = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allunitsOfProducts)
        val category =
            ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductsCategory)
        val producType =
            ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductType)

        binding.apply {
            productUnit.setAdapter(units)
            productCategory.setAdapter(category)
            productType.setAdapter(producType)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStatusBarColor()
    }


    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}