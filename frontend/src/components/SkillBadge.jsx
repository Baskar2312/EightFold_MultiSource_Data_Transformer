export default function SkillBadge({ skill }) {
  const confidence = skill.confidence || 0;
  const level = confidence >= 0.9 ? 'high' : confidence >= 0.75 ? 'medium' : 'low';

  return (
    <span className="skill-badge">
      <span className={`confidence-dot ${level}`} />
      <span>{skill.name}</span>
      <span className="skill-sources">
        {skill.sources?.map((src) => (
          <span key={src} className={`source-tag ${src.toLowerCase()}`}>
            {src}
          </span>
        ))}
      </span>
    </span>
  );
}
