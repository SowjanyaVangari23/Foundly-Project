import React, { useEffect, useState } from 'react';
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import axios from 'axios';
import '../styles/Dashboard.css';
import { AlertCircle, CheckCircle, Database, Shield } from 'lucide-react';

const Dashboard = () => {
  const [stats, setStats] = useState([]);
  const [monthlyData, setMonthlyData] = useState([]);
  const [categoryData, setCategoryData] = useState([]);
  const [locationData, setLocationData] = useState([]);
  const [claimStatusData, setClaimStatusData] = useState([]);
  const [handoverToSecurityCount, setHandoverToSecurityCount] = useState(0);
  const [transactionHandoverToSecurityCount, setTransactionHandoverToSecurityCount] = useState(0);
  const [loading, setLoading] = useState(true);

  const COLORS = ['#0088FE', '#FF8042', '#82ca9d', '#8884d8'];

  useEffect(() => {
    setLoading(true);
    fetchStats();
    fetchMonthlyTrends();
    fetchCategories();
    fetchLocations();
    fetchClaimStatus();
    fetchHandoverToSecurityCount();
    fetchTransactionHandoverToSecurityCount();
    setLoading(false);
  }, []);

  const fetchStats = async () => {
    try {
      const [lostRes, foundRes, allTransactionsRes] = await Promise.all([
        axios.get('http://localhost:8080/api/items/lost-items'),
        axios.get('http://localhost:8080/api/items/found-items'),
        axios.get('http://localhost:8080/api/transactions')
      ]);

      const totalLost = lostRes.data.length;
      const totalFound = foundRes.data.length;
      const totalReturned = allTransactionsRes.data.filter(t => t.transactionStatus === 'COMPLETED').length;

      setStats([
        { id: 1, title: 'Total Lost Items Reported', count: totalLost, icon: <AlertCircle className="stat-icon lost-icon" />, type: 'lost' },
        { id: 2, title: 'Total Found Items Reported', count: totalFound, icon: <CheckCircle className="stat-icon found-icon" />, type: 'found' },
        { id: 3, title: 'Items Successfully Returned', count: totalReturned, icon: <Database className="stat-icon neutral-icon" />, type: 'neutral' }
      ]);
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };

  const fetchMonthlyTrends = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/items');
      const data = res.data;
      const monthlyCounts = {};

      data.forEach(item => {
        if (!item.dateReported || !item.type) return;

        const date = new Date(item.dateReported);
        if (isNaN(date)) return;

        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

        if (!monthlyCounts[monthKey]) {
          monthlyCounts[monthKey] = { name: monthKey, lost: 0, found: 0 };
        }

        if (item.type === 'LOST') monthlyCounts[monthKey].lost++;
        else if (item.type === 'FOUND') monthlyCounts[monthKey].found++;
      });

      const sorted = Object.values(monthlyCounts).sort((a, b) => a.name.localeCompare(b.name));
      sorted.forEach(entry => {
        const [year, month] = entry.name.split("-");
        const date = new Date(`${year}-${month}-01`);
        entry.name = date.toLocaleString('default', { month: 'short', year: 'numeric' });
      });

      setMonthlyData(sorted);
    } catch (error) {
      console.error('Error fetching monthly trends:', error);
    }
  };

  const fetchCategories = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/items/category-counts');
      const formatted = res.data.map(item => ({
        name: item.category,
        value: item.count
      }));
      setCategoryData(formatted);
    } catch (error) {
      console.error('Error fetching categories:', error);
    }
  };

  const generateCategoryColors = (data) => {
    return data.map((_, index) => `hsl(${(index * 30) % 360}, 70%, 60%)`);
  };

  const fetchLocations = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/items');
      const data = res.data;
      const locationCounts = {};

      data.forEach(item => {
        const location = item.location || 'Unknown';
        if (!locationCounts[location]) locationCounts[location] = 0;
        locationCounts[location]++;
      });

      const formatted = Object.entries(locationCounts).map(([name, value]) => ({ name, value }));
      setLocationData(formatted);
    } catch (error) {
      console.error('Error fetching locations:', error);
    }
  };

  const fetchClaimStatus = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/transactions');
      const data = res.data;

      let counts = {
        Requested: 0,
        'Pending Completion': 0,
        Completed: 0
      };

      data.forEach(txn => {
        switch (txn.transactionStatus) {
          case 'REQUESTED':
            counts.Requested++;
            break;
          case 'PENDING_COMPLETION':
            counts['Pending Completion']++;
            break;
          case 'COMPLETED':
            counts.Completed++;
            break;
          default:
            break;
        }
      });

      const formatted = Object.entries(counts).map(([name, value]) => ({ name, value }));
      setClaimStatusData(formatted);
    } catch (error) {
      console.error('Error fetching claim status:', error);
    }
  };

  const fetchHandoverToSecurityCount = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/items/handover-to-security-count');
      setHandoverToSecurityCount(res.data);
    } catch (error) {
      console.error('Error fetching handover to security count:', error);
    }
  };

  const fetchTransactionHandoverToSecurityCount = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/transactions/count/handover-to-security');
      setTransactionHandoverToSecurityCount(res.data);
    } catch (error) {
      console.error('Error fetching transaction handover to security count:', error);
    }
  };

  const renderStatCards = () => stats.map(stat => (
    <div key={stat.id} className={`stat-card ${stat.type}-card`}>
      <div className="stat-icon-container">{stat.icon}</div>
      <div className="stat-content">
        <h2 className="stat-count">{stat.count}</h2>
        <p className="stat-title">{stat.title}</p>
      </div>
    </div>
  ));

  return (
    <div className="dashboard-container">
      {loading && <div className="loading-spinner">Loading...</div>}
      <div className="stats-container">
        {renderStatCards()}
        <div className="stat-card security-card">
          <div className="security-content">
            <Shield className="shield-icon" />
            <h2>Backed by Security Team</h2>
            <p>{handoverToSecurityCount + transactionHandoverToSecurityCount} Verified handovers</p>
          </div>
        </div>
      </div>

      <div className="charts-container">
        <div className="chart-card">
          <h3>Monthly Activity Trends</h3>
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={monthlyData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis tickFormatter={(value) => Math.floor(value)} /> {/* Ensure Y-axis values are integers */}
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="lost" stroke="#ff6b6b" name="Lost Items" />
              <Line type="monotone" dataKey="found" stroke="#69db7c" name="Found Items" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h3>Item Categories Distribution</h3>
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              <Pie
                data={categoryData}
                cx="50%"
                cy="50%"                  // Slightly move the chart up to give legend room
                labelLine={true}
                outerRadius={80}
                innerRadius={40}
                fill="#8884d8"
                dataKey="value"
                label={({ name, percent }) => `${(percent * 100).toFixed(0)}%`}
              >
                {categoryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={generateCategoryColors(categoryData)[index]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend layout="horizontal" verticalAlign="bottom" align="center" />
            </PieChart>
          </ResponsiveContainer>


        </div>

        <div className="chart-card">
          <h3>Top Locations with Most Reports</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={locationData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis tickFormatter={(value) => Math.floor(value)} /> {/* Ensure Y-axis values are integers */}
              <Tooltip />
              <Bar dataKey="value" fill="#4dabf7" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h3>Claim Status Distribution</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={claimStatusData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis tickFormatter={(value) => Math.floor(value)} /> {/* Ensure Y-axis values are integers */}
              <Tooltip />
              <Legend />
              <Bar dataKey="value" fill="#4dabf7" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>

  );
};

export default Dashboard;
