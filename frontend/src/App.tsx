import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  addDose,
  createMedicine,
  deleteDose,
  deleteMedicine,
  listMedicines,
  logoutUrl,
  updateMedicine,
  type AuthUser,
  type Medicine,
  type MedicinePayload,
} from "./api";

type FormState = MedicinePayload;
type ListFilter = "all" | "soon" | "expired";

const emptyForm: FormState = {
  name: "",
  purchasedOn: "",
  expiresOn: "",
  intakeSchedule: "",
  homeLocation: "",
};

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

function expiryStatus(expiresOn: string): "expired" | "soon" | "ok" {
  const today = todayIso();
  if (expiresOn < today) {
    return "expired";
  }
  const limit = new Date();
  limit.setDate(limit.getDate() + 30);
  const soon = limit.toISOString().slice(0, 10);
  if (expiresOn <= soon) {
    return "soon";
  }
  return "ok";
}

function listFilterFromHash(): ListFilter {
  const hash = window.location.hash.replace("#", "");
  if (hash === "soon" || hash === "expired") {
    return hash;
  }
  return "all";
}

function filterTitle(filter: ListFilter): string {
  if (filter === "soon") {
    return "30 gün içinde bitenler";
  }
  if (filter === "expired") {
    return "Süresi geçmiş ilaçlar";
  }
  return "İlaçlarım";
}

function formatDate(value: string): string {
  const [year, month, day] = value.split("-");
  return `${day}.${month}.${year}`;
}

