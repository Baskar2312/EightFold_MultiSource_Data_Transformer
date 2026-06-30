export default function ConfidenceMeter({ value, showBar = true }) {
  const percent = Math.round((value || 0) * 100);
  const level = percent >= 85 ? 'high' : percent >= 70 ? 'medium' : 'low';

  return (
    <div className="confidence-meter">
      {showBar && (
        <div className="confidence-bar-track">
          <div
            className={`confidence-bar-fill ${level}`}
            style={{ width: `${percent}%` }}
          />
        </div>
      )}
      <span className={`confidence-value ${level}`}>{percent}%</span>
    </div>
  );
}
