package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
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
        "سلام! من دستیار هوشمند آفلاین شما هستم. بدون نیاز به اینترنت میتوانیم گفتگو کنیم."
    else
        "Hello! I am your offline AI assistant. We can chat completely without internet."

    fun noModelMessage(isFa: Boolean) = if (isFa)
        "برای شروع، ابتدا یک مدل هوش مصنوعی دانلود کنید."
    else
        "To get started, please download or select an AI model first."

    fun offlineReadyBadge(isFa: Boolean) = if (isFa) "هوش مصنوعی آفلاین آماده است" else "Offline AI Ready"

    fun loadingModel(isFa: Boolean) = if (isFa) "در حال بارگذاری مدل..." else "Loading model..."

    fun noModelLoaded(isFa: Boolean) = if (isFa) "مدلی انتخاب نشده است" else "No model selected"

    fun inputPlaceholder(isFa: Boolean) = if (isFa) "پیام خود را بنویسید..." else "Ask anything offline..."

    fun newChat(isFa: Boolean) = if (isFa) "گفتگوی جدید" else "New Conversation"

    fun clearChat(isFa: Boolean) = if (isFa) "پاک کردن گفتگو" else "Clear Conversation"

    fun stopGeneration(isFa: Boolean) = if (isFa) "توقف تولید" else "Stop"

    fun copied(isFa: Boolean) = if (isFa) "پاسخ کپی شد" else "Copied to clipboard"

    fun models(isFa: Boolean) = if (isFa) "مدیریت مدل‌ها" else "Model Manager"

    fun settings(isFa: Boolean) = if (isFa) "تنظیمات" else "Settings"

    fun download(isFa: Boolean) = if (isFa) "دانلود" else "Download"

    fun pause(isFa: Boolean) = if (isFa) "مکث" else "Pause"

    fun resume(isFa: Boolean) = if (isFa) "ادامه دانلود" else "Resume"

    fun delete(isFa: Boolean) = if (isFa) "حذف" else "Delete"

    fun active(isFa: Boolean) = if (isFa) "مدل فعال" else "Active Model"

    fun selectAsActive(isFa: Boolean) = if (isFa) "انتخاب مدل" else "Select Model"

    fun importLocalModel(isFa: Boolean) = if (isFa) "وارد کردن مدل از حافظه (.litertlm)" else "Import local model (.litertlm)"

    fun ramRequirement(isFa: Boolean, ramGb: Int) = if (isFa) "حداقل رم: $ramGb گیگابایت" else "Min RAM: ${ramGb}GB"

    fun languages(isFa: Boolean) = if (isFa) "زبان‌ها" else "Languages"

    fun downloadWarningTitle(isFa: Boolean) = if (isFa) "دانلود مدل هوش مصنوعی" else "Download AI Model"

    fun downloadWarningMessage(isFa: Boolean, size: String) = if (isFa)
        "حجم این فایل حدود $size است. این مدل یک‌بار دانلود شده و سپس برای همیشه به صورت کاملاً آفلاین کار خواهد کرد. مایل به دانلود هستید؟"
    else
        "This file is approximately $size. Once downloaded, it runs 100% offline without any internet connection. Proceed with download?"

    fun cancel(isFa: Boolean) = if (isFa) "انصراف" else "Cancel"

    fun proceed(isFa: Boolean) = if (isFa) "دانلود و ذخیره" else "Download Now"

    fun languageSetting(isFa: Boolean) = if (isFa) "زبان برنامه" else "App Language"

    fun themeSetting(isFa: Boolean) = if (isFa) "پوسته" else "Theme"

    fun temperatureSetting(isFa: Boolean) = if (isFa) "میزان خلاقیت (Temperature)" else "Temperature"

    fun maxTokensSetting(isFa: Boolean) = if (isFa) "حداکثر طول پاسخ" else "Max Response Length"

    fun backendSetting(isFa: Boolean) = if (isFa) "شتاب‌دهنده سخت‌افزاری" else "Hardware Acceleration"

    fun clearAllHistory(isFa: Boolean) = if (isFa) "پاکسازی تاریخچه گفتگوها" else "Clear Chat History"

    fun clearHistoryConfirm(isFa: Boolean) = if (isFa) "آیا از حذف تمام گفتگوها اطمینان دارید؟" else "Are you sure you want to delete all conversations?"

    fun modelInfoSection(isFa: Boolean) = if (isFa) "اطلاعات مدل و دستگاه" else "Model & Device Info"

    fun availableRam(isFa: Boolean, ramMb: Long) = if (isFa) "رم آزاد دستگاه: ${ramMb} مگابایت" else "Available RAM: ${ramMb} MB"

    fun activeBackend(isFa: Boolean, backend: String) = if (isFa) "پردازنده فعال: $backend" else "Active Backend: $backend"

    fun systemDefault(isFa: Boolean) = if (isFa) "پیش‌فرض سیستم" else "System Default"

    fun lightTheme(isFa: Boolean) = if (isFa) "روشن" else "Light"

    fun darkTheme(isFa: Boolean) = if (isFa) "تاریک" else "Dark"

    fun backendAuto(isFa: Boolean) = if (isFa) "خودکار (GPU با امکان بازگشت به CPU)" else "Auto (GPU with CPU fallback)"

    fun backendCpu(isFa: Boolean) = if (isFa) "فقط CPU (پایدار)" else "CPU Only (Stable)"

    fun backendGpu(isFa: Boolean) = if (isFa) "ترجیح GPU" else "Prefer GPU"

    fun privacyNotice(isFa: Boolean) = if (isFa)
        "حریم خصوصی کامل: تمام پردازش‌های هوش مصنوعی منحصراً درون این دستگاه انجام می‌شود و هیچ پیامی ارسال نمی‌شود."
    else
        "Complete Privacy: All AI inference runs strictly on-device. No data leaves your phone."
}
