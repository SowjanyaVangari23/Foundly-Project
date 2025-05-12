import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../styles/UserManagement.css';

function EmployeeManagement() {
  const [employees, setEmployees] = useState([]);
  const [formData, setFormData] = useState({
    empId: '',
    name: '',
    empEmailId: ''
  });
  const [isEditing, setIsEditing] = useState(false);
  const [editEmpId, setEditEmpId] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchEmployees();
  }, []);

  const fetchEmployees = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/admin/employees');
      setEmployees(response.data);
    } catch (error) {
      console.error('Error fetching employees:', error);
    }
  };

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (isEditing) {
        await axios.put(`http://localhost:8080/api/admin/employees/${editEmpId}`, formData);
        setIsEditing(false);
        setEditEmpId(null);
      } else {
        await axios.post('http://localhost:8080/api/admin/employees', formData);
      }
      setFormData({ empId: '', name: '', empEmailId: '' });
      fetchEmployees();
    } catch (error) {
      console.error('Error saving employee:', error);
      setError('Failed to save employee. Please check the details and try again.');
    }
  };

  const handleEdit = (employee) => {
    setFormData({
      empId: employee.empId,
      name: employee.name,
      empEmailId: employee.empEmailId
    });
    setIsEditing(true);
    setEditEmpId(employee.empId);
  };

  const handleDelete = async (empId) => {
    if (window.confirm('Are you sure you want to delete this employee?')) {
      try {
        await axios.delete(`http://localhost:8080/api/admin/employees/${empId}`);
        fetchEmployees();
      } catch (error) {
        console.error('Error deleting employee:', error);
        setError('Failed to delete employee.');
      }
    }
  };

  return (
    <div className="user-management-container">
      <h2>Employee Management</h2>
      {error && <p className="error-message">{error}</p>}
      <form onSubmit={handleSubmit} className="user-form">
        <input
          type="text"
          name="empId"
          placeholder="Employee ID"
          value={formData.empId}
          onChange={handleChange}
          required
          disabled={isEditing}
        />
        <input
          type="text"
          name="name"
          placeholder="Name"
          value={formData.name}
          onChange={handleChange}
          required
        />
        <input
          type="email"
          name="empEmailId"
          placeholder="Email"
          value={formData.empEmailId}
          onChange={handleChange}
          required
        />
        <button type="submit">{isEditing ? 'Update' : 'Add'} Employee</button>
        {isEditing && <button type="button" onClick={() => { setIsEditing(false); setFormData({ empId: '', name: '', empEmailId: '' }); }}>Cancel</button>}
      </form>
      <table className="user-table">
        <thead>
          <tr>
            <th>Employee ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {employees.map(emp => (
            <tr key={emp.empId}>
              <td>{emp.empId}</td>
              <td>{emp.name}</td>
              <td>{emp.empEmailId}</td>
              <td>
                <button onClick={() => handleEdit(emp)}>Edit</button>
                <button onClick={() => handleDelete(emp.empId)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default EmployeeManagement;
