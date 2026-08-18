"use client"

import { useEffect, useState, type InputHTMLAttributes } from "react"
import { SlidersHorizontal } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
} from "@/components/ui/select"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { cn } from "@/lib/utils"
import type { DishFilterOption } from "@/lib/mock-data"

export type FilterKey = "cuisine" | "ingredient" | "method" | "flavor"

export type DishFilterValues = {
  cuisine: string
  ingredient: string
  method: string
  flavor: string
  minPrice: string
  maxPrice: string
}

export const DEFAULT_DISH_FILTER_VALUES: DishFilterValues = {
  cuisine: "all",
  ingredient: "all",
  method: "all",
  flavor: "all",
  minPrice: "",
  maxPrice: "",
}

export function isDishFilterActive(values: DishFilterValues) {
  return (
    values.cuisine !== "all" ||
    values.ingredient !== "all" ||
    values.method !== "all" ||
    values.flavor !== "all" ||
    values.minPrice !== "" ||
    values.maxPrice !== ""
  )
}

type FilterSelectProps = {
  label: string
  value: string
  options: DishFilterOption[]
  onValueChange: (value: string) => void
}

const FilterSelect = ({ label, value, options, onValueChange }: FilterSelectProps) => {
  const selectedOptionLabel = options.find((option) => option.value === value)?.label

  return (
    <Select value={value} onValueChange={onValueChange}>
      <SelectTrigger className="w-full text-left h-auto py-6">
        <div className="flex flex-col items-start">
          <span className="text-xs text-muted-foreground">{label}</span>
          <span className="text-sm font-medium text-foreground">{selectedOptionLabel}</span>
        </div>
      </SelectTrigger>
      <SelectContent>
        {options.map((option) => (
          <SelectItem key={option.value} value={option.value}>
            {option.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}

type FloatingLabelInputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string
}

const FloatingLabelInput = ({ label, id, className, ...props }: FloatingLabelInputProps) => {
  return (
    <div className="relative">
      <Input
        id={id}
        placeholder=" "
        className={cn("peer h-13 pt-6 text-foreground", className)}
        {...props}
      />
      <label
        htmlFor={id}
        className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground transition-all duration-200 ease-in-out peer-placeholder-shown:top-1/2 peer-placeholder-shown:text-base peer-focus:top-4 peer-focus:text-xs peer-[:not(:placeholder-shown)]:top-4 peer-[:not(:placeholder-shown)]:text-xs"
      >
        {label}
      </label>
    </div>
  )
}

type PriceRangeFilterProps = {
  minPrice: string
  maxPrice: string
  onMinPriceChange: (value: string) => void
  onMaxPriceChange: (value: string) => void
}

const PriceRangeFilter = ({ minPrice, maxPrice, onMinPriceChange, onMaxPriceChange }: PriceRangeFilterProps) => {
  return (
    <div className="flex flex-col gap-3">
      <FloatingLabelInput
        id="min-price"
        type="number"
        label="From"
        value={minPrice}
        onChange={(e) => onMinPriceChange(e.target.value)}
      />
      <FloatingLabelInput
        id="max-price"
        type="number"
        label="To"
        value={maxPrice}
        onChange={(e) => onMaxPriceChange(e.target.value)}
      />
    </div>
  )
}

type FilterPopoverProps = {
  filters: DishFilterValues
  filterOptions: Record<FilterKey, DishFilterOption[]>
  onApply: (values: DishFilterValues) => void
}

export function FilterPopover({ filters, filterOptions, onApply }: FilterPopoverProps) {
  const [open, setOpen] = useState(false)
  const [draft, setDraft] = useState<DishFilterValues>(filters)

  useEffect(() => {
    if (open) setDraft(filters)
  }, [open, filters])

  const hasActiveFilters = isDishFilterActive(filters)

  const handleClear = () => {
    onApply(DEFAULT_DISH_FILTER_VALUES)
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
          aria-label="Filters"
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
        aria-label="Filters"
        align="end"
        side="bottom"
        sideOffset={8}
        collisionPadding={16}
        className="flex w-[340px] flex-col gap-0 p-0"
        style={{ maxHeight: "var(--radix-popover-content-available-height)" }}
      >
        <div className="flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto p-4">
          <PriceRangeFilter
            minPrice={draft.minPrice}
            maxPrice={draft.maxPrice}
            onMinPriceChange={(value) => setDraft((prev) => ({ ...prev, minPrice: value }))}
            onMaxPriceChange={(value) => setDraft((prev) => ({ ...prev, maxPrice: value }))}
          />
          <div className="grid grid-cols-2 gap-3">
            <FilterSelect
              label="Ẩm thực"
              value={draft.cuisine}
              options={filterOptions.cuisine}
              onValueChange={(value) => setDraft((prev) => ({ ...prev, cuisine: value }))}
            />
            <FilterSelect
              label="Nguyên liệu chính"
              value={draft.ingredient}
              options={filterOptions.ingredient}
              onValueChange={(value) => setDraft((prev) => ({ ...prev, ingredient: value }))}
            />
            <FilterSelect
              label="Phương pháp chế biến"
              value={draft.method}
              options={filterOptions.method}
              onValueChange={(value) => setDraft((prev) => ({ ...prev, method: value }))}
            />
            <FilterSelect
              label="Hương vị"
              value={draft.flavor}
              options={filterOptions.flavor}
              onValueChange={(value) => setDraft((prev) => ({ ...prev, flavor: value }))}
            />
          </div>
        </div>
        <div className="flex justify-end gap-2 border-t border-border p-4">
          <Button type="button" variant="secondary" onClick={handleClear}>
            Clear All
          </Button>
          <Button type="button" onClick={handleApply}>
            Apply Filters
          </Button>
        </div>
      </PopoverContent>
    </Popover>
  )
}
