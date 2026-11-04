import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, saveAuth } from "../api";

export default function Auth({ mode, onAuth }) {
  const isLogin = mode === "login";
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      const body = isLogin
        ? { email: form.email, password: form.password }
        : form;
      const data = await api(isLogin ? "/auth/login" : "/auth/register", { method: "POST", body });
      saveAuth(data);
      onAuth(data);
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-wrap">
      <section className="auth-intro">
        <h1>Pinewood Bank</h1>
        <p>Open an account, move money, and see every rupee in your statement.</p>
      </section>
      <form className="auth-card" onSubmit={submit}>
        <h2>{isLogin ? "Sign in" : "Create your account"}</h2>
        {!isLogin && (
          <label>Full name
            <input value={form.fullName} onChange={set("fullName")} required />
          </label>
        )}
        <label>Email
          <input type="email" value={form.email} onChange={set("email")} required />
        </label>
        <label>Password
          <input type="password" minLength={6} value={form.password} onChange={set("password")} required />
        </label>
        {error && <p className="msg bad" role="alert">{error}</p>}
        <button className="primary" disabled={busy}>
          {busy ? "Please wait…" : isLogin ? "Sign in" : "Create account"}
        </button>
        <p className="switch">
          {isLogin ? <>New here? <Link to="/register">Create an account</Link></>
                   : <>Already a customer? <Link to="/login">Sign in</Link></>}
        </p>
      </form>
    </div>
  );
}
