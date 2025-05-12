import React, { useState, useRef, useEffect } from 'react';
import '../styles/ProfileDropdown.css'; // Import the updated CSS file
import { FaUserCircle } from 'react-icons/fa';

function ProfileDropdown({ onEditProfile, onLogout }) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="profile-container" ref={dropdownRef}>
  <FaUserCircle
    className="profile-icon"
    title="My Profile"
    onClick={toggleDropdown}
  />
  {isOpen && (
    <div className="profile-dropdown">
      <div className="dropdown-item" onClick={onEditProfile}>
        Edit Profile
      </div>
      <div className="dropdown-item" onClick={onLogout}>
        Logout
      </div>
    </div>
  )}
</div>

  );
}

export default ProfileDropdown;