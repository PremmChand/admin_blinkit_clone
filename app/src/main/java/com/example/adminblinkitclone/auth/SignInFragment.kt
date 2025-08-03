package com.example.adminblinkitclone.auth

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.databinding.FragmentSignInBinding
import com.example.adminblinkitclone.utils.Utils


class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentSignInBinding.inflate(layoutInflater)
        Handler(Looper.getMainLooper()).postDelayed({
            // findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
        },3000)
        getUserNumber()
        onContinueButtonClick()
        return binding.root
    }

    private fun onContinueButtonClick() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.signInFragment, false) // Don't remove SignIn
            .build()

        binding.btnContinue.setOnClickListener{
            val number = binding.mobileNumber.text.toString()
            if(number.isEmpty() || number.length != 10){
                Utils.showToast(requireContext(),"Please enter valid phone number")
            }else{
                val bundle  = Bundle()
                bundle.putString("number", number)
                findNavController().navigate(R.id.action_signInFragment_to_OTPFragment, bundle, navOptions)

            }
        }
    }
    private fun getUserNumber() {
        binding.mobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                val len = number?.length
                if (len == 10) {
                    binding.btnContinue.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green
                        )
                    )
                } else {
                    binding.btnContinue.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.grayish_blue
                        )
                    )

                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStatusBarColor()
    }

    @Suppress("DEPRECATION")
    private fun setStatusBarColor() {
        activity?.window?.apply {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.yellow)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }


}