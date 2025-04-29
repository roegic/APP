package com.example.bondbuddy.ui.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.bondbuddy.data.remote.models.ChatUserInfo
import com.example.bondbuddy.databinding.FragmentChatListBinding
import com.example.bondbuddy.ui.chat.ChatViewModel
import com.example.bondbuddy.ui.person_card.FullProfileFragmentDirections
import com.example.bondbuddy.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatListAdapter
    private val viewModel: ChatViewModel by viewModels()

    private var allChats: List<ChatUserInfo> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun openChat(otherUserId: String, firstName: String, lastName: String) {
        val action = InteractionFragmentDirections
            .actionInteractionFragmentToChatFragment(
                otherUserId = otherUserId.toInt(),
                firstName = firstName,
                lastName = lastName
            )
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatListAdapter(emptyList()) { user ->
            openChat(
                otherUserId = user.otherUserId.toString(),
                firstName = user.firstName,
                lastName = user.lastName
            )
        }

        binding.rvChats.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        binding.rvChats.adapter = adapter

        binding.etSearch.addTextChangedListener { text ->
            val query = text?.toString()?.trim()?.lowercase().orEmpty()

            val filtered = if (query.isEmpty()) {
                allChats
            } else {
                allChats.filter {
                    "${it.firstName} ${it.lastName}".lowercase().contains(query)
                }
            }

            adapter.updateList(filtered)
            binding.tvEmptyChats.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userChatsState.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            allChats = result.data?.sortedBy { it.isRead } ?: emptyList()
                            adapter.updateList(allChats)
                            binding.tvEmptyChats.visibility =
                                if (allChats.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is Result.Loading -> {

                        }
                        is Result.Error -> {
                        }
                    }
                }
            }
        }

        viewModel.getUserChats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ChatListFragment {
            return ChatListFragment()
        }
    }
}
