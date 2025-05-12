import React, { useEffect, useState } from "react";
import axios from "axios";
import ActivityCard from "./ActivityCard";
import "../styles/MyActivity.css";

const filters = [
  { label: "Lost Reports", value: "LOST" },
  { label: "Found Reports", value: "FOUND" },
  { label: "Claims", value: "CLAIM" },
  { label: "Handovers", value: "HANDOVER" },
];

const MyActivity = () => {
  const [selectedFilter, setSelectedFilter] = useState("LOST");
  const [allActivityData, setAllActivityData] = useState({
    LOST: [],
    FOUND: [],
    CLAIM: [],
    HANDOVER: [],
  });
  const [loading, setLoading] = useState(true);
  const [userId, setUserId] = useState(null);

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (user?.userId) {
      setUserId(user.userId);
    } else {
      console.warn("User object or userId is not found in localStorage");
    }
  }, []);

  const fetchAllData = async () => {
    if (!userId) return;
    setLoading(true);

    const endpoints = {
      LOST: `http://localhost:8080/api/items/lost/user/${userId}`,
      FOUND: `http://localhost:8080/api/items/found/user/${userId}`,
      CLAIM: `http://localhost:8080/api/transactions/claims/${userId}`,
      HANDOVER: `http://localhost:8080/api/transactions/handovers/${userId}`,
    };

    const updatedData = {};

    for (const key of Object.keys(endpoints)) {
      try {
        const res = await axios.get(endpoints[key]);
        updatedData[key] = res.data.map((item) => ({
          ...item,
          itemType: key,
        }));
      } catch (error) {
        console.error(`Error fetching ${key} data:`, error);
        updatedData[key] = [];
      }
    }

    setAllActivityData(updatedData);
    setLoading(false);
  };

  useEffect(() => {
    fetchAllData();
  }, [userId]);

  const currentData = allActivityData[selectedFilter] || [];

  return (
    <div className="my-activity-container">
      <h1 className="activity-heading">My Activity</h1>

      {/* Filter Buttons */}
      <div className="filter-buttons">
        {filters.map((filter) => (
          <button
            key={filter.value}
            className={`filter-button ${selectedFilter === filter.value ? "active" : ""}`}
            onClick={() => setSelectedFilter(filter.value)}
          >
            {filter.label}
            <span className="count-badge">{allActivityData[filter.value]?.length || 0}</span>
          </button>
        ))}
      </div>

      {/* Activity Cards */}
      <div className="activity-cards-container">
          {loading ? (
          <p className="loading-message">Loading...</p>
        ) : currentData.length > 0 ? (
          currentData.map((item) => (
            <ActivityCard
              key={item.transactionId ?? item.id ?? item.itemId ?? Math.random()}
              item={item}
              onTransactionUpdate={fetchAllData}  // Pass fetchAllData to refresh the list after update
            />
          ))
        ) : (
          <p className="no-data">No items found for this category.</p>
        )}
      </div>
    </div>
  );
};

export default MyActivity;
