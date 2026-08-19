"use client"

import { useEffect, useState } from "react"
import { SlidersHorizontal } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { cn } from "@/lib/utils"

export type FilterOption = { value: string; label: string }
export type FilterSectionDef = { key: string; label: string; options: FilterOption[] }

const toggleItemClass = "flex-none rounded-md border border-input px-3 text-xs sm:text-sm"

type AdminFilterPopoverProps<T extends Record<string, string>> = {
  sections: FilterSectionDef[]
  values: T
  defaultValues: T
  onApply: (values: T) => void
}

export function AdminFilterPopover<T extends Record<string, string>>({
  sections,
  values,
  defaultValues,
  onApply,
}: AdminFilterPopoverProps<T>) {
  const [open, setOpen] = useState(false)
  const [draft, setDraft] = useState<T>(values)

  useEffect(() => {
    if (open) setDraft(values)
  }, [open, values])

  const hasActiveFilters = sections.some((s) => values[s.key] !== defaultValues[s.key])

  const handleClear = () => {
    onApply(defaultValues)
    setOpen(false)
  }

  const handleApply = () => {
    onApply(draft)
    setOpen(false)
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-haspopup="dialog"
          aria-expanded={open}
          aria-label="Bộ lọc"
          className={cn(
            "relative h-10 w-10 shrink-0",
            open && "border-primary bg-accent text-accent-foreground",
          )}
        >
          <SlidersHorizontal className="h-4 w-4" />
          {hasActiveFilters && (
            <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-primary" />
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent
        role="dialog"
        aria-label="Bộ lọc"
        align="start"
        side="bottom"
        sideOffset={8}
        collisionPadding={16}
        className="flex w-[320px] flex-col gap-0 p-0"
        style={{ maxHeight: "var(--radix-popover-content-available-height)" }}
      >
        <div className="flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto p-4">
          {sections.map((section) => (
            <div key={section.key} className="flex flex-col gap-2">
              <Label>{section.label}</Label>
              <ToggleGroup
                type="single"
                value={draft[section.key]}
                onValueChange={(value) => {
                  if (!value) return
                  setDraft((prev) => ({ ...prev, [section.key]: value }))
                }}
                className="w-full flex-wrap justify-start gap-2"
              >
                {section.options.map((option) => (
                  <ToggleGroupItem key={option.value} value={option.value} className={toggleItemClass}>
                    {option.label}
                  </ToggleGroupItem>
                ))}
              </ToggleGroup>
            </div>
          ))}
        </div>
        <div className="flex justify-end gap-2 border-t border-border p-4">
          <Button type="button" variant="secondary" onClick={handleClear}>
            Xoá lọc
          </Button>
          <Button type="button" onClick={handleApply}>
            Áp dụng
          </Button>
        </div>
      </PopoverContent>
    </Popover>
  )
}
