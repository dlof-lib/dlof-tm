# dlof-TM

**صانع قوالب حزم DLoF** — تطبيق أندرويد مستقل (Kotlin + Jetpack Compose) لإنشاء
ملفات `.dlofpkg` احترافية، متوافق مع مواصفة *DLoF Package Formats v2.0*.

مشروع مستقل بالكامل — مستودع منفصل عن `dlof-reader` و`dlof-go`، بدون أي اعتماد
عليهما، يبني ملف APK قائم بذاته.

## المزايا

- إنشاء مشروع قالب: معرّف الحزمة، العنوان، النوع (series/book/comic/document/qa)،
  المؤلف، اللغة، الإصدار.
- تصميم كامل: الألوان الأساسية/الثانوية/الخلفية/النص، نوع الخط، والتخطيط
  (standard/card/magazine/minimal) — يُكتب في `setting/dlotemplate.xml`.
- إدارة الوسائط: فصول صور (`media/image/<chapter>/`)، حلقات فيديو مع ترجمة
  اختيارية (`media/video/Episodes/<episode>/`)، وملفات خطوط
  (`media/fonts/fonts/`).
- إعدادات الحزمة: وضع base64 (`optional`/`always`/`never`)، تفعيل التشفير
  AES-256-GCM بملف تعريف Best64 (`setting/pro/Best64.xml` + `WQ.JSON`).
- تصدير ملف `.dlofpkg` (ZIP) بالهيكلية الكاملة مع `set.txt`، `package.dlof`،
  `meta.json`، ومجلد `Lighthouse/` (`ep.kt`/`df.kt`/`cr.kt`) — ثم مشاركته مباشرة.
- حفظ القوالب محلياً (JSON) لإعادة فتحها وتعديلها لاحقاً.
- واجهة عربية RTL بالكامل + دعم اللغة الإنجليزية.
- أيقونة خضراء مخصّصة بكتابة "dlof-TM".

## هيكل الحزمة الناتجة (.dlofpkg)

```
mypackage.dlofpkg
├── set.txt
├── package.dlof
├── meta.json
├── setting/
│   ├── dlotemplate.xml
│   ├── map.dlof
│   ├── Documentation.dlof
│   ├── license.dlof
│   └── pro/                 (عند تفعيل التشفير)
│       ├── Best64.xml
│       └── WQ.JSON
├── media/
│   ├── image/<chapterName>/*.jpg|png|...
│   ├── video/Episodes/<episodeName>/{video, subtitle}
│   └── fonts/
│       ├── dlof/font.dlof
│       └── fonts/*.ttf|*.otf
└── Lighthouse/
    ├── ep.kt
    ├── df.kt
    └── cr.kt
```

## البنية التقنية

- Kotlin + Jetpack Compose (Material 3), Navigation Compose.
- بدون قاعدة بيانات — تخزين محلي كملفات JSON تحت `filesDir/templates/`.
- بناء الحزمة عبر `java.util.zip` مباشرة (بدون مكتبات خارجية) في
  `builder/DlofPkgBuilder.kt`.
- الحد الأدنى لإصدار أندرويد: API 24 (Android 7.0)، الهدف: API 35.

## البناء محلياً

```bash
./gradlew :app:assembleDebug
# أو أي gradle مثبت محلياً:
gradle :app:assembleDebug
```

> ملاحظة: ملف `gradle/wrapper/gradle-wrapper.jar` الثنائي غير مُضمَّن في هذا
> المستودع. نفّذ `gradle wrapper` مرة واحدة محلياً لتوليده، أو استخدم أمر
> `gradle` المثبت لديك مباشرة كما هو موضح أعلاه.

## البناء عبر GitHub Actions

الـ workflow في `.github/workflows/build.yml` يبني APK تلقائياً عند كل push
إلى `main` (نسخة debug دائماً، ونسخة release موقّعة إذا أضفت الأسرار التالية
في إعدادات المستودع):

| Secret | الوصف |
|---|---|
| `DLOFTM_KEYSTORE_BASE64` | ملف الـ keystore مُرمّز base64 |
| `DLOFTM_KEYSTORE_PASSWORD` | كلمة سر الـ keystore |
| `DLOFTM_KEY_ALIAS` | اسم مفتاح التوقيع |
| `DLOFTM_KEY_PASSWORD` | كلمة سر المفتاح |

## الأيقونة

خلفية خضراء (`#1E8E3E`) مع كلمة **dlof-TM** بيضاء، بصيغة Adaptive Icon
(`mipmap-anydpi-v26/ic_launcher.xml`) + نسخ PNG تقليدية لكل الكثافات.

## الرخصة

MIT — انظر `LICENSE`.
