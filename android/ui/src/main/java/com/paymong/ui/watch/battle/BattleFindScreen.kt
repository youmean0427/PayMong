package com.paymong.ui.watch.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import coil.annotation.ExperimentalCoilApi
import com.paymong.common.navigation.WatchNavItem
import com.paymong.common.R
import com.paymong.common.code.BackgroundCode
import com.paymong.common.code.MatchingCode
import com.paymong.common.code.SoundCode
import com.paymong.domain.watch.BattleViewModel
import com.paymong.domain.SoundViewModel
import com.paymong.ui.watch.common.Background
import com.paymong.ui.watch.common.BattleBackgroundGif

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BattleFind(
    navController: NavHostController,
    soundViewModel: SoundViewModel,
    battleViewModel: BattleViewModel
) {
    LaunchedEffect(true) {
        soundViewModel.soundPlay(SoundCode.BATTLE_FIND_SOUND)
    }
    Background(BackgroundCode.BG000)
    BattleBackgroundGif()

    val playerResourceCodeA = painterResource(battleViewModel.playerCodeA.resourceCode)
    val playerResourceCodeB = painterResource(battleViewModel.playerCodeB.resourceCode)

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val characterSize = if (screenWidthDp < 200) 80 else 100

    when(battleViewModel.matchingState) {
        MatchingCode.ACTIVE -> {
            navController.navigate(WatchNavItem.BattleActive.route) {
                popUpTo(0)
                launchSingleTop =true
            }
            battleViewModel.battleActive()
        }
        else -> {}
    }

    Button(
        onClick = { navController.navigate(WatchNavItem.BattleActive.route) },
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)
    ){}

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (battleViewModel.battleActive.order == "B"){
                Image(painter = playerResourceCodeB, contentDescription = null, modifier = Modifier
                    .width(characterSize.dp)
                    .height(characterSize.dp)
                )
            } else {
                Image(painter = playerResourceCodeA, contentDescription = null, modifier = Modifier
                    .width(characterSize.dp)
                    .height(characterSize.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val battle = painterResource(R.drawable.battle)
            Image(painter = battle, contentDescription = null)
        }
        Row(
            horizontalArrangement = Arrangement.Start,
        ) {
            if (battleViewModel.battleActive.order == "B") {
                Image(
                    painter = playerResourceCodeA, contentDescription = null, modifier = Modifier
                        .width(characterSize.dp)
                        .height(characterSize.dp)
                )
            } else {
                // playerResourceCodeB :: 위쪽
                Image(
                    painter = playerResourceCodeB, contentDescription = null, modifier = Modifier
                        .width(characterSize.dp)
                        .height(characterSize.dp)
                )
            }
        }
    }
}