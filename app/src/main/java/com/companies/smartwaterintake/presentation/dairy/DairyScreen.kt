package com.companies.smartwaterintake.presentation.dairy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.DateRangeType
import com.companies.smartwaterintake.presentation.navigation.BottomNavigationBar
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.common.component.TextComponent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DairyScreen(
    navHostController: NavHostController,
    state: AppState,
    dispatch: (AppAction) -> Unit,
) {
    var selectedRange by remember { mutableStateOf(DateRangeType.WEEKLY) }

    LaunchedEffect(selectedRange) {
        dispatch(AppAction.LoadHydrationChartData(selectedRange))
    }

    val chartValues = state.hydrationChartData.map { it.second.toInt() }
   val xAxisLabels = when (selectedRange) {
        DateRangeType.WEEKLY -> state.hydrationChartData.map { it.first.dayOfWeek.name.take(2) } // Mon, Tue...
        DateRangeType.MONTHLY -> state.hydrationChartData.map { it.first.dayOfMonth.toString() } // 1, 2, ...
        DateRangeType.YEARLY -> state.hydrationChartData.map { it.first.month.name.take(1) } // Jan, Feb...
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = { BottomNavigationBar(navController = navHostController) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date range selector
            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            ) {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(text = selectedRange.name)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.onSurface
                ) {
                    DateRangeType.entries.forEach { range ->
                        DropdownMenuItem(
                            onClick = {
                                selectedRange = range
                                expanded = false
                            },
                            text = { Text(range.name, color = MaterialTheme.colorScheme.surface) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Chart display
            if (chartValues.isNotEmpty()) {
                JetpackComposeBasicColumnChart(
                    chartValues = chartValues,
                    xAxisLabels = xAxisLabels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hydration data available", color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(state.hydrationChartData.filter { it.second != 0 }) { (date, ml) ->
                    val format = LocalDate.Formats.ISO
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = date.format(format),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$ml ml",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun JetpackComposeBasicColumnChart(
    chartValues: List<Int>,
    xAxisLabels: List<String>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val axisLabelTextColor = MaterialTheme.colorScheme.surface



    val bottomAxis = HorizontalAxis.rememberBottom(
        valueFormatter = CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt()
            if (index in xAxisLabels.indices) xAxisLabels[index] else ""
        },
        itemPlacer = CustomItemPlacer(xAxisLabels.size),
        label = rememberTextComponent(
            color = axisLabelTextColor,
        ),
    )


    LaunchedEffect(chartValues) {
        modelProducer.runTransaction {
            columnSeries {
                series(chartValues)
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(
                    color = axisLabelTextColor,
                ),
            ),
            bottomAxis = bottomAxis,
        ),
        modelProducer = modelProducer,
        modifier = modifier
    )
}

class CustomItemPlacer(private val labelCount: Int) : HorizontalAxis.ItemPlacer {
    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> {
        return (0 until labelCount).map { it.toDouble() }
    }


    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>,
    ): List<Double> {
        return (0 until labelCount).map { it.toDouble() }
    }

    override fun getHeightMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> {
        return (0 until labelCount).map { it.toDouble() }
    }

    override fun getStartLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
    ): Float {
        return 0f
    }

    override fun getEndLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
    ): Float {
        return 0f
    }
}


