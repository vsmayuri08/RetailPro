import React, { useEffect, useState } from 'react';
import { getProducts, createProduct, updateProduct, activateProduct, deactivateProduct, getManagerCategories } from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const emptyForm = {
  name: '', sku: '', barcode: '', brand: '', description: '', categoryId: '',
  price: '', costPrice: '', quantity: 0, reorderLevel: 10, expiryDate: '',
};

const Products = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const loadProducts = async () => {
    setLoading(true);
    try {
      const data = await getProducts();
      setProducts(data);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
    getManagerCategories().then(setCategories).catch(() => {});
  }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (product) => {
    setEditingId(product.id);
    setForm({
      name: product.name,
      sku: product.sku,
      barcode: product.barcode || '',
      brand: product.brand || '',
      description: product.description || '',
      categoryId: product.categoryId ? String(product.categoryId) : '',
      price: product.price,
      costPrice: product.costPrice != null ? String(product.costPrice) : '',
      quantity: product.quantity,
      reorderLevel: product.reorderLevel,
      expiryDate: product.expiryDate || '',
    });
    setShowModal(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        categoryId: form.categoryId ? Number(form.categoryId) : undefined,
        price: Number(form.price),
        costPrice: form.costPrice ? Number(form.costPrice) : undefined,
        quantity: Number(form.quantity),
        reorderLevel: Number(form.reorderLevel),
        expiryDate: form.expiryDate || null,
      };
      if (editingId) {
        await updateProduct(editingId, payload);
        setSuccessMsg('Product updated successfully');
      } else {
        await createProduct(payload);
        setSuccessMsg('Product created successfully');
      }
      setShowModal(false);
      await loadProducts();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save product');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (product) => {
    try {
      if (product.active) {
        await deactivateProduct(product.id);
      } else {
        await activateProduct(product.id);
      }
      await loadProducts();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update product status');
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Products</h1>
          <p className="page-subtitle">Add and update your branch's product catalog</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>+ New Product</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading products…</div>
        ) : products.length === 0 ? (
          <div className="empty-state">No products yet. Add your first product to get started.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>SKU</th>
                <th>Brand</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Flags</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.sku}</td>
                  <td>{p.brand || '—'}</td>
                  <td>{p.category || '—'}</td>
                  <td>
                    {Number(p.expiryDiscountPercent) > 0 ? (
                      <>
                        <span style={{ textDecoration: 'line-through', color: 'var(--text-muted)', marginRight: '0.4rem' }}>
                          ₹{Number(p.price).toFixed(2)}
                        </span>
                        ₹{Number(p.discountedPrice).toFixed(2)}
                      </>
                    ) : (
                      <>₹{Number(p.price).toFixed(2)}</>
                    )}
                  </td>
                  <td>{p.quantity}</td>
                  <td>
                    <div className="tag-row">
                      {p.lowStock && <span className="badge badge-warning">Low stock</span>}
                      {p.expired && <span className="badge badge-danger">Expired</span>}
                      {!p.expired && p.nearExpiry && <span className="badge badge-warning">Near expiry</span>}
                      {Number(p.expiryDiscountPercent) > 0 && (
                        <span className="badge badge-gold">-{Number(p.expiryDiscountPercent).toFixed(0)}%</span>
                      )}
                    </div>
                  </td>
                  <td>
                    <span className={`badge ${p.active ? 'badge-active' : 'badge-inactive'}`}>
                      {p.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(p)}>Edit</button>
                    <button
                      className={`btn-sm ${p.active ? 'btn-danger' : 'btn-success'}`}
                      onClick={() => toggleStatus(p)}
                    >
                      {p.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <p className="form-hint">
        Stock and expiry are managed from the Inventory page (Receive Stock / stock history) once a
        product exists — editing a product here only changes its catalog details.
      </p>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">{editingId ? 'Edit Product' : 'New Product'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Product Name</label>
                <input name="name" value={form.name} onChange={handleChange} required />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>SKU</label>
                  <input name="sku" value={form.sku} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Barcode (optional)</label>
                  <input name="barcode" value={form.barcode} onChange={handleChange} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Brand (optional)</label>
                  <input name="brand" value={form.brand} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label>Category (optional)</label>
                  <select name="categoryId" value={form.categoryId} onChange={handleChange}>
                    <option value="">Uncategorized</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Description (optional)</label>
                <input name="description" value={form.description} onChange={handleChange} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Selling Price (₹)</label>
                  <input type="number" step="0.01" min="0" name="price" value={form.price} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Cost Price (₹, optional)</label>
                  <input type="number" step="0.01" min="0" name="costPrice" value={form.costPrice} onChange={handleChange} />
                </div>
              </div>
              <div className="form-group">
                <label>Reorder Level</label>
                <input type="number" min="0" name="reorderLevel" value={form.reorderLevel} onChange={handleChange} required />
              </div>
              {!editingId && (
                <div className="form-row">
                  <div className="form-group">
                    <label>Opening Stock</label>
                    <input type="number" min="0" name="quantity" value={form.quantity} onChange={handleChange} required />
                  </div>
                  <div className="form-group">
                    <label>Expiry Date (optional)</label>
                    <input type="date" name="expiryDate" value={form.expiryDate} onChange={handleChange} />
                  </div>
                </div>
              )}
              {editingId && (
                <p className="form-hint">
                  Current stock: {form.quantity} {form.expiryDate ? `· nearest expiry ${form.expiryDate}` : ''}.
                  Use Inventory → Receive Stock to add more.
                </p>
              )}
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Create Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Products;
