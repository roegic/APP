package com.example.bondbuddy.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bondbuddy.R
import com.example.bondbuddy.databinding.FragmentLoginBinding
import com.example.bondbuddy.ui.discover.DiscoverViewModel
import com.example.bondbuddy.ui.favourites.FollowViewModel
import com.example.bondbuddy.ui.person_card.UserDataViewModel
import com.example.bondbuddy.ui.profile.InterestsViewModel
import com.example.bondbuddy.ui.profile.LanguagesViewModel
import com.example.bondbuddy.ui.profile.SocialsViewModel
import com.example.bondbuddy.ui.profile.UserInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.bondbuddy.utils.Result

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val userInfoViewModel: UserInfoViewModel by activityViewModels()
    private val userInterestsViewModel: InterestsViewModel by activityViewModels()
    private val userSocialsViewModel: SocialsViewModel by activityViewModels()
    private val languagesViewModel: LanguagesViewModel by activityViewModels()
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private val discoverViewModel: DiscoverViewModel by activityViewModels()


    private val followViewModel: FollowViewModel by activityViewModels()

    private val userAuthViewModel: UserAuthViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerTextView = binding.registerTextView

        registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        subscribeToLoginEvents()

        binding?.loginBtn?.setOnClickListener {
            val email = binding!!.emailEditTxt.text.toString()
            val password = binding!!.passwordEdtTxt.text.toString()

            userInterestsViewModel.clearSaveState();
            languagesViewModel.clearUpdateState();
            userSocialsViewModel.clearSaveState();
            userInfoViewModel.clearSaveState();
            followViewModel.refreshFollowerList();
            discoverViewModel.resetToDefaultRecommendations()

            userAuthViewModel.loginUser(
                email.trim(),
                password.trim()
            )
        }
    }

    private fun subscribeToLoginEvents() = lifecycleScope.launch {
        userAuthViewModel.loginState.collect { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Добро пожаловать!", Toast.LENGTH_SHORT).show()
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