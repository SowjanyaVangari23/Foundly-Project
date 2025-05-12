import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "../styles/ReportItem.css";

const ReportItem = () => {
  const navigate = useNavigate();
  const [itemType, setItemType] = useState("Found");
  const [handoverOption, setHandoverOption] = useState("keep");
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [categories, setCategories] = useState([]);
  const [customLocation, setCustomLocation] = useState("");

  const [formData, setFormData] = useState({
    userId: "",
    name: "",
    location: "",
    categoryId: "",
    itemName: "",
    description: "",
    date: "",
    time: "",
    pickupMessage: "",
    securityId: "",
    securityName: "",
  });

  

  useEffect(() => {
    fetch("http://localhost:8080/api/admin/categories")
      .then((res) => res.json())
      .then((data) => setCategories(data))
      .catch((err) => console.error("Failed to fetch categories:", err));

    const userFromStorage = JSON.parse(localStorage.getItem("user"));
    if (userFromStorage?.userId && userFromStorage?.name) {
      setFormData((prev) => ({
        ...prev,
        userId: userFromStorage.userId.toString(),
        name: userFromStorage.name,
      }));
    }
  }, []);

  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);
    files.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setUploadedFiles((prev) => [...prev, { file, base64: reader.result }]);
      };
      reader.readAsDataURL(file);
    });
  };

  const removeFile = (index) => {
    setUploadedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "location") {
      setFormData((prev) => ({ ...prev, location: value }));
      if (value !== "Others") setCustomLocation("");
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  

  const handleSubmit = async () => {
    const dateTime = `${formData.date} ${formData.time}`;
    const locationToSend =
      formData.location === "Others" ? customLocation : formData.location;

    const payload = {
      userId: parseInt(formData.userId),
      name: formData.name,
      location: locationToSend,
      categoryId: parseInt(formData.categoryId),
      itemName: formData.itemName,
      description: formData.description,
      dateLostOrFound: dateTime,
      imageUrl: uploadedFiles.length ? uploadedFiles[0].base64 : "",
      handoverToSecurity: itemType === "Found" && handoverOption === "security",
      pickupMessage:
        itemType === "Found" && handoverOption === "keep"
          ? formData.pickupMessage
          : null,
      securityId:
        itemType === "Found" && handoverOption === "security"
          ? formData.securityId
          : null,
      securityName:
        itemType === "Found" && handoverOption === "security"
          ? formData.securityName
          : null,
    };

    const endpoint = itemType === "Found" ? "/api/items/found" : "/api/items/lost";

    try {
      const response = await fetch(`http://localhost:8080${endpoint}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        toast.success("Item reported successfully!");
        setFormData({
          userId: "",
          name: "",
          location: "",
          categoryId: "",
          itemName: "",
          description: "",
          date: "",
          time: "",
          pickupMessage: "",
          securityId: "",
          securityName: "",
        });
        setUploadedFiles([]);
        setItemType("Found");
        setHandoverOption("keep");
        setCustomLocation("");
        return true;
      } else {
        toast.error("Failed to report item.");
        return false;
      }
    } catch (error) {
      console.error("Error reporting item:", error);
      toast.error("Something went wrong!");
      return false;
    }
  };

  return (
    <div className="report-container">
      <div className="report-box">
        <h2 className="report-title">Report Item</h2>

        <div className="toggle-container">
          <input
            type="checkbox"
            id="toggleSwitch"
            className="toggle-input"
            checked={itemType === "Found"}
            onChange={() =>
              setItemType(itemType === "Lost" ? "Found" : "Lost")
            }
          />
          <label htmlFor="toggleSwitch" className="toggle-label">
            <span
              className={`toggle-text lost ${itemType === "Lost" ? "active" : ""}`}
            >
              Lost
            </span>
            <span
              className={`toggle-text found ${itemType === "Found" ? "active" : ""}`}
            >
              Found
            </span>
            <span className="toggle-knob"></span>
          </label>
        </div>

        <div className="form-row">
          <select
            name="location"
            value={formData.location}
            onChange={handleInputChange}
            className="input-field"
          >
            <option>Select location</option>
            <option>Cafeteria</option>
            <option>Lobby</option>
            <option>Meeting room</option>
            <option>Washroom</option>
            <option>Playing zone</option>
            <option>Conference room</option>
            <option>Others</option>
          </select>
        </div>

        {formData.location === "Others" && (
          <div className="form-row">
            <input
              type="text"
              name="customLocation"
              value={customLocation}
              placeholder="Enter custom location"
              onChange={(e) => setCustomLocation(e.target.value)}
              className="customLocation input-field"
            />
          </div>
        )}

        <div className="form-row flex gap-2">
          <select
            name="categoryId"
            value={formData.categoryId}
            className="input-field flex-1"
            onChange={handleInputChange}
          >
            <option value="">Select category</option>
            {categories.map((cat) => (
              <option key={cat.categoryId} value={cat.categoryId}>
                {cat.categoryName}
              </option>
            ))}
          </select>

          <input
            type="text"
            name="itemName"
            placeholder="Enter item name"
            value={formData.itemName}
            onChange={handleInputChange}
            className="itemName-field input-field"
          />
        </div>

        <textarea
          name="description"
          placeholder="Enter description"
          value={formData.description}
          onChange={handleInputChange}
          className="description-field input-field"
        ></textarea>

        <div className="form-row">
          <input
            type="date"
            name="date"
            value={formData.date}
            onChange={handleInputChange}
            className="input-field"
          />
          <input
            type="time"
            name="time"
            value={formData.time}
            onChange={handleInputChange}
            className="input-field"
          />
        </div>

        <div className="upload-box">
          <label htmlFor="file-upload">
            ⬆ Upload media {itemType === "Found" && <span style={{ color: "red" }}>*</span>}
          </label>
          <input
            id="file-upload"
            type="file"
            multiple
            onChange={handleFileChange}
            style={{ display: "none" }}
          />
        </div>

        <div className="uploaded-files">
          {uploadedFiles.map((fileObj, index) => (
            <div className="file-tag" key={index}>
              <span>{fileObj.file.name}</span>
              <button onClick={() => removeFile(index)}>×</button>
              <img
                src={fileObj.base64}
                alt="preview"
                style={{ width: "100px", marginTop: "5px", borderRadius: "8px" }}
              />
            </div>
          ))}
        </div>

        <hr className="section-divider" />

        {itemType === "Found" && (
          <>
            <div className="handover-options">
              <label
                className={`radio-option ${handoverOption === "keep" ? "active" : ""}`}
              >
                <input
                  type="radio"
                  name="handover"
                  value="keep"
                  checked={handoverOption === "keep"}
                  onChange={() => setHandoverOption("keep")}
                />
                Keep it with me
              </label>

              <label
                className={`radio-option ${handoverOption === "security" ? "active" : ""}`}
              >
                <input
                  type="radio"
                  name="handover"
                  value="security"
                  checked={handoverOption === "security"}
                  onChange={() => setHandoverOption("security")}
                />
                Handover to security
              </label>
            </div>

            {handoverOption === "keep" && (
              <input
                type="text"
                name="pickupMessage"
                placeholder="Enter pickup message"
                value={formData.pickupMessage}
                onChange={handleInputChange}
                className="input-field full-width"
              />
            )}

            {handoverOption === "security" && (
              <>
                <input
                  type="text"
                  name="securityId"
                  placeholder="Enter security ID"
                  value={formData.securityId}
                  onChange={handleInputChange}
                  className="input-field full-width"
                />
                <input
                  type="text"
                  name="securityName"
                  placeholder="Enter security name"
                  value={formData.securityName}
                  onChange={handleInputChange}
                  className="input-field full-width"
                />
              </>
            )}
          </>
        )}

        <div className="form-buttons">
          <button className="cancel-btn" onClick={() => navigate("/")}>
            Cancel
          </button>

          <button className="submit-btn" onClick={handleSubmit}>
            Submit
          </button>
        </div>

        <ToastContainer position="top-center" autoClose={3000} />
      </div>
    </div>
  );
};

export default ReportItem;