package com.phinion.dailyjournal.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.phinion.dailyjournal.data.repository.MongoDB
import com.phinion.dailyjournal.model.GalleryImage
import com.phinion.dailyjournal.model.Mood
import com.phinion.dailyjournal.presentation.components.DisplayAlertDialog
import com.phinion.dailyjournal.presentation.screens.auth.AuthenticationScreen
import com.phinion.dailyjournal.presentation.screens.auth.AuthenticationViewModel
import com.phinion.dailyjournal.presentation.screens.home.HomeScreen
import com.phinion.dailyjournal.presentation.screens.home.HomeViewModel
import com.phinion.dailyjournal.presentation.screens.write.WriteScreen
import com.phinion.dailyjournal.presentation.screens.write.WriteViewModel
import com.phinion.dailyjournal.util.Constants
import com.phinion.dailyjournal.util.Constants.APP_ID
import com.phinion.dailyjournal.model.RequestState
import com.phinion.dailyjournal.model.rememberGalleryState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit
) {

    NavHost(navController = navController, startDestination = startDestination) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            onDataLoaded = onDataLoaded,
            navigateToWriteWithArgs = {
                navController.navigate(Screen.Write.passDairyId(it))
            }
        )
        writeRoute(
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }

}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Authentication.route) {

        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val authenticated by viewModel.authenticated
        val onTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }

        AuthenticationScreen(
            loadingState = loadingState, onButtonClicked = {

                onTapState.open()
                viewModel.setLoading(true)

            }, onTapState = onTapState, messageBarState = messageBarState,
            onSuccessfulFirebaseSignIn = { tokenId ->

                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {

                        messageBarState.addSuccess("Successfully Authenticated...")

                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(Exception(it))
                        viewModel.setLoading(false)
                    }
                )

            },
            onFailedFirebaseSignIn = {
                messageBarState.addError(it)
                viewModel.setLoading(false)
            },
            onDialogDismissed = {
                messageBarState.addError(Exception(it))
            }, authenticated = authenticated, navigateToHome = navigateToHome
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit
) {
    composable(route = Screen.Home.route) {

        val viewModel: HomeViewModel = hiltViewModel()

        val context = LocalContext.current

        val diaries by viewModel.diaries

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember {
            mutableStateOf(false)
        }

        var deleteAllDialogOpened by remember {
            mutableStateOf(false)
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            navigateToWrite = navigateToWrite,
            onMenuClicked = {

                scope.launch {
                    drawerState.open()
                }

            },
            drawerState = drawerState,
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            onDeleteAllClicked = {
                deleteAllDialogOpened = true
            },
            dateSelected = viewModel.dateIsSelected,
            onDateSelected = {
                viewModel.getDiaries(zonedDateTime = it)
            },
            onDateReset = { viewModel.getDiaries() }
        )


        LaunchedEffect(key1 = Unit) {
            MongoDB.configureTheRealm()
        }

        DisplayAlertDialog(
            title = "Delete All Diaries",
            message = "Are you sure you want permanently delete all your diaries?",
            dialogOpened = deleteAllDialogOpened,
            onCloseDialog = { deleteAllDialogOpened = false },
            onYesClicked = {

                viewModel.deleteDiaries(
                    onSuccess = {
                        deleteAllDialogOpened = false
                        Toast.makeText(
                            context,
                            "All Diaries Deleted Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {

                        deleteAllDialogOpened = false
                        Toast.makeText(
                            context,
                            if (it.message == "No internet connection") "We need an internet connection to delete the diaries" else it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }

                    }
                )

            })


        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out from your google account?",
            dialogOpened = signOutDialogOpened,
            onCloseDialog = { signOutDialogOpened = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            navigateToAuth()
                        }
                    }
                }
            })

    }
}

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(
    onBackPressed: () -> Unit
) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = Constants.WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val context = LocalContext.current
        val viewModel: WriteViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val pagerState = rememberPagerState()
        val galleryState = viewModel.galleryState
        val pageNumber by remember {
            derivedStateOf { pagerState.currentPage }
        }

        LaunchedEffect(key1 = uiState) {
            Log.d("SelectedDiary", "${uiState.selectedDiaryId}")
        }

        WriteScreen(onBackPressed = {
            onBackPressed()
        },
            moodName = { Mood.values()[pageNumber].name },
            onDeleteConfirmed = {
                viewModel.deleteDiary(onSuccess = {
                    Toast.makeText(
                        context,
                        "Deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    onBackPressed()
                }, onError = {
                    Toast.makeText(
                        context,
                        it,
                        Toast.LENGTH_SHORT
                    ).show()
                })
            },
            pagerState = pagerState,
            uiState = uiState,
            onTitleChanged = { viewModel.setTitle(it) },
            onDescriptionChanged = { viewModel.setDescription(it) },
            onSaveClick = {
                viewModel.upsertDiary(diary = it.apply {
                    mood = Mood.values()[pageNumber].name
                },
                    onSuccess = { onBackPressed() },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    })
            },
            onDateTimeUpdated = {
                viewModel.updateDateTime(zonedDateTime = it)
            },
            galleryState = galleryState,
            onImageSelect = {
                val type = context.contentResolver.getType(it)?.split("/")?.last() ?: "jpg"
                viewModel.addImage(
                    image = it,
                    imageType = type
                )
            },
            onImageDeleteClicked = {
                galleryState.removeImage(it)
            }
        )


    }
}