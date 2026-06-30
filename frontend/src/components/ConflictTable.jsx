export default function ConflictTable({ conflicts }) {
  if (!conflicts || conflicts.length === 0) {
    return (
      <div className="empty-state">
        <p style={{ fontSize: '0.9rem' }}>No conflicts detected — all sources agree. ✓</p>
      </div>
    );
  }

  return (
    <div className="table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            <th>Field</th>
            <th>Resume Value</th>
            <th>CSV Value</th>
            <th>Selected</th>
            <th>Reason</th>
          </tr>
        </thead>
        <tbody>
          {conflicts.map((conflict, index) => (
            <tr key={index} className="conflict-row">
              <td style={{ fontWeight: 600, textTransform: 'capitalize' }}>{conflict.fieldName}</td>
              <td style={{ color: 'var(--info)' }}>{conflict.resumeValue}</td>
              <td style={{ color: 'var(--warning)' }}>{conflict.csvValue}</td>
              <td style={{ color: 'var(--success)', fontWeight: 600 }}>{conflict.selectedValue}</td>
              <td style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', maxWidth: 300 }}>
                {conflict.reason}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
