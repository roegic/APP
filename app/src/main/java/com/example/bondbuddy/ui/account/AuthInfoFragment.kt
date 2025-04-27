package com.example.bondbuddy.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bondbuddy.R
import com.example.bondbuddy.databinding.FragmentAuthInfoBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AuthInfoFragment : Fragment() {

    private var _binding: FragmentAuthInfoBinding? = null
    private val binding get() = _binding!!

    private val userAuthViewModel: UserAuthViewModel by activityViewModels()

    private val args: AuthInfoFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = args.userName
        val userEmail = args.userEmail

        binding.userTxt.text = "Имя: $userName"
        binding.userEmail.text = "Email: $userEmail"
        val logoutbutton = binding.logoutBtn
        logoutbutton.setOnClickListener {
            userAuthViewModel.logout()
            findNavController().navigate(R.id.action_authInfoFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}