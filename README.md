# İlaç Takip

Eczaneden alınan ilaçları web üzerinden takip etmek için React + Spring Boot uygulaması.

Kaydedilen bilgiler:

- İlaç adı
- Eczaneden alındığı tarih
- Son kullanma tarihi
- Reçetede alınış sıklığı
- Evde bulunduğu yer
- Dozlar (`doses`): ilacı ne zaman içtiğin

## Giriş (Google)

Uygulama Google hesabıyla açılır. Kaydedilen kullanıcı bilgisi yalnızca **ad, soyad, e-posta**dır. Google kullanıcı kimliği (`sub`) hesap eşlemesi için dahili tutulur.

1. [Google Cloud Console](https://console.cloud.google.com/) içinde bir OAuth 2.0 Client ID oluştur (Web application).
2. Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
3. Authorized JavaScript origins: `http://localhost:5173` ve `http://localhost:8080`
4. Client ID ve secret `~/.ilac-takip/oauth.properties` dosyasındadır (git’e girmez). Backend’i normal başlatman yeterli:

```bash
cd backend
./mvnw spring-boot:run
```

Tarayıcıda **http://localhost:5173** kullan (`127.0.0.1` oturum çereziyle uyumsuz olabilir).

Her kullanıcının ilaçları ayrıdır.

## Veritabanı

Şu an **H2 file** kullanılıyor: ilişkisel bir veritabanı. Uygulama restart olsa da kayıtlar silinmez.

Dosya: `~/.ilac-takip/db.mv.db`

İleride PostgreSQL’e geçmek için `backend/src/main/resources/application.properties` içindeki JDBC URL, kullanıcı adı ve sürücüyü değiştirmek yeter. JPA entity’ler aynı kalır.

## Çalıştırma

Java 21 gerekir.

```bash
# backend
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw spring-boot:run
```

```bash
# frontend
cd frontend
npm install
npm run dev
```

Tarayıcı: http://localhost:5173

API: http://localhost:8080/api/medicines

Doz ekleme: `POST /api/medicines/{id}/doses`  
Doz silme: `DELETE /api/doses/{id}`
