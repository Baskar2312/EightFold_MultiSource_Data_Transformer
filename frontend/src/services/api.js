const API_BASE = 'http://localhost:8080/api';

export async function uploadCandidateData(resumeFile, csvFile) {
  const formData = new FormData();
  formData.append('resumeFile', resumeFile);
  formData.append('csvFile', csvFile);

  const response = await fetch(`${API_BASE}/candidates/upload`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Upload failed' }));
    throw new Error(error.message || 'Upload failed');
  }

  return response.json();
}

export async function getAllCandidates() {
  const response = await fetch(`${API_BASE}/candidates`);
  if (!response.ok) {
    throw new Error('Failed to fetch candidates');
  }
  return response.json();
}

export async function getCandidateById(id) {
  const response = await fetch(`${API_BASE}/candidates/${id}`);
  if (!response.ok) {
    throw new Error('Failed to fetch candidate');
  }
  return response.json();
}

export async function generateCustomCandidateJson(id, config) {
  const response = await fetch(`${API_BASE}/candidates/${id}/project`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(config),
  });

  if (!response.ok) {
    throw new Error('Failed to generate custom JSON');
  }

  return response.json();
}
