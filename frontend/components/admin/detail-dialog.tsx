import type { ReactNode } from "react"
import { cn } from "@/lib/utils"

export function InfoGrid({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn("grid grid-cols-2 gap-x-6 gap-y-4", className)}>{children}</div>
}

export function DetailRow({ label, value, className }: { label: string; value: ReactNode; className?: string }) {
  return (
    <div className={className}>
      <p className="text-sm font-semibold">{label}</p>
      <p className="text-sm text-muted-foreground break-words">{value}</p>
    </div>
  )
}

export function DetailSection({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="space-y-3 border-t pt-4 first:border-t-0 first:pt-0">
      <h4 className="text-sm font-semibold">{title}</h4>
      {children}
    </div>
  )
}
