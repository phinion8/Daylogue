package com.phinion.dailyjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.phinion.dailyjournal.data.ImageToDeleteDao
import com.phinion.dailyjournal.data.database.ImageToUploadDao
import com.phinion.dailyjournal.navigation.Screen
import com.phinion.dailyjournal.navigation.SetupNavGraph
import com.phinion.dailyjournal.ui.theme.DailyJournalTheme
import com.phinion.dailyjournal.util.Constants.APP_ID
import com.phinion.dailyjournal.util.retryDeletingImageFromFirebase
import com.phinion.dailyjournal.util.retryUploadImageToFirebase
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao
    @Inject
    lateinit var imageToDeleteDao: ImageToDeleteDao
    var keepSplashOpened = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        FirebaseApp.initializeApp(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DailyJournalTheme {

                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = {
                        keepSplashOpened = false
                    }
                )


            }
        }
        cleanupCheck(scope = lifecycleScope, imageToUploadDao = imageToUploadDao, imageToDeleteDao = imageToDeleteDao)
    }
}

private fun getStartDestination(): String {
    val user = App.create(APP_ID).currentUser
    return if ((user != null) && user.loggedIn) Screen.Home.route
    else Screen.Authentication.route
}

private fun cleanupCheck(
    scope: CoroutineScope,
    imageToUploadDao: ImageToUploadDao,
    imageToDeleteDao: ImageToDeleteDao
) {

    scope.launch(Dispatchers.IO) {
        val result = imageToUploadDao.getAllImages()
        result.forEach { imageToUpload ->

            retryUploadImageToFirebase(
                imageToUpload = imageToUpload,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToUploadDao.cleanUpImage(imageId = imageToUpload.id)
                    }
                }
            )



        }
    }

    scope.launch(Dispatchers.IO) {
        val result2 = imageToDeleteDao.getAllImage()
        result2.forEach {imageToDelete->
            retryDeletingImageFromFirebase(
                imageToDelete = imageToDelete,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToUploadDao.cleanUpImage(imageId = imageToDelete.id)
                    }
                }
            )

        }

    }


}

