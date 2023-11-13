package com.lalilu.lmusic.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lalilu.component.base.BottomSheetNavigator
import com.lalilu.lmusic.compose.component.CustomTransition

@OptIn(ExperimentalMaterialApi::class)
object DialogWrapper {
    var navigator: BottomSheetNavigator? = null
        private set

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        BottomSheetNavigator(
            modifier = modifier.fillMaxSize(),
            scrimColor = Color.Black.copy(alpha = 0.5f),
            sheetBackgroundColor = MaterialTheme.colors.background,
            animationSpec = SpringSpec(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 1000f
            ),
            sheetContent = { bottomSheetNavigator ->
                this.navigator = bottomSheetNavigator
                CustomTransition(navigator = bottomSheetNavigator.getNavigator()) {
                    it.Content()
                }
            },
            content = { content() }
        )
    }
}