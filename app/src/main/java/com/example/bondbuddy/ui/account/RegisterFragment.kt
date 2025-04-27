package com.example.bondbuddy.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bondbuddy.R
import com.example.bondbuddy.databinding.FragmentRegisterBinding
import com.example.bondbuddy.ui.discover.DiscoverViewModel
import com.example.bondbuddy.ui.favourites.FollowViewModel
import com.example.bondbuddy.ui.profile.InterestsViewModel
import com.example.bondbuddy.ui.profile.LanguagesViewModel
import com.example.bondbuddy.ui.profile.SocialsViewModel
import com.example.bondbuddy.ui.profile.UserInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.bondbuddy.utils.Result


@AndroidEntryPoint
class RegisterFragment : Fragment() {


    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val userAuthViewModel: UserAuthViewModel by activityViewModels()

    private val userInfoViewModel: UserInfoViewModel by activityViewModels()
    private val userInterestsViewModel: InterestsViewModel by activityViewModels()
    private val userSocialsViewModel: SocialsViewModel by activityViewModels()
    private val languagesViewModel: LanguagesViewModel by activityViewModels()

    private val discoverViewModel: DiscoverViewModel by activityViewModels()


    private val followViewModel: FollowViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerTextView = binding.loginTextView


        registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        subscribeToRegisterEvents()

        binding.createAccountBtn.setOnClickListener {
            val firstName = binding.firstNameEdtTxt.text.toString().trim()
            val lastname = binding.lastNameEdtTxt.text.toString().trim()
            val username = binding.usernameEditTxt.text.toString().trim()
            val email = binding.emailEditTxt.text.toString().trim()
            val password = binding.passwordEdtTxt.text.toString().trim()
            val confirmpassword = binding.passwordReEnterEdtTxt.text.toString().trim()

            if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Пароль должен быть как минимум 6 символов",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (password != confirmpassword) {
                Toast.makeText(
                    requireContext(),
                    "Пароли не совпадают",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (firstName.isEmpty() || lastname.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmpassword.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Пожалуйста, заполните все поля",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            userInterestsViewModel.clearSaveState();
            languagesViewModel.clearUpdateState();
            userSocialsViewModel.clearSaveState();
            userInfoViewModel.clearSaveState();
            discoverViewModel.resetToDefaultRecommendations()
            followViewModel.refreshFollowerList();

            userAuthViewModel.registerUser(
                firstName,
                lastname,
                username,
                email,
                password,
                confirmpassword
            )
        }
    }

    private fun subscribeToRegisterEvents() = lifecycleScope.launch {
        userAuthViewModel.registerState.collect { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Account Successfully Created!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.profileFragment)
                }

                is Result.Error -> {
                    Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                }

                is Result.Loading -> {
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}