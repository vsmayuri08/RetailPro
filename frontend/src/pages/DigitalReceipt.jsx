import React, { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import Receipt from '../components/Receipt';
import '../styles/dashboard.css';
import '../styles/cashier.css';

const DigitalReceipt = () => {
  const { invoiceNumber } = useParams();
  const [params] = useSearchParams();
  const token = params.get('t') || '';
  const [sale, setSale] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!invoiceNumber || !token) {
      setError('This receipt link is missing its access token.');
      return;
    }
    fetch(`/api/public/receipts/${encodeURIComponent(invoiceNumber)}?t=${encodeURIComponent(token)}`)
      .then(async (res) => {
        const body = await res.json().catch(() => ({}));
        if (!res.ok) {
          throw new Error(body.message || 'Receipt not found');
        }
        setSale(body.data);
      })
      .catch((err) => setError(err.message || 'Receipt not found'));
  }, [invoiceNumber, token]);

  const downloadPdf = () => {
    window.open(
      `/api/public/receipts/${encodeURIComponent(invoiceNumber)}/invoice.pdf?t=${encodeURIComponent(token)}`,
      '_blank',
    );
  };

  return (
    <div className="dashboard-container" style={{ maxWidth: '520px', margin: '0 auto', padding: '2rem 1rem' }}>
      <div className="page-header">
        <div>
          <h1 className="page-title">Digital Receipt</h1>
          <p className="page-subtitle">RetailPro</p>
        </div>
      </div>
      {error && <div className="alert-banner alert-error">{error}</div>}
      {sale && <Receipt sale={sale} onDownloadPdf={downloadPdf} />}
      {!sale && !error && <div className="data-card"><div className="empty-state">Loading receipt…</div></div>}
    </div>
  );
};

export default DigitalReceipt;
