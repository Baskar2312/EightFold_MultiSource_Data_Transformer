import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadCandidateData } from '../services/api';

export default function UploadPage() {
  const navigate = useNavigate();
  const [resumeFile, setResumeFile] = useState(null);
  const [csvFile, setCsvFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleUpload = async () => {
    setError(null);

    if (!resumeFile) {
      setError('Please select a recruiter notes PDF file.');
      return;
    }
    if (!csvFile) {
      setError('Please select a CSV file.');
      return;
    }
    if (!resumeFile.name.toLowerCase().endsWith('.pdf')) {
      setError('Recruiter notes file must be a PDF.');
      return;
    }
    if (!csvFile.name.toLowerCase().endsWith('.csv')) {
      setError('Data file must be a CSV.');
      return;
    }

    setLoading(true);
    try {
      const response = await uploadCandidateData(resumeFile, csvFile);
      if (response.success && response.candidateId) {
        navigate(`/candidates/${response.candidateId}`);
      } else {
        setError(response.message || 'Upload failed. Please try again.');
      }
    } catch (err) {
      setError(err.message || 'Upload failed. Please check your files and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h2 className="section-title fade-in-up">Upload Candidate Data</h2>
      <p className="section-subtitle fade-in-up">
        Upload a recruiter notes PDF and a CSV file to create a normalized candidate profile.
      </p>

      {error && (
        <div className="alert alert-error fade-in-up">
          <span>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      <div className="upload-grid fade-in-up fade-in-up-delay-1">
        {/* Recruiter Notes Upload */}
        <div className={`upload-zone ${resumeFile ? 'has-file' : ''}`}>
          <div className="upload-zone-icon">📄</div>
          <p className="upload-zone-text">
            <strong>Recruiter Notes PDF</strong><br />
            Drop your recruiter notes here or click to browse
          </p>
          {resumeFile && <p className="file-name">✓ {resumeFile.name}</p>}
          <input
            type="file"
            accept=".pdf"
            onChange={(e) => {
              setResumeFile(e.target.files[0] || null);
              setError(null);
            }}
          />
        </div>

        {/* CSV Upload */}
        <div className={`upload-zone ${csvFile ? 'has-file' : ''}`}>
          <div className="upload-zone-icon">📊</div>
          <p className="upload-zone-text">
            <strong>Candidate CSV</strong><br />
            Drop your CSV here or click to browse
          </p>
          {csvFile && <p className="file-name">✓ {csvFile.name}</p>}
          <input
            type="file"
            accept=".csv"
            onChange={(e) => {
              setCsvFile(e.target.files[0] || null);
              setError(null);
            }}
          />
        </div>
      </div>

      <div style={{ textAlign: 'center' }} className="fade-in-up fade-in-up-delay-2">
        <button
          className="btn btn-primary btn-lg"
          onClick={handleUpload}
          disabled={loading || !resumeFile || !csvFile}
        >
          {loading ? (
            <>
              <span className="spinner" style={{ width: 20, height: 20, borderWidth: 2 }} />
              Processing...
            </>
          ) : (
            <>🚀 Upload & Process</>
          )}
        </button>
      </div>

      {loading && (
        <div className="loading-container fade-in-up" style={{ marginTop: '2rem' }}>
          <div className="spinner" />
          <p className="loading-text">
            Parsing files, normalizing data, merging sources...
          </p>
        </div>
      )}
    </div>
  );
}
