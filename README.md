# Roaming Assistant 🚀
**Turkcell Code Night – Roaming Asistanı**  
Seyahat planına göre **en ekonomik roaming çözümünü** (paket / PAYG / kombinasyon) öneren, maliyet simülasyonu yapan ve **sürpriz faturayı önlemeyi** amaçlayan **Java / Spring Boot** uygulaması.

---

## İçindekiler

- [Öne Çıkanlar](#öne-çıkanlar)
- [Mimari ve Dizayn](#mimari-ve-dizayn)
- [Domain Modeli](#domain-modeli)
- [Kurulum](#kurulum)
- [Veri Tohumlama (Seeding)](#veri-tohumlama-seeding)
- [FX (Kur) Yapılandırması](#fx-kur-yapılandırması)
- [API Dokümantasyonu (Swagger/OpenAPI)](#api-dokümantasyonu-swaggeropenapi)
- [Endpointler](#endpointler)
  - [POST /api/simulate](#post-apisimulate)
  - [POST /api/recommendation](#post-apirecommendation)
  - [POST /api/checkout](#post-apicheckout)
- [Hesaplama Mantığı](#hesaplama-mantığı)
- [Örnek Akış](#örnek-akış)
- [Proje Yapısı](#proje-yapısı)
- [Geliştirme Notları](#geliştirme-notları)
- [Gelecek Geliştirmeler](#gelecek-geliştirmeler)
- [Lisans](#lisans)

---

## Öne Çıkanlar

- ✅ **Java 21 + Spring Boot 3.5**
- ✅ **PostgreSQL + JPA/Hibernate**
- ✅ **OpenAPI/Swagger UI** ile uçtan uca test
- ✅ **Idempotent CSV seeding** (tekrar veri yazmaz)
- ✅ **Jakarta Validation** ile istek doğrulama
- ✅ **TRY bazlı kur dönüşümü** (mock FX, properties ile)
- ✅ **Katmanlı mimari** (Controller → Service → Manager → Repository)

---

## Mimari ve Dizayn

- **Layered Architecture**
  - `controller` → REST uçları
  - `business.abstracts` → servis arayüzleri
  - `business.concretes` → iş mantığı (Managers)
  - `repository` → Spring Data JPA
  - `model.entity` → JPA varlıkları
  - `dto.requests / dto.responses` → dış dünya sözleşmesi
- **DTO’larda snake_case JSON**
  - `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)`
- **Doğrulama**
  - `jakarta.validation` (`@NotNull`, `@Positive`, `@NotBlank` vb.)
- **Swagger/OpenAPI**
  - `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0`

---

## Domain Modeli

- **Country**: `countryCode`, `countryName`, `region`  
- **RoamingRate**: ülke bazında PAYG ücretleri (data/voice/sms, currency)  
- **RoamingPack**: paket tanımı (kapsama, kota, fiyat, `validityDays`, currency)  
- **AppUser**: örnek kullanıcılar  
- **UsageProfile**: kullanıcı günlük ortalama `data/min/sms`  
- **Trip**: seyahat (`country`, `startDate`, `endDate`)  

---

## Kurulum

### Gereksinimler

- Java 21
- Maven 3.9+
- PostgreSQL 14+

### Veritabanı

`application.properties`:

```properties
server.port=8000

spring.datasource.url=jdbc:postgresql://localhost:5432/roaming
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true
Uygulamayı Çalıştırma

mvn clean spring-boot:run
# veya
mvn clean package
java -jar target/roaming-assistant-0.0.1-SNAPSHOT.jar
Veri Tohumlama (Seeding)
src/main/resources/data/*.csv dosyaları ilk açılışta yüklenir.

DataLoader, veritabanı boşsa çalışır; tekrar başlatmalarda idempotent davranır:

Ülke+kur kombinasyonları ve kimlikli kayıtlar tekrar eklenmez.

Yeniden tohumlamak için DB’yi temizleyip uygulamayı yeniden başlatın.

FX (Kur) Yapılandırması

app.fx.base=TRY
app.fx.rates.EUR=35.0
app.fx.rates.USD=33.0
Maliyetler kendi para biriminde hesaplanır, ayrıca TRY karşılığı total_cost_try olarak döner.

API Dokümantasyonu (Swagger/OpenAPI)
Swagger UI: http://localhost:8000/swagger-ui/index.html

OpenAPI JSON: http://localhost:8000/v3/api-docs

Endpointler
POST /api/simulate
Body (snake_case):

{
  "user_id": 1001,
  "trips": [
    { "country_code": "DE", "start_date": "2025-08-25", "end_date": "2025-08-29" }
  ],
  "profile": { "avg_daily_mb": 500, "avg_daily_min": 10, "avg_daily_sms": 2 }
}
200 Response (örnek):

{
  "summary": {
    "total_days": 5,
    "total_need_gb": 2.44,
    "total_need_min": 50,
    "total_need_sms": 10
  },
  "options": [
    {
      "kind": "payg",
      "pack_id": null,
      "pack_name": null,
      "n_packs": 0,
      "coverage_hit": false,
      "validity_ok": true,
      "overflow_breakdown": {
        "over_gb": 0,
        "over_min": 0,
        "over_sms": 0,
        "over_cost_data": 0,
        "over_cost_voice": 0,
        "over_cost_sms": 0
      },
      "total_cost": 274.5,
      "currency": "EUR",
      "total_cost_try": 9607.5
    },
    {
      "kind": "pack",
      "pack_id": 201,
      "pack_name": "Avrupa 5GB",
      "n_packs": 1,
      "coverage_hit": true,
      "validity_ok": true,
      "overflow_breakdown": {
        "over_gb": 0,
        "over_min": 0,
        "over_sms": 0,
        "over_cost_data": 0,
        "over_cost_voice": 0,
        "over_cost_sms": 0
      },
      "total_cost": 19.9,
      "currency": "EUR",
      "total_cost_try": 696.5
    }
  ]
}
POST /api/recommendation
Body, /api/simulate ile aynıdır.

200 Response (örnek):

{
  "top3": [
    {
      "label": "Avrupa 10GB (best value)",
      "total_cost": 29.9,
      "currency": "EUR",
      "total_cost_try": 1046.5,
      "explanation": "Kapsama uygun, 5 günlük geçerlilikte overflow yok.",
      "details": { "pack_id": 202, "n_packs": 1 }
    },
    {
      "label": "Avrupa 5GB",
      "total_cost": 19.9,
      "currency": "EUR",
      "total_cost_try": 696.5,
      "explanation": "Kapsama uygun, overflow yok.",
      "details": { "pack_id": 201, "n_packs": 1 }
    },
    {
      "label": "PAYG (fallback)",
      "total_cost": 274.5,
      "currency": "EUR",
      "total_cost_try": 9607.5,
      "explanation": "Paket kullanılmadığında en pahalı seçenek.",
      "details": { "kind": "payg" }
    }
  ],
  "rationale": "Toplam maliyete göre (TRY) sıralandı; overflow ve geçerlilik kontrol edildi."
}
POST /api/checkout
Mock uç; sipariş ID üretir.

Body (pack seçimi):

{
  "user_id": 1001,
  "selection": {
    "kind": "pack",
    "pack_id": 202,
    "n_packs": 1
  }
}
Body (PAYG seçimi):

{
  "user_id": 1001,
  "selection": { "kind": "payg" }
}
201 Response:

{ "status": "ok", "order_id": "MOCK-5d18a7f7-6b53-4a77-9a0a-6db4fef2d1b2" }
selection.kind = "pack" | "payg".
kind=pack ise pack_id ve n_packs zorunludur.

Hesaplama Mantığı
Gün (days) = seyahat başlangıç/bitiş dahil toplam gün

İhtiyaç (summary) = days × profile (MB → GB dönüşümü yapılır)

PAYG = RoamingRate tablosundan data/min/sms fiyatları × ihtiyaç

Paket:

coverage_hit: destinasyon ülke paketin kapsamasında mı?

validity_ok: validityDays ≥ days

n_packs: ihtiyacı karşılayacak minimum paket sayısı (GB/min/sms bazlı)

overflow: paket sonrası kalan ihtiyaç (PAYG ile fiyatlanır)

FX dönüşümü = total_cost_try = total_cost × app.fx.rates[<CURRENCY>]
(base: TRY, X: EUR, USD vs.)

Örnek Akış
/api/simulate → maliyet opsiyonları hesaplanır

/api/recommendation → en iyi 3 seçenek sıralanır

/api/checkout → seçilen seçenek mock order_id ile satın alınır

Proje Yapısı

src/main/java/com/turkcell/roaming/roaming_assistant
├── business
│   ├── abstracts            # Service arayüzleri
│   └── concretes            # Manager implementasyonları
├── config                   # DataLoader, OpenAPI config
├── controller               # REST Controller'lar
├── dto
│   ├── requests             # Simulate / Recommendation / Checkout / TripInput
│   └── responses            # OptionDto, OverflowBreakdown, ...
├── model
│   └── entity               # JPA Entity'ler
└── repository               # Spring Data JPA Repositories
Geliştirme Notları
Swagger “Example Value” alanındaki değerler şema örneğidir (ör: "string", 1073741824).
Gerçek cevabı her zaman “Server response” bölümünde görebilirsiniz.

Seeding yeniden başlatmalarda tekrara düşmez (countryRepo.count()>0 kontrolü).

Validation ile hatalı istekler 400 döndürür.

Gelecek Geliştirmeler
Gerçek zamanlı kur servisleri (ECB, fixer.io vb.)

Kapsama ve geçerlilik için harici servis entegrasyonu

JWT / Spring Security ile kimlik doğrulama

Unit & Integration Testleri, Testcontainers

Caching (kur / tarife / paket verileri)

Docker Compose ile PostgreSQL + App

CI/CD (GitHub Actions)

Lisans
Bu proje bir case study çalışmasıdır; eğitim/deneme amaçlıdır.