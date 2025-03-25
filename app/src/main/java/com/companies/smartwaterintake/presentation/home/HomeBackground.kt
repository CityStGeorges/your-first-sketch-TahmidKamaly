package com.companies.smartwaterintake.presentation.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.companies.smartwaterintake.R
import com.companies.smartwaterintake.data.Percent

@Composable
internal fun HomeBackground(
    modifier: Modifier = Modifier,
    isSystemInDarkTheme: Boolean,
    hydrationProgress: Percent
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            if (isSystemInDarkTheme) R.raw.water_dark else R.raw.water_light
        )
    )
    val clipSpec by remember(hydrationProgress) {
        // 196 == last frame we want to show
        val maximumFrame = (minOf(1.0f, hydrationProgress.value) * 210).toInt()
        mutableStateOf(
            LottieClipSpec.Frame(
                min = 0,
                // if max frame = 0 we glitch, so we set it to 1
                max = maxOf(maximumFrame, 1)
            )
        )
    }
    LottieAnimation(
        modifier = modifier.fillMaxSize(),
        composition = composition,
        clipSpec = clipSpec,
        contentScale = ContentScale.FillBounds
    )
}