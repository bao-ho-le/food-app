"use client"

import type { ReactNode } from "react"
import { InputGroup, InputGroupAddon, InputGroupInput } from "@/components/ui/input-group"
import { cn } from "@/lib/utils"

type SearchBarProps = {
  value: string
  onChange: (value: string) => void
  placeholder?: string
  leftSlot?: ReactNode
  className?: string
}

export function SearchBar({ value, onChange, placeholder, leftSlot, className }: SearchBarProps) {
  return (
    <InputGroup className={cn("h-12", className)}>
      {leftSlot && <InputGroupAddon align="inline-start">{leftSlot}</InputGroupAddon>}
      <InputGroupInput
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </InputGroup>
  )
}
