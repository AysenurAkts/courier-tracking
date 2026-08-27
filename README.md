# Kurye Konum Takip Uygulaması

Bu proje, kuryelerden gelen konum verilerini işleyen küçük bir REST uygulamasıdır. Her konum
geldiğinde kurye için toplam gidilen mesafe güncellenir ve kurye bir Migros mağazasının
100 metre yarıçapına girmişse bu olay kaydedilir.

Mağazaların koordinatları `src/main/resources/stores.json` dosyasında tutulur. Uygulama
şimdilik verileri bellekte saklar; uygulama yeniden başlatıldığında önceki kayıtlar silinir.

## Kullanılan teknolojiler

- Java 23
- Spring Boot
- Maven
- JUnit 5

## Projeyi çalıştırma

Maven yüklü bir terminalde proje klasöründe:

```powershell
mvn clean test
mvn spring-boot:run
```

İsterseniz Windows PowerShell üzerinden testleri çalıştırıp uygulamayı tek komutla
başlatabilirsiniz:

```powershell
.\run.ps1
```

PowerShell script çalıştırmaya izin vermiyorsa yalnızca mevcut terminal için şu komutu
çalıştırın:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

Uygulama başladıktan sonra varsayılan adres:

```text
http://localhost:8080
```

## API kullanımı

### Konum gönderme

```text
POST /api/locations
Content-Type: application/json
```

Örnek istek:

```powershell
curl.exe -X POST http://localhost:8080/api/locations `
  -H "Content-Type: application/json" `
  -d '{"courierId":"courier-1","time":"2026-01-01T10:00:00Z","lat":40.9923307,"lng":29.1244229}'
```

İstek başarılı olursa `202 Accepted` döner.

### Toplam mesafeyi sorgulama

```text
GET /api/couriers/{courierId}/distance
```

Sonuç metre cinsinden sayısal bir değerdir:

```text
1250.74
```

### Mağaza girişlerini listeleme

```text
GET /api/entrances
```

Örnek cevap:

```json
[
  {
    "courierId": "courier-1",
    "storeName": "Ataşehir MMM Migros",
    "time": "2026-01-01T10:00:00Z",
    "distanceMeters": 18.42
  }
]
```

## Giriş kontrolü nasıl çalışır?

Bir kurye mağazaya 100 metre mesafeden daha yakına geldiğinde giriş olayı oluşturulur.
Kurye aynı mağazanın çevresinde hareket etmeye devam ederse her konumda yeni kayıt
oluşturulmaz. Yeni bir giriş sayılabilmesi için önce yarıçapın dışına çıkması gerekir.

Ayrıca aynı mağazaya yapılan yeniden girişler için bir dakikalık bekleme süresi uygulanır.
Bu nedenle kısa süre içinde gerçekleşen çıkış-giriş hareketi yeni bir giriş olarak
loglanmaz.

## Tasarım kararları

Mesafe hesabı `DistanceCalculator` arayüzü üzerinden yapılır. Mevcut uygulamada
Haversine formülü kullanan `HaversineDistanceCalculator` vardır. Bu yapı, ileride başka
bir mesafe hesaplama yöntemi eklenirse takip servisinin değiştirilmemesini sağlar
(Strategy Pattern).

Mağazaya giriş tespit edildiğinde `TrackingService`, `CourierEventListener` nesnelerine
olayı bildirir. `EntranceLogService` bu olayı dinleyip kaydeder ve uygulama loguna yazar.
Böylece giriş tespiti ile giriş sonrası yapılacak işler birbirinden ayrılır
(Observer Pattern).

## Kaynak kod yapısı

```text
src/main/java/com/example/couriertracking
├── controller   REST endpoint'leri
├── dto          İstek ve cevap modelleri
├── model        Uygulama veri modelleri
└── service      Mesafe, mağaza ve takip işlemleri
```
