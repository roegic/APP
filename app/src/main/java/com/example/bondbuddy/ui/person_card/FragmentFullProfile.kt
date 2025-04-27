package com.example.bondbuddy.ui.person_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.databinding.FragmentFullProfileBinding
import com.example.bondbuddy.ui.adapters.InterestAdapter
import com.example.bondbuddy.ui.adapters.SocialMediaAdapter
import com.example.bondbuddy.utils.Result
import com.example.bondbuddy.utils.byteArrayToBitmap
import com.example.bondbuddy.utils.showErrorScreen
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FullProfileFragment : Fragment() {

    private var _binding: FragmentFullProfileBinding? = null
    private val binding get() = _binding!!
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private lateinit var interestAdapter: InterestAdapter
    private lateinit var socialMediaAdapter: SocialMediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInterestsRecyclerView()
        setupSocialMediaRecyclerView()

        subscribeToCurrentUserInfoEvents()
    }

    override fun onStart() {
        super.onStart()
        val userId = arguments?.let { FullProfileFragmentArgs.fromBundle(it).userId }
        userDataViewModel.getCurrentUserInterests(userId.toString())
        userDataViewModel.getCurrentUserInfo(userId.toString())
        userDataViewModel.getCurrentUserSocials(userId.toString())
        userDataViewModel.getCurrentUserLanguages(userId.toString())
    }

    private fun setupInterestsRecyclerView() {
        interestAdapter = InterestAdapter(
            onInterestClick = { position, interest ->
            },
            isEditModeEnabled = { false }
        )

        val flexLayoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        binding.rvInterests.apply {
            adapter = interestAdapter
            layoutManager = flexLayoutManager
        }
    }

    private fun setupSocialMediaRecyclerView() {
        socialMediaAdapter = SocialMediaAdapter(
            onEdit = { position ->

            },
            onDelete = { position ->

            },
            isEditModeEnabled = { false }
        )

        binding.rvSocialMedia.apply {
            adapter = socialMediaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun subscribeToCurrentUserInfoEvents() {
        lifecycleScope.launch {
            userDataViewModel.userState.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val userInfo = result.data ?: return@collect
                        binding.tvFirstName.text = userInfo.firstName
                        binding.tvLastName.text = userInfo.lastName
                        binding.tvAge.text = userInfo.age.toString()
                        binding.tvBio.text = userInfo.bio
                        binding.tvCity.text = userInfo.city
                        binding.tvCountry.text = userInfo.country
                        binding.tvOccupation.text = userInfo.occupation
                        val photoByteArray = userInfo.photo
                        val bitmap = byteArrayToBitmap(photoByteArray)
                        if (bitmap != null) {
                            binding.ivProfilePhoto.setImageBitmap(bitmap)
                        } else {
                            binding.ivProfilePhoto.setImageResource(R.drawable.ic_person)
                        }

                    }

                    is Result.Error -> {
                        context?.let { showErrorScreen(it) }
                    }

                    is Result.Loading -> {

                    }
                }
            }
        }

        lifecycleScope.launch {
            userDataViewModel.getInterestsState.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val interestsList = result.data ?: emptyList()
                        interestAdapter.interests = interestsList
                        interestAdapter.notifyDataSetChanged()
                    }

                    is Result.Error -> {
                        context?.let { showErrorScreen(it) }
                    }

                    is Result.Loading -> {

                    }
                }
            }
        }

        lifecycleScope.launch {
            userDataViewModel.getSocialsState.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val socialMediaList = result.data ?: emptyList()
                        socialMediaAdapter.socialMediaList = socialMediaList
                        socialMediaAdapter.notifyDataSetChanged()
                    }

                    is Result.Error -> {

                    }

                    is Result.Loading -> {

                    }
                }
            }
        }
        lifecycleScope.launch {
            userDataViewModel.getLanguagesState.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val languages = result.data ?: emptyList()
                        binding.tvLanguages.text =
                            languages.joinToString(separator = ", ") { it.nativeName }
                    }

                    is Result.Error -> {
                        context?.let { showErrorScreen(it) }
                    }

                    is Result.Loading -> {

                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(user: UserInfo): FullProfileFragment {
            val fragment = FullProfileFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
