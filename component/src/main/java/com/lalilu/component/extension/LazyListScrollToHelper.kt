package com.lalilu.component.extension

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LazyListScrollToHelper internal constructor(
    private val onScrollTo: (delay: Long, scrollOffset: Int, animateTo: Boolean, action: () -> Int?) -> Unit
) {
    private val keys: MutableSet<Any> = mutableSetOf()
    private var finished: Boolean = false

    fun getKeys(): Collection<Any> = keys

    fun startRecord() {
        keys.clear()
        finished = false
    }

    fun doRecord(key: Any) {
        if (finished) return
        keys.add(key)
    }

    fun doRecord(key: Collection<Any>) {
        if (finished) return
        keys.addAll(key)
    }

    fun endRecord() {
        finished = true
    }

    fun scrollToItem(
        key: Any,
        animateTo: Boolean = false,
        scrollOffset: Int = 0,
        delay: Long = 0L
    ) {
        onScrollTo(delay, scrollOffset, animateTo) {
            keys.indexOf(key)
                .takeIf { it >= 0 }
        }
    }
}

@Composable
fun rememberLazyListScrollToHelper(
    listState: LazyListState
): LazyListScrollToHelper {
    val scope = rememberCoroutineScope()

    return remember {
        LazyListScrollToHelper { delayTimeMillis, scrollOffset, animateTo, action ->
            scope.launch {
                delay(delayTimeMillis)
                val index = action() ?: return@launch
                if (animateTo) {
                    listState.animateScrollToItem(
                        index = index,
                        scrollOffset = scrollOffset
                    )
                } else {
                    listState.scrollToItem(
                        index = index,
                        scrollOffset = scrollOffset
                    )
                }
            }
        }
    }
}