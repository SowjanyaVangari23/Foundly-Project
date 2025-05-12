import React, { useState, useEffect } from 'react';
import '../styles/CategoryManagement.css';

function CategoryManagement() {
  const [categories, setCategories] = useState([]);
  const [newCategory, setNewCategory] = useState({ name: '' });
  const [editCategory, setEditCategory] = useState({ id: null, name: '' });

  useEffect(() => {
    fetch('http://localhost:8080/api/admin/categories')
      .then(res => res.json())
      .then(data => setCategories(data))
      .catch(error => console.error('Error fetching categories:', error));
  }, []);

 // When adding a new category
const handleSubmit = (e) => {
    e.preventDefault();
    fetch('http://localhost:8080/api/admin/categories', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ categoryName: newCategory.name }) // Match backend field name
    })
    .then(res => res.json())
    .then(category => {
      setCategories([...categories, category]);
      setNewCategory({ name: '' });
    });
  };
  
  // When updating a category
  const handleUpdate = (id) => {
    fetch(`http://localhost:8080/api/admin/categories/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ categoryName: editCategory.name }) // Match backend field name
    })
    .then(res => res.json())
    .then(updatedCategory => {
      setCategories(categories.map(cat => 
        cat.categoryId === updatedCategory.categoryId ? updatedCategory : cat
      ));
      setEditCategory({ id: null, name: '' });
    });
  };

  const handleDelete = (id) => {
    fetch(`http://localhost:8080/api/admin/categories/${id}`, { method: 'DELETE' })
      .then(() => setCategories(categories.filter(cat => cat.categoryId !== id)));
  };

  return (
    <div>
      <h2>Category Management</h2>
      <form onSubmit={handleSubmit}>
        <input 
          type="text" 
          placeholder="Category Name" 
          value={newCategory.name} 
          onChange={(e) => setNewCategory({ name: e.target.value })} 
        />
        <button type="submit">Add Category</button>
      </form>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {categories.map(category => (
            <tr key={category.categoryId}>
              <td>{category.categoryId}</td>
              <td>
                {editCategory.id === category.categoryId ? (
                  <input 
                    type="text" 
                    value={editCategory.name} 
                    onChange={(e) => setEditCategory({ ...editCategory, name: e.target.value })} 
                  />
                ) : (
                  category.categoryName
                )}
              </td>
              <td>
                {editCategory.id === category.categoryId ? (
                  <button onClick={() => handleUpdate(category.categoryId)}>Update</button>
                ) : (
                  <button onClick={() => setEditCategory({ id: category.categoryId, name: category.categoryName })}>
                    Edit
                  </button>
                )}
                <button onClick={() => handleDelete(category.categoryId)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default CategoryManagement;

