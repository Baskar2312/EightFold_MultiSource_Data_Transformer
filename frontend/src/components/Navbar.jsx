import { NavLink } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <NavLink to="/" className="navbar-brand">
          <span className="logo-icon">⚙</span>
          <span>CPB</span>
        </NavLink>
        <ul className="navbar-links">
          <li>
            <NavLink to="/" end className={({ isActive }) => isActive ? 'active' : ''}>
              Home
            </NavLink>
          </li>
          <li>
            <NavLink to="/upload" className={({ isActive }) => isActive ? 'active' : ''}>
              Upload
            </NavLink>
          </li>
          <li>
            <NavLink to="/candidates" className={({ isActive }) => isActive ? 'active' : ''}>
              Candidates
            </NavLink>
          </li>
        </ul>
      </div>
    </nav>
  );
}
