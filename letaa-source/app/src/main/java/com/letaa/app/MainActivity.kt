package com.letaa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.letaa.app.ui.LetaaApp
import com.letaa.app.ui.theme.LetaaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Безграничный браузер: контент рисуется под статус-баром/навигацией,
        // никаких шторок сверху/снизу, обрезающих страницу.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LetaaTheme {
                LetaaApp()
            }
        }
    }
}
