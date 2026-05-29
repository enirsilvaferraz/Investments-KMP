package com.eferraz.design_system_v2.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme

/**
 * Light Material 3 Expressive color scheme (delegates to [expressiveLightColorScheme]).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
public fun lightExpressiveColorScheme(): ColorScheme = expressiveLightColorScheme()

/**
 * Dark color scheme paired with expressive light per Material3 guidance
 * ([expressiveLightColorScheme] KDoc: dark mode uses [darkColorScheme]).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
public fun darkExpressiveColorScheme(): ColorScheme = darkColorScheme()
