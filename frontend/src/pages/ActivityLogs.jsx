import React, { useEffect, useState } from 'react';
import { getActivityLogs as getManagerLogs } from '../api/managerApi';
import { getActivityLogs as getAdminLogs } from '../api/adminApi';
import { useAuth } from '../context/AuthContext';
import '../styles/dashboard.css';
import '../styles/admin.css';

const ActivityLogs = () => {
  const { user } = useAuth();
  const [logs, setLogs] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = user?.role === 'SUPER_ADMIN' ? getAdminLogs : getManagerLogs;
    load()
      .then(setLogs)
      .catch((err) => setError(err?.response?.data?.message || 'Failed to load activity log'))
      .finally(() => setLoading(false));
  }, [user?.role]);

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Activity Log</h1>
          <p className="page-subtitle">
            Recent sign-ins, sales, returns, product creates, and stock receipts
          </p>
        </div>
      </div>
      {error && <div className="alert-banner alert-error">{error}</div>}
      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading activity…</div>
        ) : logs.length === 0 ? (
          <div className="empty-state">No activity recorded yet.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>When</th>
                <th>Actor</th>
                <th>Action</th>
                <th>Entity</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>{log.createdAt ? new Date(log.createdAt).toLocaleString() : '—'}</td>
                  <td>
                    {log.actorName || '—'}
                    <div className="form-hint">{log.actorRole}</div>
                  </td>
                  <td><span className="badge badge-silver">{log.action}</span></td>
                  <td>{log.entityType}{log.entityId ? ` #${log.entityId}` : ''}</td>
                  <td>{log.details}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default ActivityLogs;
