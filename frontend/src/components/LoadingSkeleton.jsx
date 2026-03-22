export default function LoadingSkeleton({ count = 3, height = 'h-24' }) {
  return (
    <div className="space-y-4 w-full">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className={`skeleton ${height} w-full`} />
      ))}
    </div>
  );
}
