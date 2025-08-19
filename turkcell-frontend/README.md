# Turkcell Roaming Asistanı

Kullanıcının seyahat planına göre en ekonomik roaming çözümünü öneren, maliyet simülasyonu yapan ve uyarılar üreten web uygulaması.

## 🚀 Özellikler

### MVP (Zorunlu) Özellikler
- ✅ **Kullanıcı & Seyahat Girişi**: Kullanıcı seçimi ve çoklu ülke seyahat planlaması
- ✅ **Katalog & Tarifeler**: Roaming paketleri ve pay-as-you-go ücretleri
- ✅ **Maliyet Simülasyonu**: Tüm seçenekler için detaylı maliyet hesaplama
- ✅ **Öneri & Gerekçe**: En uygun 3 seçeneğin karşılaştırmalı analizi
- ✅ **Uyarılar**: Validity, kapsama ve aşım uyarıları
- ✅ **İşlem Akışı**: Mock paket seçimi ve onay simülasyonu

### Bonus Özellikler
- ✅ **Modern Turkcell Tasarımı**: [Turkcell.com.tr](https://www.turkcell.com.tr/) site tasarımından ilham alınan modern UI
- ✅ **Gelişmiş Dark/Light Mode**: Kullanıcı tercihi kaydedilen tema değiştirme
- ✅ **Döviz Çevirimi**: Tüm tutarları TL'ye normalize etme
- ✅ **Responsive Tasarım**: Mobil uyumlu modern UI/UX
- ✅ **Erişilebilirlik**: Klavye navigasyonu ve ARIA desteği
- ✅ **Inter Font**: Modern tipografi için Google Fonts entegrasyonu
- ✅ **Gelişmiş Animasyonlar**: Smooth geçişler ve hover efektleri
- ✅ **Modern Component Design**: Card-based layout ve glassmorphism etkiler

## 🛠️ Teknolojiler

- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Framework**: Bootstrap 5
- **Icons**: Font Awesome 6
- **Responsive**: Mobile-first yaklaşım

## 📋 Kurulum

### Gereksinimler
- Modern web tarayıcı (Chrome, Firefox, Safari, Edge)
- HTTP sunucu (development için)

### Hızlı Başlangıç

1. **Projeyi klonlayın**
   ```bash
   git clone <repository-url>
   cd turkcell-roaming-assistant
   ```

2. **HTTP sunucu başlatın**
   
   **Python ile:**
   ```bash
   # Python 3
   python -m http.server 8000
   
   # Python 2
   python -m SimpleHTTPServer 8000
   ```
   
   **Node.js ile:**
   ```bash
   npx http-server . -p 8000
   ```
   
   **PHP ile:**
   ```bash
   php -S localhost:8000
   ```

3. **Tarayıcıda açın**
   ```
   http://localhost:8000
   ```

## 🎮 Demo Kullanımı

1. **Demo & Tema**: Header'daki demo butonu ile örnek veri yükleyin, tema butonu ile dark/light mode arasında geçiş yapın
2. **Kullanıcı Seçimi**: Dropdown'dan bir kullanıcı seçin
3. **Seyahat Planı**: Ülke ve tarih bilgilerini girin, "Ülke Ekle" butonuna tıklayın
4. **Kullanım Profili**: Günlük data, arama ve SMS ihtiyaçlarınızı ayarlayın
5. **Hesaplama**: "Hesapla" butonuna tıklayarak önerileri görün
6. **Paket Seçimi**: Önerilen paketlerden birini seçin ve satın alma simülasyonunu tamamlayın

## 📊 Veri Yapısı

### Countries (Ülkeler)
```javascript
{
  country_code: 'DE',
  country_name: 'Germany',
  region: 'Europe'
}
```

### Roaming Rates (Tekil Ücretler)
```javascript
{
  country_code: 'DE',
  data_per_mb: 0.10,
  voice_per_min: 0.45,
  sms_per_msg: 0.20,
  currency: 'EUR'
}
```

### Roaming Packs (Paketler)
```javascript
{
  pack_id: 201,
  name: 'Avrupa 5GB',
  coverage: 'Europe',
  coverage_type: 'region',
  data_gb: 5,
  voice_min: 50,
  sms: 50,
  price: 19.9,
  validity_days: 7,
  currency: 'EUR'
}
```

## 🧮 Maliyet Hesaplama Algoritması

### 1. Trip Analizi
- Toplam gün sayısı hesaplanır
- Günlük kullanım × gün sayısı ile toplam ihtiyaç belirlenir

### 2. Paket Uygunluğu
- **Region Coverage**: Ülkenin bölgesi paket kapsamında mı?
- **Country Coverage**: Ülke özel paketi var mı?
- **Validity Control**: Paket süresi yeterli mi?

### 3. Maliyet Hesaplama
- **Paket Maliyeti**: Gerekli paket sayısı × paket fiyatı
- **Aşım Maliyeti**: Paket kapasitesini aşan kullanım
- **Kapsam Dışı Maliyet**: Paket kapsamındaki olmayan ülkeler için tekil ücret

### 4. Optimizasyon
- En düşük maliyetli seçenekler önceliklendirilir
- Kapsama genişliği ve validity uyumu bonus puanlanır

## 🎨 UI/UX Tasarım Prensipleri

### Modern Tasarım Sistemi
- **Primary Colors**: #2855AC (Turkcell Mavi), #FFD500 (Turkcell Sarı), #00A651 (Turkcell Yeşil)
- **Typography**: Inter font family ile modern, okunabilir tasarım
- **Dark/Light Theme**: Dinamik tema değiştirme sistemi
- **Spacing System**: CSS custom properties ile tutarlı aralıklar
- **Border Radius**: 6px-24px arası modern köşe yuvarlaklığı
- **Shadow System**: 4 seviyeli gölge sistemi (sm, md, lg, xl)

### Kullanıcı Deneyimi
- **Adım Adım Süreç**: Soldan sağa doğru doğal akış
- **Görsel Geri Bildirim**: Loading, success ve error durumları
- **Uyarı Sistemi**: Önemli bilgiler için badge ve alert'ler
- **Responsive Design**: Tüm cihazlarda optimum deneyim

## 🔧 API Sözleşmesi (Mock)

### Simulate Request
```javascript
POST /api/simulate
{
  "user_id": 1001,
  "trips": [
    {
      "country_code": "DE",
      "start_date": "2025-08-20",
      "end_date": "2025-08-25"
    }
  ],
  "profile": {
    "avg_daily_mb": 600,
    "avg_daily_min": 10,
    "avg_daily_sms": 2
  }
}
```

### Response
```javascript
{
  "summary": {
    "days": 6,
    "total_need": {
      "gb": 3.5,
      "min": 60,
      "sms": 12
    }
  },
  "options": [
    {
      "kind": "pack",
      "pack_id": 201,
      "total_cost": 19.9,
      "total_cost_tl": 706.45,
      "currency": "EUR",
      "explanation": "5GB data, 50 dakika, 50 SMS - 7 gün geçerli"
    }
  ]
}
```

## 🧪 Test Senaryoları

### Senaryo 1: Avrupa Çoklu Ülke
- **Ülkeler**: Almanya (5 gün) + Yunanistan (3 gün)
- **Kullanım**: Orta seviye (600MB/gün)
- **Beklenen**: Avrupa paketi önerisi

### Senaryo 2: ABD Uzun Seyahat
- **Ülke**: ABD (15 gün)
- **Kullanım**: Yoğun (1200MB/gün)
- **Beklenen**: Çoklu paket uyarısı

### Senaryo 3: Kapsam Dışı Ülke
- **Ülke**: Mısır (7 gün)
- **Kullanım**: Az (450MB/gün)
- **Beklenen**: Tekil ücret + global paket karşılaştırması

## 📱 Mobil Uyumluluk

- **Responsive Grid**: Bootstrap breakpoint'leri
- **Touch-Friendly**: Minimum 44px dokunma alanları
- **Fast Loading**: Optimize edilmiş asset'ler
- **Offline Ready**: Kritik CSS inline

## 🔒 Güvenlik

- **XSS Protection**: innerHTML yerine textContent kullanımı
- **Input Validation**: Form validasyonları
- **HTTPS Ready**: Güvenli protokol desteği

## 🚀 Production Hazırlığı

### Optimizasyon
- CSS/JS minification
- Image compression
- CDN entegrasyonu
- Caching stratejileri

### Monitoring
- Error tracking
- Performance metrics
- User analytics
- A/B testing ready

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/AmazingFeature`)
3. Commit yapın (`git commit -m 'Add some AmazingFeature'`)
4. Push yapın (`git push origin feature/AmazingFeature`)
5. Pull Request açın

## 📄 Lisans

Bu proje MIT lisansı ile lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakınız.

## 📞 İletişim

**Turkcell Teknoloji**
- Website: [turkcell.com.tr](https://turkcell.com.tr)
- Email: support@turkcell.com.tr

---

**🎯 Codenight Case Study - Turkcell Roaming Asistanı**
