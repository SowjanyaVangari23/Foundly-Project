import React from 'react';
import '../styles/AboutUs.css';
import signupImg from '../assets/signup.jpg';
import listoutImg from '../assets/listout.jpg';
import notifyImg from '../assets/notify.jpg';
import teamImg from '../assets/team.jpg';

function AboutUs() {
  return (
    <div className="about-us-page">
      {/* What is Foundly + Why Foundly Section */}
      <section className="intro">
        <div className="intro-box">
          <h2>What is Foundly?</h2>
          <p>
            Foundly is a smart and secure lost & found item management system built to reconnect people with their misplaced belongings.
            Whether you're a student who lost a water bottle or someone who found a misplaced ID card, Foundly makes the process of reporting, tracking, and returning items smooth, simple, and stress-free.
          </p>

          <h2>Why Foundly?</h2>
          <p>
            Losing something shouldn’t feel like the end of the world — and finding something shouldn’t be a dead end.
            We built Foundly to bridge the gap between seekers and finders in a more organized, transparent, and reliable way.
            No more missing notice boards, endless emails, or confusion — just fast, fair, and secure item recovery.
          </p>
        </div>
      </section>

      {/* Horizontal Roadmap Section */}
      <section className="roadmap-container">
        <div className="roadmap-block">
          <img src={signupImg} alt="Sign Up" />
          <div className="label">Sign up</div>
          <div className="curve-line-horizontal" />
        </div>

        <div className="roadmap-block">
          <img src={listoutImg} alt="List Out" />
          <div className="label">List out</div>
          <div className="curve-line-horizontal" />
        </div>

        <div className="roadmap-block">
          <img src={notifyImg} alt="Get Notified" />
          <div className="label">Get notified</div>
        </div>
      </section>


    </div>
  );
}

export default AboutUs;