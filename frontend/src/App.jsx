import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import UploadPage from './pages/UploadPage';
import CandidateListPage from './pages/CandidateListPage';
import CandidateDetailsPage from './pages/CandidateDetailsPage';

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/upload" element={<UploadPage />} />
        <Route path="/candidates" element={<CandidateListPage />} />
        <Route path="/candidates/:id" element={<CandidateDetailsPage />} />
      </Routes>
    </BrowserRouter>
  );
}
