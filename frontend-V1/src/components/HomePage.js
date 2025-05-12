import React, { useRef, useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Testimonials from '../components/Testimonials';
import Navbar from '../components/Navbar';
import '../styles/HomePage.css';
import '../styles/AboutUs.css';
import mem1 from '../assets/member01.jpg';
import mem2 from '../assets/member02.jpg';
import mem3 from '../assets/member03.jpg';
import mem4 from '../assets/member04.jpg';
import mem5 from '../assets/member05.jpg';
import logo from '../assets/foundlylogo.png';

import backgroundImage from '../assets/img.png';
import signupImg from '../assets/signup.jpg';
import notifyImg from '../assets/notify.jpg';
import listoutImg from '../assets/listout.jpg';
import teamImg from '../assets/team.jpg';

function HomePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isLoggedIn, setShowAuthBox } = useAuth();
  const [isScrolledUp, setIsScrolledUp] = useState(false);
  const [activeSection, setActiveSection] = useState('home');
  const [activeTab, setActiveTab] = useState(null);
  const tabRefs = useRef([]);
  const [feedbacks, setFeedbacks] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({ name: '', story: '' });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleFormSubmit = (e) => {
    e.preventDefault();
    if (formData.name && formData.story) {
      setFeedbacks((prev) => [...prev, { author: formData.name, text: formData.story }]);
      setFormData({ name: '', story: '' });
      setShowForm(false);
    }
  };

  const aboutRef = useRef(null);

  const scrollToAbout = () => {
    if (aboutRef.current) {
      aboutRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  };

  const handleProtectedClick = (path) => {
    if (isLoggedIn) {
      setIsScrolledUp(true);
      setTimeout(() => {
        navigate(path);
        setIsScrolledUp(false);
      }, 600);
    } else {
      setShowAuthBox(true);
    }
  };

  const handleTabClick = (tabName, path) => {
    setActiveTab(tabName);
    handleProtectedClick(path);
  };

  useEffect(() => {
    if (activeTab) {
      const activeTabIndex = ['lost-items', 'found-items', 'my-activity'].indexOf(activeTab);
      const activeTabElement = tabRefs.current[activeTabIndex];

      if (activeTabElement) {
        const tabWidth = activeTabElement.offsetWidth;
        const tabLeft = activeTabElement.offsetLeft;

        const slider = document.querySelector('.tab-slider');
        if (slider) {
          slider.style.width = `${tabWidth}px`;
          slider.style.left = `${tabLeft}px`;
        }
      }
    }
  }, [activeTab]);

  return (
    <div className={`homepage-container ${isScrolledUp ? 'scrolled-up' : ''}`}>
      <header
        className="App-header"
        style={{
          backgroundImage: `url(${backgroundImage})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          backgroundRepeat: 'no-repeat',
        }}
      >
        <Navbar onAboutClick={scrollToAbout} activeSection={activeSection} />

        <div className="hero-text">
          <h1 className="main-title">Foundly</h1>
          <div className="subtitle">
            <span className="dot-label lost">Lost It</span>
            <span className="dot-label list">List It</span>
            <span className="dot-label find">Find It</span>
          </div>
          <div className="center-report">
            <button className="report-btn" onClick={() => handleProtectedClick('/report')}>
              Report ➜
            </button>
          </div>
        </div>

        <div className="tab-buttons">
          <button
            ref={(el) => (tabRefs.current[0] = el)}
            className={`tab ${activeTab === 'lost-items' ? 'active' : ''}`}
            onClick={() => handleTabClick('lost-items', '/lost-items')}
          >
            Lost Items
          </button>
          <button
            ref={(el) => (tabRefs.current[1] = el)}
            className={`tab ${activeTab === 'found-items' ? 'active' : ''}`}
            onClick={() => handleTabClick('found-items', '/found-items')}
          >
            Found Items
          </button>
          <button
            ref={(el) => (tabRefs.current[2] = el)}
            className={`tab ${activeTab === 'my-activity' ? 'active' : ''}`}
            onClick={() => handleTabClick('my-activity', '/my-activity')}
          >
            My Activity
          </button>

          <div className="tab-slider"></div>
        </div>
      </header>

      <section ref={aboutRef} className="about-us-page">
        <div className="about-flow">
          <div className="text-section right">
            <h2>What is Foundly?</h2>
            <p>
              Foundly is a smart and secure lost & found item management system built to reconnect people with their misplaced belongings.
            </p>
            <h2>Why Foundly?</h2>
            <p>
              Losing something shouldn’t feel like the end of the world — and finding something shouldn’t be a dead end.
            </p>
          </div>

          <div className="roadmap-vertical">
            <div className="roadmap-step">
              <img src={signupImg} alt="Sign up" />
              <div className="step-label">Sign up</div>
              <div className="curve-line vertical"></div>
            </div>
            <div className="roadmap-step">
              <img src={listoutImg} alt="List out" />
              <div className="step-label">List out</div>
              <div className="curve-line vertical"></div>
            </div>
            <div className="roadmap-step">
              <img src={notifyImg} alt="Get notified" />
              <div className="step-label">Get notified</div>
            </div>
          </div>

          <div className="features">
            <h2>Key Features</h2>
            <ul>
              <li>📋 Report Items</li>
              <li>👥 Role-Based Access</li>
              <li>🔔 Smart Notifications</li>
              <li>🤝 Claim & Handover</li>
              <li>📊 Dashboard Analytics</li>
              <li>🔍 Filter & Search</li>
            </ul>
          </div>

          <section className="team">
            <h2>Meet the Team</h2>
            <p>
              We are a group of passionate developers and problem-solvers who came together to build something that makes a real-world difference.
              With expertise in ReactJS, Spring Boot, MySQL, Devops and a vision for smarter communities, Foundly is our way of bringing order to everyday chaos.
            </p>

            <div className="team-members">
              <div className="member">
                <img src={mem1} alt="Teammate 1" />
                <h4>Akanksha</h4>
                <p>Devops Engineer</p>
              </div>
              <div className="member">
                <img src={mem2} alt="Teammate 2" />
                <h4>Rakesh</h4>
                <p>Backend Developer</p>
              </div>
              <div className="member">
                <img src={mem3} alt="Teammate 3" />
                <h4>Roopa</h4>
                <p>Backend Developer</p>
              </div>
              <div className="member">
                <img src={mem4} alt="Teammate 4" />
                <h4>Subham</h4>
                <p>Frontend + Testing</p>
              </div>
              <div className="member">
                <img src={mem5} alt="Teammate 5" />
                <h4>Sowjanya</h4>
                <p>Frontend Developer</p>
              </div>
            </div>
          </section>
        </div>
      </section>

      <section className="content-section">
        <h2 className="section-heading">What users say</h2>
        <Testimonials testimonials={feedbacks} />
        <button className="feedback-button" onClick={() => setShowForm(true)}>
          Share Your Experience
        </button>
      </section>

      {showForm && (
        <div className="feedback-modal">
          <div className="feedback-form-box">
            <h3>Share your Success Story</h3>
            <form className="feedbackform-container" onSubmit={handleFormSubmit}>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="Your Name"
                required
              />
              <textarea
                name="story"
                value={formData.story}
                onChange={handleInputChange}
                placeholder="Your experience using Foundly"
                required
              ></textarea>
              <div className="feedback-button-group">
                <button type="button" className="fbcancel-button" onClick={() => setShowForm(false)}>Cancel</button>
                <button type="submit" className="fbsubmit-button">Submit</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <footer className="footer">
        <div className="footer-content">
          <div className="footer-logo">
            <img src={logo} alt="Foundly Logo" className="footer-logo-img" />
          </div>

          <div className="footer-links">
            <a href="/">Home</a>
            <a href="/dashboard">Dashboard</a>
            <a href="#about">About us</a>
          </div>
          <div className="footer-contact">
            <p>Contact : +91 8734629540</p>
            <p>Email : support@insightglobal.com</p>
          </div>
        </div>
        <div className="footer-copyright">
          ©2025 Insight Global, A Staffing Services Company. All rights reserved.
        </div>
      </footer>
    </div>
  );
}

export default HomePage;