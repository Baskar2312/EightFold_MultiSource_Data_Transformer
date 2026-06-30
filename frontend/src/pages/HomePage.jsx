import { Link } from 'react-router-dom';

export default function HomePage() {
  return (
    <div className="page-container">
      {/* Hero */}
      <section className="hero fade-in-up">
        <h1 className="hero-title">Candidate Profile Builder</h1>
        <p className="hero-desc">
          Upload a recruiter notes PDF and a structured CSV file. Our rule-based engine
          parses, normalizes, and merges both data sources into a single canonical
          candidate profile — with full confidence scoring, provenance tracking,
          and conflict resolution.
        </p>
        <div className="hero-actions">
          <Link to="/upload" className="btn btn-primary btn-lg">
            🚀 Upload Candidate Data
          </Link>
          <Link to="/candidates" className="btn btn-secondary btn-lg">
            📋 View Candidates
          </Link>
        </div>
      </section>

      {/* Features */}
      <section className="features-grid">
        <div className="glass-card feature-card fade-in-up fade-in-up-delay-1">
          <div className="feature-icon">📄</div>
          <h3>Multi-Source Parsing</h3>
          <p>
            Parse unstructured recruiter notes PDFs using PDFBox with regex-based extraction,
            and structured CSV files using Apache Commons CSV.
          </p>
        </div>

        <div className="glass-card feature-card fade-in-up fade-in-up-delay-2">
          <div className="feature-icon">🔀</div>
          <h3>Smart Data Merging</h3>
          <p>
            Merge candidate data from multiple sources with intelligent conflict
            resolution — preferring richer recruiter notes data when sources disagree.
          </p>
        </div>

        <div className="glass-card feature-card fade-in-up fade-in-up-delay-3">
          <div className="feature-icon">📊</div>
          <h3>Confidence Scoring</h3>
          <p>
            Every field gets a confidence score. Skills found in both sources score
            0.95, recruiter notes-only 0.80, CSV-only 0.75. Overall confidence is computed automatically.
          </p>
        </div>

        <div className="glass-card feature-card fade-in-up fade-in-up-delay-4">
          <div className="feature-icon">🔍</div>
          <h3>Full Provenance</h3>
          <p>
            Track exactly where every piece of data came from — which file, which
            extraction method, and what confidence level. Complete audit trail.
          </p>
        </div>

        <div className="glass-card feature-card fade-in-up fade-in-up-delay-1">
          <div className="feature-icon">⚡</div>
          <h3>Normalization Engine</h3>
          <p>
            Emails lowercased, phones formatted to +91, skill aliases resolved
            (ReactJS → React, JS → JavaScript), names cleaned and compared.
          </p>
        </div>

        <div className="glass-card feature-card fade-in-up fade-in-up-delay-2">
          <div className="feature-icon">⚠️</div>
          <h3>Conflict Detection</h3>
          <p>
            When recruiter notes and CSV disagree on a field, the conflict is recorded with
            both values, the selected value, and the resolution reason.
          </p>
        </div>
      </section>
    </div>
  );
}
