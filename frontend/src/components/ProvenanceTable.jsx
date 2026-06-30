export default function ProvenanceTable({ records }) {
  if (!records || records.length === 0) {
    return (
      <div className="empty-state">
        <p style={{ fontSize: '0.9rem' }}>No provenance records available.</p>
      </div>
    );
  }

  return (
    <div className="table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            <th>Field</th>
            <th>Value</th>
            <th>Source</th>
            <th>Method</th>
            <th>Confidence</th>
          </tr>
        </thead>
        <tbody>
          {records.map((record, index) => (
            <tr key={index}>
              <td style={{ fontWeight: 600, textTransform: 'capitalize' }}>{record.fieldName}</td>
              <td style={{ maxWidth: 240, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {record.value}
              </td>
              <td>
                <span className={`source-tag ${record.sourceType?.toLowerCase()}`}>
                  {record.sourceType}
                </span>
              </td>
              <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                {record.extractionMethod}
              </td>
              <td>
                <span
                  className={`confidence-value ${
                    record.confidence >= 0.85 ? 'high' : record.confidence >= 0.7 ? 'medium' : 'low'
                  }`}
                  style={{ fontFamily: 'var(--font-mono)', fontSize: '0.85rem' }}
                >
                  {Math.round((record.confidence || 0) * 100)}%
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
