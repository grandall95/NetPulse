package page.gagerandall.netpulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.rememberDrawerState
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import page.gagerandall.netpulse.ui.theme.MyApplicationTheme

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w960dp-h540dp-land-television", sdk = [35])
class TelevisionNavigationScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun tv_navigation_simplified_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val lazyListState = rememberLazyListState()

        ModalNavigationDrawer(
            modifier = Modifier.fillMaxSize(),
            drawerState = drawerState,
            scrimBrush = SolidColor(Color.Transparent),
            drawerContent = { drawerValue ->
                val isClosed = drawerValue == DrawerValue.Closed
                val drawerWidth = if (isClosed) 80.dp else 240.dp
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(drawerWidth)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.Start
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isClosed) "NP" else "NetPulse TV",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        itemsIndexed(listOf("Home", "Settings")) { index, title ->
                            val isSelected = index == 0
                            NavigationDrawerItem(
                                selected = isSelected,
                                onClick = {},
                                icon = {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Default.Home else Icons.Default.Settings,
                                        contentDescription = title
                                    )
                                },
                                label = {
                                    if (!isClosed) {
                                        Text(text = title)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 80.dp, end = 16.dp)
                    .background(Color.Red),
            ) {
                Text(
                    text = "This is the content area!",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tv_navigation.png")
  }
}
