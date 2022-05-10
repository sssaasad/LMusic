package com.lalilu.lmusic.screen

import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.dirror.lyricviewx.GRAVITY_CENTER
import com.dirror.lyricviewx.GRAVITY_LEFT
import com.dirror.lyricviewx.GRAVITY_RIGHT
import com.lalilu.R
import com.lalilu.common.HapticUtils
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.manager.SpManager
import com.lalilu.lmusic.screen.viewmodel.MainViewModel
import com.lalilu.lmusic.service.GlobalData
import com.lalilu.ui.*
import com.lalilu.ui.appbar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val CLICK_HANDLE_MODE_CLICK = 0
const val CLICK_HANDLE_MODE_DOUBLE_CLICK = 1
const val CLICK_HANDLE_MODE_LONG_CLICK = 2

@IntDef(
    CLICK_HANDLE_MODE_CLICK,
    CLICK_HANDLE_MODE_LONG_CLICK,
    CLICK_HANDLE_MODE_DOUBLE_CLICK
)
@Retention(AnnotationRetention.SOURCE)
annotation class ClickHandleMode

@Composable
@ExperimentalMaterialApi
fun PlayingScreen(
    scope: CoroutineScope = rememberCoroutineScope(),
    scaffoldShow: suspend () -> Unit = {},
    scaffoldHide: suspend () -> Unit = {},
    onSongSelected: suspend (MediaItem) -> Unit = {},
    onSongMoveToNext: (MediaItem) -> Boolean = { false },
    onSongRemoved: (MediaItem) -> Boolean = { false },
    onSongShowDetail: suspend (MediaItem) -> Unit = {},
    onPlayNext: suspend () -> Unit = {},
    onPlayPrevious: suspend () -> Unit = {},
    onPlayPause: suspend () -> Unit = {},
    onSeekToPosition: suspend (Float) -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel()
) {
    fun playHandle(@ClickPart clickPart: Int) {
        when (clickPart) {
            CLICK_PART_LEFT -> onPlayPrevious
            CLICK_PART_MIDDLE -> onPlayPause
            CLICK_PART_RIGHT -> onPlayNext
            else -> null
        }?.let {
            scope.launch { it() }
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val statusPaddingTop = LocalDensity.current.run {
        statusBarPadding.calculateTopPadding().toPx()
    }

    val context = LocalContext.current
    AndroidViewBinding(factory = { inflater, parent, attachToParent ->
        FragmentPlayingBinding.inflate(inflater, parent, attachToParent).apply {
            @ClickHandleMode
            var clickHandleMode = CLICK_HANDLE_MODE_CLICK
            val activity = context.getActivity()!!
            val haptic = { HapticUtils.haptic(this.root) }
            val doubleHaptic = { HapticUtils.doubleHaptic(this.root) }
            val behavior = fmAppbarLayout.behavior as MyAppbarBehavior
            activity.setSupportActionBar(fmToolbar)

            adapter = PlayingAdapter().apply {
                onItemClick = { item, _ -> scope.launch { onSongSelected(item) } }
                onItemLongClick = { item, _ -> scope.launch { onSongShowDetail(item) } }
                onItemDragOrSwipedListener = object : PlayingAdapter.OnItemDragOrSwipedListener {
                    override fun onDelete(mediaItem: MediaItem): Boolean {
                        return onSongRemoved(mediaItem)
                    }

                    override fun onAddToNext(mediaItem: MediaItem): Boolean {
                        return onSongMoveToNext(mediaItem)
                    }
                }
            }

            behavior.addOnStateChangeListener(object :
                ExpendHeaderBehavior.OnScrollToStateListener(STATE_COLLAPSED, STATE_NORMAL) {
                override fun onScrollToStateListener() {
                    if (fmToolbar.hasExpandedActionView())
                        fmToolbar.collapseActionView()
                }
            })
            behavior.addOnStateChangeListener(object :
                ExpendHeaderBehavior.OnScrollToStateListener(STATE_NORMAL, STATE_EXPENDED) {
                override fun onScrollToStateListener() {
                    if (fmToolbar.hasExpandedActionView())
                        fmToolbar.collapseActionView()
                }
            })

            fmToolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.appbar_search) {
                    fmAppbarLayout.setExpanded(false, true)
                }
                true
            }

            fmRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0 &&
                        fmToolbar.hasExpandedActionView() &&
                        KeyboardUtils.isSoftInputVisible(activity)
                    ) {
                        KeyboardUtils.hideSoftInput(activity)
                    }
                }
            })

            SpManager.listen(Config.KEY_SETTINGS_LYRIC_GRAVITY,
                SpManager.SpIntListener(Config.DEFAULT_SETTINGS_LYRIC_GRAVITY) {
                    when (it) {
                        0 -> fmLyricViewX.setTextGravity(GRAVITY_LEFT)
                        1 -> fmLyricViewX.setTextGravity(GRAVITY_CENTER)
                        2 -> fmLyricViewX.setTextGravity(GRAVITY_RIGHT)
                    }
                })
            SpManager.listen(Config.KEY_SETTINGS_SEEKBAR_HANDLER,
                SpManager.SpIntListener(Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER) {
                    clickHandleMode = it
                })

            SpManager.listen(Config.KEY_SETTINGS_LYRIC_TEXT_SIZE,
                SpManager.SpIntListener(Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE) {
                    val textSize = ConvertUtils.sp2px(it.toFloat()).toFloat()
                    fmLyricViewX.setNormalTextSize(textSize)
                    fmLyricViewX.setCurrentTextSize(textSize * 1.2f)
                })

            fmTopPic.palette.observe(activity, this::setPalette)

            maSeekBar.scrollListeners.add(object : OnSeekBarScrollToThresholdListener({ 300f }) {
                override fun onScrollToThreshold() {
                    scope.launch { scaffoldShow() }
                    haptic()
                }

                override fun onScrollRecover() {
                    scope.launch { scaffoldHide() }
                    haptic()
                }
            })
            maSeekBar.clickListeners.add(object : OnSeekBarClickListener {
                override fun onClick(@ClickPart clickPart: Int, action: Int) {
                    haptic()
                    if (clickHandleMode != CLICK_HANDLE_MODE_CLICK) {
                        scope.launch { onPlayPause() }
                        return
                    }
                    playHandle(clickPart)
                }

                override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                    haptic()
                    if (clickHandleMode != CLICK_HANDLE_MODE_LONG_CLICK) return
                    playHandle(clickPart)
                }

                override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                    doubleHaptic()
                    if (clickHandleMode != CLICK_HANDLE_MODE_DOUBLE_CLICK) return
                    playHandle(clickPart)
                }
            })
            maSeekBar.seekToListeners.add(object : OnSeekBarSeekToListener {
                override fun onSeekTo(value: Float) {
                    scope.launch { onSeekToPosition(value) }
                }
            })
            maSeekBar.cancelListeners.add(object : OnSeekBarCancelListener {
                override fun onCancel() {
                    haptic()
                }
            })
            GlobalData.currentPlaylistLiveData.observe(activity) {
                adapter?.setDiffNewData(it.toMutableList())
            }
            GlobalData.currentMediaItemLiveData.observe(activity) {
                maSeekBar.maxValue = (it?.mediaMetadata?.getDuration()
                    ?.coerceAtLeast(0) ?: 0f).toFloat()
                song = it
            }
            GlobalData.currentPositionLiveData.observe(activity) {
                maSeekBar.updateValue(it.toFloat())
                fmLyricViewX.updateTime(it)
            }
            mainViewModel.songLyric.observe(activity) {
                fmLyricViewX.setLyricEntryList(emptyList())
                fmLyricViewX.loadLyric(it?.first, it?.second)
            }
        }
    }) {
        (fmToolbar.layoutParams as ViewGroup.MarginLayoutParams).also {
            it.topMargin = statusPaddingTop.toInt()
        }
    }
}

fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}