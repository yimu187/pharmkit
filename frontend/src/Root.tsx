import { useEffect, useState } from "react";
import App from "./App";
import LoginPage from "./LoginPage";
import { getCurrentUser, type AuthUser } from "./api";
import "./index.css";

export default function Root() {
  const [user, setUser] = useState<AuthUser | null | undefined>(undefined);

  useEffect(() => {
    getCurrentUser()
      .then(setUser)
      .catch(() => setUser(null));
  }, []);

  if (user === undefined) {
    return (
      <div className="login-page">
        <p className="muted">Yükleniyor...</p>
      </div>
    );
  }

  if (user == null) {
    return <LoginPage />;
  }

  return <App user={user} />;
}
