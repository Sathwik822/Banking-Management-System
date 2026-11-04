import { Routes, Route, Navigate, Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { getAuth, clearAuth } from "./api";
import Auth from "./pages/Auth.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Admin from "./pages/Admin.jsx";

function Layout({ auth, onLogout, children }) {
  return (
    <>
      <header className="topbar">
        <Link to="/" className="brand">Pinewood Bank</Link>
        <nav>
          <Link to="/">My accounts</Link>
          {auth.role === "ADMIN" && <Link to="/admin">Admin</Link>}
          <span className="who">{auth.fullName}</span>
          <button className="ghost" onClick={onLogout}>Sign out</button>
        </nav>
      </header>
      <main className="page">{children}</main>
    </>
  );
}

export default function App() {
  const [auth, setAuth] = useState(getAuth());
  const navigate = useNavigate();

  const logout = () => {
    clearAuth();
    setAuth(null);
    navigate("/login");
  };

  const guard = (el, adminOnly = false) => {
    if (!auth) return <Navigate to="/login" replace />;
    if (adminOnly && auth.role !== "ADMIN") return <Navigate to="/" replace />;
    return <Layout auth={auth} onLogout={logout}>{el}</Layout>;
  };

  return (
    <Routes>
      <Route path="/login" element={<Auth mode="login" onAuth={setAuth} />} />
      <Route path="/register" element={<Auth mode="register" onAuth={setAuth} />} />
      <Route path="/" element={guard(<Dashboard />)} />
      <Route path="/admin" element={guard(<Admin />, true)} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
