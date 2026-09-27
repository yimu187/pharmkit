import { googleLoginUrl } from "./api";

export default function LoginPage() {
  const error = new URLSearchParams(window.location.search).get("error");

  return (
    <div className="login-page">
      <section className="card login-card">
        <p className="eyebrow">Ev eczanesi</p>
        <h1>İlaç Takip</h1>
        <p className="lede">Google hesabınla giriş yap. Sadece ad, soyad ve e-posta kaydedilir.</p>
        {error ? <div className="banner">Google girişi tamamlanamadı. Tekrar dene.</div> : null}
        <a className="google-button" href={googleLoginUrl()}>
          Google ile giriş yap
        </a>
      </section>
    </div>
  );
}
