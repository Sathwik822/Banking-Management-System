import { useEffect, useState } from "react";
import { api, money } from "../api";

export default function Admin() {
  const [users, setUsers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([api("/admin/users"), api("/admin/accounts")])
      .then(([u, a]) => { setUsers(u); setAccounts(a); })
      .catch((e) => setError(e.message));
  }, []);

  return (
    <>
      <h2>Bank overview</h2>
      {error && <p className="msg bad">{error}</p>}
      <h3>Customers ({users.length})</h3>
      <div className="scroll">
        <table>
          <thead><tr><th>Name</th><th>Email</th><th>Role</th></tr></thead>
          <tbody>{users.map((u) => <tr key={u.id}><td>{u.fullName}</td><td>{u.email}</td><td>{u.role}</td></tr>)}</tbody>
        </table>
      </div>
      <h3>All accounts ({accounts.length})</h3>
      <div className="scroll">
        <table>
          <thead><tr><th>Account</th><th>Type</th><th>Owner</th><th className="num">Balance</th></tr></thead>
          <tbody>{accounts.map((a) => (
            <tr key={a.accountNumber}><td>{a.accountNumber}</td><td>{a.accountType}</td><td>{a.ownerEmail}</td><td className="num">{money(a.balance)}</td></tr>
          ))}</tbody>
        </table>
      </div>
    </>
  );
}
