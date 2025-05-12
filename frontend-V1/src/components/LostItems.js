import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../styles/LostItems.css';
import { HandoverModal } from './HandoverModal';
import { FiFilter } from 'react-icons/fi';

const LostItems = () => {
  const [items, setItems] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterOpen, setFilterOpen] = useState(false);
  const [filters, setFilters] = useState({
    category: '',
    location: '',
    date: ''
  });
  const [customLocation, setCustomLocation] = useState('');
  const [customCategory, setCustomCategory] = useState('');
  const [categories, setCategories] = useState([]);

  const user = JSON.parse(localStorage.getItem("user"));
  const requesterId = user?.id;

  const [selectedItem, setSelectedItem] = useState(null);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const fetchLostItems = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/items/lost-items');
        const data = response.data.map(item => ({
          id: item.itemId,
          itemName: item.itemName,
          description: item.description,
          location: item.location,
          categoryName: item.category?.categoryName || 'Uncategorized',
          dateReported: item.dateReported,
          dateLostOrFound: item.dateLostOrFound,
          imageUrl: item.imageUrl,
          itemStatus: item.itemStatus
        }));
        const sortedData = data.sort((a, b) => new Date(b.dateReported) - new Date(a.dateReported));
        setItems(sortedData);
      } catch (error) {
        console.error('Error fetching lost items:', error);
      }
    };

    const fetchCategories = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/admin/categories');
        setCategories(response.data);
      } catch (error) {
        console.error('Error fetching categories:', error);
      }
    };

    fetchLostItems();
    fetchCategories();
  }, []);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    if (name === 'location' && value !== 'Others') {
      setCustomLocation('');
    }
    if (name === 'category' && value !== 'Others') {
      setCustomCategory('');
    }
    setFilters({ ...filters, [name]: value });
  };

  const applyFilters = () => {
    setFilterOpen(false);
  };

  const handleHandoverClick = (item) => {
    setSelectedItem(item);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setSelectedItem(null);
    setShowModal(false);
  };

  const normalize = str => str?.toLowerCase().replace(/\s+/g, '');

  const filteredItems = items.filter((item) => {
    const selectedLocation = filters.location === 'Others' ? customLocation : filters.location;
    const selectedCategory = filters.category === 'Others' ? customCategory : filters.category;

    const matchesSearch = normalize(item.itemName).includes(normalize(searchQuery));
    const matchesCategory = !selectedCategory || normalize(item.categoryName) === normalize(selectedCategory);
    const matchesLocation = !selectedLocation || normalize(item.location) === normalize(selectedLocation);
    const matchesDate = !filters.date || item.dateReported.startsWith(filters.date);

    return matchesSearch && matchesCategory && matchesLocation && matchesDate;
  });

  return (
    <div className="items-page">
      <h1 className="title">Lost Item Reports</h1>

      <div className="search-container">
        <input
          type="text"
          placeholder="Search lost items..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="search-input"
        />
        <button
          onClick={() => setFilterOpen(!filterOpen)}
          className="filter-icon-button"
          title="Toggle filters"
        >
          <FiFilter className="filter-icon" />
        </button>
      </div>

      {filterOpen && (
        <div className="filters-inline">
          {/* Category Filter */}
          <select name="category" value={filters.category} onChange={handleFilterChange}>
            <option value="">Category</option>
            {categories.map((cat) => (
              <option key={cat.categoryId} value={cat.categoryName}>
                {cat.categoryName}
              </option>
            ))}
            <option value="Others">Others</option>
          </select>
          {filters.category === 'Others' && (
            <input
              type="text"
              placeholder="Enter custom category"
              value={customCategory}
              onChange={(e) => setCustomCategory(e.target.value)}
              className="custom-location-input"
              style={{ appearance: 'none', WebkitAppearance: 'none' }}
            />
          )}

          {/* Location Filter */}
          <select name="location" value={filters.location} onChange={handleFilterChange}>
            <option value="">Select location</option>
            <option value="Cafeteria">Cafeteria</option>
            <option value="Lobby">Lobby</option>
            <option value="Meeting room">Meeting room</option>
            <option value="Washroom">Washroom</option>
            <option value="Playing zone">Playing zone</option>
            <option value="Conference room">Conference room</option>
            <option value="Others">Others</option>
          </select>
          {filters.location === 'Others' && (
            <input
              type="text"
              placeholder="Enter custom location"
              value={customLocation}
              onChange={(e) => setCustomLocation(e.target.value)}
              className="custom-location-input"
              style={{ appearance: 'none', WebkitAppearance: 'none' }}
            />
          )}

          {/* Date Filter */}
          <input
            type="date"
            name="date"
            value={filters.date}
            onChange={handleFilterChange}
          />
          
        </div>
      )}

      {/* Item Cards */}
      {filteredItems.length > 0 ? (
        <div className="grid-container">
          {filteredItems.slice(0, 10).map((item, index) => (
            <div className="flip-card" key={index}>
              <div className="flip-card-inner">
                <div className="flip-card-front">
                  {item.imageUrl && (
                    <img src={item.imageUrl} alt={item.itemName} className="card-image" />
                  )}
                  <h2 className="card-title">{item.itemName}</h2>
                </div>
                <div className="flip-card-back">
                  <table className="item-details-table">
                    <tbody>
                      <tr>
                        <td><strong>Category</strong></td>
                        <td>{item.categoryName}</td>
                      </tr>
                      <tr>
                        <td><strong>Location</strong></td>
                        <td>{item.location}</td>
                      </tr>
                      <tr>
                        <td><strong>Date</strong></td>
                        <td>{new Date(item.dateReported).toLocaleDateString()}</td>
                      </tr>
                      <tr>
                        <td><strong>Time</strong></td>
                        <td>{new Date(item.dateReported).toLocaleTimeString()}</td>
                      </tr>
                      <tr>
                        <td colSpan="2">
                          <strong>Description:</strong><br />
                          <div className="scrollable-description">{item.description}</div>
                        </td>
                      </tr>

                      <tr>
                        <td colSpan="2" style={{ textAlign: 'center' }}>
                          <button className="handover-button" onClick={() => handleHandoverClick(item)}>
                            Handover
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="no-results">No items match your search or filters.</p>
      )}

      {/* Handover Modal */}
      {showModal && selectedItem && (
        <HandoverModal
          isOpen={showModal}
          onClose={handleCloseModal}
          onSubmit={handleCloseModal}
          itemId={selectedItem.id}
          requesterId={requesterId}
        />
      )}
    </div>
  );
};

export default LostItems;