function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value.replace("T", " ");
  }
  return date.toLocaleString("tr-TR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function App({ user }: { user: AuthUser }) {
  const [medicines, setMedicines] = useState<Medicine[]>([]);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [doseBusyId, setDoseBusyId] = useState<number | null>(null);
  const [listFilter, setListFilter] = useState<ListFilter>(listFilterFromHash);

  async function refresh() {
    const data = await listMedicines();
    setMedicines(data);
  }

  useEffect(() => {
    refresh()
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    function onHashChange() {
      setListFilter(listFilterFromHash());
    }
    window.addEventListener("hashchange", onHashChange);
    return () => window.removeEventListener("hashchange", onHashChange);
  }, []);

  const summary = useMemo(() => {
    const expired = medicines.filter((item) => expiryStatus(item.expiresOn) === "expired").length;
    const soon = medicines.filter((item) => expiryStatus(item.expiresOn) === "soon").length;
    return { expired, soon };
  }, [medicines]);

  const visibleMedicines = useMemo(() => {
    if (listFilter === "all") {
      return medicines;
    }
    return medicines.filter((item) => expiryStatus(item.expiresOn) === listFilter);
  }, [medicines, listFilter]);

  function startEdit(medicine: Medicine) {
    setEditingId(medicine.id);
    setForm({
      name: medicine.name,
      purchasedOn: medicine.purchasedOn,
      expiresOn: medicine.expiresOn,
      intakeSchedule: medicine.intakeSchedule ?? "",
      homeLocation: medicine.homeLocation,
    });
  }

  function resetForm() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      if (editingId == null) {
        await createMedicine(form);
      } else {
        await updateMedicine(editingId, form);
      }
      await refresh();
      resetForm();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Kaydedilemedi");
    } finally {
      setSaving(false);
    }
  }

  async function onDelete(id: number) {
    if (!window.confirm("Bu ilacı silmek istiyor musun?")) {
      return;
    }
    setError(null);
    try {
      await deleteMedicine(id);
      if (editingId === id) {
        resetForm();
      }
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Silinemedi");
    }
  }

  async function onTakeNow(id: number) {
    setDoseBusyId(id);
    setError(null);
    try {
      await addDose(id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Doz kaydedilemedi");
    } finally {
      setDoseBusyId(null);
    }
  }

  async function onDeleteDose(id: number) {
    setError(null);
    try {
      await deleteDose(id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Doz silinemedi");
    }
  }

  return (
    <div className="page">
      <header className="hero">
        <div>
          <p className="eyebrow">Ev eczanesi</p>
          <h1>İlaç Takip</h1>
          <p className="lede">
            Eczaneden aldığın ilaçların alış tarihini, son kullanma tarihini, evdeki yerini ve ne zaman içtiğini tut.
          </p>
          <p className="session">
            {user.givenName} {user.familyName} · {user.email}
            {" · "}
            <a href={logoutUrl()}>Çıkış</a>
          </p>
        </div>
        <div className="stats">
          <a href="#all" className={listFilter === "all" ? "active" : undefined}>
            <strong>{medicines.length}</strong>
            <span>kayıt</span>
          </a>
          <a href="#soon" className={listFilter === "soon" ? "active" : undefined}>
            <strong>{summary.soon}</strong>
            <span>30 gün içinde</span>
          </a>
          <a href="#expired" className={listFilter === "expired" ? "active" : undefined}>
            <strong>{summary.expired}</strong>
            <span>süresi geçmiş</span>
          </a>
        </div>
      </header>

      {error ? <div className="banner">{error}</div> : null}

      <main className="layout">
        <section className="card">
          <h2>{editingId == null ? "Yeni ilaç" : "İlacı düzenle"}</h2>
          <form onSubmit={onSubmit}>
            <label>
              İlaç adı
              <input
                required
                value={form.name}
                onChange={(event) => setForm({ ...form, name: event.target.value })}
                placeholder="Parol, vitamin D..."
              />
            </label>
            <label>
              Eczaneden alındığı tarih
              <input
                required
                type="date"
                value={form.purchasedOn}
                onChange={(event) => setForm({ ...form, purchasedOn: event.target.value })}
              />
            </label>
            <label>
              Son kullanma tarihi
              <input
                required
                type="date"
                value={form.expiresOn}
                onChange={(event) => setForm({ ...form, expiresOn: event.target.value })}
              />
            </label>
            <label>
              Reçetede alınış sıklığı
              <input
                required
                value={form.intakeSchedule}
                onChange={(event) => setForm({ ...form, intakeSchedule: event.target.value })}
                placeholder="Günde 2 kez, 8 saatte bir..."
              />
            </label>
            <label>
              Evde nerede
              <input
                required
                value={form.homeLocation}
                onChange={(event) => setForm({ ...form, homeLocation: event.target.value })}
                placeholder="Banyo dolabı, mutfak üst raf..."
              />
            </label>
            <div className="actions">
              <button type="submit" disabled={saving}>
                {saving ? "Kaydediliyor..." : editingId == null ? "Ekle" : "Güncelle"}
              </button>
              {editingId != null ? (
                <button type="button" className="ghost" onClick={resetForm}>
                  Vazgeç
                </button>
              ) : null}
            </div>
          </form>
        </section>

        <section className="card list-card" id="medicines">
          <h2>{filterTitle(listFilter)}</h2>
          {loading ? <p className="muted">Yükleniyor...</p> : null}
          {!loading && medicines.length === 0 ? (
            <p className="muted">Henüz kayıt yok. Soldan ilk ilacı ekle.</p>
          ) : null}
          {!loading && medicines.length > 0 && visibleMedicines.length === 0 ? (
            <p className="muted">Bu grupta ilaç yok.</p>
          ) : null}
          <ul className="medicine-list">
            {visibleMedicines.map((medicine) => {
              const status = expiryStatus(medicine.expiresOn);
              return (
                <li key={medicine.id} className={`medicine ${status}`}>
                  <div>
                    <h3>{medicine.name}</h3>
                    <p>
                      Alındı: {formatDate(medicine.purchasedOn)} · SKT: {formatDate(medicine.expiresOn)}
                    </p>
                    <p className="schedule">{medicine.intakeSchedule || "Reçete sıklığı belirtilmedi"}</p>
                    <p className="place">{medicine.homeLocation}</p>
                    <p>
                      {medicine.lastTakenAt
                        ? `Son doz: ${formatDateTime(medicine.lastTakenAt)} (${medicine.doseCount})`
                        : "Henüz doz kaydı yok"}
                    </p>
                    {status === "expired" ? <span className="tag">Süresi geçmiş</span> : null}
                    {status === "soon" ? <span className="tag soon">Yakında bitiyor</span> : null}
                  </div>
                  <div className="row-actions">
                    <button
                      type="button"
                      onClick={() => onTakeNow(medicine.id)}
                      disabled={doseBusyId === medicine.id}
                    >
                      {doseBusyId === medicine.id ? "Kaydediliyor..." : "Şimdi aldım"}
                    </button>
                    <button type="button" className="ghost" onClick={() => startEdit(medicine)}>
                      Düzenle
                    </button>
                    <button type="button" className="danger" onClick={() => onDelete(medicine.id)}>
                      Sil
                    </button>
                  </div>
                  {medicine.doses.length > 0 ? (
                    <ul className="dose-list">
                      {medicine.doses.map((dose) => (
                        <li key={dose.id}>
                          <span>{formatDateTime(dose.takenAt)}</span>
                          <button type="button" className="ghost" onClick={() => onDeleteDose(dose.id)}>
                            Sil
                          </button>
                        </li>
                      ))}
                    </ul>
                  ) : null}
                </li>
              );
            })}
          </ul>
        </section>
      </main>
    </div>
  );
}
