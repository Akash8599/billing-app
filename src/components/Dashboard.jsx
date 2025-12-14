import React, { useState } from 'react';
import ProductManagement from '../pages/ProductManagement';

function Dashboard({ user, onLogout }) {
  const [currentPage, setCurrentPage] = useState('home');

  const renderContent = () => {
    switch (currentPage) {
      case 'products':
        return <ProductManagement />;
      case 'home':
      default:
        return (
          <div className="dashboard-content">
            <h2>Welcome, {user.username}!</h2>
            <p>You are logged in as: <strong>{user.role}</strong></p>
            
            <div className="features">
              <h3>Available Features:</h3>
              <ul>
                {(user.role === 'ADMIN' || user.role === 'MANAGER') && (
                  <>
                    <li>✓ Manage Products</li>
                    <li>✓ Manage Customers</li>
                    <li>✓ Create Purchase Orders</li>
                  </>
                )}
                <li>✓ Create Sales Invoices</li>
                <li>✓ View Invoice History</li>
                <li>✓ Download PDF Bills</li>
              </ul>
            </div>
          </div>
        );
    }
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>💼 Billing System</h1>
        <div className="user-info">
          <span>{user.username} ({user.role})</span>
          <button onClick={onLogout} className="logout-btn">Logout</button>
        </div>
      </header>

      <div className="dashboard-wrapper">
        {(user.role === 'ADMIN' || user.role === 'MANAGER') && (
          <nav className="sidebar">
            <h3>Menu</h3>
            <button 
              className={`nav-item ${currentPage === 'home' ? 'active' : ''}`}
              onClick={() => setCurrentPage('home')}
            >
              🏠 Dashboard
            </button>
            <button 
              className={`nav-item ${currentPage === 'products' ? 'active' : ''}`}
              onClick={() => setCurrentPage('products')}
            >
              📦 Products
            </button>
            <button className="nav-item enabled">👥 Customers</button>
            <button className="nav-item enabled">📋 Purchase Orders</button>
          </nav>
        )}

        <main className="main-content">
          {renderContent()}
        </main>
      </div>

      <style>{`
        .dashboard {
          min-height: 100vh;
          background: #f5f5f5;
          display: flex;
          flex-direction: column;
        }
        .dashboard-header {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          padding: 20px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .dashboard-header h1 {
          font-size: 24px;
          margin: 0;
        }
        .user-info {
          display: flex;
          gap: 20px;
          align-items: center;
        }
        .logout-btn {
          padding: 8px 16px;
          background: rgba(255,255,255,0.2);
          color: white;
          border: 1px solid white;
          border-radius: 4px;
          cursor: pointer;
          font-weight: 500;
          transition: all 0.3s ease;
        }
        .logout-btn:hover {
          background: rgba(255,255,255,0.3);
        }
        
        .dashboard-wrapper {
          display: flex;
          flex: 1;
        }

        .sidebar {
          width: 200px;
          background: white;
          padding: 20px;
          box-shadow: 2px 0 4px rgba(0,0,0,0.05);
          border-right: 1px solid #e0e0e0;
        }

        .sidebar h3 {
          margin: 0 0 15px 0;
          color: #333;
          font-size: 14px;
          text-transform: uppercase;
          font-weight: 600;
        }

        .nav-item {
          display: block;
          width: 100%;
          padding: 10px 12px;
          margin-bottom: 8px;
          border: none;
          background: transparent;
          color: #666;
          border-radius: 4px;
          cursor: pointer;
          text-align: left;
          font-size: 14px;
          transition: all 0.3s ease;
        }

        .nav-item:hover:not(.disabled) {
          background: #f0f0f0;
          color: #333;
        }

        .nav-item.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          font-weight: 600;
        }

        .nav-item.disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .main-content {
          flex: 1;
          overflow-y: auto;
        }

        .dashboard-content {
          max-width: 1200px;
          margin: 40px auto;
          padding: 30px;
          background: white;
          border-radius: 8px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
          width: 90%;
        }

        .features {
          margin-top: 30px;
          padding: 20px;
          background: #f9f9f9;
          border-radius: 6px;
        }

        .features ul {
          list-style: none;
          margin-top: 10px;
          padding: 0;
        }

        .features li {
          padding: 8px 0;
          color: #333;
        }

        @media (max-width: 768px) {
          .dashboard-wrapper {
            flex-direction: column;
          }
          .sidebar {
            width: 100%;
            border-right: none;
            border-bottom: 1px solid #e0e0e0;
            padding: 10px;
          }
          .sidebar h3 {
            display: none;
          }
          .nav-item {
            display: inline-block;
            margin-right: 8px;
            margin-bottom: 8px;
          }
        }
      `}</style>
    </div>
  );
}

export default Dashboard;
