// import React, { createContext, useState, useContext } from 'react';

// const AuthContext = createContext();

// export const useAuth = () => useContext(AuthContext);

// export const AuthProvider = ({ children }) => {
//   const [isLoggedIn, setIsLoggedIn] = useState(false);
//   const [showAuthBox, setShowAuthBox] = useState(false);

//   const handleAuthSuccess = () => {
//     setIsLoggedIn(true);
//     setShowAuthBox(false);
//   };

//   return (
//     <AuthContext.Provider value={{
//       isLoggedIn,
//       setIsLoggedIn,
//       showAuthBox,
//       setShowAuthBox,
//       handleAuthSuccess,
//     }}>
//       {children}
//     </AuthContext.Provider>
//   );
// };
import React, { createContext, useState, useEffect, useContext } from 'react';

const AuthContext = createContext();
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(
    () => localStorage.getItem('isLoggedIn') === 'true'
  );
  const [isAdmin, setIsAdmin] = useState(
    () => localStorage.getItem('isAdmin') === 'true'
  );
  const [showAuthBox, setShowAuthBox] = useState(false);
  const [currentUser, setCurrentUser] = useState(
    () => JSON.parse(localStorage.getItem('user')) || null
  );

  useEffect(() => {
    localStorage.setItem('isLoggedIn', isLoggedIn);
    localStorage.setItem('isAdmin', isAdmin);
    if (currentUser) {
      localStorage.setItem('user', JSON.stringify(currentUser));
    }
  }, [isLoggedIn, isAdmin, currentUser]);

  const handleAuthSuccess = (user) => {
    setIsLoggedIn(true);
    setIsAdmin(user.role === 'ADMIN');
    setCurrentUser(user);
    setShowAuthBox(false);
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setIsAdmin(false);
    setCurrentUser(null);
    setShowAuthBox(false);
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('isAdmin');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{
      isLoggedIn,
      isAdmin,
      currentUser,
      setIsLoggedIn,
      setIsAdmin,
      showAuthBox,
      setShowAuthBox,
      handleAuthSuccess,
      handleLogout
    }}>
      {children}
    </AuthContext.Provider>
  );
};


