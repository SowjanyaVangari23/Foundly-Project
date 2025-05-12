import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../styles/EditProfile.css';
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";


const EditProfile = () => {
  const userFromStorage = JSON.parse(localStorage.getItem('user'));
  const userId = userFromStorage?.userId;

  const [formData, setFormData] = useState({
    userId: '',
    employeeId: '',
    name: '',
    email: '',
    username: '',
    oldPassword: '',
    newPassword: '',
  });

  useEffect(() => {
    if (!userId) return;

    axios.get(`http://localhost:8080/api/users/profile/${userId}`)
      .then(response => {
        const user = response.data;
        setFormData(prev => ({
          ...prev,
          userId: user.userId || '',
          employeeId: user.employeeId || '',
          name: user.name || '',
          email: user.email || '',
          username: user.username || '',
        }));
      })
      .catch(error => {
        console.error('Failed to fetch user profile:', error);
      });
  }, [userId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleUpdate = () => {
    axios.put('http://localhost:8080/api/users/profile/update', formData)
      .then(response => {
        toast.success('Profile updated successfully!');
      })
      .catch(error => {
        console.error('Failed to update profile:', error);
        toast.error(error.response?.data?.message || 'Update failed!');
      });
  };
  document.querySelector('.profile-icon').addEventListener('mousedown', function(e) {
    e.preventDefault();
  });

  return (
    <div className="edit-profile-container">
      <div className="edit-profile-content">
        <h2>Edit Profile</h2>

        <input
          className="input-field"
          type="text"
          name="userId"
          value={formData.userId}
          disabled
          placeholder="User ID"
        />
        <input
          className="input-field"
          type="text"
          name="employeeId"
          value={formData.employeeId}
          disabled
          placeholder="Employee ID"
        />
        <input
          className="input-field"
          type="text"
          name="name"
          value={formData.name}
          onChange={handleChange}
          placeholder="Enter your full name"
        />
        <input
          className="input-field"
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          placeholder="Enter your email"
        />
        <input
          className="input-field"
          type="text"
          name="username"
          value={formData.username}
          onChange={handleChange}
          placeholder="Enter your username"
        />
        <input
          className="input-field"
          type="password"
          name="oldPassword"
          value={formData.oldPassword}
          onChange={handleChange}
          placeholder="Enter current password"
        />
        <input
          className="input-field"
          type="password"
          name="newPassword"
          value={formData.newPassword}
          onChange={handleChange}
          placeholder="Enter new password (leave blank to keep current)"
        />

        <div className="button-group">
          <button
            type="button"
            className="cancel-button"
            onClick={() => window.history.back()}
          >
            Cancel
          </button>
          <button
            type="button"
            className="update-button"
            onClick={handleUpdate}
          >
            Update
          </button>
        </div>
      </div>
      <ToastContainer position="top-center" autoClose={3000} />
    </div>
  );
};

export default EditProfile;
