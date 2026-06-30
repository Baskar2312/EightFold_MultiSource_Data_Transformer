import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllCandidates } from '../services/api';
import ConfidenceMeter from '../components/ConfidenceMeter';

export default function CandidateListPage() {
  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadCandidates();
  }, []);

  const loadCandidates = async () => {
    try {
      const data = await getAllCandidates();
      setCandidates(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="spinner" />
          <p className="loading-text">Loading candidates...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="alert alert-error">
          <span>⚠️</span>
          <span>{error}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h2 className="section-title fade-in-up">
        Candidate Profiles
      </h2>

      <p className="section-subtitle fade-in-up">
        Manage all normalized candidate profiles generated from multiple sources.
      </p>

      <div
        className="fade-in-up"
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '2rem',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        <span
          style={{
            color: '#9ca3af',
            fontSize: '0.95rem',
            fontWeight: 500,
          }}
        >
          📋 {candidates.length} Candidate{candidates.length !== 1 ? 's' : ''}
        </span>

        <Link to="/upload" className="btn btn-primary">
          + New Upload
        </Link>
      </div>

      {candidates.length === 0 ? (
        <div className="empty-state fade-in-up">
          <div className="icon">📭</div>

          <h3 style={{ marginBottom: '0.8rem' }}>
            No Candidate Profiles Found
          </h3>

          <p style={{ color: '#9ca3af', marginBottom: '1.5rem' }}>
            Upload a Resume PDF and a CSV file to generate your first canonical candidate profile.
          </p>

          <Link to="/upload" className="btn btn-primary">
            Upload Candidate Data
          </Link>
        </div>
      ) : (
        <div className="candidates-grid">
          {candidates.map((candidate, index) => (
            <Link
              to={`/candidates/${candidate.candidateId}`}
              key={candidate.candidateId}
              className={`glass-card candidate-card fade-in-up fade-in-up-delay-${(index % 4) + 1}`}
              style={{ textDecoration: 'none' }}
            >
              <div className="candidate-card-header">
                <span className="candidate-name">
                  {candidate.fullName}
                </span>

                <ConfidenceMeter
                  value={candidate.overallConfidence}
                  showBar={false}
                />
              </div>

              <div className="candidate-headline">
                {candidate.headline || 'No headline'}
              </div>

              <div className="candidate-meta">
                {candidate.emails?.length > 0 && (
                  <span>✉ {candidate.emails[0]}</span>
                )}

                {candidate.phones?.length > 0 && (
                  <span>📱 {candidate.phones[0]}</span>
                )}

                {candidate.skills?.length > 0 && (
                  <span>
                    🛠 {candidate.skills.length} skill
                    {candidate.skills.length !== 1 ? 's' : ''}
                  </span>
                )}
              </div>

              <div style={{ textAlign: 'right' }}>
                <span className="btn btn-secondary btn-sm">
                  View Details →
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
