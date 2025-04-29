package com.example.bondbuddy.ui.favourites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.databinding.FragmentListBinding
import com.example.bondbuddy.utils.Result
import com.example.bondbuddy.utils.showErrorScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private var allUsers: List<UserInfo> = emptyList()


    private val followViewModel: FollowViewModel by activityViewModels()
    private lateinit var savedUsersAdapter: SavedUsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedUsersAdapter = SavedUsersAdapter(emptyList(), { user -> unfollowUser(user) }, { user -> openFullProfile(user) }) // pass an empty List as a parameter

        binding.rvSavedUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavedUsers.adapter = savedUsersAdapter

        binding.tvSavedTitle.text = getString(R.string.savedTitle)

        subscribeToFollowEvents()

        if (followViewModel.followerListState.value !is Result.Success) {
            loadUserFollowerList()
        }
    }

    private fun loadUserFollowerList() {
        followViewModel.getUserFollowerList()
    }

    private fun updateFilteredList(query: String?) {
        val search = query?.trim()?.lowercase().orEmpty()

        val filtered = if (search.isEmpty()) {
            allUsers
        } else {
            allUsers.filter {
                "${it.firstName} ${it.lastName}".lowercase().contains(search)
            }
        }

        savedUsersAdapter.updateList(filtered)
        binding.tvEmptyList.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvSavedUsers.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }


    private fun subscribeToFollowEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                followViewModel.followerListState.collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            allUsers = result.data ?: emptyList()
                            updateFilteredList(binding.etSearch.text?.toString())
                        }

                        is Result.Error -> {
                            context?.let { showErrorScreen(it) }
                            Log.e("ListFragment", "Error fetching follower list: ${result.errorMessage}")
                        }

                        is Result.Loading -> {
                        }
                    }
                }
            }
        }

        binding.etSearch.addTextChangedListener { text ->
            updateFilteredList(text?.toString())
        }
    }


    private fun unfollowUser(user: UserInfo) {
        followViewModel.unfollowUser(user.userId,
            onSuccess = { message ->
                Toast.makeText(requireContext(), "${user.firstName} удален(-a)!", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun openFullProfile(user: UserInfo) {
        val action = InteractionFragmentDirections.actionInteractionFragmentToFullProfileFragment(user.userId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("ListFragment", "onDestroyView called")
    }

    companion object {
        fun newInstance(): ListFragment {
            return ListFragment()
        }
    }
}