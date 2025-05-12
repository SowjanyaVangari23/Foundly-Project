import React, { useEffect, useState } from "react";
import axios from "axios";
import "../styles/LostItems.css"; // Use LostItems styles for consistent card styling
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";


const fallbackImage = 'https://via.placeholder.com/300x200';

const ActivityCard = ({ item, onTransactionUpdate }) => {
  const [collected, setCollected] = useState(item.requesterCompleted === true);
  const [status, setStatus] = useState(item.transactionStatus);
  const [handoverToSecurity, setHandoverToSecurity] = useState(item.handedOverToSecurity);

  useEffect(() => {
    setCollected(item.requesterCompleted === true);
    setStatus(item.transactionStatus);
    setHandoverToSecurity(item.handedOverToSecurity);
  }, [item]);

  const handleCollectedClick = async () => {
    try {
      const response = await axios.put(
        `http://localhost:8080/api/transactions/requester-completed/${item.transactionId}`
      );

      if (response.data) {
        setCollected(true);
        setStatus(response.data.transactionStatus || "PENDING_COMPLETION");

        if (onTransactionUpdate) {
          onTransactionUpdate(); // Refresh parent component
        }
      }
    } catch (error) {
      toast.error("Something went wrong. Please try again.");
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'Not Found': return 'status-missing';
      case 'With Security': return 'status-security';
      case 'Received': return 'status-received';
      case 'With Finder': return 'status-finder';
      case 'Requested':
      case 'PENDING':
      case 'PENDING_COMPLETION': return 'status-pending';
      case 'COMPLETED': return 'status-completed';
      default: return 'status-missing';
    }
  };

  const getBorderClass = (type) => {
    switch (type?.toUpperCase()) {
      case 'LOST': return 'border-lost';
      case 'FOUND': return 'border-found';
      case 'CLAIM': return 'border-claim';
      case 'HANDOVER': return 'border-handover';
      default: return '';
    }
  };

  const shouldDisableButton = collected || status === 'COMPLETED' || (handoverToSecurity && status !== 'COMPLETED');

  const showStatus = item.itemType === 'CLAIM' || item.itemType === 'HANDOVER';

  return (
    <div className={`flip-card ${getBorderClass(item.itemType)}`}>
      <div className="flip-card-inner">

        {/* Front Side */}
        <div className="flip-card-front">
          <img 
            src={item.photo || item.imageUrl || fallbackImage} 
            alt="Activity Item" 
            className="card-image"
          />
          <h2 className="card-title">{item.itemName}</h2>
          
          {showStatus && (
            <div className={`status-tag ${getStatusClass(status)}`}>
              {status}
            </div>
          )}
        </div>

        {/* Back Side */}
        <div className="flip-card-back">
          <table className="item-details-table">
            <tbody>
              <tr>
                <td><strong>Category</strong></td>
                <td>{item.category}</td>
              </tr>
              <tr>
                <td><strong>Date</strong></td>
                <td>{item.dateReported}</td>
              </tr>
              {item.description && (
                <tr>
                  <td colSpan="2"><strong>Description:</strong><br />{item.description}</td>
                </tr>
              )}
              {item.securityName && item.securityId && (
                <tr>
                  <td><strong>Security</strong></td>
                  <td>{item.securityName} ({item.securityId})</td>
                </tr>
              )}
              {item.pickupMessage && (
                <tr>
                  <td><strong>Pickup Message</strong></td>
                  <td>{item.pickupMessage}</td>
                </tr>
              )}
              <tr>
                <td colSpan="2" style={{ textAlign: 'center' }}>
                  {(item.itemType === 'CLAIM' || item.itemType === 'HANDOVER') && (
                    <button
                      className={`claim-button ${shouldDisableButton ? 'disabled' : 'active'}`}
                      onClick={handleCollectedClick}
                      disabled={shouldDisableButton}
                    >
                      {collected || status === 'COMPLETED' ? "Completed" : "Mark as Collected"}
                    </button>
                  )}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <ToastContainer position="top-center" autoClose={3000} />
    </div>
  );
};

export default ActivityCard;
