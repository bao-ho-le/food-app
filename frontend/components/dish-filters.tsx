"use client"

import { useEffect, useMemo, useState } from "react"
import { SlidersHorizontal } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { cn } from "@/lib/utils"
import { fetchAllTags } from "@/services/tags"
import type { Category, Tag } from "@/types"

export type DishFilterValues = {
  priceRange: string
  ratingMin: string
  tagIds: string[]
  sortOrder: "" | "asc" | "desc"
}

export const DEFAULT_DISH_FILTER_VALUES: DishFilterValues = {
  priceRange: "",
  ratingMin: "",
  tagIds: [],
  sortOrder: "",
}

export function isDishFilterActive(values: DishFilterValues) {
  return values.priceRange !== "" || values.ratingMin !== "" || values.tagIds.length > 0
}

export type PriceRangeOption = { value: string; label: string; min: number; max?: number }

export const PRICE_RANGE_OPTIONS: PriceRangeOption[] = [
  { value: "10-40", label: "10.000đ - 40.000đ", min: 10000, max: 40000 },
  { value: "40-80", label: "40.000đ - 80.000đ", min: 40000, max: 80000 },
  { value: "80+", label: "Trên 80.000đ", min: 80000 },
]

const RATING_OPTIONS = [
  { value: "1", label: "Trên 1 sao" },
  { value: "2", label: "Trên 2 sao" },
  { value: "3", label: "Trên 3 sao" },
  { value: "4", label: "Trên 4 sao" },
]

const SORT_ORDER_OPTIONS = [
  { value: "asc", label: "Giá tăng dần" },
  { value: "desc", label: "Giá giảm dần" },
]

const toggleItemClass = "flex-none rounded-md border border-input px-3 text-xs sm:text-sm"

function getTagChipClasses(categoryName?: string) {
  const normalized = categoryName?.toLowerCase() ?? ""
  if (normalized.includes("ẩm thực")) return "bg-blue-100 text-blue-700 ring-blue-200"
  if (normalized.includes("nguyên liệu")) return "bg-green-100 text-green-700 ring-green-200"
  if (normalized.includes("phương pháp")) return "bg-amber-100 text-amber-800 ring-amber-200"
  if (normalized.includes("hương vị")) return "bg-purple-100 text-purple-700 ring-purple-200"
  return "bg-slate-100 text-slate-700 ring-slate-200"
}

type FilterPopoverProps = {
  filters: DishFilterValues
  onApply: (values: DishFilterValues) => void
  onTagsLoaded?: (tags: Tag[]) => void
}

