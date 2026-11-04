import { useCallback, useEffect, useState } from "react";
import { api, money } from "../api";

const DONE = { deposit: "Deposited", withdraw: "Withdrawn", transfer: "Transferred" };

export default function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [selected, setSelected] = useState("");
  const [history, setHistory] = useState([]);
  const [tab, setTab] = useState("deposit");
  const [amount, setAmount] = useState("");
  const [to, setTo] = useState("");
  const [msg, setMsg] = useState(null);
  const [open, setOpen] = useState({ accountType: "SAVINGS", initialDeposit: "0" });

  const loadAccounts = useCallback(async () => {
    const list = await api("/accounts");
    setAccounts(list);
    setSelected((s) => s || list[0]?.accountNumber || "");
  }, []);

  useEffect(() => { loadAccounts().catch((e) => setMsg({ ok: false, text: e.message })); }, [loadAccounts]);

  useEffect(() => {
    if (!selected) { setHistory([]); return; }
    api(`/accounts/${selected}/transactions`).then(setHistory).catch(() => setHistory([]));
  }, [selected, accounts]);

  const current = accounts.find((a) => a.accountNumber === selected);

  async function openAccount(e) {
    e.preventDefault();
    setMsg(null);
    try {
      const a = await api("/accounts", {
        method: "POST",
        body: { accountType: open.accountType, initialDeposit: Number(open.initialDeposit || 0) },
      });
      setSelected(a.accountNumber);
      await loadAccounts();
      setMsg({ ok: true, text: `Account ${a.accountNumber} opened.` });
    } catch (err) { setMsg({ ok: false, text: err.message }); }
  }

  async function run(e) {
    e.preventDefault();
    setMsg(null);
    try {
      if (tab === "transfer") {
        await api("/transactions/transfer", {
          method: "POST",
          body: { fromAccount: selected, toAccount: to.trim(), amount: Number(amount) },
        });
      } else {
        await api(`/transactions/${tab}`, {
          method: "POST",
          body: { accountNumber: selected, amount: Number(amount) },
        });
      }
      setMsg({ ok: true, text: `${DONE[tab]} ${money(Number(amount))}.` });
      setAmount(""); setTo("");
      await loadAccounts();
    } catch (err) { setMsg({ ok: false, text: err.message }); }
  }

  return (
    <div className="grid">
      <aside>
        <h3>Your accounts</h3>
        {accounts.length === 0 && <p className="muted">You have no accounts yet. Open one below.</p>}
        <ul className="acc-list">
          {accounts.map((a) => (
            <li key={a.id}>
              <button className={a.accountNumber === selected ? "acc active" : "acc"} onClick={() => setSelected(a.accountNumber)}>
                <span>{a.accountType === "SAVINGS" ? "Savings" : "Current"} · {a.accountNumber}</span>
                <strong>{money(a.balance)}</strong>
              </button>
            </li>
          ))}
        </ul>

        <form className="panel" onSubmit={openAccount}>
          <h3>Open a new account</h3>
          <label>Account type
            <select value={open.accountType} onChange={(e) => setOpen({ ...open, accountType: e.target.value })}>
              <option value="SAVINGS">Savings</option>
              <option value="CURRENT">Current</option>
            </select>
          </label>
          <label>Opening deposit (₹)
            <input type="number" min="0" step="0.01" value={open.initialDeposit}
                   onChange={(e) => setOpen({ ...open, initialDeposit: e.target.value })} />
          </label>
          <button className="primary">Open account</button>
        </form>
      </aside>

      <section>
        {msg && <p className={msg.ok ? "msg good" : "msg bad"} role="status">{msg.text}</p>}

        {current ? (
          <>
            <div className="balance">
              <span>{current.accountType === "SAVINGS" ? "Savings account" : "Current account"} {current.accountNumber}</span>
              <strong>{money(current.balance)}</strong>
            </div>

            <div className="panel">
              <div className="tabs" role="tablist">
                {["deposit", "withdraw", "transfer"].map((t) => (
                  <button key={t} role="tab" aria-selected={tab === t} className={tab === t ? "tab on" : "tab"} onClick={() => setTab(t)}>
                    {t[0].toUpperCase() + t.slice(1)}
                  </button>
                ))}
              </div>
              <form onSubmit={run} className="row">
                {tab === "transfer" && (
                  <label>To account number
                    <input value={to} onChange={(e) => setTo(e.target.value)} placeholder="AC1234567890" required />
                  </label>
                )}
                <label>Amount (₹)
                  <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required />
                </label>
                <button className="primary">{tab[0].toUpperCase() + tab.slice(1)}</button>
              </form>
            </div>

            <h3>Statement</h3>
            {history.length === 0 ? <p className="muted">No transactions yet. Make a deposit to get started.</p> : (
              <div className="scroll">
                <table>
                  <thead><tr><th>Date</th><th>Details</th><th className="num">Amount</th><th className="num">Balance</th></tr></thead>
                  <tbody>
                    {history.map((t) => {
                      const credit = t.type === "DEPOSIT" || t.type === "TRANSFER_IN";
                      return (
                        <tr key={t.id}>
                          <td>{new Date(t.createdAt).toLocaleString()}</td>
                          <td>{t.description}</td>
                          <td className={credit ? "num good" : "num bad"}>{credit ? "+" : "−"}{money(t.amount)}</td>
                          <td className="num">{money(t.balanceAfter)}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </>
        ) : (
          <p className="muted">Select or open an account to see its balance and statement.</p>
        )}
      </section>
    </div>
  );
}
