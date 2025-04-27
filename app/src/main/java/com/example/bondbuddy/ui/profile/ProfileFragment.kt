package com.example.bondbuddy.ui.profile

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.MultiAutoCompleteTextView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.Interest
import com.example.bondbuddy.data.remote.models.SocialMedia
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.databinding.FragmentProfileBinding
import com.example.bondbuddy.ui.adapters.InterestAdapter
import com.example.bondbuddy.ui.adapters.SocialMediaAdapter
import com.example.bondbuddy.utils.Result
import com.example.bondbuddy.utils.SessionManager
import com.example.bondbuddy.utils.bitmapToByteArray
import com.example.bondbuddy.utils.byteArrayToBitmap
import com.example.bondbuddy.utils.showErrorScreen
import com.example.bondbuddy.utils.uriToByteArray
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    @Inject
    lateinit var sessionManager: SessionManager


    private val userInfoViewModel: UserInfoViewModel by activityViewModels()
    private val userInterestsViewModel: InterestsViewModel by activityViewModels()
    private val userSocialsViewModel: SocialsViewModel by activityViewModels()
    private val languagesViewModel: LanguagesViewModel by activityViewModels()

    private var profileImageUri: Uri? = null
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                profileImageUri = it
                binding.imageViewProfile.setImageURI(it)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchLocationAndFill()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Разрешение на геолокацию отклонено",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    private var allLanguagesList: List<String> = emptyList()
    private var tagList: List<String> = emptyList()


    private lateinit var imageViewProfile: ImageView

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextCity: EditText
    private lateinit var editTextCountry: EditText
    private lateinit var editTextAge: EditText
    private lateinit var editTextBio: EditText
    private lateinit var editTextOccupation: EditText
    private lateinit var editTextLanguages: MultiAutoCompleteTextView
    private lateinit var buttonEdit: Button

    private lateinit var textViewLocationHint: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private lateinit var interestsAdapter: InterestAdapter
    private lateinit var socialMediaAdapter: SocialMediaAdapter

    private var isEditing = false

    private var originalFirstName: String = ""
    private var originalLastName: String = ""
    private var originalAge: String = ""
    private var originalBio: String = ""
    private var originalOccupation: String = ""
    private var originalLanguages: String = ""
    private var originalCountry: String = ""
    private var originalPhoto: ByteArray? = null
    private var originalCity: String = ""
    private var prevEmail: String = ""

    private var originalInterests: List<Interest> = emptyList()
    private var originalSocialMediaList: List<SocialMedia> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews()
        setupRecyclerView()

        lifecycleScope.launch {
            val userEmail = sessionManager.getCurrentUserEmail()
            setupClickListeners(userEmail)
        }

        languagesViewModel.loadLanguages()
        userInfoViewModel.getCurrentUserInfo()
        userInterestsViewModel.getCurrentUserInterests()
        userSocialsViewModel.getCurrentUserSocials()
        languagesViewModel.getUserLanguages()

        userInterestsViewModel.loadTags()
        userInterestsViewModel.allInterestsTags.observe(viewLifecycleOwner) { tags ->
            if (tags != null) {
                tagList = tags
            }
        }

        editTextAge.inputType = InputType.TYPE_CLASS_NUMBER

        subscribeToCurrentUserInfoEvents()
    }


    private fun initializeViews() {
        editTextFirstName = binding.editTextFirstName
        editTextLastName = binding.editTextLastName
        editTextAge = binding.editTextAge
        editTextBio = binding.editTextBio
        editTextCountry = binding.editTextCountry
        editTextCity = binding.editTextCity
        editTextOccupation = binding.editTextOccupation
        editTextLanguages = binding.editTextLanguages
        buttonEdit = binding.buttonEdit

        textViewLocationHint = binding.textViewLocationHint

        imageViewProfile = binding.imageViewProfile

        binding.btnAddInterest.visibility = View.GONE
        binding.btnAddSocialMedia.visibility = View.GONE
        binding.imageViewAddIcon.visibility = View.GONE

        editTextLanguages.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        editTextLanguages.threshold = 0
    }

    private fun setupRecyclerView() {
        interestsAdapter = InterestAdapter(
            onInterestClick = { position, interest ->
                showInterestOptionsDialog(position, interest)
            },
            isEditModeEnabled = { isEditing }
        )
        val flexLayoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        binding.rvInterests.apply {
            adapter = interestsAdapter
            layoutManager = flexLayoutManager
        }


        socialMediaAdapter = SocialMediaAdapter(
            onEdit = { position -> showEditSocialMediaDialog(position) },
            onDelete = { position -> showDeleteSocialMediaDialog(position) },
            isEditModeEnabled = { isEditing }
        )

        binding.rvSocialMedia.apply {
            adapter = socialMediaAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners(userEmail: String?) {
        val btn = binding.buttonAccount
        if (userEmail != null) {
            prevEmail = userEmail
        }


        btn.setOnClickListener {
            val userName =
                binding.editTextFirstName.text.toString() + " " + binding.editTextLastName.text.toString()

            val directions = ProfileFragmentDirections.actionProfileFragmentToAuthInfoFragment(
                userName = userName,
                userEmail = userEmail ?: "Неизвестно"
            )
            findNavController().navigate(directions)

        }


        binding.btnAddInterest.setOnClickListener {
            showAddInterestDialog()
        }

        binding.btnAddSocialMedia.setOnClickListener {
            showAddSocialMediaDialog()
        }

        imageViewProfile.setOnClickListener {
            if (isEditing) {
                showImageOptionsDialog()
            }
        }

        buttonEdit.setOnClickListener {
            if (!isEditing) {
                storeOriginalValues()
                enableEditing(true)
            } else {
                sendDataToServer()
                enableEditing(false)
            }
        }

        binding.buttonCancelEdit.setOnClickListener {
            restoreOriginalValues()
            enableEditing(false)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.imageViewLocationIcon.setOnClickListener {
            if (!isEditing) return@setOnClickListener

            checkLocationPermissionAndFetch()
        }
    }

    private fun showImageOptionsDialog() {
        val options = arrayOf("Выбрать новое фото", "Удалить фото")

        AlertDialog.Builder(requireContext())
            .setTitle("Фото профиля")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> getContent.launch("image/*")
                    1 -> removeProfilePhoto()
                }
            }
            .show()
    }

    private fun removeProfilePhoto() {
        profileImageUri = null
        binding.imageViewProfile.setImageResource(R.drawable.ic_person)
    }

    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocationAndFill()
        }
    }

    private fun fetchLocationAndFill() {
        val context = requireContext()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Нет разрешения на доступ к геолокации", Toast.LENGTH_SHORT)
                .show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val country = address.countryName ?: ""
                    val city = address.locality ?: address.subAdminArea ?: ""

                    binding.editTextCountry.setText(country)
                    binding.editTextCity.setText(city)
                }
            } else {
                Toast.makeText(context, "Не удалось получить местоположение", Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Ошибка при получении местоположения", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun subscribeToCurrentUserInfoEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    languagesViewModel.languages.collectLatest { languages_list ->
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            languages_list ?: emptyList()
                        )
                        editTextLanguages.setAdapter(adapter)

                    }
                }

                launch {
                    languagesViewModel.userLanguagesState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                val userLanguages = result.data
                                val languageNames =
                                    userLanguages?.joinToString(", ") { it.nativeName }
                                binding.editTextLanguages.setText(languageNames ?: "")
                            }

                            is Result.Error -> {
                                binding.editTextLanguages.setText("Ошибка загрузки")
                                context?.let { showErrorScreen(it) }
                            }

                            is Result.Loading -> {

                            }

                        }


                    }
                }

                launch {
                    userInfoViewModel.userState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                binding.editTextFirstName.setText(
                                    result.data?.firstName ?: "NO Name"
                                )
                                binding.editTextLastName.setText(
                                    result.data?.lastName ?: "NO Last Name"
                                )
                                binding.editTextAge.setText(result.data?.age?.toString() ?: "")
                                binding.editTextBio.setText(result.data?.bio ?: "")
                                binding.editTextCountry.setText(result.data?.country ?: "")
                                binding.editTextCity.setText(result.data?.city ?: "")
                                binding.editTextOccupation.setText(result.data?.occupation ?: "")

                                val photoByteArray = result.data?.photo
                                val bitmap = byteArrayToBitmap(photoByteArray)
                                if (bitmap != null) {
                                    binding.imageViewProfile.setImageBitmap(bitmap)
                                } else {
                                    binding.imageViewProfile.setImageResource(R.drawable.ic_person)
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

                launch {
                    userInterestsViewModel.getInterestsState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                val interestsList = result.data ?: emptyList()
                                interestsAdapter.interests = interestsList
                                interestsAdapter.notifyDataSetChanged()
                            }

                            is Result.Error -> {
                            }

                            is Result.Loading -> {
                            }
                        }
                    }
                }

                launch {
                    userSocialsViewModel.getSocialsState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                socialMediaAdapter.socialMediaList = result.data ?: emptyList()
                                socialMediaAdapter.notifyDataSetChanged()
                            }

                            is Result.Error -> {
                            }

                            is Result.Loading -> {
                            }
                        }
                    }
                }
            }
        }

    }


    private fun sendDataToServer() {

        val firstName = binding.editTextFirstName.text.toString()
        val lastName = binding.editTextLastName.text.toString()
        val age = binding.editTextAge.text.toString().toIntOrNull()
        val bio = binding.editTextBio.text.toString()
        val country = binding.editTextCountry.text.toString()
        val city = binding.editTextCity.text.toString()
        val occupation = binding.editTextOccupation.text.toString()
        val languages = binding.editTextLanguages.text.toString()
        val photoByteArray = profileImageUri?.let { uriToByteArray(requireContext(), it) }
        val interestNames: String = interestsAdapter.interests.joinToString(", ") { it.name }


        val userInfo = UserInfo(
            userId = -1,
            firstName = firstName,
            lastName = lastName,
            age = age,
            bio = if (bio.isNotEmpty()) bio else null,
            country = if (country.isNotEmpty()) country else null,
            city = if (city.isNotEmpty()) city else null,
            occupation = if (occupation.isNotEmpty()) occupation else null,
            photo = photoByteArray,
            interests = interestNames
        )
        userInfoViewModel.saveServerCurrentUserInfo(userInfo)
        val userInterestsList = interestsAdapter.interests
        userInterestsViewModel.saveServerCurrentUserInterests(userInterestsList)

        val languagesList = languages.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        languagesViewModel.updateUserLanguages(languagesList)


        val socialsList = socialMediaAdapter.socialMediaList
        userSocialsViewModel.saveServerCurrentUserSocials(socialsList)
    }

    private fun enableEditing(enable: Boolean) {

        editTextFirstName.isEnabled = enable
        editTextLastName.isEnabled = enable
        editTextAge.isEnabled = enable
        editTextBio.isEnabled = enable
        editTextCountry.isEnabled = enable
        editTextCity.isEnabled = enable
        editTextOccupation.isEnabled = enable
        editTextLanguages.isEnabled = enable


        val background =
            if (enable) R.drawable.edittext_background_enabled else R.drawable.edittext_background
        editTextFirstName.setBackgroundResource(background)
        editTextLastName.setBackgroundResource(background)
        editTextAge.setBackgroundResource(background)
        editTextBio.setBackgroundResource(background)
        editTextCountry.setBackgroundResource(background)
        editTextCity.setBackgroundResource(background)
        editTextOccupation.setBackgroundResource(background)
        editTextLanguages.setBackgroundResource(background)

        textViewLocationHint.visibility = if (enable) View.VISIBLE else View.GONE
        binding.buttonCancelEdit.visibility = if (enable) View.VISIBLE else View.GONE

        if (enable) {
            editTextBio.requestFocus()
        }

        binding.btnAddInterest.visibility = if (enable) View.VISIBLE else View.GONE
        binding.btnAddSocialMedia.visibility = if (enable) View.VISIBLE else View.GONE
        binding.imageViewAddIcon.visibility = if (enable) View.VISIBLE else View.GONE


        buttonEdit.text = if (enable) "Сохранить" else "Редактировать"
        socialMediaAdapter.notifyDataSetChanged()

        isEditing = enable
    }


    private fun showAddInterestDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Добавить интерес")

        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_edit_interest, null)

        val interestInput = layout.findViewById<EditText>(R.id.editTextInterestName)
        val autoCompleteTextView =
            layout.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewInterestTag)

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tagList)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.threshold = 0

        autoCompleteTextView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.showDropDown()
            }
        }

        builder.setView(layout)

        builder.setPositiveButton("Добавить") { _, _ ->
            val interestName = interestInput.text.toString().trim()
            val selectedTag = autoCompleteTextView.text.toString().trim()

            if (interestName.isNotEmpty()) {
                val newList = interestsAdapter.interests.toMutableList()
                newList.add(Interest(interestName, selectedTag))
                interestsAdapter.interests = newList
                interestsAdapter.notifyDataSetChanged()
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showInterestOptionsDialog(position: Int, interest: Interest) {
        val options = arrayOf("Переименовать", "Удалить", "Отмена")
        AlertDialog.Builder(requireContext())
            .setTitle("Действия с интересом")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameInterestDialog(position, interest)
                    1 -> showDeleteConfirmationDialog(position)
                    2 -> {
                    }
                }
            }
            .show()
    }

    private fun showRenameInterestDialog(position: Int, currentInterest: Interest) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Редактировать интерес")

        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_edit_interest, null)

        val editTextName = layout.findViewById<EditText>(R.id.editTextInterestName)
        val autoCompleteTextViewTag =
            layout.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewInterestTag)


        editTextName.setText(currentInterest.name)


        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tagList)
        autoCompleteTextViewTag.setAdapter(adapter)


        autoCompleteTextViewTag.setText(currentInterest.type, false)

        builder.setView(layout)

        builder.setPositiveButton("Сохранить") { _, _ ->
            val newName = editTextName.text.toString().trim()
            val newTag = autoCompleteTextViewTag.text.toString().trim()

            if (newName.isNotEmpty() && newTag.isNotEmpty()) {
                val newList = interestsAdapter.interests.toMutableList()
                newList[position] = Interest(newName, newTag)
                interestsAdapter.interests = newList
                interestsAdapter.notifyDataSetChanged()
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }

        builder.show()
    }


    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить интерес?")
            .setMessage("Вы уверены, что хотите удалить этот интерес?")
            .setPositiveButton("Удалить") { _, _ ->
                val newList = interestsAdapter.interests.toMutableList()
                newList.removeAt(position)
                interestsAdapter.interests = newList
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
            .show()
    }


    private fun showAddSocialMediaDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Добавить социальную сеть")

        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_add_social_media, null)

        val platformNameInput = layout.findViewById<EditText>(R.id.editTextSocialName)
        val linkInput = layout.findViewById<EditText>(R.id.editTextSocialLink)

        builder.setView(layout)

        builder.setPositiveButton("Добавить") { _, _ ->
            val platformName = platformNameInput.text.toString().trim()
            val link = linkInput.text.toString().trim()

            if (platformName.isNotEmpty() && link.isNotEmpty()) {
                val newSocialMedia = SocialMedia(platformName, link)
                val newList = socialMediaAdapter.socialMediaList.toMutableList()
                newList.add(newSocialMedia)
                socialMediaAdapter.socialMediaList = newList

                socialMediaAdapter.notifyDataSetChanged()
            }
        }
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showEditSocialMediaDialog(position: Int) {
        val socialMedia = socialMediaAdapter.socialMediaList[position]
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Редактировать социальную сеть")

        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_social_media, null)
        builder.setView(view)

        val editTextPlatformName = view.findViewById<EditText>(R.id.editTextSocialName)
        val editTextLink = view.findViewById<EditText>(R.id.editTextSocialLink)

        editTextPlatformName.setText(socialMedia.socialName)
        editTextLink.setText(socialMedia.socialLink)

        builder.setPositiveButton("Сохранить") { _, _ ->
            val newPlatformName = editTextPlatformName.text.toString().trim()
            val newLink = editTextLink.text.toString().trim()

            if (newPlatformName.isNotEmpty() && newLink.isNotEmpty()) {
                val newSocialMedia = SocialMedia(newPlatformName, newLink)
                val newList = socialMediaAdapter.socialMediaList.toMutableList()
                newList[position] = newSocialMedia
                socialMediaAdapter.socialMediaList = newList
                socialMediaAdapter.notifyDataSetChanged()
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showDeleteSocialMediaDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить социальную сеть?")
            .setMessage("Вы уверены, что хотите удалить эту социальную сеть?")
            .setPositiveButton("Удалить") { _, _ ->
                val newList = socialMediaAdapter.socialMediaList.toMutableList()
                newList.removeAt(position)
                socialMediaAdapter.socialMediaList = newList
                socialMediaAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun restoreOriginalValues() {
        binding.editTextFirstName.setText(originalFirstName)
        binding.editTextLastName.setText(originalLastName)
        binding.editTextAge.setText(originalAge)
        binding.editTextBio.setText(originalBio)
        binding.editTextOccupation.setText(originalOccupation)
        binding.editTextLanguages.setText(originalLanguages)
        interestsAdapter.interests = originalInterests.toMutableList()
        socialMediaAdapter.socialMediaList = originalSocialMediaList.toMutableList()
        if (originalPhoto != null) {
            val bitmap = byteArrayToBitmap(originalPhoto)
            binding.imageViewProfile.setImageBitmap(bitmap)
        } else {
            binding.imageViewProfile.setImageResource(R.drawable.ic_person)
        }

        binding.editTextCity.setText(originalCity)
        binding.editTextCountry.setText(originalCountry)

        socialMediaAdapter.notifyDataSetChanged()
        interestsAdapter.notifyDataSetChanged()
    }

    private fun storeOriginalValues() {
        originalFirstName = binding.editTextFirstName.text.toString()
        originalLastName = binding.editTextLastName.text.toString()
        originalAge = binding.editTextAge.text.toString()
        originalBio = binding.editTextBio.text.toString()
        originalCity = binding.editTextCity.text.toString()
        originalCountry = binding.editTextCountry.text.toString()
        originalOccupation = binding.editTextOccupation.text.toString()
        originalLanguages = binding.editTextLanguages.text.toString()
        originalInterests = interestsAdapter.interests.toList()
        originalSocialMediaList = socialMediaAdapter.socialMediaList.toList()
        val drawable: Drawable? = binding.imageViewProfile.drawable
        originalPhoto = if (drawable is BitmapDrawable) {
            val bitmap: Bitmap = drawable.bitmap
            bitmapToByteArray(bitmap)
        } else {
            null
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}