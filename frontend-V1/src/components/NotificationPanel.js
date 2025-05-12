import React, { useState, useEffect } from 'react';
import '../styles/NotificationPanel.css';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";


function NotificationPanel() {
  const { currentUser } = useAuth();
  const [open, setOpen] = useState(false);
  const [selectedNote, setSelectedNote] = useState(null);
  const [activeTab, setActiveTab] = useState('all');
  const [notificationData, setNotificationData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!currentUser?.userId) {
      setError('User not logged in.');
      setLoading(false);
      return;
    }
  
    let intervalId;
  
    const fetchNotifications = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/transactions/notifications/${currentUser.userId}`);
        setNotificationData(response.data);
        setError(null);
      } catch (err) {
        console.error('Error fetching notifications:', err);
        setError('Could not load notifications.');
      } finally {
        setLoading(false);
      }
    };
  
    fetchNotifications();
  
    // Add this polling interval to fetch notifications every 15 seconds
    intervalId = setInterval(fetchNotifications, 15000);
  
    // Cleanup function to clear interval on unmount
    return () => clearInterval(intervalId);
  }, [currentUser]);

  const openDetails = (note) => setSelectedNote(note);
  const closeDetails = () => setSelectedNote(null);

  const handleCompletedClick = async () => {
    if (!selectedNote) return;

    try {
      const response = await axios.put(
        `http://localhost:8080/api/transactions/reporter-completed/${selectedNote.transactionId}`
      );

      if (response.data) {
        toast.success('Transaction marked as completed.');
        setSelectedNote((prev) => ({
          ...prev,
          transactionStatus: 'COMPLETED',
          reporterCompleted: true, // ✅ Update locally
        }));

        // Update the notification list
        setNotificationData((prev) =>
          prev.map((n) =>
            n.transactionId === selectedNote.transactionId
              ? { ...n, transactionStatus: 'COMPLETED', reporterCompleted: true }
              : n
          )
        );
      }
    } catch (error) {
      console.error("Error marking transaction as completed:", error);
      toast.error('Failed to mark as completed. Please try again.');
    }
  };

  const filteredNotifications = notificationData.filter((note) => {
    if (activeTab === 'all') return true;
    return note.type?.toLowerCase() === activeTab;
  });

  return (
    <div className="notification-wrapper">
      {/* Bell Button */}
      <button className="bell-icon" onClick={() => setOpen(true)}>
        <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C10.343 2 9 3.343 9 5V6.068C6.163 7.142 4 9.97 4 13V18L2 20V21H22V20L20 18V13C20 9.97 17.837 7.142 15 6.068V5C15 3.343 13.657 2 12 2ZM6 13C6 10.243 8.243 8 11 8H13C15.757 8 18 10.243 18 13V18H6V13ZM12 23C13.657 23 15 21.657 15 20H9C9 21.657 10.343 23 12 23Z" />
        </svg>
        {notificationData.some(note => note.transactionStatus !== 'COMPLETED') && <span className="dot" />}
      </button>

      {/* Notification Panel */}
      {open && (
        <>
          <div className="panel open">
            <div className="panel-header">
              <h2>Notifications</h2>
              <button onClick={() => setOpen(false)} className="close-btn">✖</button>
            </div>

            {/* Tabs */}
            <div className="tabs">
              {['all', 'claim', 'handover'].map((tab) => (
                <button
                  key={tab}
                  className={activeTab === tab ? 'active-tab' : ''}
                  onClick={() => setActiveTab(tab)}
                >
                  {tab.charAt(0).toUpperCase() + tab.slice(1)}
                </button>
              ))}
            </div>

            {/* Notification Cards */}
            <div className="notifications-list">
              {loading && <p>Loading notifications...</p>}
              {error && <p className="error">{error}</p>}
              {!loading && !error && filteredNotifications.length === 0 && (
                <p>No notifications found.</p>
              )}
              {!loading && !error && filteredNotifications.map((note, index) => (
                <div key={note.transactionId || index} className="notification-card">
                  <h3>{note.title || 'Notification'}</h3>
                  <p>{note.message}</p>
                  <div className="card-footer">
                    <button className="view-btn" onClick={() => openDetails(note)}>View Details</button>
                    <span className="time">{note.time}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Overlay */}
          <div className="overlay" onClick={() => setOpen(false)} />
        </>
      )}

      {/* Modal for Notification Details */}
{selectedNote && (
  <div className="modal-overlay" onClick={closeDetails}>
    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
      <h2>{selectedNote.title || 'Notification Details'}</h2>

      <div className="modal-body">
        <table className="details-table">
          <tbody>
            {selectedNote.description && (
              <tr>
                <td><strong>Description:</strong></td>
                <td>{selectedNote.description}</td>
              </tr>
            )}

            {selectedNote.type?.toLowerCase() === 'handover' && (
              <>
                {selectedNote.itemStatus && (
                  <tr>
                    <td><strong>Item Status:</strong></td>
                    <td>{selectedNote.itemStatus}</td>
                  </tr>
                )}
                {selectedNote.pickupMessage && (
                  <tr>
                    <td><strong>Pickup Message:</strong></td>
                    <td>{selectedNote.pickupMessage}</td>
                  </tr>
                )}
                {selectedNote.securityId && (
                  <tr>
                    <td><strong>Security ID:</strong></td>
                    <td>{selectedNote.securityId}</td>
                  </tr>
                )}
                {selectedNote.securityName && (
                  <tr>
                    <td><strong>Security Name:</strong></td>
                    <td>{selectedNote.securityName}</td>
                  </tr>
                )}
              </>
            )}

            {selectedNote.type?.toLowerCase() === 'claim' && (
              <>
                {selectedNote.employeeId && (
                  <tr>
                    <td><strong>Employee ID:</strong></td>
                    <td>{selectedNote.employeeId}</td>
                  </tr>
                )}
                {selectedNote.employeeName && (
                  <tr>
                    <td><strong>Employee Name:</strong></td>
                    <td>{selectedNote.employeeName}</td>
                  </tr>
                )}
              </>
            )}
          </tbody>
        </table>

        {selectedNote.photo && (
          <div className="proof-image-container">
            <img src={selectedNote.photo} alt="Proof" className="proof-image" />
          </div>
        )}
      </div>

      {/* Mark Completed Button */}
      {(selectedNote.type?.toLowerCase() === 'claim' || selectedNote.type?.toLowerCase() === 'handover') && (
        <button
          className="collected-btn"
          onClick={handleCompletedClick}
          disabled={selectedNote.transactionStatus === 'COMPLETED' || selectedNote.reporterCompleted}
        >
          {(selectedNote.transactionStatus === 'COMPLETED' || selectedNote.reporterCompleted)
            ? 'Completed'
            : 'Mark as Completed'}
        </button>
      )}

      <button className="view-btn" onClick={closeDetails}>Close</button>
    </div>
  </div>
)}

    <ToastContainer position="top-center" autoClose={3000} />
    </div>
  );
}
export default NotificationPanel;
