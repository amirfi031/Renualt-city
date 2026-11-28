package ir.renocity.app

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.util.UUID

data class Part(
    val name: String,
    val category: String,
    val truck: String,
    val condition: String,
    val price: String,
    val stock: String,
    val code: String,
    val description: String,
    val photoPath: String = ""
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

private fun enc(value: String): String =
    Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)

private fun dec(value: String): String =
    try { String(Base64.decode(value, Base64.NO_WRAP or Base64.URL_SAFE), Charsets.UTF_8) } catch (_: Exception) { "" }

private fun loadSavedParts(activity: ComponentActivity): List<Part> {
    val raw = activity.getSharedPreferences("renocity_local", 0).getString("parts", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.lines().mapNotNull { line ->
        val f = line.split("|")
        if (f.size != 9) null else Part(
            dec(f[0]), dec(f[1]), dec(f[2]), dec(f[3]), dec(f[4]),
            dec(f[5]), dec(f[6]), dec(f[7]), dec(f[8])
        )
    }
}

private fun saveParts(activity: ComponentActivity, parts: List<Part>) {
    val raw = parts.joinToString("\n") { p ->
        listOf(p.name, p.category, p.truck, p.condition, p.price, p.stock, p.code, p.description, p.photoPath)
            .joinToString("|") { enc(it) }
    }
    activity.getSharedPreferences("renocity_local", 0).edit().putString("parts", raw).apply()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RenaultCityApp(this) }
    }
}

