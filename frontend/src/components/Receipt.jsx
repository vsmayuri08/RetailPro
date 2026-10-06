import React, { useMemo } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import '../styles/cashier.css';

export const digitalReceiptUrl = (sale) => {
  if (!sale?.invoiceNumber || !sale?.receiptToken) return null;
  return `${window.location.origin}/receipt/${encodeURIComponent(sale.invoiceNumber)}?t=${sale.receiptToken}`;
};

const Receipt = ({ sale, onDownloadPdf }) => {
  const qrUrl = useMemo(() => digitalReceiptUrl(sale), [sale]);
  if (!sale) return null;
  return (
    <div className="receipt">
      <div className="receipt-header">
        <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>{sale.branchName}</div>
        <div className="receipt-invoice-number">{sale.invoiceNumber}</div>
        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          {new Date(sale.createdAt).toLocaleString()}
        </div>
      </div>

      <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
        Cashier: {sale.processedByName}
        {sale.customerName && <> · Customer: {sale.customerName}</>}
      </div>

      {sale.items.map((item) => (
        <div className="receipt-line item-name" key={item.id}>
          <span>
            {item.productName} × {item.quantity}
            {Number(item.expiryDiscountPercent) > 0 && (
              <span style={{ color: '#FEB04A', fontSize: '0.78rem' }}> (-{Number(item.expiryDiscountPercent).toFixed(0)}% expiry)</span>
            )}
          </span>
          <span>₹{Number(item.lineTotal).toFixed(2)}</span>
        </div>
      ))}

      <div className="receipt-divider" />

      <div className="receipt-line">
        <span>Subtotal</span>
        <span>₹{Number(sale.subtotal).toFixed(2)}</span>
      </div>
      {Number(sale.discountAmount) > 0 && (
        <div className="receipt-line">
          <span>Discount</span>
          <span>−₹{Number(sale.discountAmount).toFixed(2)}</span>
        </div>
      )}
      <div className="receipt-line">
        <span>Tax</span>
        <span>₹{Number(sale.taxAmount).toFixed(2)}</span>
      </div>
      <div className="receipt-line" style={{ fontWeight: 700, color: 'var(--text-primary)', fontSize: '1.05rem' }}>
        <span>Total</span>
        <span>₹{Number(sale.totalAmount).toFixed(2)}</span>
      </div>

      <div className="receipt-divider" />

      <div className="receipt-line">
        <span>Payment ({sale.payment?.method})</span>
        <span>₹{Number(sale.payment?.cashReceived).toFixed(2)}</span>
      </div>
      <div className="receipt-line">
        <span>Change</span>
        <span>₹{Number(sale.payment?.changeAmount).toFixed(2)}</span>
      </div>

      {sale.loyaltyPointsEarned > 0 && (
        <>
          <div className="receipt-divider" />
          <div className="receipt-line" style={{ color: '#7f3ddb' }}>
            <span>Loyalty points earned</span>
            <span>+{sale.loyaltyPointsEarned}</span>
          </div>
        </>
      )}

      {qrUrl && (
        <div className="receipt-qr">
          <QRCodeSVG value={qrUrl} size={148} bgColor="#ffffff" fgColor="#323232" />
          <p className="form-hint">Scan for a digital copy of this receipt</p>
          {onDownloadPdf && (
            <button type="button" className="btn-secondary btn-sm" onClick={onDownloadPdf}>
              Download PDF
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default Receipt;
