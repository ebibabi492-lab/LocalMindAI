package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

object AppStrings {

    fun isPersian(selectedLanguage: String): Boolean {
        return when (selectedLanguage) {
            "fa" -> true
            "en" -> false
            else -> {
                val defaultLang = Locale.getDefault().language
                defaultLang.startsWith("fa") || defaultLang.startsWith("ar")
            }
        }
    }

    @Composable
    fun getLayoutDirection(selectedLanguage: String): LayoutDirection {
        return if (isPersian(selectedLanguage)) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    fun appName(isFa: Boolean) = "LocalMind AI"

    fun welcomeMessage(isFa: Boolean) = if (isFa)
        "سلام! من دستیار هوشمند ۱۰۰٪ آفلاین شما هستم. بدون اتصال اینترنت و با مدل‌های محلی نصب‌شده گفتگو می‌کنیم."
    else
        "Hello! I am your 100% offline AI assistant running locally on your device hardware without internet."

    fun noModelMessage(isFa: Boolean) = if (isFa)
        "برای شروع، فایل مدل (.litertlm) را در مسیر پوشه کپی کرده یا از حافظه دستگاه وارد کنید."
    else
        "To get started, please copy a .litertlm model file into the models folder or import it from your device storage."

    fun offlineReadyBadge(isFa: Boolean) = if (isFa) "هوش مصنوعی آفلاین آماده است" else "Offline AI Ready"

    fun loadingModel(isFa: Boolean) = if (isFa) "در حال بارگذاری مدل در حافظه..." else "Loading model into memory..."

    fun noModelLoaded(isFa: Boolean) = if (isFa) "مدلی انتخاب نشده است" else "No model selected"

    fun inputPlaceholder(isFa: Boolean) = if (isFa) "پیام خود را به صورت آفلاین بنویسید..." else "Ask anything offline..."

    fun newChat(isFa: Boolean) = if (isFa) "گفتگوی جدید" else "New Conversation"

    fun clearChat(isFa: Boolean) = if (isFa) "پاک کردن گفتگو" else "Clear Conversation"

    fun stopGeneration(isFa: Boolean) = if (isFa) "توقف تولید" else "Stop"

    fun copied(isFa: Boolean) = if (isFa) "پاسخ کپی شد" else "Copied to clipboard"

    fun models(isFa: Boolean) = if (isFa) "مدیریت مدل‌های محلی" else "Local Models"

    fun settings(isFa: Boolean) = if (isFa) "تنظیمات" else "Settings"

    fun copyModel(isFa: Boolean) = if (isFa) "کپی یا وارد کردن فایل" else "Copy / Import File"

    fun copyPath(isFa: Boolean) = if (isFa) "کپی آدرس مسیر" else "Copy Path"

    fun pathCopied(isFa: Boolean) = if (isFa) "آدرس مسیر در حافظه کپی شد" else "Directory path copied to clipboard"

    fun modelsDirectoryTitle(isFa: Boolean) = if (isFa) "مسیر آفلاین کپی فایل‌های مدل:" else "Offline Model Files Directory:"

    fun modelsDirectoryGuide(isFa: Boolean) = if (isFa)
        "فایل‌های مدل (.litertlm) را از طریق کابل کامپیوتر یا برنامه مدیریت فایل در این مسیر کپی کنید یا دکمه وارد کردن از حافظه را بزنید."
    else
        "Copy .litertlm files into this folder via USB/file manager, or use the Import button below to copy directly from device storage."

    fun scanFolder(isFa: Boolean) = if (isFa) "بروزرسانی و اسکن پوشه" else "Scan & Refresh Folder"

    fun delete(isFa: Boolean) = if (isFa) "حذف فایل" else "Delete File"

    fun active(isFa: Boolean) = if (isFa) "مدل فعال" else "Active Model"

    fun selectAsActive(isFa: Boolean) = if (isFa) "انتخاب مدل" else "Select Model"

    fun filePresent(isFa: Boolean) = if (isFa) "فایل موجود است" else "File Present"

    fun fileMissing(isFa: Boolean) = if (isFa) "فایل موجود نیست" else "File Missing"

    fun expectedFileName(isFa: Boolean, name: String) = if (isFa) "نام فایل مورد انتظار: $name" else "Expected file: $name"

    fun importLocalModel(isFa: Boolean) = if (isFa) "انتخاب و کپی فایل از حافظه گوشی" else "Select & Copy Model from Storage"

    fun ramRequirement(isFa: Boolean, ramGb: Int) = if (isFa) "حداقل رم: $ramGb گیگابایت" else "Min RAM: ${ramGb}GB"

    fun cancel(isFa: Boolean) = if (isFa) "انصراف" else "Cancel"

    fun copyingInProgress(isFa: Boolean) = if (isFa) "در حال کپی فایل مدل در مسیر آفلاین..." else "Copying model file to offline path..."

    fun copySuccess(isFa: Boolean) = if (isFa) "فایل مدل با موفقیت کپی شد" else "Model file copied successfully"

    fun languageSetting(isFa: Boolean) = if (isFa) "زبان برنامه" else "App Language"

    fun themeSetting(isFa: Boolean) = if (isFa) "پوسته" else "Theme"

    fun temperatureSetting(isFa: Boolean) = if (isFa) "میزان خلاقیت (Temperature)" else "Temperature"

    fun maxTokensSetting(isFa: Boolean) = if (isFa) "حداکثر طول پاسخ" else "Max Response Length"

    fun backendSetting(isFa: Boolean) = if (isFa) "شتاب‌دهنده سخت‌افزاری" else "Hardware Acceleration"

    fun clearAllHistory(isFa: Boolean) = if (isFa) "پاکسازی تاریخچه گفتگوها" else "Clear Chat History"

    fun clearHistoryConfirm(isFa: Boolean) = if (isFa) "آیا از حذف تمام گفتگوها اطمینان دارید؟" else "Are you sure you want to delete all conversations?"

    fun modelInfoSection(isFa: Boolean) = if (isFa) "اطلاعات مدل و سخت‌افزار" else "Model & Hardware Info"

    fun availableRam(isFa: Boolean, ramMb: Long) = if (isFa) "رم آزاد دستگاه: ${ramMb} مگابایت" else "Available RAM: ${ramMb} MB"

    fun activeBackend(isFa: Boolean, backend: String) = if (isFa) "پردازنده فعال: $backend" else "Active Backend: $backend"

    fun systemDefault(isFa: Boolean) = if (isFa) "پیش‌فرض سیستم" else "System Default"

    fun lightTheme(isFa: Boolean) = if (isFa) "روشن" else "Light"

    fun darkTheme(isFa: Boolean) = if (isFa) "تاریک" else "Dark"

    fun backendAuto(isFa: Boolean) = if (isFa) "خودکار (GPU با امکان بازگشت به CPU)" else "Auto (GPU with CPU fallback)"

    fun backendCpu(isFa: Boolean) = if (isFa) "فقط CPU (پایدار)" else "CPU Only (Stable)"

    fun backendGpu(isFa: Boolean) = if (isFa) "ترجیح GPU" else "Prefer GPU"

    fun privacyNotice(isFa: Boolean) = if (isFa)
        "۱۰۰٪ آفلاین و بدون اینترنت: این برنامه هیچ دسترسی یا ترافیک اینترنتی ندارد و تمامی داده‌ها روی دستگاه شما باقی می‌مانند."
    else
        "100% Offline & Private: No internet access or download links. All models and prompts execute solely on your device."
}