@Composable
fun RenaultCityApp(activity: ComponentActivity) {
    var page by remember { mutableStateOf("home") }
    var category by remember { mutableStateOf("همه قطعات") }
    var selectedPart by remember { mutableStateOf<Part?>(null) }
    var search by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var userParts by remember { mutableStateOf(loadSavedParts(activity)) }
    val allParts = sampleParts + userParts

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
                        "parts" -> PartsPage(category, search, allParts, { search = it }, { category = it }, {
                            selectedPart = it; page = "detail"
                        }, { page = "home" }, { page = "addPart" })
                        "addPart" -> AddPartPage(
                            onBack = { page = "parts" },
                            onSave = { newPart ->
                                userParts = userParts + newPart
                                saveParts(activity, userParts)
                                category = "همه قطعات"
                                search = ""
                                page = "parts"
                            }
                        )
                        "detail" -> selectedPart?.let { PartDetailPage(it, { page = "parts" }, { page = "contact" }) }
                        "ads" -> AdsPage({ page = "home" }, { page = "contact" })
                        "account" -> AccountPage(phone, { phone = it }, { page = "home" })
                        "contact" -> ContactPage { page = "home" }
                    }
                }
                NavigationBar {
                    NavigationBarItem(page == "home", { page = "home" }, icon = { Text("⌂") }, label = { Text("خانه") })
                    NavigationBarItem(page == "parts" || page == "detail" || page == "addPart", { category = "همه قطعات"; page = "parts" }, icon = { Text("▦") }, label = { Text("قطعات") })
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
fun PartsPage(
    category: String, search: String, allParts: List<Part>,
    onSearch: (String) -> Unit, onCategory: (String) -> Unit,
    onPart: (Part) -> Unit, onBack: () -> Unit, onAdd: () -> Unit
) {
    val filtered = allParts.filter {
        (category == "همه قطعات" || it.category == category) &&
        (search.isBlank() || it.name.contains(search, true) || it.code.contains(search, true) || it.category.contains(search, true))
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack) { Text("→ بازگشت به خانه") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("فهرست قطعات", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Button(onClick = onAdd) { Text("＋ ثبت قطعه") }
        }
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
fun AddPartPage(onBack: () -> Unit, onSave: (Part) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("قطعات موتور") }
    var truck by remember { mutableStateOf("Renault T و Premium") }
    var condition by remember { mutableStateOf("نو") }
    var priceMode by remember { mutableStateOf("قیمت مشخص") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                val file = File(context.filesDir, "part_${UUID.randomUUID()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                photoPath = file.absolutePath
                error = ""
            } catch (_: Exception) {
                error = "ذخیره عکس انجام نشد؛ دوباره انتخاب کن."
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack) { Text("→ بازگشت به فهرست") }
        Text("ثبت قطعه جدید", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("اطلاعات قطعه را وارد کن. برای ثبت، انتخاب عکس واقعی الزامی است.", fontSize = 13.sp)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("نام قطعه *") }, singleLine = true)
                OutlinedTextField(code, { code = it }, Modifier.fillMaxWidth(), label = { Text("شماره فنی / OEM (اختیاری)") }, singleLine = true)
                Text("دسته‌بندی", fontWeight = FontWeight.Bold)
                CategoryDropdown(category, { category = it }, categories.drop(1))
                Text("مدل کامیون سازگار", fontWeight = FontWeight.Bold)
                CategoryDropdown(truck, { truck = it }, listOf("Renault T", "Renault Premium", "Renault T و Premium", "نیازمند بررسی فنی"))
                Text("وضعیت قطعه", fontWeight = FontWeight.Bold)
                CategoryDropdown(condition, { condition = it }, listOf("نو", "استوک", "دست‌دوم"))
                Text("نوع قیمت", fontWeight = FontWeight.Bold)
                CategoryDropdown(priceMode, { priceMode = it }, listOf("قیمت مشخص", "استعلام قیمت"))
                if (priceMode == "قیمت مشخص") {
                    OutlinedTextField(price, { price = it.filter { c -> c.isDigit() } }, Modifier.fillMaxWidth(), label = { Text("قیمت به تومان *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                } else {
                    Text("قیمت برای این قطعه ثبت نمی‌شود و مشتری باید استعلام بگیرد.")
                }
                OutlinedTextField(stock, { stock = it.filter { c -> c.isDigit() } }, Modifier.fillMaxWidth(), label = { Text("موجودی فیزیکی (عدد)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("توضیحات فنی و سازگاری") }, minLines = 3)
                OutlinedButton(onClick = { photoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (photoPath.isBlank()) "انتخاب عکس واقعی قطعه *" else "تغییر عکس انتخاب‌شده")
                }
                if (photoPath.isNotBlank()) {
                    val bitmap = remember(photoPath) { BitmapFactory.decodeFile(photoPath) }
                    if (bitmap != null) {
                        Image(bitmap.asImageBitmap(), contentDescription = "عکس قطعه", modifier = Modifier.fillMaxWidth().heightIn(max = 190.dp))
                    }
                    Text("عکس با موفقیت در حافظه برنامه ذخیره شد.", color = MaterialTheme.colorScheme.primary)
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                Button(
                    onClick = {
                        when {
                            name.isBlank() -> error = "نام قطعه را وارد کن."
                            priceMode == "قیمت مشخص" && price.isBlank() -> error = "قیمت را وارد کن یا حالت استعلام را انتخاب کن."
                            photoPath.isBlank() -> error = "برای ثبت قطعه، عکس واقعی انتخاب کن."
                            truck == "نیازمند بررسی فنی" -> error = "این قطعه هنوز سازگاری تأییدشده ندارد؛ فعلاً ثبت نهایی ممکن نیست."
                            else -> {
                                val internalCode = if (code.isBlank()) "RC-${System.currentTimeMillis()}" else code.trim()
                                val priceText = if (priceMode == "استعلام قیمت") "استعلام قیمت" else "${price.toLongOrNull()?.let { "%,d".format(it) } ?: price} تومان"
                                val stockText = stock.ifBlank { "0" } + " عدد"
                                onSave(Part(name.trim(), category, truck, condition, priceText, stockText, internalCode, description.trim(), photoPath))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("ذخیره قطعه در این گوشی") }
            }
        }
    }
}

@Composable
fun CategoryDropdown(value: String, onValue: (String) -> Unit, options: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(value, modifier = Modifier.weight(1f))
            Text("⌄")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onValue(option); expanded = false })
            }
        }
    }
}

@Composable
fun PartDetailPage(part: Part, onBack: () -> Unit, onQuote: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("→ بازگشت به فهرست") }
        Text(part.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (part.photoPath.isNotBlank()) {
            val bitmap = remember(part.photoPath) { BitmapFactory.decodeFile(part.photoPath) }
            if (bitmap != null) Image(bitmap.asImageBitmap(), contentDescription = "عکس قطعه", modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp))
        }
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("دسته‌بندی: ${part.category}")
                Text("مدل سازگار: ${part.truck}")
                Text("وضعیت قطعه: ${part.condition}")
                Text("شماره فنی / شناسه: ${part.code}")
                Text("قیمت: ${part.price}", fontWeight = FontWeight.Bold)
                Text("موجودی: ${part.stock}")
                Text(part.description)
            }
        }
        Button(onClick = onQuote, modifier = Modifier.fillMaxWidth()) { Text("درخواست استعلام / هماهنگی خرید") }
        Text("قطعاتی که در این نسخه اضافه می‌کنی فقط روی همین گوشی ذخیره می‌شوند؛ هنوز در فروشگاه آنلاین منتشر نمی‌شوند.", fontSize = 12.sp)
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
