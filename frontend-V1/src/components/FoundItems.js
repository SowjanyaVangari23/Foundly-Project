import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../styles/FoundItems.css';
import ClaimModal from './ClaimModal';
import { FiFilter } from 'react-icons/fi';

const FoundItems = () => {
  const [items, setItems] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterOpen, setFilterOpen] = useState(false);
  const [filters, setFilters] = useState({
    category: '',
    location: '',
    date: '',
  });
  const [customCategory, setCustomCategory] = useState('');
  const [customLocation, setCustomLocation] = useState('');
  const [categories, setCategories] = useState([]);
  const [locationOptions, setLocationOptions] = useState([
    'Cafeteria',
    'Lobby',
    'Meeting room',
    'Washroom',
    'Playing zone',
    'Conference room',
  ]);

  const user = JSON.parse(localStorage.getItem("user"));
  const requesterId = user?.requesterId;

  const [selectedItem, setSelectedItem] = useState(null);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const fetchFoundItems = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/items/found-items');
        const sortedItems = response.data.sort((a, b) => new Date(b.dateReported) - new Date(a.dateReported));
        setItems(sortedItems);
      } catch (error) {
        console.error('Error fetching found items:', error);
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

    fetchFoundItems();
    fetchCategories();
  }, []);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    if (name === 'category' && value !== 'Others') {
      setCustomCategory('');
    }
    if (name === 'location' && value !== 'Others') {
      setCustomLocation('');
    }
    setFilters({ ...filters, [name]: value });
  };

  const applyFilters = () => {
    setFilterOpen(false);
  };

  const handleClaimClick = (item) => {
    setSelectedItem(item);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setSelectedItem(null);
    setShowModal(false);
  };

  const normalize = (str) => str?.toLowerCase().replace(/\s+/g, '');

const filteredItems = items.filter((item) => {
  const selectedCategory = filters.category === 'Others' ? customCategory : filters.category;
  const selectedLocation = filters.location === 'Others' ? customLocation : filters.location;
  const matchesSearch = normalize(item.itemName).includes(normalize(searchQuery));
  const matchesCategory = !selectedCategory || normalize(item.category) === normalize(selectedCategory);
  const matchesLocation = !selectedLocation || normalize(item.location) === normalize(selectedLocation);
  const matchesDate = !filters.date || item.dateReported.startsWith(filters.date);
  return matchesSearch && matchesCategory && matchesLocation && matchesDate;
});


  return (
    <div className="items-page">
      <h1 className="title">Found Item Reports</h1>

      <div className="search-container">
        <input
          type="text"
          placeholder="Search found items..."
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
              className="custom-category-input"
            />
          )}

          {/* Location Filter */}
          <select
            name="location"
            value={filters.location}
            onChange={handleFilterChange}
            className="location-select"
          >
            <option value="">Select location</option>
            {locationOptions.map((location, index) => (
              <option key={index} value={location}>{location}</option>
            ))}
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

          {/* Date Picker */}
          <input
            type="date"
            name="date"
            value={filters.date}
            onChange={handleFilterChange}
          />
        </div>
      )}

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
                      <td>{item.category}</td>
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
                      <td colSpan="2"><strong>Description:</strong><br />{item.description}</td>
                    </tr>
                    <tr>
                      <td colSpan="2" style={{ textAlign: 'center' }}>
                        <button className="claim-button" onClick={() => handleClaimClick(item)}>
                          Claim
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

      {showModal && selectedItem && (
        <ClaimModal
          itemId={selectedItem.itemId}
          requesterId={requesterId}
          onClose={handleCloseModal}
        />
      )}
    </div>
  );
};

export default FoundItems;
