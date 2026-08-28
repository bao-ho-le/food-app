"use client"

import Link from "next/link"
import { useEffect, useState } from "react"
import { trySilentRefresh } from "@/lib/api-client"

export function Logo({ className = "" }: { className?: string }) {
  const [href, setHref] = useState("/")

  useEffect(() => {
    let cancelled = false
    trySilentRefresh().then((loggedIn) => {
      if (!cancelled && loggedIn) setHref("/user/food")
    })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <Link href={href} className={`flex items-center gap-2 ${className}`}>
      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary">
        <span className="text-xl font-bold text-primary-foreground">F</span>
      </div>
      <span className="text-xl font-semibold text-foreground">FoodOrder</span>
    </Link>
  )
}
