package ir.renocity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Part(
    val name: String, val category: String, val truck: String,
    val condition: String, val price: String, val stock: String,
    val code: String, val description: String
)

private val sampleParts = listOf(
    Part("فیلتر روغن موتور", "قطعات موتور", "Renault T و Premium", "نو", "۱,۲۵۰,۰۰۰ تومان", "موجود", "R-001", "فیلتر روغن؛ سازگاری دقیق با مدل و سال ساخت بررسی شود."),
    Part("لنت ترمز جلو", "سیستم ترمز", "Renault Premium", "نو", "استعلام قیمت", "استعلام موجودی", "R-002", "لنت ترمز جلو؛ انتخاب دقیق بر اساس تیپ کامیون انجام شود."),
    Part("کیت کلاچ", "قطعات گیربکس", "Renault T", "نو", "۸,۹۰۰,۰۰۰ تومان", "۲ عدد", "R-003", "کیت کلاچ؛ مشخصات فنی پیش از سفارش تأیید شود."),
    Part("چراغ جلو", "قطعات برقی", "Renault T و Premium", "استوک", "۳,۴۰۰,۰۰۰ تومان", "۱ عدد", "R-004", "چراغ جلو با وضعیت استوک."),
    Part("کمک فنر کابین", "سیستم تعلیق", "Renault Premium", "نو", "استعلام قیمت", "استعلام موجودی", "R-005", "برای اعلام قیمت، مدل و سال ساخت را ارسال کنید."),
    Part("آینه بغل", "لوازم بدنه", "Renault T", "نو", "۲,۱۰۰,۰۰۰ تومان", "موجود", "R-006", "سمت و نوع پایه هنگام سفارش مشخص شود.")
)
private val categories = listOf("همه قطعات", "قطعات موتور", "قطعات گیربکس", "سیستم ترمز", "قطعات برقی", "سیستم تعلیق", "لوازم بدنه")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RenaultCityApp() }
    }
}

@Composable
fun RenaultCityApp() {
    var page by remember { mutableStateOf("home") }
    var category by remember { mutableStateOf("همه قطعات") }
    var selectedPart by remember { mutableStateOf<Part?>(null) }
    var search by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme {
            Column(Modifier.fillMaxSize()) {
                Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                        Text("رنو سیتی", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        Text("مرجع قطعات کامیون‌های Renault T و Renault Premium", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (page) {
                        "home" -> HomePage(
                            onCategory = { category = it; page = "parts" },
                            onParts = { category = "همه قطعات"; page = "parts" },
                            onAds = { page = "ads" }, onAccount = { page = "account" }, onContact = { page = "contact" }
                        )
                        "parts" -> PartsPage(category, search, { search = it }, { category = it }, {
                            selectedPart = it; page = "detail"
                        }, { page = "home" })
                        "detail" -> selectedPart?.let { PartDetailPage(it, { page = "parts" }, { page = "contact" }) }
                        "ads" -> AdsPage({ page = "home" }, { page = "contact" })
                        "account" -> AccountPage(phone, { phone = it }, { page = "home" })
                        "contact" -> ContactPage { page = "home" }
                    }
                }
                NavigationBar {
                    NavigationBarItem(page == "home", { page = "home" }, icon = { Text("⌂") }, label = { Text("خانه") })
                    NavigationBarItem(page == "parts" || page == "detail", { category = "همه قطعات"; page = "parts" }, icon = { Text("▦") }, label = { Text("قطعات") })
                    NavigationBarItem(page == "ads", { page = "ads" }, icon = { Text("🚚") }, label = { Text("آگهی‌ها") })
                    NavigationBarItem(page == "account", { page = "account" }, icon = { Text("♙") }, label = { Text("حساب") })
                }
            }
        }
    }
}

@Composable
fun HomePage(onCategory: (String) -> Unit, onParts: () -> Unit, onAds: () -> Unit, onAccount: () -> Unit, onContact: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("به رنو سیتی خوش آمدید", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("قطعات مورد نیاز کامیون خود را پیدا کنید.")
                    Button(onClick = onParts, modifier = Modifier.fillMaxWidth()) { Text("مشاهده همه قطعات") }
                }
            }
        }
        item { Text("دسته‌بندی قطعات", fontSize = 21.sp, fontWeight = FontWeight.Bold) }
        items(categories.drop(1)) { item ->
            Card(Modifier.fillMaxWidth().clickable { onCategory(item) }, shape = RoundedCornerShape(15.dp)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    Text("‹", fontSize = 25.sp)
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("خرید و فروش کامیون", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("آگهی‌های Renault T و Renault Premium")
                    OutlinedButton(onClick = onAds, modifier = Modifier.fillMaxWidth()) { Text("مشاهده آگهی‌ها") }
                }
            }
            OutlinedButton(onClick = onContact, modifier = Modifier.fillMaxWidth()) { Text("ارتباط با رنو سیتی") }
            TextButton(onClick = onAccount, modifier = Modifier.fillMaxWidth()) { Text("ورود / حساب کاربری") }
        }
    }
}

