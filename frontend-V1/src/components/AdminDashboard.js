import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import '../styles/AdminStyles.css';

// Main Dashboard Component
function AdminDashboard() {
  const [summary, setSummary] = useState({
    totalUsers: 0,
    totalEmployees: 0,
    totalItemReports: 0,
    totalTransactions: 0,
  });

  useEffect(() => {
    fetch('http://localhost:8080/api/admin/dashboard/summary')
      .then((response) => response.json())
      .then((data) => {
        setSummary(data);
      })
      .catch((error) => {
        console.error('Error fetching dashboard summary:', error);
      });
  }, []);

  const colors = ['#4A90E2', '#50E3C2', '#F5A623', '#D0021B'];


  return (
    <div className="dashboard-container">
      {/* <div className="dashboard-header"> */}

      <h1 className="dashboard-title">Admin Dashboard</h1>
      {/* </div> */}
      <div className="dashboard-summary">
        <div className="summary-card" style={{ backgroundColor: colors[0] }}>
          <h3>Total Users</h3>
          <p className="summary-number">{summary.totalUsers}</p>
        </div>
        <div className="summary-card" style={{ backgroundColor: colors[1] }}>
          <h3>Total Employees</h3>
          <p className="summary-number">{summary.totalEmployees}</p>
        </div>
        <div className="summary-card" style={{ backgroundColor: colors[2] }}>
          <h3>Total Item Reports</h3>
          <p className="summary-number">{summary.totalItemReports}</p>
        </div>
        <div className="summary-card" style={{ backgroundColor: colors[3] }}>
          <h3>Total Transactions</h3>
          <p className="summary-number">{summary.totalTransactions}</p>
        </div>
      </div>
      <div className="dashboard-menu">
        <Link to="/users" className="dashboard-card">
          <div className="card-icon">👥</div>
          <h3>Manage Users</h3>
          <p>Add, edit, and manage system users</p>
        </Link>
        <Link to="/employees" className="dashboard-card">
          <div className="card-icon">🧑‍💼</div>
          <h3>Manage Employees</h3>
          <p>Add, edit, and manage employee list</p>
        </Link>
        <Link to="/categories" className="dashboard-card">
          <div className="card-icon">📂</div>
          <h3>Manage Categories</h3>
          <p>Create and organize product categories</p>
        </Link>
        <Link to="/transactions" className="dashboard-card">
          <div className="card-icon">💰</div>
          <h3>Transaction Management</h3>
          <p>View and delete transactions</p>
        </Link>
        <Link to="/item-reports" className="dashboard-card">
          <div className="card-icon">📋</div>
          <h3>Item Reports Management</h3>
          <p>View and delete item reports</p>
        </Link>
      </div>
    </div>
  );
};

export default AdminDashboard;