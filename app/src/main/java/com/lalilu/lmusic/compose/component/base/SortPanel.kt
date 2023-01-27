package com.lalilu.lmusic.compose.component.base

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import com.google.accompanist.flowlayout.FlowRow
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmusic.compose.component.SmartBar

/**
 * 将元素的分类分组和顺序设置功能统一成一个SortPanel组件
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SortPanel(
    sortFor: String,
    supportGroupRules: () -> List<GroupRule>,
    supportSortRules: () -> List<SortRule>,
    supportOrderRules: () -> List<OrderRule>,
    recoverTo: @Composable () -> Unit
) {
    var sortRule by rememberDataSaverState(
        key = "${sortFor}_SORT_RULE", default = SortRule.Normal.name
    )
    var orderRule by rememberDataSaverState(
        key = "${sortFor}_ORDER_RULE", default = OrderRule.ASC.name
    )
    var groupRule by rememberDataSaverState(
        key = "${sortFor}_GROUP_RULE", default = GroupRule.Normal.name
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "排序依据")
        FlowRow(mainAxisSpacing = 8.dp) {
            supportSortRules().forEach { item ->
                FilterChip(
                    onClick = { sortRule = item.name },
                    selected = item.name == sortRule,
                    colors = ChipDefaults.filterChipColors(),
                ) {
                    Text(text = item.title)
                }
            }
        }
        Text(text = "分组依据")
        FlowRow(mainAxisSpacing = 8.dp) {
            supportGroupRules().forEach { item ->
                FilterChip(
                    onClick = { groupRule = item.name },
                    selected = item.name == groupRule,
                    colors = ChipDefaults.filterChipColors(),
                ) {
                    Text(text = item.title)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "排序顺序")
                FlowRow(mainAxisSpacing = 8.dp) {
                    supportOrderRules().forEach { item ->
                        FilterChip(
                            onClick = { orderRule = item.name },
                            selected = item.name == orderRule,
                            colors = ChipDefaults.filterChipColors(),
                        ) {
                            Text(text = item.title)
                        }
                    }
                }
            }
            IconTextButton(
                text = "关闭",
                color = Color(0xFF006E7C),
                onClick = { SmartBar.setMainBar(content = recoverTo) }
            )
        }
    }

    BackHandler {
        SmartBar.setMainBar(content = recoverTo)
    }
}