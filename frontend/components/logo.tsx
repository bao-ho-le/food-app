import Link from "next/link"

export function Logo({ className = "" }: { className?: string }) {
  return (
    <Link href="/" className={`flex items-center gap-2 ${className}`}>
      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary">
        <span className="text-xl font-bold text-primary-foreground">F</span>
      </div>
      <span className="text-xl font-semibold text-foreground">FoodOrder</span>
    </Link>
  )
}
