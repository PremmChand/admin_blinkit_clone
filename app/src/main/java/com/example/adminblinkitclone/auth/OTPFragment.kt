package com.example.adminblinkitclone.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.adminblinkitclone.activity.AdminMainActivity
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.databinding.FragmentOTPBinding
import com.example.adminblinkitclone.models.Admins
import com.example.adminblinkitclone.utils.Utils
import com.example.blinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OTPFragment : Fragment() {
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: FragmentOTPBinding
    private lateinit var userNumber: String
    private var otpSentHandled = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOTPBinding.inflate(inflater, container, false)
        getUserNumber()
        observeLoginSuccess()
        observeOtpSent()
        setupOnLoginClick()
        customizeOtpEntry()
        setupBackButton()
        sendOTP()
        return binding.root
    }

    private fun observeOtpSent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.otpSent.collect { sent ->
                    if (!otpSentHandled && !sent) return@collect
                    otpSentHandled = true

                    Utils.hideDialog()
                    if (sent) {
                        Utils.showToast(requireContext(), "OTP Sent...")
                    } else {
                        Utils.showToast(requireContext(), "Failed to send OTP")
                    }
                }
            }
        }
    }

    private fun observeLoginSuccess() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSignedInSuccessfully.collectLatest { success ->
                    Log.d("OTPFragment", "Login result: $success")
                    Utils.hideDialog()
                    if (success) {
                        Utils.showToast(requireContext(), "Logged In...")
                        startActivity(Intent(requireContext(), AdminMainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        Utils.showToast(requireContext(), "Invalid OTP or sign-in failed")
                    }
                }
            }
        }
    }


    private fun setupOnLoginClick() {
        binding.btnLogin.setOnClickListener {
            val otpFields = arrayOf(
                binding.otp1, binding.otp2, binding.otp3,
                binding.otp4, binding.otp5, binding.otp6
            )
            val otp = otpFields.joinToString(separator = "") { it.text.toString() }
            if (otp.length < otpFields.size) {
                Utils.showToast(requireContext(), "Please enter full OTP")
                return@setOnClickListener
            }
            Utils.showDialog(requireContext(), "Signing you in...")
            otpFields.forEach {
                it.text?.clear()
                it.clearFocus()
            }
            verifyOTP(otp)
        }
    }

    private fun sendOTP() {
        otpSentHandled = false  // Reset event control
        Utils.showDialog(requireContext(), "Sending OTP")
        viewModel.sendOTP(userNumber, requireActivity())
    }

    private fun customizeOtpEntry() {
        val otpFields = arrayOf(
            binding.otp1, binding.otp2, binding.otp3,
            binding.otp4, binding.otp5, binding.otp6
        )
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    }
                    if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }
                    if ((s?.length ?: 0) > 1) {
                        val firstChar = s?.get(0).toString()
                        otpFields[i].setText(firstChar)
                        otpFields[i].setSelection(1)
                    }
                }
            })
        }
    }

    private fun setupBackButton() {
        binding.otpFragment.setNavigationOnClickListener {
            findNavController().navigate(
                R.id.action_OTPFragment_to_signInFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.OTPFragment, true).build()
            )
        }
    }

    private fun verifyOTP(otp: String) {
        val admin = Admins(null, userPhoneNumber = userNumber)
        viewModel.signInWithPhoneAuthCredential(otp, userNumber,admin)
    }

    private fun getUserNumber() {
        userNumber = arguments?.getString("number").orEmpty()
        binding.userNumber.text = userNumber
    }



}