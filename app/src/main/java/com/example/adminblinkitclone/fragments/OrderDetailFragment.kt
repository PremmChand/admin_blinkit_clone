package com.example.adminblinkitclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.adapter.AdapterCartProducts
import com.example.adminblinkitclone.databinding.FragmentOrderDetailBinding
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import com.example.adminblinkitclone.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {
    lateinit var binding: FragmentOrderDetailBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts
    private var status = 0
    private var orderId = ""
    private var currentStatus = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)

        getValues()
        settingStatus(status)
        onBackButtonClicked()
        lifecycleScope.launch { getOrderedProducts() }
        onChangeStatusButtonClicked()
        return binding.root
    }

    private fun onChangeStatusButtonClicked() {
        binding.btnChangeStatus.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_popup, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { menu ->
                when (menu.itemId) {

                    R.id.menuReceived -> {
                        currentStatus = 1
                        if(currentStatus > status){
                            status = 1
                            settingStatus(1)
                            viewModel.updateOrderStatus(orderId, 1)
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.sendNotification(orderId,"Received","Your order is received", requireContext())

                            }
                        }else{
                            Utils.showToast(requireContext(), "Order is already received...")
                        }
                        true
                    }

                    R.id.menuDispatched -> {
                        currentStatus = 2
                        if(currentStatus > status){
                            status = 2
                            settingStatus(2)
                            viewModel.updateOrderStatus(orderId, 2)
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.sendNotification(orderId,"Dispatched","Your order is dispatched", requireContext())

                            }
                        }
                        else{
                            Utils.showToast(requireContext(), "Order is already dispatched...")
                        }
                        true
                    }

                    R.id.menuDelivered -> {
                        currentStatus = 3
                        if(currentStatus > status){
                            status = 3
                            settingStatus(3)
                            viewModel.updateOrderStatus(orderId, 3)
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.sendNotification(orderId,"Delivered","Your order is delivered", requireContext())

                            }
                        }
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }
    }

    private fun onBackButtonClicked() {
        binding.tbOderDetailFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_orderFragment)
        }
    }

    private suspend fun getOrderedProducts() {
        viewModel.getOrderedProducts(orderId).collect { cartList ->
            adapterCartProducts = AdapterCartProducts()
            binding.rvProductItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartList)
        }
    }

    private fun settingStatus(status: Int) {

        val views = listOf(
            binding.iv1,
            binding.iv2,
            binding.view1,
            binding.iv3,
            binding.view2,
            binding.iv4,
            binding.view3
        )

        val indicesToEnable = when (status) {
            0 -> 0..0
            1 -> 0..2
            2 -> 0..4
            3 -> 0..6
            else -> -1..-1
        }

        val blueColor = ContextCompat.getColorStateList(requireContext(), R.color.blue)

        for (i in indicesToEnable) {
            views[i].backgroundTintList = blueColor
        }

    }

    private fun getValues() {
        val bundle = arguments
        status = bundle?.getInt("status")!!
        orderId = bundle?.getString("orderId").toString()
        binding.tvUserAddress.text = bundle?.getString("userAddress").toString()
    }


}