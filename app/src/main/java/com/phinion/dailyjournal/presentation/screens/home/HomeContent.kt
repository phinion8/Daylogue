package com.phinion.dailyjournal.presentation.screens.home

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phinion.dailyjournal.model.Diary
import com.phinion.dailyjournal.presentation.components.DiaryHolder
import java.time.LocalDate
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    diaryNotes: Map<LocalDate, List<Diary>>,
    onClick: (String) -> Unit
) {

    Log.d("DBPS", diaryNotes.size.toString())
    if (diaryNotes.isNotEmpty()) {

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            diaryNotes.forEach { (localDate, diaries) ->
                stickyHeader(key = localDate) {
                    DateHeader(localDate = localDate)
                }

                items(items = diaries, key = {
                    it._id.toString()
                }) {

                    DiaryHolder(diary = it, onClick = onClick)

                }

            }
        }

    } else {
        EmptyPage()
    }

}


@Composable
fun DateHeader(
    localDate: LocalDate
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                //%02d means from the number 1 to 9 will have a 0 appended before the number
                text = String.format("%02d", localDate.dayOfMonth),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Light
            )

            Text(
                //take will skip the characters after 3 characters for eg for TUESDAY it will be TUE
                text = localDate.dayOfWeek.toString().take(3),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Light
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    //month returns the month in english and it will get converted into title case by using the string functions
                    text = localDate.month.toString().lowercase().replaceFirstChar {
                        it.titlecase()
                    },
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light
                )

                Text(
                    //take will skip the characters after 3 characters for eg for TUESDAY it will be TUE
                    text = "${localDate.year}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Light
                )
            }
        }

    }

}

@Composable
fun EmptyPage(
    title: String = "Empty Diary",
    subtitle: String = "Write Something"
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            style = TextStyle(
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Medium
            )
        )

        Text(
            text = subtitle,
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Medium
            )
        )

    }

}

@Preview(showBackground = true)
@Composable
fun DateHeaderPreview() {
    DateHeader(localDate = LocalDate.now())
}