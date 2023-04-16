package com.phinion.dailyjournal.navigation

import com.phinion.dailyjournal.util.Constants.WRITE_SCREEN_ARGUMENT_KEY

sealed class Screen(val route: String){
    object Authentication: Screen(route = "authentication_screen")
    object Home: Screen(route = "home_screen")
    //? mark means the argument will be optional and the key indicate that the argument will be optional
    object Write: Screen(route = "write_screen?$WRITE_SCREEN_ARGUMENT_KEY={$WRITE_SCREEN_ARGUMENT_KEY}"){
        fun passDairyId(dairyId: String) =
            "write_screen?$WRITE_SCREEN_ARGUMENT_KEY=$dairyId"
    }
}
