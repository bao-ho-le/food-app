"use client"

import { useEffect, useMemo, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Switch } from "@/components/ui/switch"
import { Skeleton } from "@/components/ui/skeleton"
import { AdminTableFrame } from "@/components/admin/admin-table-frame"
import { AdminFilterPopover, type FilterSectionDef } from "@/components/admin/filter-popover"
import { DetailRow, DetailSection, InfoGrid } from "@/components/admin/detail-dialog"
import { PRICE_RANGE_OPTIONS } from "@/components/dish-filters"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Star, UtensilsCrossed, AlertTriangle, FilterX } from "lucide-react"
import { Search, MoreVertical, Plus, CheckCircle2, Eye, Pencil, Ban, PackagePlus } from "lucide-react"
import type { Dish, AdminDish, Tag, Category } from "@/types"
import { createDish, updateDish, fetchAdminDishes, setDishBlocking, restockDish } from "@/services/dishes"
import { useRestaurants } from "@/hooks/restaurants/use-restaurants"
import { fetchAllTags } from "@/services/tags"
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { useForm } from "react-hook-form"
import { useToast } from "@/hooks/use-toast"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog"
import { cn } from "@/lib/utils"

type FoodFilterValues = { priceRange: string; status: string; sort: string }
const DEFAULT_FOOD_FILTER_VALUES: FoodFilterValues = { priceRange: "all", status: "all", sort: "price_asc" }
const FOOD_FILTER_SECTIONS: FilterSectionDef[] = [
  {
    key: "priceRange",
    label: "Khoảng giá",
    options: [{ value: "all", label: "Tất cả" }, ...PRICE_RANGE_OPTIONS.map((o) => ({ value: o.value, label: o.label }))],
  },
  {
    key: "status",
    label: "Trạng thái món ăn",
    options: [
      { value: "all", label: "Tất cả" },
      { value: "available", label: "Đang bán" },
      { value: "unavailable", label: "Ngưng bán" },
    ],
  },
  {
    key: "sort",
    label: "Sắp xếp",
    options: [
      { value: "price_asc", label: "Giá tiền ↑" },
      { value: "price_desc", label: "Giá tiền ↓" },
      { value: "rating_asc", label: "Đánh giá ↑" },
      { value: "rating_desc", label: "Đánh giá ↓" },
    ],
  },
]

