package com.phinion.dailyjournal.model

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Composable
//this will allow us to remember the gallery state across multiple recomposition
fun rememberGalleryState(): GalleryState {
    return remember {
        GalleryState()
    }
}
//This class will hold the images that we select from the photo picker
class GalleryState {

    //this variable will represent list of the gallery images
    val images = mutableStateListOf<GalleryImage>()
    val imageToBeDeleted = mutableStateListOf<GalleryImage>()

    fun addImage(galleryImage: GalleryImage) {
        images.add(galleryImage)
    }

    fun removeImage(galleryImage: GalleryImage) {
        images.remove(galleryImage)
        imageToBeDeleted.add(galleryImage)
    }


}

//This data class will represent single gallery image
data class GalleryImage(
    val image: Uri,
    val remoteImagePath: String = ""
)
