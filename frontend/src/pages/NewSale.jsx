import React, { useCallback, useEffect, useState } from 'react';
import {
  searchProducts, searchCustomers, registerCustomer,
  checkout, getBillingPreviewSettings, lookupProductByCode, downloadInvoicePdf,
} from '../api/cashierApi';
import Receipt from '../components/Receipt';
import BarcodeScanner from '../components/BarcodeScanner';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';
import '../styles/cashier.css';

const emptyCustomerForm = { fullName: '', phone: '', email: '', address: '' };

const NewSale = () => {
  const [productQuery, setProductQuery] = useState('');
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]); // { productId, name, sku, unitPrice, quantity, availableStock }

  const [customerQuery, setCustomerQuery] = useState('');
  const [customerResults, setCustomerResults] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [showCustomerModal, setShowCustomerModal] = useState(false);
  const [customerForm, setCustomerForm] = useState(emptyCustomerForm);
  const [registering, setRegistering] = useState(false);

  const [discountAmount, setDiscountAmount] = useState('0');
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [amountPaid, setAmountPaid] = useState('');
  const [taxRatePercent, setTaxRatePercent] = useState(0);

  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [completedSale, setCompletedSale] = useState(null);
  const [scanning, setScanning] = useState(false);
  const [scanHint, setScanHint] = useState('');

  useEffect(() => {
    getBillingPreviewSettings().then((s) => setTaxRatePercent(Number(s.taxRatePercent))).catch(() => {});
    handleProductSearch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleProductSearch = async (e) => {
    if (e) e.preventDefault();
    try {
      const results = await searchProducts(productQuery);
      setProducts(results);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to search products');
    }
  };

  const addToCart = (product) => {
    setCart((prev) => {
      const existing = prev.find((i) => i.productId === product.id);
      if (existing) {
        if (existing.quantity >= product.quantity) return prev; // can't exceed available stock
        return prev.map((i) => i.productId === product.id ? { ...i, quantity: i.quantity + 1 } : i);
      }
      if (product.quantity < 1) return prev;
      const effectivePrice = Number(product.expiryDiscountPercent) > 0 ? Number(product.discountedPrice) : Number(product.price);
      return [...prev, {
        productId: product.id, name: product.name, sku: product.sku,
        unitPrice: effectivePrice, discountPercent: Number(product.expiryDiscountPercent) || 0,
        quantity: 1, availableStock: product.quantity,
      }];
    });
  };

  const handleScannedCode = useCallback(async (code) => {
    try {
      const product = await lookupProductByCode(code);
      addToCart(product);
      setScanHint(`Added ${product.name}`);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || `No product matched “${code}”`);
    }
  }, []);

  const stepQuantity = (productId, delta) => {
    setCart((prev) => prev
      .map((i) => {
        if (i.productId !== productId) return i;
        const newQty = i.quantity + delta;
        if (newQty <= 0) return null; // remove
        if (newQty > i.availableStock) return i; // already at max, no-op
        return { ...i, quantity: newQty };
      })
      .filter(Boolean)
    );
  };

  const removeFromCart = (productId) => setCart((prev) => prev.filter((i) => i.productId !== productId));

  const handleCustomerSearch = async (e) => {
    e.preventDefault();
    try {
      const results = await searchCustomers(customerQuery);
      setCustomerResults(results);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to search customers');
    }
  };

  const handleRegisterCustomer = async (e) => {
    e.preventDefault();
    setRegistering(true);
    setError('');
    try {
      const customer = await registerCustomer(customerForm);
      setSelectedCustomer(customer);
      setShowCustomerModal(false);
      setCustomerForm(emptyCustomerForm);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to register customer');
    } finally {
      setRegistering(false);
    }
  };

  const subtotal = cart.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);
  const discount = Number(discountAmount) || 0;
  const taxableAmount = Math.max(subtotal - discount, 0);
  const taxAmount = taxableAmount * (taxRatePercent / 100);
  const totalAmount = taxableAmount + taxAmount;
  const changeDue = (Number(amountPaid) || 0) - totalAmount;

  const resetForNewSale = () => {
    setCompletedSale(null);
    setCart([]);
    setSelectedCustomer(null);
    setCustomerQuery('');
    setCustomerResults([]);
    setDiscountAmount('0');
    setPaymentMethod('CASH');
    setAmountPaid('');
    setError('');
    handleProductSearch();
  };

  const handleCheckout = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      const sale = await checkout({
        customerId: selectedCustomer ? selectedCustomer.id : undefined,
        items: cart.map((i) => ({ productId: i.productId, quantity: i.quantity })),
        discountAmount: discount,
        paymentMethod,
        amountPaid: Number(amountPaid) || 0,
      });
      setCompletedSale(sale);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to complete sale');
    } finally {
      setSubmitting(false);
    }
  };

  if (completedSale) {
    return (
      <div className="dashboard-container">
        <div className="page-header">
          <div>
            <h1 className="page-title">Sale Complete</h1>
            <p className="page-subtitle">Invoice generated successfully</p>
          </div>
          <button className="btn-primary" onClick={resetForNewSale}>+ New Sale</button>
        </div>
        <Receipt sale={completedSale} onDownloadPdf={() => downloadInvoicePdf(completedSale.id, completedSale.invoiceNumber)} />
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">New Sale</h1>
          <p className="page-subtitle">Search products, build the cart, and check out</p>
        </div>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}

      <div className="pos-layout">
        <div>
          <form className="search-bar" onSubmit={handleProductSearch} style={{ marginBottom: '1rem' }}>
            <input
              placeholder="Search by product name, SKU, or barcode…"
              value={productQuery}
              onChange={(e) => setProductQuery(e.target.value)}
            />
            <button type="submit" className="btn-secondary">Search</button>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => { setScanning((open) => !open); setScanHint(''); }}
            >
              {scanning ? 'Close scanner' : 'Scan barcode'}
            </button>
          </form>
          {scanning && (
            <div className="data-card" style={{ marginBottom: '1rem' }}>
              <BarcodeScanner
                onScan={handleScannedCode}
                onError={(message) => setError(message)}
              />
              {scanHint && <p className="form-hint" style={{ textAlign: 'center' }}>{scanHint}</p>}
            </div>
          )}

          <div className="data-card">
            {products.length === 0 ? (
              <div className="empty-state">No products found.</div>
            ) : (
              products.map((p) => (
                <div className="pos-product-row" key={p.id}>
                  <div className="pos-product-info">
                    <span className="pos-product-name">{p.name}</span>
                    <span className="pos-product-meta">
                      {p.sku}{p.barcode ? ` · barcode ${p.barcode}` : ''} ·{' '}
                      {Number(p.expiryDiscountPercent) > 0 ? (
                        <>
                          <span style={{ textDecoration: 'line-through' }}>₹{Number(p.price).toFixed(2)}</span>
                          {' '}₹{Number(p.discountedPrice).toFixed(2)}
                        </>
                      ) : (
                        <>₹{Number(p.price).toFixed(2)}</>
                      )}
                      {' '}· {p.quantity} in stock
                      {p.expired && <span className="badge badge-danger" style={{ marginLeft: '0.5rem' }}>Cannot be billed</span>}
                      {!p.expired && Number(p.expiryDiscountPercent) > 0 && (
                        <span className="badge badge-gold" style={{ marginLeft: '0.5rem' }}>-{Number(p.expiryDiscountPercent).toFixed(0)}%</span>
                      )}
                    </span>
                  </div>
                  <button
                    className="btn-primary btn-sm"
                    onClick={() => addToCart(p)}
                    disabled={p.quantity < 1 || p.expired}
                  >
                    Add
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="pos-cart-sticky">
          <div className="data-card" style={{ padding: '1.25rem' }}>
            <h3 style={{ marginBottom: '0.75rem' }}>Cart</h3>
            {cart.length === 0 ? (
              <div className="pos-empty-cart">Cart is empty — add products from the left.</div>
            ) : (
              cart.map((item) => (
                <div className="cart-item" key={item.productId}>
                  <div className="cart-item-info">
                    <span className="cart-item-name">{item.name}</span>
                    <span className="cart-item-price">
                      ₹{item.unitPrice.toFixed(2)} each
                      {item.discountPercent > 0 && <span style={{ color: '#FEB04A' }}> (-{item.discountPercent.toFixed(0)}% expiry discount)</span>}
                    </span>
                  </div>
                  <div className="stock-stepper">
                    <button type="button" onClick={() => stepQuantity(item.productId, -1)}>−</button>
                    <span className="stock-qty">{item.quantity}</span>
                    <button type="button" onClick={() => stepQuantity(item.productId, 1)} disabled={item.quantity >= item.availableStock}>+</button>
                  </div>
                  <button className="cart-remove-btn" onClick={() => removeFromCart(item.productId)} title="Remove">✕</button>
                </div>
              ))
            )}

            <div style={{ marginTop: '1rem' }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Customer</label>
              {selectedCustomer ? (
                <div className="pos-summary-row" style={{ alignItems: 'center' }}>
                  <span>{selectedCustomer.fullName} ({selectedCustomer.phone})</span>
                  <button className="btn-secondary btn-sm" onClick={() => setSelectedCustomer(null)}>Clear</button>
                </div>
              ) : (
                <>
                  <form className="search-bar" onSubmit={handleCustomerSearch} style={{ margin: '0.4rem 0' }}>
                    <input
                      placeholder="Search by name or phone (optional)…"
                      value={customerQuery}
                      onChange={(e) => setCustomerQuery(e.target.value)}
                    />
                    <button type="submit" className="btn-secondary btn-sm">Find</button>
                  </form>
                  {customerResults.length > 0 && (
                    <div className="data-card" style={{ maxHeight: '140px', overflowY: 'auto', marginBottom: '0.5rem' }}>
                      {customerResults.map((c) => (
                        <div className="pos-product-row" key={c.id}>
                          <span>{c.fullName} ({c.phone})</span>
                          <button className="btn-secondary btn-sm" onClick={() => { setSelectedCustomer(c); setCustomerResults([]); }}>Select</button>
                        </div>
                      ))}
                    </div>
                  )}
                  <button type="button" className="btn-secondary btn-sm" onClick={() => setShowCustomerModal(true)}>
                    + Register New Customer
                  </button>
                </>
              )}
            </div>

            <form onSubmit={handleCheckout}>
              <div className="form-group" style={{ marginTop: '1rem' }}>
                <label>Discount (₹)</label>
                <input type="number" step="0.01" min="0" value={discountAmount} onChange={(e) => setDiscountAmount(e.target.value)} />
              </div>
              <div className="form-group">
                <label>Payment Method</label>
                <select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)}>
                  <option value="CASH">Cash</option>
                  <option value="CARD">Card</option>
                  <option value="UPI">UPI</option>
                </select>
              </div>
              <div className="form-group">
                <label>{paymentMethod === 'CASH' ? 'Cash Received (₹)' : 'Amount Paid (₹)'}</label>
                <input type="number" step="0.01" min="0" value={amountPaid} onChange={(e) => setAmountPaid(e.target.value)} required />
              </div>

              <div className="pos-summary-row"><span>Subtotal</span><span>₹{subtotal.toFixed(2)}</span></div>
              <div className="pos-summary-row"><span>Discount</span><span>−₹{discount.toFixed(2)}</span></div>
              <div className="pos-summary-row"><span>Tax ({taxRatePercent}%)</span><span>₹{taxAmount.toFixed(2)}</span></div>
              <div className="pos-summary-row total"><span>Total</span><span>₹{totalAmount.toFixed(2)}</span></div>
              {amountPaid !== '' && (
                <div className="pos-summary-row"><span>Change</span><span>₹{Math.max(changeDue, 0).toFixed(2)}</span></div>
              )}

              <div className="form-actions">
                <button type="submit" className="btn-primary" disabled={submitting || cart.length === 0} style={{ width: '100%' }}>
                  {submitting ? 'Processing…' : 'Complete Sale'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {showCustomerModal && (
        <div className="modal-overlay" onClick={() => setShowCustomerModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">Register Customer</h2>
            <form onSubmit={handleRegisterCustomer}>
              <div className="form-group">
                <label>Full Name</label>
                <input
                  value={customerForm.fullName}
                  onChange={(e) => setCustomerForm({ ...customerForm, fullName: e.target.value })}
                  required
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Phone Number</label>
                  <input
                    value={customerForm.phone}
                    onChange={(e) => setCustomerForm({ ...customerForm, phone: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Email (optional)</label>
                  <input
                    type="email"
                    value={customerForm.email}
                    onChange={(e) => setCustomerForm({ ...customerForm, email: e.target.value })}
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Address (optional)</label>
                <input
                  value={customerForm.address}
                  onChange={(e) => setCustomerForm({ ...customerForm, address: e.target.value })}
                />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowCustomerModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={registering}>
                  {registering ? 'Registering…' : 'Register & Select'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default NewSale;