export default function FoodsPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [page, setPage] = useState(1)
  const [filters, setFilters] = useState<FoodFilterValues>(DEFAULT_FOOD_FILTER_VALUES)
  const pageSize = 10
  const [open, setOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const [editing, setEditing] = useState<Dish | null>(null)
  const { toast } = useToast()
  const [confirmDish, setConfirmDish] = useState<Dish | null>(null)
  const [detailDish, setDetailDish] = useState<AdminDish | null>(null)
  const [blockingId, setBlockingId] = useState<string | null>(null)
  const [restockTarget, setRestockTarget] = useState<AdminDish | null>(null)
  const [restockAmount, setRestockAmount] = useState("")
  const [restockSubmitting, setRestockSubmitting] = useState(false)
  const [restaurantSearch, setRestaurantSearch] = useState("")
  const [restaurantLimit, setRestaurantLimit] = useState(10)
  const [restaurantPopoverOpen, setRestaurantPopoverOpen] = useState(false)
  const [tagSearch, setTagSearch] = useState("")
  const [tagPopoverOpen, setTagPopoverOpen] = useState(false)
  const [selectedTags, setSelectedTags] = useState<string[]>([])
  const { data: restaurants, loading: restaurantsLoading, error: restaurantsError, refresh: refreshRestaurants } = useRestaurants()
  const restaurantOptions = useMemo(() => restaurants ?? [], [restaurants])
  const [tagOptions, setTagOptions] = useState<Tag[]>([])
  const [tagCategories, setTagCategories] = useState<Category[]>([])
  const [tagsLoading, setTagsLoading] = useState(false)
  const [tagsError, setTagsError] = useState<string | null>(null)
  const [tagsReload, setTagsReload] = useState(0)
  const refreshTags = () => setTagsReload((v) => v + 1)
  const tagCategoryNameById = useMemo(() => new Map(tagCategories.map((cat) => [cat.id, cat.name])), [tagCategories])

  const getTagChipClasses = (categoryName?: string) => {
    const normalized = categoryName?.toLowerCase() ?? ""
    if (normalized.includes("ẩm thực")) return "bg-blue-100 text-blue-700 ring-blue-200"
    if (normalized.includes("nguyên liệu")) return "bg-green-100 text-green-700 ring-green-200"
    if (normalized.includes("phương pháp")) return "bg-amber-100 text-amber-800 ring-amber-200"
    if (normalized.includes("hương vị")) return "bg-purple-100 text-purple-700 ring-purple-200"
    return "bg-slate-100 text-slate-700 ring-slate-200"
  }

  type CreateDishInput = {
    name: string
    restaurantId: string
    price: string
    imageUrl: string
  }

  const form = useForm<CreateDishInput>({
    defaultValues: { name: "", restaurantId: "", price: "", imageUrl: "" },
    mode: "onTouched",
  })

  // Edit form
  type EditDishInput = CreateDishInput & { tags: string[] }
  const editForm = useForm<EditDishInput>({
    defaultValues: { name: "", restaurantId: "", price: "", imageUrl: "", tags: [] },
    mode: "onTouched",
  })

  useEffect(() => {
    if (editing) {
      const tagIds = editing.tags.map((t) => t.id)
      editForm.reset({
        name: editing.name,
        restaurantId: editing.restaurantId,
        price: String(editing.price),
        imageUrl: editing.image,
        tags: tagIds
      })
      setSelectedTags(tagIds)
    }
  }, [editing])

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
          const categoryId = tag.category?.id !== undefined && tag.category?.id !== null
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
  }, [tagsReload])

  function normalizeId(value: string) {
    const trimmed = value.trim()
    return /^-?\d+$/.test(trimmed) ? Number(trimmed) : trimmed
  }

  async function onSubmit(values: CreateDishInput) {
    try {
      const price = Number(values.price.replace(/\./g, "").trim() || 0)
      const payload = {
        name: values.name.trim(),
        price,
        restaurantId: normalizeId(values.restaurantId),
        tags: selectedTags.map((tagId) => normalizeId(tagId)),
        imageUrl: values.imageUrl.trim(),
      }
      const created = await createDish(payload)
      const fallbackRestaurantName = restaurantOptions.find((r) => r.id === values.restaurantId)?.name
      const nextDish = created.restaurantName === "Không rõ" && fallbackRestaurantName
        ? { ...created, restaurantName: fallbackRestaurantName }
        : created
      setDishes((prev) => [nextDish, ...prev])
      toast({
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">Món ăn đã được thêm thành công!</span>
          </div>
        )
      })
      setOpen(false)
      form.reset()
      setSelectedTags([])
      setRestaurantSearch("")
      setRestaurantLimit(10)
      setTagSearch("")
    } catch (e) {
      toast({ variant: "destructive", title: "Tạo thất bại", description: e instanceof Error ? e.message : "Vui lòng thử lại sau." })
    }
  }

  const [dishes, setDishes] = useState<AdminDish[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [reloadTick, setReloadTick] = useState(0)
  const reloadDishes = () => setReloadTick((v) => v + 1)

  useEffect(() => {
    let isMounted = true
    setIsLoading(true)
    setLoadError(null)
    fetchAdminDishes()
      .then((data) => {
        if (isMounted) {
          setDishes(data)
        }
      })
      .catch((error) => {
        if (isMounted) {
          setLoadError(error instanceof Error ? error.message : "Không thể tải danh sách món ăn.")
        }
      })
      .finally(() => {
        if (isMounted) setIsLoading(false)
      })
    return () => {
      isMounted = false
    }
  }, [reloadTick])

  function resetFilters() {
    setFilters(DEFAULT_FOOD_FILTER_VALUES)
    setSearchQuery('')
    setPage(1)
  }

  const priceRangeByValue = useMemo(() => new Map(PRICE_RANGE_OPTIONS.map((o) => [o.value, o])), [])

  const filtered = useMemo(() => {
    const q = searchQuery.toLowerCase().trim()
    const priceOption = filters.priceRange !== "all" ? priceRangeByValue.get(filters.priceRange) : undefined
    return dishes.filter((d) => {
      const restaurantName = "restaurantName" in d ? d.restaurantName : restaurantOptions.find(r => r.id === (d as Dish).restaurantId)?.name
      const matchesText = !q || (
        d.name.toLowerCase().includes(q) ||
        restaurantName?.toLowerCase().includes(q)
      )
      const matchesPrice = !priceOption || (d.price >= priceOption.min && (priceOption.max === undefined || d.price <= priceOption.max))
      const matchesStatus = filters.status === "all" || (filters.status === "available" ? d.isAvailable : !d.isAvailable)
      return matchesText && matchesPrice && matchesStatus
    })
  }, [searchQuery, dishes, filters, priceRangeByValue, restaurantOptions])

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const sorted = useMemo(() => {
    const list = [...filtered]
    switch (filters.sort) {
      case "price_desc":
        list.sort((a,b)=> b.price - a.price); break
      case "rating_asc":
        list.sort((a,b)=> a.rating - b.rating); break
      case "rating_desc":
        list.sort((a,b)=> b.rating - a.rating); break
      default:
        list.sort((a,b)=> a.price - b.price)
    }
    return list
  }, [filtered, filters.sort])

  const paged = useMemo(() => {
    const safePage = Math.min(Math.max(1, page), totalPages)
    const start = (safePage - 1) * pageSize
    return sorted.slice(start, start + pageSize)
  }, [sorted, page, totalPages])

  useEffect(() => { setPage(1) }, [searchQuery, filters])
  useEffect(() => { if (page > totalPages) setPage(totalPages) }, [totalPages, page])

  const getRestaurantName = (dish: AdminDish | Dish) => {
    if ("restaurantName" in dish) return dish.restaurantName
    return restaurantOptions.find((r) => r.id === dish.restaurantId)?.name || "-"
  }

  return (
    <div className="flex flex-1 min-h-0 flex-col gap-8 px-18 pt-6 pb-10 bg-background">
      <div className="shrink-0 flex items-center justify-between gap-3 text-[15px] sm:text-base">
        <div className="flex items-center gap-2">
          <div className="relative w-full sm:w-64">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm theo tên món hoặc quán..."
              value={searchQuery}
              onChange={(e) => { setSearchQuery(e.target.value); setPage(1) }}
              className="pl-10 h-10"
            />
          </div>
          <AdminFilterPopover
            sections={FOOD_FILTER_SECTIONS}
            values={filters}
            defaultValues={DEFAULT_FOOD_FILTER_VALUES}
            onApply={setFilters}
          />
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />Thêm món ăn
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader className="items-center text-center">
              <DialogTitle className="text-center">Thêm món ăn</DialogTitle>
            </DialogHeader>
            <Form {...form}>
              <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
                <FormField
                  control={form.control}
                  name="name"
                  rules={{ required: "Vui lòng nhập tên món ăn" }}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tên món ăn</FormLabel>
                      <FormControl>
                        <Input placeholder="VD: Cơm tấm sườn bì" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="restaurantId"
                  rules={{ required: "Vui lòng chọn quán ăn" }}
                  render={({ field }) => {
                    const filtered = restaurantOptions
                      .filter(r => r.name.toLowerCase().includes(restaurantSearch.toLowerCase()))
                      .slice(0, restaurantLimit)
                    const totalMatches = restaurantOptions.filter(r => r.name.toLowerCase().includes(restaurantSearch.toLowerCase())).length
                    const selectedName = restaurantOptions.find(r => r.id === field.value)?.name
                    const emptyMessage = restaurantsLoading
                      ? "Đang tải danh sách quán..."
                      : restaurantsError
                      ? "Không thể tải danh sách quán."
                      : "Không tìm thấy quán phù hợp."
                    return (
                      <FormItem>
                        <FormLabel>Quán ăn</FormLabel>
                        <FormControl>
                          <Popover open={restaurantPopoverOpen} onOpenChange={setRestaurantPopoverOpen}>
                            <PopoverTrigger asChild>
                              <Button type="button" variant="outline" role="combobox" className="w-full justify-between">
                                {selectedName || "Chọn quán"}
                              </Button>
                            </PopoverTrigger>
                            <PopoverContent sideOffset={6} className="w-[420px] p-0">
                              <Command>
                                <CommandInput placeholder="Tìm quán theo tên..." value={restaurantSearch} onValueChange={setRestaurantSearch} />
                                <div className="max-h-[300px] overflow-y-auto" onWheel={(e) => e.stopPropagation()}>
                                  <CommandList>
                                    <CommandEmpty>{emptyMessage}</CommandEmpty>
                                    <CommandGroup heading="Quán ăn">
                                      {filtered.map(r => (
                                        <CommandItem key={r.id} value={r.name} onSelect={() => { field.onChange(r.id); setRestaurantPopoverOpen(false) }}>
                                          {r.name}
                                        </CommandItem>
                                      ))}
                                    </CommandGroup>
                                  </CommandList>
                                </div>
                                {totalMatches > restaurantLimit && (
                                  <div className="p-2 border-t flex justify-center">
                                    <Button type="button" variant="ghost" size="sm" onClick={() => setRestaurantLimit(l => l + 20)}>
                                      Xem thêm
                                    </Button>
                                  </div>
                                )}
                                {restaurantsError && (
                                  <div className="p-2 border-t flex justify-center">
                                    <Button type="button" variant="ghost" size="sm" onClick={refreshRestaurants}>
                                      Thử tải lại
                                    </Button>
                                  </div>
                                )}
                              </Command>
                            </PopoverContent>
                          </Popover>
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )
                  }}
                />
                {/* Tags multi-select */}
                <FormItem>
                  <FormLabel>Tag</FormLabel>
                  <FormControl>
                    <div className="space-y-2">
                      {selectedTags.length > 0 && (
                        <div className="flex flex-wrap gap-2">
                          {selectedTags.map(tid => {
                            const t = tagOptions.find(x => x.id === tid)
                            const catId = t?.categoryId
                            const categoryName = catId ? tagCategoryNameById.get(catId) : undefined
                            const color = getTagChipClasses(categoryName)
                            return (
                              <span key={tid} className={`inline-flex items-center gap-1 rounded-full px-2 py-1 text-xs ring-1 ring-inset ${color}`}>
                                {t?.name || tid}
                                <button type="button" className="ml-1 opacity-60 hover:opacity-100" onClick={() => setSelectedTags(prev => prev.filter(x => x !== tid))}>×</button>
                              </span>
                            )
                          })}
                        </div>
                      )}
                      <Popover open={tagPopoverOpen} onOpenChange={setTagPopoverOpen}>
                        <PopoverTrigger asChild>
                          <Button type="button" variant="outline" className="w-full justify-between">
                            {selectedTags.length > 0 ? `Đã chọn ${selectedTags.length} tag` : "Chọn tag"}
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent sideOffset={6} className="w-[520px] p-0">
                          <Command>
                            <CommandInput placeholder="Tìm tag theo tên..." value={tagSearch} onValueChange={setTagSearch} />
                            <div className="max-h-[320px] overflow-y-auto" onWheel={(e)=>e.stopPropagation()}>
                              <CommandList>
                                <CommandEmpty>
                                  {tagsLoading ? "Đang tải danh sách tag..." : tagsError ? "Không thể tải danh sách tag." : "Không tìm thấy tag phù hợp."}
                                </CommandEmpty>
                                {tagCategories.map(cat => {
                                  const catTags = tagOptions.filter(t => t.categoryId === cat.id && t.name.toLowerCase().includes(tagSearch.toLowerCase()))
                                  if (catTags.length === 0) return null
                                  return (
                                    <CommandGroup key={cat.id} heading={cat.name}>
                                      {catTags.map(t => {
                                        const active = selectedTags.includes(t.id)
                                        return (
                                          <CommandItem key={t.id} value={t.name} onSelect={() => {
                                            setSelectedTags(prev => prev.includes(t.id) ? prev.filter(x=>x!==t.id) : [...prev, t.id])
                                          }}>
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
                            {tagsError && (
                              <div className="p-2 border-t flex justify-center">
                                <Button type="button" variant="ghost" size="sm" onClick={refreshTags}>
                                  Thử tải lại
                                </Button>
                              </div>
                            )}
                          </Command>
                        </PopoverContent>
                      </Popover>
                    </div>
                  </FormControl>
                </FormItem>
                <FormField
                  control={form.control}
                  name="price"
                  rules={{
                    required: "Vui lòng nhập giá tiền",
                    validate: (v) => {
                      const n = Number((v || "").replace(/\./g, "").trim())
                      return n > 1000 || "Giá tiền phải lớn hơn 1000"
                    }
                  }}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Giá tiền (VND)</FormLabel>
                      <FormControl>
                        <Input inputMode="numeric" placeholder="VD: 45000" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="imageUrl"
                  rules={{
                    required: "Vui lòng nhập link ảnh",
                    pattern: { value: /^(https?:\/\/).+$/i, message: "Link ảnh không hợp lệ" }
                  }}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Link ảnh</FormLabel>
                      <FormControl>
                        <Input placeholder="https://..." {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <DialogFooter>
                  <Button type="button" variant="outline" onClick={() => setOpen(false)}>
                    Hủy
                  </Button>
                  <Button type="submit">Lưu</Button>
                </DialogFooter>
              </form>
            </Form>
          </DialogContent>
        </Dialog>
      </div>

      {loadError ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon" className="bg-destructive/10 text-destructive">
                  <AlertTriangle />
                </EmptyMedia>
                <EmptyTitle>Không thể tải danh sách món ăn</EmptyTitle>
                <EmptyDescription>{loadError}</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button variant="outline" onClick={reloadDishes}>Thử lại</Button>
              </EmptyContent>
            </Empty>
          ) : isLoading ? (
            null
          ) : dishes.length === 0 ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <UtensilsCrossed />
                </EmptyMedia>
                <EmptyTitle>Chưa có món ăn nào</EmptyTitle>
                <EmptyDescription>Chưa có món ăn nào trong hệ thống. Bắt đầu bằng cách thêm món ăn đầu tiên.</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button onClick={() => setOpen(true)}>
                  <Plus className="mr-2 h-4 w-4" />Thêm món ăn
                </Button>
              </EmptyContent>
            </Empty>
          ) : filtered.length === 0 ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <FilterX />
                </EmptyMedia>
                <EmptyTitle>Không có món ăn phù hợp</EmptyTitle>
                <EmptyDescription>Không có món ăn phù hợp với bộ lọc hiện tại.</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button variant="outline" onClick={resetFilters}>Xoá bộ lọc</Button>
              </EmptyContent>
            </Empty>
          ) : (
            <AdminTableFrame
              className="flex-1 text-[15px] sm:text-base"
              page={page}
              totalPages={totalPages}
              onPageChange={setPage}
              caption={
                <>Hiển thị {(filtered.length === 0) ? 0 : (page - 1) * pageSize + 1}
                –{Math.min(page * pageSize, filtered.length)} trong tổng {filtered.length}</>
              }
            >
              <Table className="table-fixed [&_th]:py-4 [&_td]:py-4 [&_th]:px-6 [&_td]:px-6 [&_td:last-child]:py-2">
                <TableHeader className="sticky top-0 z-10 bg-background">
                  <TableRow>
                    <TableHead className="w-[8%]">Hình ảnh</TableHead>
                    <TableHead className="w-[26%]">Tên món</TableHead>
                    <TableHead className="w-[14%]">Tên quán</TableHead>
                    <TableHead className="w-[12%]">Giá tiền</TableHead>
                    <TableHead className="w-[10%]">Tồn kho</TableHead>
                    <TableHead className="w-[10%]">Đánh giá</TableHead>
                    <TableHead className="w-[10%]">Tình trạng</TableHead>
                    <TableHead className="w-[10%] text-right">Thao tác</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {paged.map((d, idx) => (
                    <TableRow className={cn("hover:bg-muted/40", idx === paged.length - 1 && "!border-b")} key={d.id}>
                      <TableCell>
                        <img src={d.image} alt={d.name} className="h-12 w-16 object-cover rounded-md border" />
                      </TableCell>
                      <TableCell>
                        <span className="block truncate" title={d.name}>{d.name}</span>
                      </TableCell>
                      <TableCell>
                        <span className="block truncate" title={getRestaurantName(d)}>{getRestaurantName(d)}</span>
                      </TableCell>
                      <TableCell>
                        {d.price.toLocaleString("vi-VN")}₫
                      </TableCell>
                      <TableCell>
                        <span className={cn("tabular-nums font-medium", d.stockQuantity <= 0 && "text-rose-600")}>
                          {d.stockQuantity}
                        </span>
                      </TableCell>
                      <TableCell>
                        <div className="inline-flex items-center gap-1">
                          <Star className="h-4 w-4 text-amber-500 fill-amber-500" />
                          <span className="tabular-nums font-medium">{d.rating.toFixed(1)}</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        {d.isAvailable ? (
                          <span className="inline-flex items-center rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-200">Đang bán</span>
                        ) : (
                          <span className="inline-flex items-center rounded-full bg-rose-100 px-2.5 py-1 text-xs font-medium text-rose-700 ring-1 ring-inset ring-rose-200">Ngưng bán</span>
                        )}
                      </TableCell>
                      <TableCell className="text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <MoreVertical className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => setDetailDish(d)}>
                              <Eye className="h-4 w-4" />Xem chi tiết
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => { setRestockTarget(d); setRestockAmount("") }}>
                              <PackagePlus className="h-4 w-4" />Nhập kho
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => { setEditing(d); setEditOpen(true) }}>
                              <Pencil className="h-4 w-4" />Chỉnh sửa
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => setConfirmDish(d)}>
                              {d.isAvailable ? <Ban className="h-4 w-4" /> : <CheckCircle2 className="h-4 w-4" />}
                              {d.isAvailable ? "Tạm ngưng bán" : "Mở bán lại"}
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </AdminTableFrame>
          )}

      {/* Dish detail dialog */}
      <Dialog open={!!detailDish} onOpenChange={(o) => { if (!o) setDetailDish(null) }}>
        <DialogContent className="sm:max-w-xl">
          <DialogHeader className="sr-only">
            <DialogTitle>{detailDish?.name}</DialogTitle>
          </DialogHeader>
          {detailDish && (
            <>
              <div className="flex flex-col gap-5 sm:flex-row sm:items-start">
                <img
                  src={detailDish.image || "/placeholder-dish.png"}
                  alt={detailDish.name}
                  className="h-40 w-40 shrink-0 self-center rounded-lg border object-cover sm:self-start"
                />
                <div className="min-w-0 flex-1 space-y-3">
                  <div className="space-y-1">
                    <h3 className="text-lg font-semibold">{detailDish.name}</h3>
                    <p className="text-xl font-bold text-primary">{detailDish.price.toLocaleString("vi-VN")}₫</p>
                    {detailDish.isAvailable ? (
                      <span className="inline-flex items-center rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-200">Đang bán</span>
                    ) : (
                      <span className="inline-flex items-center rounded-full bg-rose-100 px-2.5 py-1 text-xs font-medium text-rose-700 ring-1 ring-inset ring-rose-200">Ngưng bán</span>
                    )}
                  </div>
                  <div className="flex items-center gap-1">
                    <Star className="h-4 w-4 text-amber-500 fill-amber-500" />
                    <span className="font-medium tabular-nums">{detailDish.rating.toFixed(1)}</span>
                  </div>
                  {detailDish.tags.length > 0 && (
                    <div className="flex flex-wrap gap-2">
                      {detailDish.tags.map((t) => (
                        <span key={t.id} className="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-1 text-xs text-slate-700 ring-1 ring-inset ring-slate-200">
                          {t.name}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </div>
              <DetailSection title="Tồn kho">
                <InfoGrid className="grid-cols-1">
                  <DetailRow label="Số lượng còn lại" value={String(detailDish.stockQuantity)} />
                </InfoGrid>
              </DetailSection>
              <DetailSection title="Thông tin quán ăn">
                <InfoGrid className="grid-cols-1">
                  <DetailRow label="Tên quán" value={detailDish.restaurantName} />
                  <DetailRow label="Địa chỉ" value={detailDish.restaurantAddress || "Chưa cập nhật"} />
                  <DetailRow label="Số điện thoại" value={detailDish.restaurantPhone || "Chưa cập nhật"} />
                </InfoGrid>
              </DetailSection>
            </>
          )}
        </DialogContent>
      </Dialog>

      {/* Confirm toggle availability */}
      <AlertDialog open={!!confirmDish} onOpenChange={(o)=>{ if(!o) setConfirmDish(null) }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {confirmDish?.isAvailable ? 'Xác nhận tạm ngưng bán' : 'Xác nhận mở bán lại'}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {confirmDish?.isAvailable
                ? `Bạn có chắc chắn muốn tạm ngưng bán món "${confirmDish?.name}"? Người dùng sẽ không thể đặt món cho đến khi mở bán lại.`
                : `Bạn có chắc chắn muốn mở bán lại món "${confirmDish?.name}"? Món sẽ hiển thị cho người dùng đặt.`}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={()=>setConfirmDish(null)}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              disabled={!confirmDish || blockingId === confirmDish?.id}
              onClick={async ()=>{
                if(confirmDish){
                  const next = !confirmDish.isAvailable
                  const type = next ? 1 : 0
                  setBlockingId(confirmDish.id)
                  try {
                    await setDishBlocking(normalizeId(confirmDish.id), type)
                    setDishes((prev) => prev.map((dish) => dish.id === confirmDish.id ? { ...dish, isAvailable: next } : dish))
                    toast({ title: (
                      <div className="flex items-center gap-3">
                        <CheckCircle2 className="h-5 w-5 text-green-500" />
                        <span className="font-medium">{next ? 'Đã mở bán lại món ăn' : 'Đã tạm ngưng bán món ăn'}</span>
                      </div>
                    )})
                    setConfirmDish(null)
                  } catch (e) {
                    toast({ variant: "destructive", title: "Cập nhật thất bại", description: e instanceof Error ? e.message : "Vui lòng thử lại sau." })
                  } finally {
                    setBlockingId(null)
                  }
                }
              }}
            >
              Xác nhận
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Restock dialog */}
      <Dialog open={!!restockTarget} onOpenChange={(o) => { if (!o) { setRestockTarget(null); setRestockAmount("") } }}>
        <DialogContent className="sm:max-w-sm">
          <DialogHeader>
            <DialogTitle>Nhập kho: {restockTarget?.name}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <p className="text-sm text-muted-foreground">
              Tồn kho hiện tại: <span className="font-medium text-foreground">{restockTarget?.stockQuantity}</span>
            </p>
            <Input
              type="number"
              min="1"
              placeholder="Số lượng nhập thêm"
              value={restockAmount}
              onChange={(e) => setRestockAmount(e.target.value)}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => { setRestockTarget(null); setRestockAmount("") }}>Hủy</Button>
            <Button
              disabled={restockSubmitting || !restockTarget || !Number.isInteger(Number(restockAmount)) || Number(restockAmount) <= 0}
              onClick={async () => {
                if (!restockTarget) return
                const amount = Number(restockAmount)
                setRestockSubmitting(true)
                try {
                  const updated = await restockDish(normalizeId(restockTarget.id), amount)
                  setDishes((prev) => prev.map((dish) => dish.id === restockTarget.id ? { ...dish, stockQuantity: updated.stockQuantity } : dish))
                  toast({
                    title: (
                      <div className="flex items-center gap-3">
                        <CheckCircle2 className="h-5 w-5 text-green-500" />
                        <span className="font-medium">Đã nhập thêm {amount} phần vào kho</span>
                      </div>
                    ),
                  })
                  setRestockTarget(null)
                  setRestockAmount("")
                } catch (e) {
                  toast({ variant: "destructive", title: "Nhập kho thất bại", description: e instanceof Error ? e.message : "Vui lòng thử lại sau." })
                } finally {
                  setRestockSubmitting(false)
                }
              }}
            >
              Xác nhận nhập kho
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Dish Dialog */}
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent>
          <DialogHeader className="items-center text-center">
            <DialogTitle className="text-center">Chỉnh sửa món ăn</DialogTitle>
          </DialogHeader>
          <Form {...editForm}>
            <form className="space-y-4" onSubmit={editForm.handleSubmit(async (values)=>{
              if (!editing) return
              try {
                const payload = {
                  name: values.name.trim(),
                  price: Number(values.price.replace(/\./g, "").trim() || 0),
                  restaurantId: normalizeId(values.restaurantId),
                  tags: selectedTags.map((tagId) => normalizeId(tagId)),
                  imageUrl: values.imageUrl.trim(),
                }
                const updated = await updateDish(editing.id, payload)
                const fallbackRestaurantName = restaurantOptions.find((r) => r.id === values.restaurantId)?.name
                const nextDish = updated.restaurantName === "Không rõ" && fallbackRestaurantName
                  ? { ...updated, restaurantName: fallbackRestaurantName }
                  : updated
                setDishes((prev) => prev.map((d) => (d.id === editing.id ? nextDish : d)))
                toast({ title: (
                  <div className="flex items-center gap-3">
                    <CheckCircle2 className="h-5 w-5 text-green-500" />
                    <span className="font-medium">Đã cập nhật món ăn!</span>
                  </div>
                )})
                setEditOpen(false)
                setEditing(null)
              } catch(e) {
                toast({ variant:'destructive', title:'Cập nhật thất bại', description: e instanceof Error ? e.message : 'Vui lòng thử lại sau.' })
              }
            })}>
              <FormField control={editForm.control} name="name" rules={{ required:'Vui lòng nhập tên món ăn' }} render={({field})=> (
                <FormItem>
                  <FormLabel>Tên món ăn</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )} />
              <FormField control={editForm.control} name="restaurantId" rules={{ required:'Vui lòng chọn quán ăn' }} render={({field})=>{
                const filtered = restaurantOptions.filter(r=> r.name.toLowerCase().includes(restaurantSearch.toLowerCase())).slice(0, restaurantLimit)
                const totalMatches = restaurantOptions.filter(r => r.name.toLowerCase().includes(restaurantSearch.toLowerCase())).length
                const selectedName = restaurantOptions.find(r=> r.id === field.value)?.name
                const emptyMessage = restaurantsLoading
                  ? "Đang tải danh sách quán..."
                  : restaurantsError
                  ? "Không thể tải danh sách quán."
                  : "Không tìm thấy quán phù hợp."
                return (
                  <FormItem>
                    <FormLabel>Quán ăn</FormLabel>
                    <FormControl>
                      <Popover open={restaurantPopoverOpen} onOpenChange={setRestaurantPopoverOpen}>
                        <PopoverTrigger asChild>
                          <Button type="button" variant="outline" role="combobox" className="w-full justify-between">
                            {selectedName || 'Chọn quán'}
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent sideOffset={6} className="w-[420px] p-0">
                          <Command>
                            <CommandInput placeholder="Tìm quán theo tên..." value={restaurantSearch} onValueChange={setRestaurantSearch} />
                            <div className="max-h-[300px] overflow-y-auto" onWheel={(e)=>e.stopPropagation()}>
                              <CommandList>
                                <CommandEmpty>{emptyMessage}</CommandEmpty>
                                <CommandGroup heading="Quán ăn">
                                  {filtered.map(r=> (
                                    <CommandItem key={r.id} value={r.name} onSelect={()=>{ field.onChange(r.id); setRestaurantPopoverOpen(false) }}>
                                      {r.name}
                                    </CommandItem>
                                  ))}
                                </CommandGroup>
                              </CommandList>
                            </div>
                            {totalMatches > restaurantLimit && (
                              <div className="p-2 border-t flex justify-center">
                                <Button type="button" variant="ghost" size="sm" onClick={() => setRestaurantLimit(l => l + 20)}>
                                  Xem thêm
                                </Button>
                              </div>
                            )}
                            {restaurantsError && (
                              <div className="p-2 border-t flex justify-center">
                                <Button type="button" variant="ghost" size="sm" onClick={refreshRestaurants}>
                                  Thử tải lại
                                </Button>
                              </div>
                            )}
                          </Command>
                        </PopoverContent>
                      </Popover>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )
              }} />

              {/* Tags (reuse chips and popover) */}
              <FormItem>
                <FormLabel>Tag</FormLabel>
                <FormControl>
                  <div className="space-y-2">
                    {selectedTags.length > 0 && (
                      <div className="flex flex-wrap gap-2">
                        {selectedTags.map(tid => {
                          const t = tagOptions.find(x => x.id === tid)
                          const catId = t?.categoryId
                          const categoryName = catId ? tagCategoryNameById.get(catId) : undefined
                          const color = getTagChipClasses(categoryName)
                          return (
                            <span key={tid} className={`inline-flex items-center gap-1 rounded-full px-2 py-1 text-xs ring-1 ring-inset ${color}`}>
                              {t?.name || tid}
                              <button type="button" className="ml-1 opacity-60 hover:opacity-100" onClick={() => setSelectedTags(prev => prev.filter(x => x !== tid))}>×</button>
                            </span>
                          )
                        })}
                      </div>
                    )}
                    <Popover open={tagPopoverOpen} onOpenChange={setTagPopoverOpen}>
                      <PopoverTrigger asChild>
                        <Button type="button" variant="outline" className="w-full justify-between">
                          {selectedTags.length > 0 ? `Đã chọn ${selectedTags.length} tag` : "Chọn tag"}
                        </Button>
                      </PopoverTrigger>
                      <PopoverContent sideOffset={6} className="w-[520px] p-0">
                        <Command>
                          <CommandInput placeholder="Tìm tag theo tên..." value={tagSearch} onValueChange={setTagSearch} />
                          <div className="max-h-[320px] overflow-y-auto" onWheel={(e)=>e.stopPropagation()}>
                            <CommandList>
                              <CommandEmpty>
                                {tagsLoading ? "Đang tải danh sách tag..." : tagsError ? "Không thể tải danh sách tag." : "Không tìm thấy tag phù hợp."}
                              </CommandEmpty>
                              {tagCategories.map(cat => {
                                const catTags = tagOptions.filter(t => t.categoryId === cat.id && t.name.toLowerCase().includes(tagSearch.toLowerCase()))
                                if (catTags.length === 0) return null
                                return (
                                  <CommandGroup key={cat.id} heading={cat.name}>
                                    {catTags.map(t => {
                                      const active = selectedTags.includes(t.id)
                                      return (
                                        <CommandItem key={t.id} value={t.name} onSelect={() => {
                                          setSelectedTags(prev => prev.includes(t.id) ? prev.filter(x=>x!==t.id) : [...prev, t.id])
                                        }}>
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
                          {tagsError && (
                            <div className="p-2 border-t flex justify-center">
                              <Button type="button" variant="ghost" size="sm" onClick={refreshTags}>
                                Thử tải lại
                              </Button>
                            </div>
                          )}
                        </Command>
                      </PopoverContent>
                    </Popover>
                  </div>
                </FormControl>
              </FormItem>

              <FormField control={editForm.control} name="price" rules={{
                required:'Vui lòng nhập giá tiền',
                validate:(v)=>{ const n = Number((v||'').replace(/\./g,'').trim()); return n>1000 || 'Giá tiền phải lớn hơn 1000' }
              }} render={({field})=> (
                <FormItem>
                  <FormLabel>Giá tiền (VND)</FormLabel>
                  <FormControl>
                    <Input inputMode="numeric" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )} />

              <FormField control={editForm.control} name="imageUrl" rules={{ required:'Vui lòng nhập link ảnh', pattern:{ value:/^(https?:\/\/).+$/i, message:'Link ảnh không hợp lệ' } }} render={({field})=> (
                <FormItem>
                  <FormLabel>Link ảnh</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )} />
              <DialogFooter>
                <Button type="button" variant="outline" onClick={()=>{ setEditOpen(false); setEditing(null) }}>Hủy</Button>
                <Button type="submit">Lưu</Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
