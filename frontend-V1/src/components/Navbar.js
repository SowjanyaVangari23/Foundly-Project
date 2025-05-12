import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import '../styles/Navbar.css';
import { FaUserCircle } from 'react-icons/fa';
import { useAuth } from '../context/AuthContext';
import NotificationPanel from './NotificationPanel';
import ProfileDropdown from './ProfileDropdown';
import logo from '../assets/foundlylogo.png';

function Navbar({ onAboutClick, activeSection }) {
  const { isLoggedIn, isAdmin, setShowAuthBox, handleLogout } = useAuth();
  const [showDropdown, setShowDropdown] = useState(false);
  const [activeTab, setActiveTab] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Update active tab based on URL + section
    if (location.pathname === '/dashboard') {
      setActiveTab('dashboard');
    } else if (location.pathname === '/admin/dashboard') {
      setActiveTab('admin-dashboard');
    } else if (location.pathname === '/') {
      if (activeSection === 'about') {
        setActiveTab('about');
      } else {
        setActiveTab('home');
      }
    } else {
      setActiveTab('');
    }
  }, [location.pathname, activeSection]);

  const handleHomeClick = () => {
    if (location.pathname !== '/') {
      navigate('/');
    } else {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
    setActiveTab('home');
  };

  const handleDashboardClick = () => {
    navigate('/dashboard');
    setActiveTab('dashboard');
  };

  const handleAdminDashboardClick = () => {
    navigate('/admin/dashboard');
    setActiveTab('admin-dashboard');
  };

  const handleAboutClick = () => {
    setActiveTab('about');
    if (location.pathname === '/') {
      onAboutClick?.();
    } else {
      navigate('/', { state: { scrollToAbout: true } });
    }
  };

  const handleEditProfile = () => {
    navigate('/edit-profile');
    setShowDropdown(false);
  };

  const onLogoutClick = () => {
    handleLogout();
    navigate('/');
    setShowDropdown(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-logo" onClick={handleHomeClick}>
        <img src={logo} alt="Foundly Logo" className="navbar-logo-img" />
      </div>

      <ul className="navbar-links">
        <li className={`nav-item ${activeTab === 'home' ? 'active' : ''}`}>
          <button className="nav-link" onClick={handleHomeClick}>
            Home
          </button>
        </li>
        <li className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}>
          <button className="nav-link" onClick={handleDashboardClick}>
            Dashboard
          </button>
        </li>
        <li className={`nav-item ${activeTab === 'about' ? 'active' : ''}`}>
          <button className="nav-link" onClick={handleAboutClick}>
            About Us
          </button>
        </li>
        {isLoggedIn && isAdmin && (
          <li className={`nav-item ${activeTab === 'admin-dashboard' ? 'active' : ''}`}>
            <button className="nav-link" onClick={handleAdminDashboardClick}>
              Admin Dashboard
            </button>
          </li>
        )}
      </ul>

      <div className="nav-right-section">
        {isLoggedIn && <NotificationPanel />}

        <div className="auth-button-container">
          {isLoggedIn ? (
            <ProfileDropdown
              onEditProfile={handleEditProfile}
              onLogout={onLogoutClick}
            />
          ) : (
            <button className="navbar-auth-button" onClick={() => setShowAuthBox(true)}>
              Sign In / Sign Up
            </button>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