@Composable
fun PartsPage(category: String, search: String, onSearch: (String) -> Unit, onCategory: (String) -> Unit, onPart: (Part) -> Unit, onBack: () -> Unit) {
    val filtered = sampleParts.filter {
        (category == "همه قطعات" || it.category == category) &&
        (search.isBlank() || it.name.contains(search, true) || it.code.contains(search, true) || it.category.contains(search, true))
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack) { Text("→ بازگشت به خانه") }
        Text("فهرست قطعات", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(search, onSearch, Modifier.fillMaxWidth(), label = { Text("جست‌وجوی نام یا شماره فنی") }, singleLine = true)
        Text("دسته: $category", fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            TextButton(onClick = { onCategory("همه قطعات") }) { Text("همه") }
            TextButton(onClick = { onCategory("قطعات موتور") }) { Text("موتور") }
            TextButton(onClick = { onCategory("سیستم ترمز") }) { Text("ترمز") }
            TextButton(onClick = { onCategory("قطعات گیربکس") }) { Text("گیربکس") }
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { part ->
                Card(Modifier.fillMaxWidth().clickable { onPart(part) }, shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(part.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${part.category} • ${part.truck}", fontSize = 13.sp)
                        Text("قیمت: ${part.price}")
                        Text("موجودی: ${part.stock}")
                        Text("مشاهده جزئیات ←", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            if (filtered.isEmpty()) item { Text("موردی پیدا نشد.") }
        }
    }
}

@Composable
fun PartDetailPage(part: Part, onBack: () -> Unit, onQuote: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("→ بازگشت به فهرست") }
        Text(part.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("دسته‌بندی: ${part.category}")
                Text("مدل سازگار نمونه: ${part.truck}")
                Text("وضعیت قطعه: ${part.condition}")
                Text("شماره داخلی نمونه: ${part.code}")
                Text("قیمت: ${part.price}", fontWeight = FontWeight.Bold)
                Text("موجودی: ${part.stock}")
                Spacer(Modifier.height(1.dp))
                Text(part.description)
            }
        }
        Button(onClick = onQuote, modifier = Modifier.fillMaxWidth()) { Text("درخواست استعلام / هماهنگی خرید") }
        Text("اطلاعات و قیمت‌ها نمونه و غیرواقعی هستند؛ هنوز به موجودی فروشگاه وصل نشده‌اند.", fontSize = 12.sp)
    }
}

@Composable
fun AdsPage(onBack: () -> Unit, onContact: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("→ بازگشت به خانه") }
        Text("آگهی کامیون‌ها", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("خرید و فروش Renault T", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("آگهی‌های کامیون Renault T در این بخش نمایش داده می‌شوند.")
        } }
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("خرید و فروش Renault Premium", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("آگهی‌های کامیون Renault Premium در این بخش نمایش داده می‌شوند.")
        } }
        Button(onClick = onContact, modifier = Modifier.fillMaxWidth()) { Text("برای ثبت آگهی با ما تماس بگیرید") }
        Text("ثبت و انتشار آگهی در این نسخه هنوز به سرور متصل نیست.", fontSize = 12.sp)
    }
}

@Composable
fun AccountPage(phone: String, onPhone: (String) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("→ بازگشت به خانه") }
        Text("حساب کاربری", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(phone, onPhone, Modifier.fillMaxWidth(), label = { Text("شماره موبایل") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("ادامه") }
        Text("ورود پیامکی در این نسخه فعال نیست و پس از اتصال سرویس پیامک راه‌اندازی می‌شود.", fontSize = 12.sp)
    }
}

@Composable
fun ContactPage(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("→ بازگشت به خانه") }
        Text("ارتباط با رنو سیتی", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("برای خرید قطعه، استعلام قیمت یا هماهنگی آگهی با فروشگاه تماس بگیرید.")
        Text("شماره تماس فروشگاه هنوز در تنظیمات اپ وارد نشده است.")
        Text("در نسخه بعد شماره تماس و گفت‌وگوی داخل برنامه اضافه می‌شود.")
    }
}
