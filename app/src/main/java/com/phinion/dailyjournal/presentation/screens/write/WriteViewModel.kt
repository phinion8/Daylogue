package com.phinion.dailyjournal.presentation.screens.write

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.phinion.dailyjournal.data.ImageToDeleteDao
import com.phinion.dailyjournal.data.database.ImageToUploadDao
import com.phinion.dailyjournal.data.database.entity.ImageToDelete
import com.phinion.dailyjournal.data.database.entity.ImageToUpload
import com.phinion.dailyjournal.data.repository.MongoDB
import com.phinion.dailyjournal.model.*
import com.phinion.dailyjournal.util.Constants
import com.phinion.dailyjournal.util.fetchImagesFromFirebase
import com.phinion.dailyjournal.util.toRealmInstant
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    //this saved state handle allow us to access the argument
    private val savedStateHandle: SavedStateHandle,
    private val imageToUploadDao: ImageToUploadDao,
    private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {

    val galleryState = GalleryState()
    var uiState by mutableStateOf(UiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument() {

        uiState = uiState.copy(
            selectedDiaryId = savedStateHandle.get<String>(
                key = Constants.WRITE_SCREEN_ARGUMENT_KEY
            )
        )


    }

    private fun fetchSelectedDiary() {
        if (uiState.selectedDiaryId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                MongoDB.getSelectedDiary(
                    diaryId = ObjectId.Companion.from(uiState.selectedDiaryId!!)
                ).catch {
                    emit(RequestState.Error(Exception("Diary is already deleted...")))
                }
                    .collect { diary ->

                    if (diary is RequestState.Success) {
                        setSelectedDiary(diary = diary.data)
                        setTitle(diary.data.title)
                        setDescription(diary.data.description)
                        setMood(mood = Mood.valueOf(diary.data.mood))
                        fetchImagesFromFirebase(
                            remoteImagePath = diary.data.images,
                            onImageDownload = {downloadImage->
                                galleryState.addImage(
                                    GalleryImage(
                                        image = downloadImage,
                                        remoteImagePath = extractImagePath(
                                            fullImageUrl = downloadImage.toString()
                                        )
                                    ),
                                )

                            },
                            onImageDownloadFailed = {

                            },
                            onReadyToDisplay = {

                            }

                        )

                    }

                }
            }
        }
    }

    private fun setSelectedDiary(diary: Diary) {
        uiState = uiState.copy(selectedDiary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    fun updateDateTime(zonedDateTime: ZonedDateTime) {

        uiState = uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant())


    }

    //Update or insert the diary
    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedDiaryId != null) {

                updateDiary(diary = diary, onSuccess = onSuccess, onError = onError)

            } else {
                insertDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            }


        }

    }

    private suspend fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = MongoDB.insertDiary(diary = diary.apply {
                if (uiState.updatedDateTime != null) {
                    date = uiState.updatedDateTime!!
                }
            })
            if (result is RequestState.Success) {
                uploadImageToFirebase()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else if (result is RequestState.Error) {
                withContext(Dispatchers.Main) {
                    onError(result.error.message.toString())
                }
            }
        }
    }

    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDB.updateDiary(diary = diary.apply {
            _id = ObjectId.from(uiState.selectedDiaryId!!)
            date = if (uiState.updatedDateTime != null) {
                uiState.updatedDateTime!!
            } else {
                uiState.selectedDiary!!.date
            }
        })
        if (result is RequestState.Success) {
            uploadImageToFirebase()
            deleteImagesFromFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }
    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {

            if (uiState.selectedDiaryId != null){
                val result = MongoDB.deleteDiary(id = ObjectId.from(uiState.selectedDiaryId!!))
                if (result is RequestState.Success){
                    withContext(Dispatchers.Main){
                        uiState.selectedDiary?.let { deleteImagesFromFirebase(images = it.images) }
                        onSuccess()
                    }
                }else if (result is RequestState.Error){
                    withContext(Dispatchers.IO){
                        onError(result.error.message.toString())
                    }
                }
            }
        }
    }
    fun addImage(image: Uri, imageType: String){

        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/"+
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"

        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath
            )
        )

    }

    private fun uploadImageToFirebase(){
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach {galleryImage->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri
                    if (sessionUri != null){
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToUploadDao.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = galleryImage.remoteImagePath,
                                    imageUri = galleryImage.image.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun deleteImagesFromFirebase(images: List<String>? = null){
        val storage = FirebaseStorage.getInstance().reference
        if (images != null) {
            images.forEach{remotePath->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(
                                    remoteImagePath = remotePath
                                )
                            )
                        }
                    }
            }
        }else{
            galleryState.imageToBeDeleted.map {
                it.remoteImagePath
            }.forEach {remotePath->
                storage.child(remotePath).delete().addOnFailureListener {
                    viewModelScope.launch(Dispatchers.IO) {
                        imageToDeleteDao.addImageToDelete(
                            ImageToDelete(
                                remoteImagePath = remotePath
                            )
                        )
                    }
                }
            }
        }
    }

    private fun extractImagePath(fullImageUrl: String): String{
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }

}

data class UiState(
    val selectedDiaryId: String? = null,
    val selectedDiary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null
)