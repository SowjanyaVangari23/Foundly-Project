import React, { useState, useEffect } from 'react';
import '../styles/UserManagement.css'
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";


function UserManagement() {
  const [users, setUsers] = useState([]);
  const [newUser, setNewUser] = useState({
    employeeId: '',
    name: '',
    email: '',
    username: '',
    password: '',
    role: 'USER',
    security: false  // Changed from isSecurity to security
  });
  const [editingUser, setEditingUser] = useState(null);

  // Fetch all users
  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = () => {
    fetch('http://localhost:8080/api/admin/users')
      .then(res => res.json())
      .then(data => setUsers(data))
      .catch(error => console.error('Error fetching users:', error));
  };

  // Add new user
  const handleSubmit = (e) => {
    e.preventDefault();
    fetch('http://localhost:8080/api/admin/users', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newUser)
    })
    .then(res => {
      if (!res.ok) throw new Error('Failed to add user');
      return res.json();
    })
    .then(user => {
      setUsers([...users, user]);
      setNewUser({
        employeeId: '',
        name: '',
        email: '',
        username: '',
        password: '',
        role: 'USER',
        isSecurity: '',
      });
    })
    .catch(error => toast.error(error.message));
  };

  // Edit user
  const handleEdit = (user) => {
    setEditingUser({...user});
  };

  // Update user
  const handleUpdate = (e) => {
    e.preventDefault();
    
    console.log('Updating user with data:', {...editingUser});
    
    fetch(`http://localhost:8080/api/admin/users/${editingUser.userId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editingUser)  // No need for extra mapping since we're using the same field name
    })
    .then(res => {
      if (!res.ok) {
        return res.text().then(text => { throw new Error(text) });
      }
      return res.json();
    })
    .then(updatedUser => {
      console.log('Received updated user:', updatedUser);
      setUsers(users.map(u => u.userId === updatedUser.userId ? updatedUser : u));
      setEditingUser(null);
    })
    .catch(error => {
      console.error('Error updating user:', error);
      toast.error('Error updating user: ' + error.message);
    });
  };
  // Delete user
  const handleDelete = (userId) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      fetch(`http://localhost:8080/api/admin/users/${userId}`, { method: 'DELETE' })
        .then(res => {
          if (!res.ok) throw new Error('Failed to delete user');
          setUsers(users.filter(u => u.userId !== userId));
        })
        .catch(error => toast.error(error.message));
    }
  };

  // Promote user to admin
  const handlePromote = (userId) => {
    fetch(`http://localhost:8080/api/admin/users/${userId}/promote`, { method: 'POST' })
      .then(res => {
        if (!res.ok) throw new Error('Failed to promote user');
        return res.json();
      })
      .then(updatedUser => {
        setUsers(users.map(u => u.userId === updatedUser.userId ? updatedUser : u));
      })
      .catch(error => toast.error(error.message));
  };

  // Demote admin to user
  const handleDemote = (userId) => {
    fetch(`http://localhost:8080/api/admin/users/${userId}/demote`, { method: 'POST' })
      .then(res => {
        if (!res.ok) throw new Error('Failed to demote user');
        return res.json();
      })
      .then(updatedUser => {
        setUsers(users.map(u => u.userId === updatedUser.userId ? updatedUser : u));
      })
      .catch(error => toast.error(error.message));
  };

  return (
    <div>
      <h2>User Management</h2>
      
      {/* Add/Edit User Form */}
      <form onSubmit={editingUser ? handleUpdate : handleSubmit}>
        <input type="text" placeholder="Employee ID" 
               value={editingUser ? editingUser.employeeId : newUser.employeeId} 
               onChange={(e) => editingUser 
                 ? setEditingUser({...editingUser, employeeId: e.target.value}) 
                 : setNewUser({...newUser, employeeId: e.target.value})} 
               required />
               
        <input type="text" placeholder="Full Name" 
               value={editingUser ? editingUser.name : newUser.name} 
               onChange={(e) => editingUser 
                 ? setEditingUser({...editingUser, name: e.target.value}) 
                 : setNewUser({...newUser, name: e.target.value})} 
               required />
               
        <input type="email" placeholder="Email"
               value={editingUser ? editingUser.email : newUser.email} 
               onChange={(e) => editingUser 
                 ? setEditingUser({...editingUser, email: e.target.value}) 
                 : setNewUser({...newUser, email: e.target.value})} 
               required />
               
        <input type="text" placeholder="Username"
               value={editingUser ? editingUser.username : newUser.username} 
               onChange={(e) => editingUser 
                 ? setEditingUser({...editingUser, username: e.target.value}) 
                 : setNewUser({...newUser, username: e.target.value})} 
               required />
               
        {!editingUser && (
          <input type="password" placeholder="Password"
                 value={newUser.password} 
                 onChange={(e) => setNewUser({...newUser, password: e.target.value})} 
                 required />
        )}
               
        <select value={editingUser ? editingUser.role : newUser.role} 
                onChange={(e) => editingUser 
                  ? setEditingUser({...editingUser, role: e.target.value}) 
                  : setNewUser({...newUser, role: e.target.value})}>
          <option value="USER">User</option>
          <option value="ADMIN">Admin</option>
        </select>
        
        <label>
  <input type="checkbox" 
         checked={editingUser ? editingUser.security : newUser.security} 
         onChange={(e) => editingUser 
           ? setEditingUser({...editingUser, security: e.target.checked}) 
           : setNewUser({...newUser, security: e.target.checked})} />
  Security Access
</label>
        
        <button type="submit">
          {editingUser ? 'Update User' : 'Add User'}
        </button>
        {editingUser && (
          <button type="button" onClick={() => setEditingUser(null)}>
            Cancel
          </button>
        )}
      </form>

      {/* Users Table */}
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Employee ID</th>
            <th>Name</th>
            <th>Username</th>
            <th>Email</th>
            <th>Role</th>
            <th>Security</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => (
            <tr key={user.userId}>
              <td>{user.userId}</td>
              <td>{user.employeeId}</td>
              <td>{user.name}</td>
              <td>{user.username}</td>
              <td>{user.email}</td>
              <td>{user.role}</td>
              <td>{user.security ? 'Yes' : 'No'}</td>
              <td>
                <button onClick={() => handleEdit(user)}>Edit</button>
                {user.role === 'ADMIN' ? (
                  <button onClick={() => handleDemote(user.userId)}>Demote</button>
                ) : (
                  <button onClick={() => handlePromote(user.userId)}>Promote</button>
                )}
                <button onClick={() => handleDelete(user.userId)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <ToastContainer position="top-center" autoClose={3000} />
    </div>
  );
}

export default UserManagement;