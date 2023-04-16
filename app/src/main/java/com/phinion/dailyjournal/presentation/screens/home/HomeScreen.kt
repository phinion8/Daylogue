package com.phinion.dailyjournal.presentation.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.phinion.dailyjournal.R
import com.phinion.dailyjournal.data.repository.Diaries
import com.phinion.dailyjournal.model.RequestState
import java.time.ZonedDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    diaries: Diaries,
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    onDeleteAllClicked: () -> Unit,
    dateSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    NavigationDrawer(drawerState = drawerState, onSignOutClicked = {
        onSignOutClicked()
    }, onDeleteAllClicked = onDeleteAllClicked) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopBar(
                    scrollBehavior = scrollBehavior,
                    onMenuClicked = onMenuClicked,
                    dateSelected = dateSelected,
                    onDateSelected = onDateSelected,
                    onDateReset = onDateReset
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigateToWrite() }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Dairy Icon")
                }
            },
            content = {
                when (diaries) {
                    is RequestState.Success -> {
                        HomeContent(
                            paddingValues = it,
                            diaryNotes = diaries.data,
                            onClick = { id ->
                                navigateToWriteWithArgs(id)
                            }
                        )
                    }
                    is RequestState.Error -> {
                        EmptyPage(
                            title = "Error",
                            subtitle = "${diaries.error.message}"
                        )
                    }
                    is RequestState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {}
                }

            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    //Content will be the same as the scaffold that's why we are passing the content over here
    onDeleteAllClicked: () -> Unit,
    content: @Composable () -> Unit
) {

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(250.dp),
                        painter = painterResource(id = com.phinion.dailyjournal.R.drawable.logo),
                        contentDescription = "app logo"
                    )
                }
                NavigationDrawerItem(label = {
                    Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Image(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "delete all icon"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Delete All", color = MaterialTheme.colorScheme.onSurface)
                    }

                },
                    selected = false,
                    onClick = {
                        onDeleteAllClicked()
                    })
                NavigationDrawerItem(label = {
                    Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = "google logo"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Sign Out", color = MaterialTheme.colorScheme.onSurface)
                    }

                },
                    selected = false,
                    onClick = {
                        onSignOutClicked()
                    })
            })
        },
        content = content
    )


}