package com.example.bondbuddy.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.Message
import com.example.bondbuddy.utils.Result
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var buttonSend: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    private var currentChatId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        val args: ChatFragmentArgs by navArgs()
        val otherUserId = args.otherUserId
        val firstName = args.firstName ?: ""
        val lastName = args.lastName ?: ""
        val fullName = "$firstName $lastName".trim()

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.title = fullName

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        recyclerView = view.findViewById(R.id.recyclerViewChat)
        editText = view.findViewById(R.id.editTextMessage)
        buttonSend = view.findViewById(R.id.buttonSend)
        adapter = ChatAdapter(messages, otherUserId)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        buttonSend.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotBlank() && currentChatId != null) {
                viewModel.sendMessage(chatId = currentChatId!!, text = text)
                editText.text.clear()
            }
        }

        observeViewModel(otherUserId)

        viewModel.startChat(otherUserId)

        return view
    }

    private fun observeViewModel(otherUserId: Int) {
        viewModel.chatIdState.onEach { result ->
            when (result) {
                is Result.Loading -> {
                }
                is Result.Success -> {
                    currentChatId = result.data?.toInt()

                    result.data?.toIntOrNull()?.let { chatId ->
                        viewModel.loadMessages(chatId)

                        currentChatId?.let { id ->
                            viewModel.updateReadStatus(id, true)
                        }
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), "Ошибка загрузки чата", Toast.LENGTH_SHORT).show()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.messagesState.onEach { result ->
            when (result) {
                is Result.Loading -> { }
                is Result.Success -> {
                    messages.clear()
                    result.data?.let {
                        messages.addAll(it)
                        adapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.sendMessageResult.onEach { result ->
            when (result) {
                is Result.Loading -> {}
                is Result.Success -> {
                    currentChatId?.let { viewModel.loadMessages(it) }
                    viewModel.clearSendState()
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show()
                    viewModel.clearSendState()
                }
                null -> {}
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.readStatusResult.onEach { result ->
            when (result) {
                is Result.Loading -> {}
                is Result.Success -> {
                }
                is Result.Error -> {
                }
                null -> {}
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.VISIBLE
    }
}
