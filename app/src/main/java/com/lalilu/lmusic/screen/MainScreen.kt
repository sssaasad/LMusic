package com.lalilu.lmusic.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalilu.lmusic.screen.component.NavigateLibrary
import com.lalilu.lmusic.utils.DeviceType.Pad
import com.lalilu.lmusic.utils.DeviceType.Phone
import com.lalilu.lmusic.utils.LocalNavigatorHost
import com.lalilu.lmusic.utils.WindowSize.*
import com.lalilu.lmusic.utils.WindowSizeClass
import com.lalilu.lmusic.utils.getActivity
import com.lalilu.lmusic.utils.safeLaunch
import kotlinx.coroutines.CoroutineScope

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun MainScreen(
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val navController = LocalNavigatorHost.current
    val scaffoldState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp +
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateTopPadding()
    val screenHeight = LocalDensity.current.run { screenHeightDp.toPx() }
    val backgroundColor = MaterialTheme.colors.background
    val isVisible = { scaffoldState.offset.value < screenHeight }

    val onPopUp: () -> Unit = {
        if (navController.previousBackStackEntry == null) {
            scope.safeLaunch { scaffoldState.hide() }
        } else {
            navController.navigateUp()
        }
    }

    val onClose: () -> Unit = {
        scope.safeLaunch { scaffoldState.hide() }
    }

    val onExpendModal: () -> Unit = {
        scope.safeLaunch { scaffoldState.animateTo(ModalBottomSheetValue.Expanded) }
    }

    val showScaffold: suspend () -> Unit = {
        scaffoldState.show()
    }

    val scaffoldShow: suspend () -> Unit = {
        navController.navigate(
            from = MainScreenData.Library.name,
            to = MainScreenData.Library.name,
            clearAllBefore = true
        )
        showScaffold()
    }

    val onSongShowDetail: suspend (MediaItem) -> Unit = {
        navController.navigate(
            from = "${MainScreenData.SongsDetail.name}/${it.mediaId}",
            to = "${MainScreenData.SongsDetail.name}/${it.mediaId}",
            clearAllBefore = true
        )
        showScaffold()
    }

    ModalBottomSheetLayout(
        sheetState = scaffoldState,
        modifier = Modifier.fillMaxWidth(),
        sheetBackgroundColor = backgroundColor,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetContent = {
            Row {
                NavigateLibrary(
                    onExpendModal = onExpendModal,
                    onPopUp = onPopUp,
                    onClose = onClose
                )
            }
        }
    ) {
        PlayingScreen(
            scope = scope,
            onSongShowDetail = onSongShowDetail,
            onExpendBottomSheet = scaffoldShow,
            onCollapseBottomSheet = scaffoldState::hide,
        )
    }

    val context = LocalContext.current
    BackHandlerWithNavigator(
        navController = navController,
        onBack = {
            if (isVisible()) {
                scope.safeLaunch { scaffoldState.hide() }
            } else {
                context.getActivity()?.moveTaskToBack(false)
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun MainScreenForCompat(
    currentWindowSizeClass: WindowSizeClass,
    navController: NavHostController,
    scaffoldState: ModalBottomSheetState,
    scope: CoroutineScope = rememberCoroutineScope(),
    bottomSheetContent: @Composable () -> Unit = {},
) {
    val isEnableBottomSheet = currentWindowSizeClass.windowSize == Compact
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp +
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateTopPadding()
    val screenHeight = LocalDensity.current.run { screenHeightDp.toPx() }
    val isVisible = { scaffoldState.offset.value < screenHeight }

    val backgroundColor = MaterialTheme.colors.background
    val elevation = if (!isEnableBottomSheet) 5.dp else 0.dp
    val color = if (!isEnableBottomSheet) backgroundColor else Color.Transparent

    val showScaffold: suspend () -> Unit = {
        /**
         * 在直接从Composable的参数传入的情况下，
         * 不知为何不能在此函数内获取[currentWindowSizeClass]的最新值，
         * 故直接从单例中获取该值
         */
        if (WindowSizeClass.instance?.windowSize == Compact)
            scope.safeLaunch { scaffoldState.show() }
    }
    val scaffoldShow: suspend () -> Unit = {
        navController.navigate(
            from = MainScreenData.Library.name,
            to = MainScreenData.Library.name,
            clearAllBefore = isEnableBottomSheet
        )
        showScaffold()
    }
    val onSongShowDetail: suspend (MediaItem) -> Unit = {
        navController.navigate(
            from = "${MainScreenData.SongsDetail.name}/${it.mediaId}",
            to = "${MainScreenData.SongsDetail.name}/${it.mediaId}",
            clearAllBefore = isEnableBottomSheet
        )
        showScaffold()
    }

    ModalBottomSheetLayout(
        modifier = when (currentWindowSizeClass.deviceType) {
            Phone -> when (currentWindowSizeClass.windowSize) {
                Compact -> Modifier.fillMaxWidth()
                Medium -> Modifier.fillMaxWidth(0.5f)
                Expanded -> Modifier.fillMaxWidth(0.5f)
            }
            Pad -> when (currentWindowSizeClass.windowSize) {
                Compact -> Modifier.fillMaxWidth(0.5f)
                Medium -> Modifier.fillMaxWidth(0.5f)
                Expanded -> Modifier.width(screenHeightDp / 2f)
            }
        },
        sheetState = scaffoldState,
        sheetBackgroundColor = backgroundColor,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetContent = { bottomSheetContent() }
    ) {
        Surface(
            elevation = elevation,
            color = color
        ) {
            PlayingScreen(
                scope = scope,
                onSongShowDetail = onSongShowDetail,
                onExpendBottomSheet = scaffoldShow,
                onCollapseBottomSheet = scaffoldState::hide,
            )
        }
    }

    val context = LocalContext.current
    BackHandlerWithNavigator(
        navController = navController,
        onBack = {
            if (isVisible() && isEnableBottomSheet) {
                scope.safeLaunch { scaffoldState.hide() }
            } else {
                context.getActivity()?.moveTaskToBack(false)
            }
        }
    )
}

@Composable
fun BackHandlerWithNavigator(navController: NavController, onBack: () -> Unit) {
    val enable = remember(navController.currentBackStackEntryAsState().value) {
        navController.previousBackStackEntry == null
    }

    BackHandler(
        enabled = enable,
        onBack = onBack
    )
}