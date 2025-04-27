package com.example.bondbuddy.ui.discover

import com.example.bondbuddy.ui.adapters.DiscoverAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.databinding.FragmentDiscoverBinding
import com.example.bondbuddy.ui.favourites.FollowViewModel
import com.example.bondbuddy.ui.profile.InterestsViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.bondbuddy.utils.Result
import com.example.bondbuddy.utils.showErrorScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.random.Random

@AndroidEntryPoint
class DiscoverFragment : Fragment() {
    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private lateinit var discoverAdapter: DiscoverAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var snapHelper: PagerSnapHelper
    private lateinit var recyclerView: RecyclerView

    private val followViewModel: FollowViewModel by activityViewModels()

    private val discoverViewModel: DiscoverViewModel by activityViewModels()
    private val userInterestsViewModel: InterestsViewModel by viewModels()

    private var tagList: List<String> = emptyList()

    // saved vars if user want to not save changes
    private var currentSelectedInterests: String = ""
    private var currentMinAge: String = ""
    private var currentMaxAge: String = ""
    private var currentCity: String = ""
    private var currentCountry: String = ""
    private var currentLanguages: String = ""

    private var themeOfTheDay: String = ""
    private var shouldScrollToTop = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        userInterestsViewModel.loadTags()
        userInterestsViewModel.allInterestsTags.observe(viewLifecycleOwner) { tags ->
            if (tags != null) {
                tagList = tags
            }
            loadThemeOfDay();
        }
        discoverViewModel.getCurrentUserRecommendations()

    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                discoverViewModel.displayedUsersState.collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val users = result.data ?: emptyList()
                            discoverAdapter.submitList(users) {
                                if (shouldScrollToTop) {
                                    binding.rvDiscover.scrollToPosition(0)
                                    shouldScrollToTop = false
                                }

                            }
                            binding.tvEmptyState.visibility =
                                if (users.isNotEmpty()) View.GONE else View.VISIBLE
                            Log.d("DiscoverFragment", "Recommendations loaded: ${users.size} users")
                        }

                        is Result.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Log.e(
                                "DiscoverFragment",
                                "Error fetching recommendations: ${result.data}"
                            )
                            context?.let { showErrorScreen(it) }
                            binding.tvEmptyState.text = "ERROR"
                            binding.tvEmptyState.visibility = View.VISIBLE
                            discoverAdapter.submitList(emptyList())
                        }

                        is Result.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.tvEmptyState.visibility = View.GONE
                            Log.d("DiscoverFragment", "Loading recommendations...")
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        discoverAdapter = DiscoverAdapter(
            { user -> openFullProfile(user) },
            { user -> addToFollowed(user) }
        )

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        recyclerView = binding.rvDiscover.apply {
            adapter = discoverAdapter
            setLayoutManager(layoutManager)
        }
        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }

    private fun addToFollowed(user: UserInfo) {
        followViewModel.followUser(user.userId,
            onSuccess = { message ->
                Toast.makeText(
                    requireContext(),
                    "${user.firstName} добавлен(-а)!",
                    Toast.LENGTH_SHORT
                ).show()
                followViewModel.refreshFollowerList()
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupClickListeners() {
        binding.buttonFilters.setOnClickListener {
            showFilterDialog()
        }
        binding.buttonExploreTheme.setOnClickListener {
            currentSelectedInterests = themeOfTheDay
            val themeList = listOf(themeOfTheDay)
            applyFilters(
                themeList,
                null,
                null,
                null,
                null,
                null
            )
            Toast.makeText(requireContext(), "Поиск выполнен!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFilterDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.discover_options, null)

        builder.setView(dialogView)
        val dialog = builder.create()

        val multiAutoCompleteTextViewInterests =
            dialogView.findViewById<MultiAutoCompleteTextView>(R.id.multiAutoCompleteTextViewInterests)
        val multiAutoCompleteTextViewLanguages =
            dialogView.findViewById<MultiAutoCompleteTextView>(R.id.multiAutoCompleteTextViewLanguages)
        val editTextMinAge = dialogView.findViewById<EditText>(R.id.editTextMinAge)
        val editTextMaxAge = dialogView.findViewById<EditText>(R.id.editTextMaxAge)
        val editTextCity = dialogView.findViewById<EditText>(R.id.editTextCity)
        val editTextCountry = dialogView.findViewById<EditText>(R.id.editTextCountry)
        val buttonApply = dialogView.findViewById<Button>(R.id.buttonApply)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonClear = dialogView.findViewById<Button>(R.id.buttonClear)

        val adapterInterests =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tagList)
        multiAutoCompleteTextViewInterests.setAdapter(adapterInterests)
        multiAutoCompleteTextViewInterests.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        multiAutoCompleteTextViewInterests.threshold = 1
        multiAutoCompleteTextViewInterests.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus && multiAutoCompleteTextViewInterests.text.isEmpty()) {
                    multiAutoCompleteTextViewInterests.showDropDown()
                }
            }
        multiAutoCompleteTextViewInterests.setOnClickListener {
            multiAutoCompleteTextViewInterests.showDropDown()
        }


        val languages = listOf(
            "English", "Español", "Français", "Deutsch", "Italiano", "Português", "Русский",
            "中文", "日本語", "한국어", "العربية", "हिन्दी", "বাংলা", "ਪੰਜਾਬੀ", "Türkçe",
            "Tiếng Việt", "ไทย", "Ελληνικά", "Nederlands", "Svenska", "Suomi", "Dansk",
            "Norsk", "Polski", "Українська", "Čeština", "Magyar", "Română", "Български",
            "Српски", "Hrvatski", "Slovenčina", "Slovenščina", "Lietuvių", "Latviešu",
            "Eesti", "Íslenska", "עברית", "فارسی", "اردو", "Bahasa Melayu",
            "Bahasa Indonesia", "Filipino", "Kiswahili", "አማርኛ", "Yorùbá", "isiZulu",
            "Afrikaans", "Shqip", "Հայերեն", "Azərbaycanca", "Беларуская", "Bosanski",
            "Català", "ქართული", "Қазақша", "Kurdî", "Македонски", "Монгол",
            "नेपाली", "पश्तो", "සිංහල", "தமிழ்", "తెలుగు", "Тоҷикӣ",
            "Türkmençe", "Oʻzbekcha", "Cymraeg"
        )
        val adapterLanguages =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)
        multiAutoCompleteTextViewLanguages.setAdapter(adapterLanguages)
        multiAutoCompleteTextViewLanguages.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        multiAutoCompleteTextViewLanguages.threshold = 1
        multiAutoCompleteTextViewLanguages.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus && multiAutoCompleteTextViewLanguages.text.isEmpty()) {
                    multiAutoCompleteTextViewLanguages.showDropDown()
                }
            }
        multiAutoCompleteTextViewLanguages.setOnClickListener {
            multiAutoCompleteTextViewLanguages.showDropDown()
        }

        multiAutoCompleteTextViewInterests.setText(currentSelectedInterests)
        multiAutoCompleteTextViewLanguages.setText(currentLanguages)
        editTextMinAge.setText(currentMinAge)
        editTextMaxAge.setText(currentMaxAge)
        editTextCity.setText(currentCity)
        editTextCountry.setText(currentCountry)

        buttonClear.setOnClickListener {
            multiAutoCompleteTextViewInterests.text.clear()
            multiAutoCompleteTextViewLanguages.text.clear()
            editTextMinAge.text.clear()
            editTextMaxAge.text.clear()
            editTextCity.text.clear()
            editTextCountry.text.clear()

            currentSelectedInterests = ""
            currentLanguages = ""
            currentMinAge = ""
            currentMaxAge = ""
            currentCity = ""
            currentCountry = ""

            shouldScrollToTop = true
            discoverViewModel.resetToDefaultRecommendations()
            dialog.dismiss()
        }

        buttonApply.setOnClickListener {
            val selectedInterestsRaw = multiAutoCompleteTextViewInterests.text.toString()
            val selectedLanguagesRaw = multiAutoCompleteTextViewLanguages.text.toString()
            val minAge = editTextMinAge.text.toString().trim()
            val maxAge = editTextMaxAge.text.toString().trim()
            val city = editTextCity.text.toString().trim()
            val country = editTextCountry.text.toString().trim()

            currentSelectedInterests = selectedInterestsRaw
            currentLanguages = selectedLanguagesRaw
            currentMinAge = minAge
            currentMaxAge = maxAge
            currentCity = city
            currentCountry = country

            val interestsList = selectedInterestsRaw.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .takeIf { it.isNotEmpty() }

            val languagesList = selectedLanguagesRaw.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .takeIf { it.isNotEmpty() }

            Log.d(
                "DiscoverFragment",
                "Applying Filters - Interests: $interestsList, Languages: $languagesList, MinAge: $minAge, MaxAge: $maxAge, City: $city, Country: $country"
            )

            applyFilters(
                interestsList,
                languagesList,
                minAge,
                maxAge,
                city,
                country
            )
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyFilters(
        interests: List<String>?,
        languages: List<String>?,
        minAge: String?,
        maxAge: String?,
        city: String?,
        country: String?
    ) {
        shouldScrollToTop = true
        discoverViewModel.getFilteredRecommendations(
            interests,
            minAge,
            maxAge,
            city,
            country,
            languages
        )
    }


    private fun openFullProfile(user: UserInfo) {
        val action =
            DiscoverFragmentDirections.actionDiscoverFragmentToFullProfileFragment(user.userId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("DiscoverFragment", "onDestroyView called")
    }

    private fun loadThemeOfDay() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val seedString = String.format("%d%02d%02d", year, month + 1, day)

        val seed = seedString.hashCode().toLong()
        val random = Random(seed)
        val randomIndex = random.nextInt(tagList.size)
        themeOfTheDay = tagList[randomIndex]
        binding.tvThemeOfDay.text = themeOfTheDay
    }

}
