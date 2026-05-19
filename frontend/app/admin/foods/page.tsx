"use client"

import { useEffect, useMemo, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Switch } from "@/components/ui/switch"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Star, RefreshCw } from "lucide-react"
import { Search, MoreVertical, Plus, CheckCircle2 } from "lucide-react"
import type { Dish, AdminDish, Tag, Category } from "@/types"
import { createDish, fetchAdminDishes, setDishBlocking } from "@/services/dishes"
import { useRestaurants } from "@/hooks/restaurants/use-restaurants"
import { fetchAllTags } from "@/services/tags"
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { useForm } from "react-hook-form"
import { Textarea } from "@/components/ui/textarea"
import { useToast } from "@/hooks/use-toast"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog"

export default function FoodsPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [page, setPage] = useState(1)
  const [minPrice, setMinPrice] = useState<string>("")
  const [maxPrice, setMaxPrice] = useState<string>("")
  const [status, setStatus] = useState<"all"|"available"|"unavailable">("all")
  const pageSize = 10
  const [sortBy, setSortBy] = useState<"price_asc"|"price_desc"|"rating_asc"|"rating_desc">("price_asc")
  const [open, setOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const [editing, setEditing] = useState<Dish | null>(null)
  const { toast } = useToast()
  const [confirmDish, setConfirmDish] = useState<Dish | null>(null)
  const [blockingId, setBlockingId] = useState<string | null>(null)
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
    description: string
    price: string
    imageUrl: string
  }

  const form = useForm<CreateDishInput>({
    defaultValues: { name: "", restaurantId: "", description: "", price: "", imageUrl: "" },
    mode: "onTouched",
  })

  // Edit form
  type EditDishInput = CreateDishInput & { tags: string[] }
  const editForm = useForm<EditDishInput>({
    defaultValues: { name: "", restaurantId: "", description: "", price: "", imageUrl: "", tags: [] },
    mode: "onTouched",
  })

  useEffect(() => {
    if (editing) {
      editForm.reset({
        name: editing.name,
        restaurantId: editing.restaurantId,
        description: editing.description,
        price: String(editing.price),
        imageUrl: editing.image,
        tags: editing.tags
      })
      setSelectedTags(editing.tags)
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
      toast({ variant: "destructive", title: "Tạo thất bại", description: "Vui lòng thử lại sau." })
    }
  }

  const [dishes, setDishes] = useState<AdminDish[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)

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
  }, [])

  const filtered = useMemo(() => {
    const q = searchQuery.toLowerCase().trim()
    const clean = (v: string) => v.replace(/\./g, "").trim()
    const min = clean(minPrice) ? Number(clean(minPrice)) : undefined
    const max = clean(maxPrice) ? Number(clean(maxPrice)) : undefined
    return dishes.filter((d) => {
      const restaurantName = "restaurantName" in d ? d.restaurantName : restaurantOptions.find(r => r.id === (d as Dish).restaurantId)?.name
      const matchesText = !q || (
        d.name.toLowerCase().includes(q) ||
        restaurantName?.toLowerCase().includes(q)
      )
      const matchesPrice = (min === undefined || d.price >= min) && (max === undefined || d.price <= max)
      const matchesStatus = status === "all" || (status === "available" ? d.isAvailable : !d.isAvailable)
      return matchesText && matchesPrice && matchesStatus
    })
  }, [searchQuery, dishes, minPrice, maxPrice, status, restaurantOptions])

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const sorted = useMemo(() => {
    const list = [...filtered]
    switch (sortBy) {
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
  }, [filtered, sortBy])

  const paged = useMemo(() => {
    const safePage = Math.min(Math.max(1, page), totalPages)
    const start = (safePage - 1) * pageSize
    return sorted.slice(start, start + pageSize)
  }, [sorted, page, totalPages])

  useEffect(() => { setPage(1) }, [searchQuery])
  useEffect(() => { if (page > totalPages) setPage(totalPages) }, [totalPages, page])

  const getRestaurantName = (dish: AdminDish | Dish) => {
    if ("restaurantName" in dish) return dish.restaurantName
    return restaurantOptions.find((r) => r.id === dish.restaurantId)?.name || "-"
  }

  return (
    <div className="space-y-8 px-18 py-10 bg-background flex-1">
      <div className="text-center">
        <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight pb-3">Quản lý món ăn</h1>
        <div className="mx-auto mt-2 h-1 w-24 rounded bg-foreground/80" />
      </div>

      <div className="flex items-center justify-between gap-3 text-[15px] sm:text-base">
        <div className="relative flex-1 sm:max-w-[22rem]">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm theo tên món hoặc quán..."
              value={searchQuery}
              onChange={(e) => { setSearchQuery(e.target.value); setPage(1) }}
              className="pl-10 h-10"
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
                <FormField
                  control={form.control}
                  name="description"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Mô tả</FormLabel>
                      <FormControl>
                        <Textarea rows={3} placeholder="Mô tả ngắn gọn về món ăn" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
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

      <div className="flex flex-wrap items-end gap-4 justify-between text-[15px] sm:text-base">
        <div className="flex items-end gap-6">
          <span className="text-sm font-medium pb-2">Mức giá</span>
          <div className="flex items-center gap-2">
            <Input placeholder="Mức giá từ" value={minPrice} onChange={(e)=>setMinPrice(e.target.value)} className="w-40" inputMode="numeric"/>
            <span className="text-muted-foreground">-</span>
            <Input placeholder="Mức giá đến" value={maxPrice} onChange={(e)=>setMaxPrice(e.target.value)} className="w-40" inputMode="numeric"/>
          </div>
          <div className="flex items-center gap-3 pl-6">
            <span className="text-sm font-medium">Tình trạng</span>
            <Select value={status} onValueChange={(v)=>setStatus(v as typeof status)}>
              <SelectTrigger className="w-40 h-10">
                <SelectValue placeholder="Chọn tình trạng" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Tất cả</SelectItem>
                <SelectItem value="available">Đang bán</SelectItem>
                <SelectItem value="unavailable">Ngưng bán</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        <div className="ml-auto flex items-center gap-2">
          <Select value={sortBy} onValueChange={(v)=>{ setSortBy(v as typeof sortBy); setPage(1) }}>
            <SelectTrigger className="h-10 w-31 justify-between">
              <SelectValue placeholder="Sắp xếp" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="price_desc">Giá tiền ↓</SelectItem>
              <SelectItem value="price_asc">Giá tiền ↑</SelectItem>
              <SelectItem value="rating_desc">Đánh giá ↓</SelectItem>
              <SelectItem value="rating_asc">Đánh giá ↑</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" size="icon" className="h-10 w-10" title="Đặt lại bộ lọc" onClick={() => { setStatus('all'); setMinPrice(''); setMaxPrice(''); setSearchQuery(''); setPage(1); setSortBy('price_asc') }}>
            <RefreshCw className="h-4 w-4" />
          </Button>
        </div>
      </div>

      <div className="overflow-x-auto rounded-lg border text-[15px] sm:text-base">
        <Table className="[&_th]:py-4 [&_td]:py-3 [&_th]:px-6 [&_td]:px-6">
          <TableHeader>
            <TableRow>
              <TableHead className="w-[10%] min-w-[100px]">Hình ảnh</TableHead>
              <TableHead className="w-[26%] min-w-[200px]">Tên món</TableHead>
              <TableHead className="w-[26%] min-w-[220px]">Tên quán</TableHead>
              <TableHead className="w-[12%] min-w-[120px]">Giá tiền</TableHead>
              <TableHead className="w-[10%]">Đánh giá</TableHead>
              <TableHead className="w-[12%]">Tình trạng</TableHead>
              <TableHead className="w-[8%] text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loadError && (
              <TableRow>
                <TableCell colSpan={7} className="h-32 text-center text-destructive">
                  {loadError}
                </TableCell>
              </TableRow>
            )}
            {!loadError && filtered.length === 0 && !isLoading && (
              <TableRow>
                <TableCell colSpan={7} className="h-32 text-center text-muted-foreground">
                  Không có món ăn phù hợp với bộ lọc hiện tại
                </TableCell>
              </TableRow>
            )}
            {!loadError && isLoading && (
              <TableRow>
                <TableCell colSpan={7} className="h-32 text-center text-muted-foreground">
                  Đang tải danh sách...
                </TableCell>
              </TableRow>
            )}
            {!loadError &&
              !isLoading &&
              paged.map((d) => (
                <TableRow className="hover:bg-muted/40" key={d.id}>
                  <TableCell>
                    <img src={d.image} alt={d.name} className="h-12 w-16 object-cover rounded-md border" />
                  </TableCell>
                  <TableCell className="font-medium max-w-[260px]">
                    <span className="text-sm sm:text-base font-semibold leading-tight truncate" title={d.name}>{d.name}</span>
                  </TableCell>
                  <TableCell className="max-w-[300px]">
                    <span className="truncate" title={getRestaurantName(d)}>{getRestaurantName(d)}</span>
                  </TableCell>
                  <TableCell>
                    {d.price.toLocaleString("vi-VN")}₫
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
                        <DropdownMenuItem>Xem chi tiết</DropdownMenuItem>
                        <DropdownMenuItem onClick={() => { setEditing(d); setEditOpen(true) }}>Chỉnh sửa</DropdownMenuItem>
                        <DropdownMenuItem onClick={() => setConfirmDish(d)}>
                          {d.isAvailable ? "Tạm ngưng bán" : "Mở bán lại"}
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </div>

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
                    toast({ variant: "destructive", title: "Cập nhật thất bại", description: "Vui lòng thử lại sau." })
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

      {/* Edit Dish Dialog */}
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent>
          <DialogHeader className="items-center text-center">
            <DialogTitle className="text-center">Chỉnh sửa món ăn</DialogTitle>
          </DialogHeader>
          <Form {...editForm}>
            <form className="space-y-4" onSubmit={editForm.handleSubmit((values)=>{
              try {
                const payload = {
                  ...values,
                  price: Number(values.price.replace(/\./g, "").trim() || 0),
                  tags: selectedTags,
                  id: editing?.id,
                }
                console.log('Edit dish', payload)
                toast({ title: (
                  <div className="flex items-center gap-3">
                    <CheckCircle2 className="h-5 w-5 text-green-500" />
                    <span className="font-medium">Đã cập nhật món ăn!</span>
                  </div>
                )})
                setEditOpen(false)
                setEditing(null)
              } catch(e) {
                toast({ variant:'destructive', title:'Cập nhật thất bại', description:'Vui lòng thử lại sau.' })
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

              <FormField control={editForm.control} name="description" render={({field})=> (
                <FormItem>
                  <FormLabel>Mô tả</FormLabel>
                  <FormControl>
                    <Textarea rows={3} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )} />

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

      <div className="flex items-center justify-between gap-2">
        <p className="text-sm text-muted-foreground">
          Hiển thị {(filtered.length === 0) ? 0 : (page - 1) * pageSize + 1}
          –{Math.min(page * pageSize, filtered.length)} trong tổng {filtered.length}
        </p>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" disabled={page === 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>
            Trang trước
          </Button>
          <div className="text-sm tabular-nums">
            {page} / {totalPages}
          </div>
          <Button variant="outline" size="sm" disabled={page === totalPages} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>
            Trang sau
          </Button>
        </div>
      </div>
    </div>
  )
}
