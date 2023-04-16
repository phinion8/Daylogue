package com.phinion.dailyjournal.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.phinion.dailyjournal.R
import com.phinion.dailyjournal.model.GalleryImage
import com.phinion.dailyjournal.model.GalleryState
import com.phinion.dailyjournal.ui.theme.Elevation
import kotlin.math.max

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    images: List<Uri>,
    imageSize: Dp = 40.dp,
    spaceBetween: Dp = 10.dp,
    imageShape: CornerBasedShape = Shapes().small //small = 8.dp
) {
    BoxWithConstraints(modifier = modifier) {
        val numberOfVisibleImages = remember {
            //this will store the result simple calculation with in this variable
            derivedStateOf {
                //we are calculating the max value of the images that can hold according to the screen size
                //lets say the width size is 500dp and the here the image size + the space between size is 50
                //so the no of the images will be 500/50 will equal to 10 and and we are subtracting the value with 1
                //so it will become 9 to place one place holder that will show the no of the extra images
                max(
                    a = 0, b = this.maxWidth.div(spaceBetween + imageSize).toInt().minus(1)
                )
            }


        }

        val remainingImages = remember {
            derivedStateOf {
                images.size - numberOfVisibleImages.value
            }
        }

        Row {
            images.take(numberOfVisibleImages.value).forEach { image ->
                AsyncImage(
                    modifier = Modifier
                        .clip(imageShape)
                        .size(imageSize),
                    model = ImageRequest.Builder(LocalContext.current).data(image).build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Gallery Image",
                    placeholder = painterResource(id = R.drawable.image_placeholder)
                )
                Spacer(modifier = Modifier.width(spaceBetween))
            }
            if (remainingImages.value > 0) {

                LastImageOverlay(
                    imageSize = imageSize,
                    remainingImages = remainingImages.value,
                    imagesShape = imageShape
                )
            }
        }
    }
}

@Composable
fun GalleryUploader(
    modifier: Modifier = Modifier,
    galleryState: GalleryState,
    imageSize: Dp = 60.dp,
    imageShape: CornerBasedShape = Shapes().medium,
    spaceBetween: Dp = 12.dp,
    onAddClicked: () -> Unit,
    onImageSelect: (Uri) -> Unit,
    onImageClicked: (GalleryImage) -> Unit
) {

    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { images ->

        images.forEach {
            onImageSelect(it)
        }

    }

    BoxWithConstraints(modifier = modifier) {
        val numberOfVisibleImages = remember {
            //this will store the result simple calculation with in this variable
            derivedStateOf {
                //we are calculating the max value of the images that can hold according to the screen size
                //lets say the width size is 500dp and the here the image size + the space between size is 50
                //so the no of the images will be 500/50 will equal to 10 and and we are subtracting the value with 1
                //so it will become 9 to place one place holder that will show the no of the extra images
                max(
                    a = 0, b = this.maxWidth.div(spaceBetween + imageSize).toInt().minus(2)
                )
            }


        }

        val remainingImages = remember {
            derivedStateOf {
                galleryState.images.size - numberOfVisibleImages.value
            }
        }

        Row {
            AddImageButton(imageSize = imageSize, imageShape = imageShape, onClick = {
                onAddClicked()
                multiplePhotoPicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            })
            Spacer(modifier = Modifier.width(spaceBetween))
            galleryState.images.take(numberOfVisibleImages.value).forEach { galleryImage ->
                AsyncImage(
                    modifier = Modifier
                        .clip(imageShape)
                        .size(imageSize)
                        .clickable {
                            onImageClicked(galleryImage)
                        },
                    model = ImageRequest.Builder(LocalContext.current).data(galleryImage.image).build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Gallery Image",
                    placeholder = painterResource(id = R.drawable.image_placeholder)
                )
                Spacer(modifier = Modifier.width(spaceBetween))
            }
            if (remainingImages.value > 0) {

                LastImageOverlay(
                    imageSize = imageSize,
                    remainingImages = remainingImages.value,
                    imagesShape = imageShape
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddImageButton(
    imageSize: Dp,
    imageShape: CornerBasedShape,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .size(imageSize)
            .clip(shape = imageShape),
        onClick = onClick,
        tonalElevation = Elevation.Level1
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "add icon")
        }

    }

}

@Composable
fun LastImageOverlay(
    imageSize: Dp, remainingImages: Int, imagesShape: CornerBasedShape
) {

    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .clip(imagesShape)
                .size(imageSize),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}
        Text(
            text = "+$remainingImages", style = TextStyle(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.Medium
            ), color = Color.White
        )

    }

}
