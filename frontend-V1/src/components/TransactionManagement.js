import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";


function TransactionManagement() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchTransactions = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/transactions');
      setTransactions(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to fetch transactions');
      setLoading(false);
    }
  };

  const deleteTransaction = async (id) => {
    if (!window.confirm('Are you sure you want to delete this transaction?')) return;
    try {
      await axios.delete(`http://localhost:8080/api/transactions/${id}`);
      setTransactions(transactions.filter(tx => tx.transactionId !== id));
    } catch (err) {
      toast.error('Failed to delete transaction');
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  if (loading) return <p>Loading transactions...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>
      <h2>Transaction Management</h2>
      {transactions.length === 0 ? (
        <p>No transactions found.</p>
      ) : (
        <table border="1" cellPadding="5" cellSpacing="0">
          <thead>
            <tr>
              <th>ID</th>
              <th>Item Name</th>
              <th>Requester Name</th>
              <th>Employee ID</th>
              <th>Description</th>
              <th>Status</th>
              <th>Type</th>
              <th>Date Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map(tx => (
              <tr key={tx.transactionId}>
                <td>{tx.transactionId}</td>
                <td>{tx.item?.itemName}</td>
                <td>{tx.requesterName}</td>
                <td>{tx.employeeId}</td>
                <td>{tx.description}</td>
                <td>{tx.transactionStatus}</td>
                <td>{tx.transactionType}</td>
                <td>{new Date(tx.dateUpdated).toLocaleString()}</td>
                <td>
                  <button onClick={() => deleteTransaction(tx.transactionId)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <ToastContainer position="top-center" autoClose={3000} />
    </div>
  );
}

export default TransactionManagement;