export function FilterPopover({ filters, onApply, onTagsLoaded }: FilterPopoverProps) {
  const [open, setOpen] = useState(false)
  const [draft, setDraft] = useState<DishFilterValues>(filters)

  const [tagSearch, setTagSearch] = useState("")
  const [tagPopoverOpen, setTagPopoverOpen] = useState(false)
  const [tagOptions, setTagOptions] = useState<Tag[]>([])
  const [tagCategories, setTagCategories] = useState<Category[]>([])
  const [tagsLoading, setTagsLoading] = useState(false)
  const [tagsError, setTagsError] = useState<string | null>(null)
  const tagCategoryNameById = useMemo(
    () => new Map(tagCategories.map((cat) => [cat.id, cat.name])),
    [tagCategories],
  )

  useEffect(() => {
    if (open) setDraft(filters)
  }, [open, filters])

  useEffect(() => {
    let isMounted = true
    const loadTags = async () => {
      setTagsLoading(true)
      setTagsError(null)
      try {
        const data = await fetchAllTags()
        if (!isMounted) return
        const categoryMap = new Map<string, Category>()
        const tags: Tag[] = []
        data.forEach((tag) => {
          const categoryId =
            tag.category?.id !== undefined && tag.category?.id !== null
              ? String(tag.category.id)
              : "uncategorized"
          const categoryName = tag.category?.name?.trim() || "Khác"
          if (!categoryMap.has(categoryId)) {
            categoryMap.set(categoryId, { id: categoryId, name: categoryName })
          }
          tags.push({ id: String(tag.id), name: tag.name.trim(), categoryId })
        })
        setTagCategories(Array.from(categoryMap.values()))
        setTagOptions(tags)
        onTagsLoaded?.(tags)
      } catch (error) {
        if (!isMounted) return
        setTagsError("Không thể tải danh sách tag.")
      } finally {
        if (isMounted) setTagsLoading(false)
      }
    }
    loadTags()
    return () => {
      isMounted = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

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
          <div className="flex flex-col gap-2">
            <Label>Khoảng giá</Label>
            <ToggleGroup
              type="single"
              value={draft.priceRange}
              onValueChange={(value) => setDraft((prev) => ({ ...prev, priceRange: value }))}
              className="w-full flex-wrap justify-start gap-2"
            >
              {PRICE_RANGE_OPTIONS.map((option) => (
                <ToggleGroupItem key={option.value} value={option.value} className={toggleItemClass}>
                  {option.label}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>
          </div>

          <div className="flex flex-col gap-2">
            <Label>Đánh giá</Label>
            <ToggleGroup
              type="single"
              value={draft.ratingMin}
              onValueChange={(value) => setDraft((prev) => ({ ...prev, ratingMin: value }))}
              className="w-full flex-wrap justify-start gap-2"
            >
              {RATING_OPTIONS.map((option) => (
                <ToggleGroupItem key={option.value} value={option.value} className={toggleItemClass}>
                  {option.label}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>
          </div>

          <div className="flex flex-col gap-2">
            <Label>Tag</Label>
            {draft.tagIds.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {draft.tagIds.map((tid) => {
                  const t = tagOptions.find((x) => x.id === tid)
                  const categoryName = t ? tagCategoryNameById.get(t.categoryId) : undefined
                  const color = getTagChipClasses(categoryName)
                  return (
                    <span
                      key={tid}
                      className={`inline-flex items-center gap-1 rounded-full px-2 py-1 text-xs ring-1 ring-inset ${color}`}
                    >
                      {t?.name || tid}
                      <button
                        type="button"
                        className="ml-1 opacity-60 hover:opacity-100"
                        onClick={() => setDraft((prev) => ({ ...prev, tagIds: prev.tagIds.filter((x) => x !== tid) }))}
                      >
                        ×
                      </button>
                    </span>
                  )
                })}
              </div>
            )}
            <Popover open={tagPopoverOpen} onOpenChange={setTagPopoverOpen}>
              <PopoverTrigger asChild>
                <Button type="button" variant="outline" className="w-full justify-between">
                  {draft.tagIds.length > 0 ? `Đã chọn ${draft.tagIds.length} tag` : "Chọn tag"}
                </Button>
              </PopoverTrigger>
              <PopoverContent sideOffset={6} className="w-[300px] p-0">
                <Command>
                  <CommandInput placeholder="Tìm tag theo tên..." value={tagSearch} onValueChange={setTagSearch} />
                  <div className="max-h-[240px] overflow-y-auto" onWheel={(e) => e.stopPropagation()}>
                    <CommandList>
                      <CommandEmpty>
                        {tagsLoading
                          ? "Đang tải danh sách tag..."
                          : tagsError
                            ? "Không thể tải danh sách tag."
                            : "Không tìm thấy tag phù hợp."}
                      </CommandEmpty>
                      {tagCategories.map((cat) => {
                        const catTags = tagOptions.filter(
                          (t) => t.categoryId === cat.id && t.name.toLowerCase().includes(tagSearch.toLowerCase()),
                        )
                        if (catTags.length === 0) return null
                        return (
                          <CommandGroup key={cat.id} heading={cat.name}>
                            {catTags.map((t) => {
                              const active = draft.tagIds.includes(t.id)
                              return (
                                <CommandItem
                                  key={t.id}
                                  value={t.name}
                                  onSelect={() => {
                                    setDraft((prev) => ({
                                      ...prev,
                                      tagIds: prev.tagIds.includes(t.id)
                                        ? prev.tagIds.filter((x) => x !== t.id)
                                        : [...prev.tagIds, t.id],
                                    }))
                                  }}
                                >
                                  <input type="checkbox" className="mr-2" readOnly checked={active} />
                                  <span className="text-sm">{t.name}</span>
                                </CommandItem>
                              )
                            })}
                          </CommandGroup>
                        )
                      })}
                    </CommandList>
                  </div>
                </Command>
              </PopoverContent>
            </Popover>
          </div>

          <div className="flex flex-col gap-2">
            <Label>Sắp xếp theo giá</Label>
            <ToggleGroup
              type="single"
              value={draft.sortOrder}
              onValueChange={(value) =>
                setDraft((prev) => ({ ...prev, sortOrder: value as "" | "asc" | "desc" }))
              }
              className="w-full flex-wrap justify-start gap-2"
            >
              {SORT_ORDER_OPTIONS.map((option) => (
                <ToggleGroupItem key={option.value} value={option.value} className={toggleItemClass}>
                  {option.label}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>
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
