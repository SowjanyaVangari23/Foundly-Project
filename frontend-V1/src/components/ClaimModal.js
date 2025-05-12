import React, { useState, useEffect, useRef } from 'react';
import '../styles/ClaimModal.css';
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";


const ClaimModal = ({ itemId, requesterId: initialRequesterId, onClose }) => {
  const [requesterId, setRequesterId] = useState(initialRequesterId || '');
  const [employeeId, setEmployeeId] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [photoFile, setPhotoFile] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const modalRef = useRef(null);

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (!initialRequesterId && user?.userId) setRequesterId(user.userId);
    else setRequesterId(initialRequesterId);

    if (user?.employeeId) setEmployeeId(user.employeeId);
    if (user?.name) setName(user.name);

    const handleOutsideClick = (e) => {
      if (modalRef.current && !modalRef.current.contains(e.target)) {
        onClose();
      }
    };

    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, [initialRequesterId, onClose]);

  const convertToBase64 = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const base64Photo = await convertToBase64(photoFile);
      const claimData = {
        requesterId,
        employeeId,
        name,
        photo: base64Photo,
        description,
        itemId,
      };

      const response = await fetch('http://localhost:8080/api/transactions/claim', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(claimData),
      });

      if (!response.ok) throw new Error('Failed to submit claim');
      toast.success('Claim submitted successfully!');
      onClose();
    } catch (err) {
      console.error('Submission error:', err);
      toast.error('Failed to submit claim. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="claim-modal-overlay">
      <div className="claim-modal" ref={modalRef}>
        <div className="modal-header">
          <h2>Claim</h2>
        </div>
        <form onSubmit={handleSubmit}>

          <div className="form-group">
            <label>Employee ID</label>
            <input
              type="text"
              value={employeeId}
              onChange={(e) => setEmployeeId(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Upload Proof Image</label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setPhotoFile(e.target.files[0])}
              required
            />
          </div>

          <div className="claim-modal-buttons">
            <button type="button" onClick={onClose} className="cancel-btn">
              Cancel
            </button>
            <button type="submit" disabled={isSubmitting} className="submit-btn">
              {isSubmitting ? 'Submitting...' : 'Submit'}
            </button>
          </div>
        </form>
      </div>
      <ToastContainer position="top-center" autoClose={3000} />
    </div>
  );
};

export default ClaimModal;