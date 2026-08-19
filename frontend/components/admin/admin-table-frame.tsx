"use client"

import { ReactNode } from "react"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

interface AdminTableFrameProps {
  children: ReactNode
  page: number
  totalPages: number
  onPageChange: (page: number) => void
  caption: ReactNode
  className?: string
}

export function AdminTableFrame({ children, page, totalPages, onPageChange, caption, className }: AdminTableFrameProps) {
  return (
    <div className={cn("flex min-h-0 flex-col rounded-lg border overflow-hidden", className)}>
      <div className="min-h-0 flex-1 [&_[data-slot=table-container]]:h-full [&_[data-slot=table-container]]:overflow-auto">
        {children}
      </div>
      <div className="flex shrink-0 items-center justify-between gap-2 border-t px-6 py-2">
        <p className="text-sm text-muted-foreground">{caption}</p>
        <div className="flex items-center gap-1">
          <Button variant="ghost" size="icon" disabled={page <= 1} onClick={() => onPageChange(page - 1)}>
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="px-1 text-sm tabular-nums">Trang {page}/{totalPages}</span>
          <Button variant="ghost" size="icon" disabled={page >= totalPages} onClick={() => onPageChange(page + 1)}>
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  )
}
