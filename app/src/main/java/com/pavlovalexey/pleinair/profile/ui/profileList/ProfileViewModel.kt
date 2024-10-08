package com.pavlovalexey.pleinair.profile.ui.profileList

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.profile.data.UserRepository
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import com.pavlovalexey.pleinair.utils.image.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

private const val TAG = "ProfileViewModel"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseUserManager: FirebaseUserManager,
    private val loginAndUserUtils: LoginAndUserUtils,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _selectedArtStyles = MutableLiveData<Set<String>>(emptySet())
    val selectedArtStyles: LiveData<Set<String>> get() = _selectedArtStyles

    private val _bitmap = MutableLiveData<Bitmap?>()
    val bitmap: LiveData<Bitmap?> get() = _bitmap

    init {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUser()
        } else {
            logout()
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val loadedUser = userRepository.getUserById(userId)
                _user.value = loadedUser
                _selectedArtStyles.value = loadedUser?.selectedArtStyles?.toSet() ?: emptySet()
                Log.d(TAG, "=== Загруженный пользователь: $loadedUser")
                if (loadedUser?.profileImageUrl.isNullOrEmpty()) {
                    checkAndGenerateAvatar {
//                        loadUser()
                    }
                }
            }
        }
    }

    fun logout() {
        loginAndUserUtils.logout()
        _user.value = null
    }

    fun isUserSignedIn(): Boolean {
        return loginAndUserUtils.isUserSignedIn()
    }

    fun updateUserName(newName: String, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        loginAndUserUtils.updateUserNameOnFirebase(newName)
        _user.value = _user.value?.copy(name = newName)
        onComplete()
    }

    fun updateUserDescription(newDescription: String, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateUserDescription(
            userId,
            newDescription,
            onSuccess = {
                _user.value = _user.value?.copy(description = newDescription)
                onComplete()
            },
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating user description", e)
            }
        )
    }

    fun checkAndGenerateAvatar(onComplete: () -> Unit) {
        val currentUser = _user.value ?: return
        if (currentUser.profileImageUrl.isEmpty()) {
            val generatedAvatar = ImageUtils.generateRandomAvatar()
            uploadAvatarImageToFirebase(
                imageBitmap = generatedAvatar,
                onSuccess = { uri ->
                    updateProfileImageUrl(uri.toString())
                    onComplete()
                },
                onFailure = {
                    Log.w("ProfileViewModel", "Error uploading generated avatar", it)
                }
            )
        } else {
            onComplete()
        }
    }

    fun uploadAvatarImageToFirebase(
        imageBitmap: Bitmap,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.uploadImageToFirebase(
            userId,
            imageBitmap,
            "profile_images",
            onSuccess = { uri ->
                val updatedUser = _user.value?.copy(profileImageUrl = uri.toString())
                _user.value = updatedUser
                saveProfileImageUrl(uri.toString())
                updateProfileImageUrl(uri.toString())
                onSuccess(uri)
            },
            onFailure = {
                Log.w("ProfileViewModel", "Error uploading image", it)
                onFailure(it)
            }
        )
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.updateImageUrl(
            userId,
            imageUrl,
            "users",
            onSuccess = {
                _user.value = _user.value?.copy(profileImageUrl = imageUrl)
                saveProfileImageUrl(imageUrl)
            },
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating profile image URL", e)
            }
        )
    }

    fun handleCameraResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                val processedBitmap = ImageUtils.compressAndGetCircularBitmap(it)
                _bitmap.value = processedBitmap
                // Загружаем изображение на Firebase и обновляем URL
                uploadAvatarImageToFirebase(
                    imageBitmap = processedBitmap,
                    onSuccess = { uri ->
                        updateProfileImageUrl(uri.toString())
                    },
                    onFailure = {
                        Log.w(TAG, "Ошибка при загрузке изображения", it)
                    }
                )
            }
        }
    }

    fun handleGalleryResult(result: ActivityResult, context: Context) {
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    val processedBitmap = ImageUtils.compressAndGetCircularBitmap(imageBitmap)
                    _bitmap.value = processedBitmap
                    // Загружаем изображение на Firebase и обновляем URL
                    uploadAvatarImageToFirebase(
                        imageBitmap = processedBitmap,
                        onSuccess = { uri ->
                            updateProfileImageUrl(uri.toString())
                        },
                        onFailure = {
                            Log.w(TAG, "Ошибка при загрузке изображения", it)
                        }
                    )
                } catch (e: IOException) {
                    Log.e(TAG, "Ошибка при открытии InputStream для URI", e)
                }
            }
        }
    }

    fun getCameraIntent(): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }

    fun getGalleryIntent(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    fun loadProfileImageFromStorage(onSuccess: (Bitmap) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firebaseUserManager.loadProfileImageFromStorage(
            userId,
            "profile_images",
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun saveProfileImageUrl(url: String) {
        with(sharedPreferences.edit()) {
            putString("profileImageUrl", url)
            apply()
        }
    }

    fun updateSelectedArtStyles(newStyles: Set<String>, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val newStylesList = newStyles.toList()
        firebaseUserManager.updateUserSelectedArtStyles(
            userId,
            newStylesList,
            onSuccess = {
                _selectedArtStyles.value = newStyles
                _user.value = _user.value?.copy(selectedArtStyles = newStylesList)
                onComplete()
            },
            onFailure = { e ->
                Log.w("ProfileViewModel", "Error updating selected art styles", e)
            }
        )
    }
}
