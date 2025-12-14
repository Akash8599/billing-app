import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './ProductManagement.css';

const ProductManagement = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formData, setFormData] = useState({
    sku: '',
    name: '',
    description: '',
    costPrice: '',
    sellingPrice: '',
    gstRate: '18',
    quantity: '',
    lowStockAlert: '10'
  });

  const API_URL = 'http://localhost:8080/api/products';

  // Fetch all products
  const fetchProducts = async () => {
    setLoading(true);
    try {
      const response = await axios.get(API_URL, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        }
      });
      setProducts(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load products');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Load products on component mount
  useEffect(() => {
    fetchProducts();
  }, []);

  // Handle form input change
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Reset form
  const resetForm = () => {
    setFormData({
      sku: '',
      name: '',
      description: '',
      costPrice: '',
      sellingPrice: '',
      gstRate: '18',
      quantity: '',
      lowStockAlert: '10'
    });
    setEditingId(null);
    setShowForm(false);
  };

  // Handle create/update product
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validation
    if (!formData.sku || !formData.name || !formData.costPrice || !formData.sellingPrice || !formData.quantity) {
      setError('Please fill all required fields');
      return;
    }

    if (parseFloat(formData.sellingPrice) < parseFloat(formData.costPrice)) {
      setError('Selling price must be greater than cost price');
      return;
    }

    try {
      const payload = {
        sku: formData.sku,
        name: formData.name,
        description: formData.description,
        costPrice: parseFloat(formData.costPrice),
        sellingPrice: parseFloat(formData.sellingPrice),
        gstRate: parseFloat(formData.gstRate),
        quantity: parseInt(formData.quantity),
        lowStockAlert: parseInt(formData.lowStockAlert)
      };

      if (editingId) {
        // Update product
        await axios.put(`${API_URL}/${editingId}`, payload, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
          }
        });
        setSuccess('Product updated successfully!');
      } else {
        // Create product
        await axios.post(API_URL, payload, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
          }
        });
        setSuccess('Product added successfully!');
      }

      fetchProducts();
      resetForm();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save product');
    }
  };

  // Handle edit product
  const handleEdit = (product) => {
    setFormData({
      sku: product.sku,
      name: product.name,
      description: product.description || '',
      costPrice: product.costPrice.toString(),
      sellingPrice: product.sellingPrice.toString(),
      gstRate: product.gstRate.toString(),
      quantity: product.quantity.toString(),
      lowStockAlert: product.lowStockAlert.toString()
    });
    setEditingId(product.id);
    setShowForm(true);
  };

  // Handle delete product
  const handleDelete = async (productId) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      try {
        await axios.delete(`${API_URL}/${productId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
          }
        });
        setSuccess('Product deleted successfully!');
        fetchProducts();
      } catch (err) {
        setError('Failed to delete product');
      }
    }
  };

  // Filter products by search
  const filteredProducts = products.filter(product =>
    product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    product.sku.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="product-management">
      <div className="pm-header">
        <h1>📦 Product Management</h1>
        <button 
          className="btn-add-product"
          onClick={() => {
            resetForm();
            setShowForm(true);
          }}
        >
          + Add New Product
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {showForm && (
        <div className="form-container">
          <div className="form-card">
            <h2>{editingId ? 'Edit Product' : 'Add New Product'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>SKU (Product Code) *</label>
                  <input
                    type="text"
                    name="sku"
                    value={formData.sku}
                    onChange={handleInputChange}
                    placeholder="e.g., PROD001"
                    disabled={editingId !== null}
                  />
                </div>
                <div className="form-group">
                  <label>Product Name *</label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    placeholder="e.g., Hammer"
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  placeholder="Product details"
                  rows="2"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Cost Price (₹) *</label>
                  <input
                    type="number"
                    name="costPrice"
                    value={formData.costPrice}
                    onChange={handleInputChange}
                    placeholder="0.00"
                    step="0.01"
                  />
                </div>
                <div className="form-group">
                  <label>Selling Price (₹) *</label>
                  <input
                    type="number"
                    name="sellingPrice"
                    value={formData.sellingPrice}
                    onChange={handleInputChange}
                    placeholder="0.00"
                    step="0.01"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>GST Rate (%)</label>
                  <select
                    name="gstRate"
                    value={formData.gstRate}
                    onChange={handleInputChange}
                  >
                    <option value="5">5%</option>
                    <option value="12">12%</option>
                    <option value="18">18%</option>
                    <option value="28">28%</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Initial Quantity *</label>
                  <input
                    type="number"
                    name="quantity"
                    value={formData.quantity}
                    onChange={handleInputChange}
                    placeholder="0"
                    min="0"
                  />
                </div>
                <div className="form-group">
                  <label>Low Stock Alert</label>
                  <input
                    type="number"
                    name="lowStockAlert"
                    value={formData.lowStockAlert}
                    onChange={handleInputChange}
                    placeholder="10"
                    min="0"
                  />
                </div>
              </div>

              <div className="form-actions">
                <button type="submit" className="btn-submit">
                  {editingId ? 'Update Product' : 'Add Product'}
                </button>
                <button type="button" className="btn-cancel" onClick={resetForm}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="search-container">
        <input
          type="text"
          placeholder="🔍 Search by name or SKU..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
        <span className="result-count">{filteredProducts.length} products found</span>
      </div>

      {loading ? (
        <div className="loading">Loading products...</div>
      ) : filteredProducts.length === 0 ? (
        <div className="no-products">
          <p>No products found. {!showForm && <a onClick={() => setShowForm(true)}>Add your first product</a>}</p>
        </div>
      ) : (
        <div className="products-table-container">
          <table className="products-table">
            <thead>
              <tr>
                <th>SKU</th>
                <th>Product Name</th>
                <th>Cost Price</th>
                <th>Selling Price</th>
                <th>Profit</th>
                <th>Margin</th>
                <th>GST</th>
                <th>Stock</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredProducts.map(product => {
                const profit = product.sellingPrice - product.costPrice;
                const margin = ((profit / product.costPrice) * 100).toFixed(2);
                const isLowStock = product.quantity <= product.lowStockAlert;

                return (
                  <tr key={product.id} className={isLowStock ? 'low-stock-row' : ''}>
                    <td className="sku">{product.sku}</td>
                    <td className="name">{product.name}</td>
                    <td className="price">₹{product.costPrice.toFixed(2)}</td>
                    <td className="price">₹{product.sellingPrice.toFixed(2)}</td>
                    <td className="profit">₹{profit.toFixed(2)}</td>
                    <td className="margin">{margin}%</td>
                    <td className="gst">{product.gstRate}%</td>
                    <td className={`quantity ${isLowStock ? 'alert' : ''}`}>
                      {product.quantity}
                      {isLowStock && <span className="alert-badge">⚠️ Low</span>}
                    </td>
                    <td className="status">
                      <span className={`badge ${isLowStock ? 'badge-warning' : 'badge-success'}`}>
                        {isLowStock ? 'Low Stock' : 'In Stock'}
                      </span>
                    </td>
                    <td className="actions">
                      <button
                        className="btn-edit"
                        onClick={() => handleEdit(product)}
                        title="Edit product"
                      >
                        ✏️ Edit
                      </button>
                      <button
                        className="btn-delete"
                        onClick={() => handleDelete(product.id)}
                        title="Delete product"
                      >
                        🗑️ Delete
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ProductManagement;
