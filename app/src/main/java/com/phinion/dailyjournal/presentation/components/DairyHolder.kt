package com.phinion.dailyjournal.presentation.components

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.phinion.dailyjournal.model.Diary
import com.phinion.dailyjournal.model.Mood
import com.phinion.dailyjournal.ui.theme.Elevation
import com.phinion.dailyjournal.util.fetchImagesFromFirebase
import com.phinion.dailyjournal.util.toInstant
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

@Composable
fun DiaryHolder(
    diary: Diary,
    onClick: (String) -> Unit
) {


    //will be used to calculate the height of the line
    val localDensity = LocalDensity.current
    //Height of the line before the actual component
    var componentHeight by remember {
        mutableStateOf(0.dp)
    }

    val context = LocalContext.current

    var galleryOpened by remember {
        mutableStateOf(false)
    }

    var galleryLoading by remember {
        mutableStateOf(false)
    }

    val downloadedImages = remember {
        mutableStateListOf<Uri>()
    }

    LaunchedEffect(key1 = galleryOpened) {

        if (galleryOpened && downloadedImages.isEmpty()) {
            galleryLoading = true
            fetchImagesFromFirebase(
                remoteImagePath = diary.images,
                onImageDownload = { image ->
                    downloadedImages.add(image)

                },
                onImageDownloadFailed = {
                    Toast.makeText(
                        context, "Images not uploaded yet." +
                                "try to upload again or wait some time.", Toast.LENGTH_SHORT
                    ).show()
                    galleryLoading = false
                    galleryOpened = false

                },
                onReadyToDisplay = {
                    Log.d("DBPS", "Ready to display")
                    galleryLoading = false
                    galleryOpened = true


                }
            )
        }

    }

    Row(modifier = Modifier.clickable(
        //To disable the ripple effect
        indication = null,
        interactionSource = remember {
            MutableInteractionSource()
        }
    ) {
        onClick(diary._id.toString())
    }) {
        Spacer(modifier = Modifier.width(14.dp))

        Surface(
            modifier = Modifier
                .width(2.dp)
                .height(componentHeight + 14.dp),
            tonalElevation = Elevation.Level1
        ) {}

        Spacer(modifier = Modifier.width(20.dp))

        Surface(
            modifier = Modifier
                .clip(shape = Shapes().medium)
                .onGloballyPositioned {
                    //calculating the actual height of the line
                    componentHeight = with(localDensity) {
                        it.size.height.toDp()
                    }
                },
            tonalElevation = Elevation.Level1
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {
                DiaryHeader(moodName = diary.mood, time = diary.date.toInstant())
                Text(
                    modifier = Modifier.padding(all = 16.dp),
                    text = diary.description,
                    style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (diary.images.isNotEmpty()) {
                    ShowGalleryButton(
                        galleryOpened = galleryOpened,
                        galleryLoading = galleryLoading,
                        onClick = {
                            galleryOpened = !galleryOpened
                        }
                    )
                }

                AnimatedVisibility(
                    visible = galleryOpened && !galleryLoading,
                    enter = fadeIn() + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(all = 14.dp)) {
                        Gallery(images = downloadedImages)
                    }
                }
            }

        }


    }

}

@Composable
fun DiaryHeader(moodName: String, time: Instant) {

    val mood by remember {
        mutableStateOf(Mood.valueOf(moodName))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(mood.containerColor)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = mood.icon),
                contentDescription = "Mood icon"
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = mood.name,
                color = mood.contentColor,
                style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            )
        }

        Text(
            text = SimpleDateFormat("hh:mm a", Locale.US)
                .format(Date.from(time)),
            color = mood.contentColor,
            style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        )

    }

}

@Composable
fun ShowGalleryButton(
    galleryOpened: Boolean,
    galleryLoading: Boolean,
    onClick: () -> Unit
) {

    TextButton(onClick = { onClick() }) {
        Text(
            text = if (galleryOpened) {
                if (galleryLoading){
                    "Loading.."
                }else{
                    "Hide Gallery"
                }
            }else{
                "Show Gallery"
            },
            style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)
        )
    }

}
