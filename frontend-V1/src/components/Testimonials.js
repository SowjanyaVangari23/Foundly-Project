import React from 'react';
import '../styles/Testimonials.css';

const Testimonials = ({ testimonials = [] }) => {
  const defaultTestimonials = [
    {
      text: "Lost & Found helped me find my lost wallet within hours! I'm so grateful for this amazing platform.",
      author: "Sarah J.",
    },
    {
      text: "I accidentally left my phone in a taxi, but thanks to Lost & Found, I got it back the same day! Highly recommend.",
      author: "John D.",
    },
    {
      text: "I found a lost dog in my neighborhood and used Lost & Found to reunite it with its owner. Such a heartwarming experience!",
      author: "Emily R.",
    },
  ];

  // Combine default testimonials with user-submitted ones (if any)
  const combinedTestimonials = [...defaultTestimonials, ...testimonials];
  const extendedTestimonials = [...combinedTestimonials, ...combinedTestimonials]; // For marquee effect

  return (
    <div className="testimonials">
      <div className="testimonial-marquee">
        <div className="testimonial-track">
          {extendedTestimonials.map((testimonial, index) => (
            <div key={index} className="testimonial-card">
              <p className="testimonial-text">"{testimonial.text}"</p>
              <p className="testimonial-author">- {testimonial.author}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Testimonials;