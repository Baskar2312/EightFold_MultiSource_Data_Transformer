import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCandidateById, generateCustomCandidateJson } from '../services/api';
import SkillBadge from '../components/SkillBadge';
import ConfidenceMeter from '../components/ConfidenceMeter';
import ProvenanceTable from '../components/ProvenanceTable';
import ConflictTable from '../components/ConflictTable';

const CUSTOM_CONFIG = {
  fields: [
    { path: 'candidate_name', from: 'fullName' },
    { path: 'primary_email', from: 'emails[0]' },
    { path: 'primary_phone', from: 'phones[0]' },
    { path: 'headline', from: 'headline' },
    { path: 'skills', from: 'skills[].name' },
  ],
  includeConfidence: true,
  onMissing: 'null',
};

export default function CandidateDetailsPage() {
  const { id } = useParams();
  const [candidate, setCandidate] = useState(null);
  const [customJson, setCustomJson] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [customError, setCustomError] = useState(null);
  const [copied, setCopied] = useState(false);
  const [customCopied, setCustomCopied] = useState(false);

  useEffect(() => {
    loadCandidate();
  }, [id]);

  const loadCandidate = async () => {
    try {
      const data = await getCandidateById(id);
      setCandidate(data);

      try {
        const projected = await generateCustomCandidateJson(id, CUSTOM_CONFIG);
        setCustomJson(projected);
      } catch (projectionError) {
        setCustomError(projectionError.message);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const canonicalJson = candidate ? JSON.stringify(candidate, null, 2) : '';
  const customJsonText = customJson ? JSON.stringify(customJson, null, 2) : '';
  const configJsonText = JSON.stringify(CUSTOM_CONFIG, null, 2);

  const copyText = async (text, setCopiedState) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedState(true);
      setTimeout(() => setCopiedState(false), 2000);
    } catch (err) {
      console.error('Failed to copy JSON:', err);
    }
  };

  const downloadText = (text, filename) => {
    const blob = new Blob([text], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="spinner" />
          <p className="loading-text">Loading candidate profile...</p>
        </div>
      </div>
    );
  }

  if (error || !candidate) {
    return (
      <div className="page-container">
        <div className="alert alert-error">
          <span>⚠️</span>
          <span>{error || 'Candidate not found.'}</span>
        </div>
        <Link to="/candidates" className="btn btn-secondary">← Back to Candidates</Link>
      </div>
    );
  }

  const initials = (candidate.fullName || 'U')
    .split(' ')
    .map(w => w[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  const confidenceLevel = (candidate.overallConfidence || 0) >= 0.85
    ? 'high'
    : (candidate.overallConfidence || 0) >= 0.7
      ? 'medium'
      : 'low';

  const jsonBlockStyle = {
    marginTop: 'var(--space-md)',
    padding: 'var(--space-lg)',
    borderRadius: 'var(--radius-lg)',
    overflowX: 'auto',
    whiteSpace: 'pre-wrap',
    wordBreak: 'break-word',
    background: 'rgba(15, 23, 42, 0.95)',
    color: '#e5e7eb',
    fontFamily: 'var(--font-mono)',
    fontSize: '0.875rem',
    lineHeight: 1.6,
    border: '1px solid var(--border)'
  };

  return (
    <div className="page-container">
      <Link to="/candidates" className="back-link fade-in-up">
        ← Back to Candidates
      </Link>

      {/* Profile Header */}
      <div className="glass-card fade-in-up" style={{ marginBottom: 'var(--space-2xl)' }}>
        <div className="profile-header">
          <div className="profile-avatar">{initials}</div>
          <div className="profile-info" style={{ flex: 1 }}>
            <h1>{candidate.fullName}</h1>
            <p className="profile-headline">{candidate.headline || 'No headline'}</p>
            <span className={`overall-confidence-badge ${confidenceLevel}`}>
              📊 Overall Confidence: {Math.round((candidate.overallConfidence || 0) * 100)}%
            </span>
          </div>
        </div>

        {/* Contact Info */}
        <div className="detail-grid" style={{ marginTop: 'var(--space-lg)' }}>
          <div>
            <div className="detail-label">Email</div>
            <div className="detail-value">
              {candidate.emails?.length > 0
                ? candidate.emails.join(', ')
                : '—'}
            </div>
          </div>
          <div>
            <div className="detail-label">Phone</div>
            <div className="detail-value">
              {candidate.phones?.length > 0
                ? candidate.phones.join(', ')
                : '—'}
            </div>
          </div>
          <div>
            <div className="detail-label">Profile ID</div>
            <div className="detail-value" style={{ fontFamily: 'var(--font-mono)' }}>
              #{candidate.candidateId}
            </div>
          </div>
          <div>
            <div className="detail-label">Created</div>
            <div className="detail-value">
              {candidate.createdAt
                ? new Date(candidate.createdAt).toLocaleString()
                : '—'}
            </div>
          </div>
        </div>
      </div>

      {/* Skills */}
      <div className="profile-section fade-in-up fade-in-up-delay-1">
        <h3 className="profile-section-title">
          <span className="icon">🛠</span>
          Skills ({candidate.skills?.length || 0})
        </h3>
        {candidate.skills?.length > 0 ? (
          <div className="skills-grid">
            {candidate.skills.map((skill, i) => (
              <SkillBadge key={i} skill={skill} />
            ))}
          </div>
        ) : (
          <p style={{ color: 'var(--text-muted)' }}>No skills extracted.</p>
        )}
      </div>

      {/* Education */}
      <div className="profile-section fade-in-up fade-in-up-delay-2">
        <h3 className="profile-section-title">
          <span className="icon">🎓</span>
          Education ({candidate.education?.length || 0})
        </h3>
        {candidate.education?.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-sm)' }}>
            {candidate.education.map((edu, i) => (
              <div key={i} className="glass-card" style={{ padding: 'var(--space-md) var(--space-lg)' }}>
                <strong>{edu.degree || 'Education Entry'}</strong>
                {edu.institution && <span style={{ color: 'var(--text-secondary)' }}> — {edu.institution}</span>}
                {edu.year && <span style={{ color: 'var(--text-muted)' }}> ({edu.year})</span>}
              </div>
            ))}
          </div>
        ) : (
          <p style={{ color: 'var(--text-muted)' }}>No education data extracted.</p>
        )}
      </div>

      {/* Experience */}
      <div className="profile-section fade-in-up fade-in-up-delay-3">
        <h3 className="profile-section-title">
          <span className="icon">💼</span>
          Experience ({candidate.experience?.length || 0})
        </h3>
        {candidate.experience?.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-sm)' }}>
            {candidate.experience.map((exp, i) => (
              <div key={i} className="glass-card" style={{ padding: 'var(--space-md) var(--space-lg)' }}>
                {exp.description}
              </div>
            ))}
          </div>
        ) : (
          <p style={{ color: 'var(--text-muted)' }}>No experience data extracted.</p>
        )}
      </div>

      {/* Confidence Breakdown */}
      <div className="profile-section fade-in-up fade-in-up-delay-4">
        <h3 className="profile-section-title">
          <span className="icon">📊</span>
          Confidence Breakdown
        </h3>
        <div className="glass-card">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontWeight: 600 }}>Overall Confidence</span>
              <ConfidenceMeter value={candidate.overallConfidence} />
            </div>
            {candidate.skills?.map((skill, i) => (
              <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: 'var(--text-secondary)' }}>{skill.name}</span>
                <ConfidenceMeter value={skill.confidence} />
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Provenance */}
      <div className="profile-section fade-in-up">
        <h3 className="profile-section-title">
          <span className="icon">🔍</span>
          Provenance Records ({candidate.provenance?.length || 0})
        </h3>
        <ProvenanceTable records={candidate.provenance} />
      </div>

      {/* Conflicts */}
      <div className="profile-section fade-in-up">
        <h3 className="profile-section-title">
          <span className="icon">⚠️</span>
          Conflict Resolution ({candidate.conflicts?.length || 0})
        </h3>
        <ConflictTable conflicts={candidate.conflicts} />
      </div>

      {/* Raw Canonical JSON Output */}
      <div className="profile-section fade-in-up">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 'var(--space-md)', flexWrap: 'wrap' }}>
          <h3 className="profile-section-title" style={{ margin: 0 }}>
            <span className="icon">🧾</span>
            Canonical JSON Output
          </h3>
          <div style={{ display: 'flex', gap: 'var(--space-sm)', flexWrap: 'wrap' }}>
            <button className="btn btn-secondary" type="button" onClick={() => copyText(canonicalJson, setCopied)}>
              {copied ? 'Copied JSON' : 'Copy JSON'}
            </button>
            <button
              className="btn btn-primary"
              type="button"
              onClick={() => downloadText(canonicalJson, `candidate-${candidate?.candidateId || 'profile'}-canonical.json`)}
            >
              Download JSON
            </button>
          </div>
        </div>
        <p style={{ color: 'var(--text-muted)', marginTop: 'var(--space-sm)' }}>
          This is the default schema-valid canonical JSON produced by the backend after parsing, normalization, merging, provenance tracking, conflict detection, and confidence scoring.
        </p>
        <pre style={jsonBlockStyle}>{canonicalJson}</pre>
      </div>

      {/* Custom Config JSON Output */}
      <div className="profile-section fade-in-up">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 'var(--space-md)', flexWrap: 'wrap' }}>
          <h3 className="profile-section-title" style={{ margin: 0 }}>
            <span className="icon">⚙️</span>
            Custom Config JSON Output
          </h3>
          {customJson && (
            <div style={{ display: 'flex', gap: 'var(--space-sm)', flexWrap: 'wrap' }}>
              <button className="btn btn-secondary" type="button" onClick={() => copyText(customJsonText, setCustomCopied)}>
                {customCopied ? 'Copied JSON' : 'Copy Custom JSON'}
              </button>
              <button
                className="btn btn-primary"
                type="button"
                onClick={() => downloadText(customJsonText, `candidate-${candidate?.candidateId || 'profile'}-custom.json`)}
              >
                Download Custom JSON
              </button>
            </div>
          )}
        </div>
        <p style={{ color: 'var(--text-muted)', marginTop: 'var(--space-sm)' }}>
          This output is generated from a runtime configuration. The backend reshapes the canonical profile into a different JSON schema without changing Java code for each output format.
        </p>

        <h4 style={{ marginTop: 'var(--space-md)', marginBottom: 0 }}>Custom Configuration Used</h4>
        <pre style={jsonBlockStyle}>{configJsonText}</pre>

        {customError ? (
          <div className="alert alert-error" style={{ marginTop: 'var(--space-md)' }}>
            <span>⚠️</span>
            <span>{customError}</span>
          </div>
        ) : (
          <>
            <h4 style={{ marginTop: 'var(--space-lg)', marginBottom: 0 }}>Projected Output</h4>
            <pre style={jsonBlockStyle}>{customJsonText}</pre>
          </>
        )}
      </div>
    </div>
  );
}
