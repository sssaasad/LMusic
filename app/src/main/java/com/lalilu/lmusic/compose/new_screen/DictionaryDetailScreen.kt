package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.DictionariesViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun DictionaryDetailScreen(
    dictionaryId: String,
    songsVM: SongsViewModel = get(),
    playingVM: PlayingViewModel = get(),
    dictionariesVM: DictionariesViewModel = get(),
    navigator: DestinationsNavigator
) {
    val dictionary = dictionariesVM.requireDictionary(dictionaryId) ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "[Error]加载失败 #$dictionaryId")
        }
        return
    }

    val sortFor = remember { "DictionaryDetail" }

    LaunchedEffect(dictionary) {
        songsVM.updateBySongs(
            songs = dictionary.songs,
            showAll = true,
            sortFor = sortFor
        )
    }

    val showSortPanel = remember { mutableStateOf(false) }
    val songsState by songsVM.songsState

    SortPanelWrapper(
        sortFor = sortFor,
        showPanelState = showSortPanel,
        supportSortPresets = {
            listOf(
                SortPreset.SortByAddTime,
                SortPreset.SortByTitle,
                SortPreset.SortByLastPlayTime,
                SortPreset.SortByPlayedTimes,
                SortPreset.SortByDuration
            )
        },
        supportGroupRules = { emptyList() },
        supportSortRules = { emptyList() },
        supportOrderRules = { emptyList() }
    ) {
        SongListWrapper(
            songsState = songsState,
            isItemPlaying = { playingVM.isSongPlaying(mediaId = it.id) },
            hasLyricState = { playingVM.requireHasLyricState(item = it) },
            onLongClickItem = { navigator.navigate(SongDetailScreenDestination(mediaId = it.id)) },
            onClickItem = { playingVM.playSongWithPlaylist(songsState.values.flatten(), it) }
        ) {
            item {
                NavigatorHeader(
                    title = dictionary.name,
                    subTitle = "共 ${songsState.values.flatten().size} 首歌曲"
                ) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = dayNightTextColor(0.05f),
                        onClick = { showSortPanel.value = !showSortPanel.value }
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.subtitle2,
                            color = dayNightTextColor(0.7f),
                            text = "排序"
                        )
                    }
                }
            }
        }
    }
}