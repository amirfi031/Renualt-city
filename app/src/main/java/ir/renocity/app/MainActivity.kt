
package ir.renocity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLayoutDirection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RenocityApp()
        }
    }
}

@Composable
fun RenocityApp() {
    MaterialTheme {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "رنو سیتی",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "مرجع قطعات کامیون‌های Renault T و Renault Premium",
                    fontSize = 16.sp
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "به رنو سیتی خوش آمدید",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "قطعات مورد نیاز کامیون خود را پیدا کنید."
                        )
                    }
                }

                Text(
                    text = "دسته‌بندی قطعات",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                val categories = listOf(
                    "قطعات موتور",
                    "قطعات گیربکس",
                    "سیستم ترمز",
                    "قطعات برقی",
                    "سیستم تعلیق",
                    "لوازم بدنه"
                )

                categories.forEach { category ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(18.dp),
                            fontSize = 17.sp
                        )
                    }
                }

                Text(
                    text = "نسخه آزمایشی رنو سیتی",
                    fontSize = 13.sp
                )
            }
        }
    }
}
