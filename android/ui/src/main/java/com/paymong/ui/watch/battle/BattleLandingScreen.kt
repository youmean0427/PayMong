package com.paymong.ui.watch.battle

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.size.OriginalSize
import com.paymong.common.navigation.WatchNavItem
import com.paymong.common.R
import com.paymong.domain.watch.battle.BattleViewModel
import com.paymong.ui.theme.PayMongRed200
import com.paymong.ui.theme.PaymongTheme
import com.paymong.ui.theme.dalmoori
import com.paymong.ui.watch.activity.LoadingGif

@Composable
fun BattleLanding(
    navController: NavHostController,
    battleViewModel: BattleViewModel
) {
    val bg = painterResource(R.drawable.main_bg)
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    var fontSize = 0
    var battleImgSize = 0

    if (screenWidthDp < 200) {
        fontSize = 12
        battleImgSize = 150

    }
    else {
        fontSize = 15
        battleImgSize = 180
    }


    Image(painter = bg, contentDescription = null, contentScale = ContentScale.Crop)
    Column(
        verticalArrangement = Arrangement.Center,

        modifier = Modifier
            .fillMaxHeight()
            .clickable {
                navController.navigate(WatchNavItem.BattleWait.route) {
                    popUpTo(0)
                    launchSingleTop =true
                }
                battleViewModel.battleWait()
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom=10.dp)
        ) {
            Text(text = "PAYMONG",
                textAlign = TextAlign.Center,
                fontFamily = dalmoori,
                color = PayMongRed200,
                modifier = Modifier.fillMaxWidth(),
                fontSize = fontSize.sp,

            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
//            Text(text = "BATTLE", textAlign = TextAlign.Center)
            val battleTitle = painterResource(R.drawable.battle_title)
            Image(painter = battleTitle, contentDescription = null,  modifier = Modifier.width(battleImgSize.dp))
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(top=10.dp)
        ) {
            Text(
                text = "터치해서 배틀하기",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = dalmoori,
                color = PayMongRed200,
                fontSize = fontSize.sp,
            )
        }
    }

}

@Preview(device = Devices.WEAR_OS_LARGE_ROUND, showSystemUi = true)
@Composable
fun BattlePreview() {
    val navController = rememberSwipeDismissableNavController()
    val battleViewModel: BattleViewModel = viewModel()

    PaymongTheme {
        BattleLanding(navController, battleViewModel)
    }
}

@ExperimentalCoilApi
@Composable
fun LoadingGif(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .componentRegistry {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder(context))
            } else {
                add(GifDecoder())
            }
        }
        .build()
    Image(
        painter = rememberImagePainter(
            imageLoader = imageLoader,
            data = R.drawable.loading,
            builder = {
                size(OriginalSize)
            }
        ),
        contentDescription = null,
        modifier = Modifier
//            .padding(top = 10.dp)
    )
}