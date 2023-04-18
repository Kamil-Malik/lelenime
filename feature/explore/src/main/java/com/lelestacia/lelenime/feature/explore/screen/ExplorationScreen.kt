package com.lelestacia.explore.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.lelestacia.lelenime.core.common.displayStyle.DisplayStyle
import com.lelestacia.lelenime.core.common.lazyAnime.LazyGridAnime
import com.lelestacia.lelenime.core.common.lazyAnime.LazyListAnime
import com.lelestacia.lelenime.core.model.Anime
import com.lelestacia.lelenime.feature.explore.R
import com.lelestacia.lelenime.feature.explore.component.DashboardDisplayTypeHeader
import com.lelestacia.lelenime.feature.explore.component.DashboardSearchHeader
import com.lelestacia.lelenime.feature.explore.screen.DisplayType
import com.lelestacia.lelenime.feature.explore.stateAndEvent.ExploreScreenEvent
import com.lelestacia.lelenime.feature.explore.stateAndEvent.ExploreScreenState

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun ExplorationScreen(
    screenState: ExploreScreenState,
    isDarkMode: Boolean,
    onEvent: (ExploreScreenEvent) -> Unit,
    onAnimeClicked: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagingAnime: LazyPagingItems<Anime> = screenState.anime.collectAsLazyPagingItems()
    val listOfLazyGridState: Map<DisplayType, LazyGridState> = mapOf(
        Pair(DisplayType.POPULAR, rememberLazyGridState()),
        Pair(DisplayType.AIRING, rememberLazyGridState()),
        Pair(DisplayType.UPCOMING, rememberLazyGridState())
    )
    val listOfLazyListState: Map<DisplayType, LazyListState> = mapOf(
        Pair(DisplayType.POPULAR, rememberLazyListState()),
        Pair(DisplayType.AIRING, rememberLazyListState()),
        Pair(DisplayType.UPCOMING, rememberLazyListState())
    )

    val lazyGridState = listOfLazyGridState[screenState.displayType] ?: rememberLazyGridState()
    val lazyListState = listOfLazyListState[screenState.displayType] ?: rememberLazyListState()

    Scaffold(
        topBar = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                DashboardSearchHeader(
                    screenState = screenState,
                    onEvent = onEvent
                )
                DashboardDisplayTypeHeader(
                    state = screenState,
                    isDarkMode = isDarkMode,
                    onEvent = onEvent,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Divider()
            }
        },
        modifier = modifier
            .semantics {
                testTagsAsResourceId = true
            }
    ) { paddingValue ->
        when (val refreshing = pagingAnime.loadState.refresh) {
            is LoadState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue),
                    verticalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = refreshing.error.message ?: stringResource(id = com.lelestacia.lelenime.core.common.R.string.unknown_error))
                    Button(
                        onClick = { pagingAnime.retry() },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(text = stringResource(id = com.lelestacia.lelenime.core.common.R.string.retry))
                    }
                }
                return@Scaffold
            }

            LoadState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("explore:loading")
                    )
                }
                return@Scaffold
            }

            is LoadState.NotLoading -> {
                if (pagingAnime.itemCount == 0 && screenState.displayType == DisplayType.SEARCH) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValue)
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.anime_not_found,
                                screenState.headerScreenState.searchedAnimeTitle
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                    return@Scaffold
                }

                if (screenState.displayStyle == DisplayStyle.LIST) {
                    LazyListAnime(
                        lazyListState = lazyListState,
                        pagingAnime = pagingAnime,
                        modifier = Modifier
                            .padding(paddingValue)
                            .testTag("explore:scrollAnime"),
                        onAnimeClicked = onAnimeClicked
                    )
                } else {
                    LazyGridAnime(
                        lazyGridState = lazyGridState,
                        pagingAnime = pagingAnime,
                        displayStyle = screenState.displayStyle,
                        modifier = Modifier
                            .padding(paddingValue)
                            .testTag("explore:scrollAnime"),
                        onAnimeClicked = onAnimeClicked
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewExplorationScreen() {
    ExplorationScreen(
        screenState = ExploreScreenState(),
        isDarkMode = false,
        onEvent = {},
        onAnimeClicked = {}
    )
}
