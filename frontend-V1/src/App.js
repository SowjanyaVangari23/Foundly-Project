
import React from 'react';
import { Routes, Route, useLocation, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import ReportItem from './components/ReportItem';
import MyActivity from './components/MyActivity';
import HomePage from './components/HomePage';
import AboutUs from './components/AboutUs';
import Dashboard from './components/Dashboard';
import AuthBox from './components/AuthBox';
import AdminLogin from './components/AdminLogin';
import AdminDashboard from './components/AdminDashboard';
import UserManagement from './components/UserManagement';
import { useAuth } from './context/AuthContext';
import LostItems from './components/LostItems';
import FoundItems from './components/FoundItems';
import CategoryManagement from './components/CategoryManagement';
import EditProfile from './components/EditProfile';
import EmployeeManagement from './components/EmployeeManagement';
import TransactionManagement from './components/TransactionManagement';
import ItemReportsManagement from './components/ItemReportsManagement';
// import ForgotPasswordPage from './components/ForgotPasswordPage';



//import { LostItems } from './pages/LostItems';        
//import { FoundItemReports } from './pages/FoundItemReports';


import './styles/HomePage.css';

const RequireAuth = ({ children }) => {
  const { isLoggedIn, setShowAuthBox } = useAuth();
  const location = useLocation();

  if (isLoggedIn) {
    return children;
  } else {
    setTimeout(() => setShowAuthBox(true), 0);
    return <Navigate to="/" state={{ from: location }} replace />;
  }
};

const RequireAdminAuth = ({ children }) => {
  const { isLoggedIn, isAdmin, setShowAuthBox } = useAuth();
  const location = useLocation();

  if (isLoggedIn && isAdmin) {
    return children;
  } else {
    setTimeout(() => setShowAuthBox(true), 0);
    return <Navigate to="/" state={{ from: location }} replace />;
  }
};

const App = () => {
  const { showAuthBox, setShowAuthBox, handleAuthSuccess } = useAuth();

  return (
    <div className="App">
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/about" element={<AboutUs />} />
        <Route path="/edit-profile" element={<EditProfile />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/report" element={<RequireAuth><ReportItem /></RequireAuth>} />
        <Route path="/my-activity" element={<RequireAuth><MyActivity /></RequireAuth>} />
        <Route path="/lost-items" element={<RequireAuth><LostItems/></RequireAuth>} />
        <Route path="/found-items" element={<RequireAuth><FoundItems /></RequireAuth>} />
        {/* <Route path="/forgot-password" element={<ForgotPasswordPage />} /> */}
        {/* Admin Routes */}
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/admin/dashboard" element={<RequireAdminAuth><AdminDashboard /></RequireAdminAuth>} />
        <Route path="/users" element={<UserManagement />} />
        <Route path="/employees" element={<EmployeeManagement />} />
        <Route path="/categories" element={<CategoryManagement />} />
        <Route path="/transactions" element={<RequireAdminAuth><TransactionManagement /></RequireAdminAuth>} />
        <Route path="/item-reports" element={<RequireAdminAuth><ItemReportsManagement /></RequireAdminAuth>} />
       
       

        </Routes>

      <AuthBox
        isOpen={showAuthBox}
        onClose={() => setShowAuthBox(false)}
        onAuthSuccess={handleAuthSuccess}
      />
    </div>
  );
};

export default App;