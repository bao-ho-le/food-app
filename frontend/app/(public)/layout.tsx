import type React from "react"
import { Logo } from "@/components/logo"

export default function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-background">
      <header className="border-b border-border bg-card">
        <div className="container mx-auto flex h-16 items-center px-4">
          <Logo />
        </div>
      </header>
      {children}
    </div>
  )
}
