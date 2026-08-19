"use client"

import { useMemo, useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import { AdminTableFrame } from "@/components/admin/admin-table-frame"
import { AdminFilterPopover, type FilterSectionDef } from "@/components/admin/filter-popover"
import { DetailRow, InfoGrid } from "@/components/admin/detail-dialog"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Search, MoreVertical, Phone, MapPin, Plus, CheckCircle2, Store, AlertTriangle, FilterX, Eye, Pencil, Ban } from "lucide-react"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { useForm } from "react-hook-form"
import { Textarea } from "@/components/ui/textarea"
import type { Restaurant } from "@/types"
import { Separator } from "@/components/ui/separator"
import { cn } from "@/lib/utils"
import { useRestaurants } from "@/hooks/restaurants/use-restaurants"
import { useCreateRestaurant } from "@/hooks/restaurants/use-create-restaurant"
import { setRestaurantBlocking, updateRestaurant } from "@/services/restaurants"
import { useToast } from "@/hooks/use-toast"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog"

type RestaurantFilterValues = { status: string }
const DEFAULT_RESTAURANT_FILTER_VALUES: RestaurantFilterValues = { status: "all" }
const RESTAURANT_FILTER_SECTIONS: FilterSectionDef[] = [
  {
    key: "status",
    label: "Trạng thái quán",
    options: [
      { value: "all", label: "Tất cả" },
      { value: "active", label: "Đang mở" },
      { value: "inactive", label: "Tạm ngưng" },
    ],
  },
]

export default function RestaurantsPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [page, setPage] = useState(1)
  const [filters, setFilters] = useState<RestaurantFilterValues>(DEFAULT_RESTAURANT_FILTER_VALUES)
  const pageSize = 10
  const [open, setOpen] = useState(false)
  const { toast } = useToast()
  const { mutate: createRestaurant, loading: creating } = useCreateRestaurant()
  const [editOpen, setEditOpen] = useState(false)
  const [editing, setEditing] = useState<Restaurant | null>(null)
  const [blockingId, setBlockingId] = useState<string | null>(null)
  const [detailRestaurant, setDetailRestaurant] = useState<Restaurant | null>(null)

  type CreateRestaurantInput = {
    name: string
    address: string
    phone: string
  }

  const form = useForm<CreateRestaurantInput>({
    defaultValues: { name: "", address: "", phone: "" },
    mode: "onTouched",
  })

  async function onSubmit(values: CreateRestaurantInput) {
    try {
      const created = await createRestaurant({ name: values.name, address: values.address, phone: values.phone })
      setRestaurants((prev) => [created, ...prev.filter((item) => item.id !== created.id)])
      setPage(1)
      toast({
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">Quán ăn đã được thêm thành công!</span>
          </div>
        )
      })
      setOpen(false)
      form.reset()
    } catch (e) {
      toast({ variant: "destructive", title: "Tạo thất bại", description: "Vui lòng thử lại sau." })
    }
  }

  type EditRestaurantInput = {
    name: string
    address: string
    phone: string
  }

  const editForm = useForm<EditRestaurantInput>({
    defaultValues: { name: "", address: "", phone: "" },
    mode: "onTouched",
  })

  useEffect(() => {
    if (editing) {
      editForm.reset({ name: editing.name, address: editing.address, phone: editing.phone })
    }
  }, [editing])

  async function onEditSubmit(values: EditRestaurantInput) {
    try {
      if (!editing) return
      const updated = await updateRestaurant(normalizeId(editing.id), values)
      setRestaurants((prev) => prev.map((item) => item.id === editing.id ? updated : item))
      toast({
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">Đã cập nhật thành công!</span>
          </div>
        )
      })
      setEditOpen(false)
      setEditing(null)
    } catch (e) {
      toast({ variant: "destructive", title: "Cập nhật thất bại", description: "Vui lòng thử lại sau." })
    }
  }

  const { data, loading, error, refresh } = useRestaurants()

  const [restaurants, setRestaurants] = useState<Restaurant[]>([])

  useEffect(() => {
    setRestaurants(data || [])
  }, [data])

  function normalizeId(value: string) {
    const trimmed = value.trim()
    return /^-?\d+$/.test(trimmed) ? Number(trimmed) : trimmed
  }

  const filtered = useMemo(() => {
    const q = searchQuery.toLowerCase().trim()
    return restaurants.filter((r) => {
      const matchesQuery = !q || (
        r.name.toLowerCase().includes(q) ||
        r.address.toLowerCase().includes(q) ||
        r.phone.toLowerCase().includes(q)
      )
      const matchesStatus = filters.status === "all" || (filters.status === "active" ? r.isActive : !r.isActive)
      return matchesQuery && matchesStatus
    })
  }, [searchQuery, filters, restaurants])

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const paged = useMemo(() => {
    const safePage = Math.min(Math.max(1, page), totalPages)
    const start = (safePage - 1) * pageSize
    return filtered.slice(start, start + pageSize)
  }, [filtered, page, totalPages])

  useEffect(() => { setPage(1) }, [searchQuery, filters])
  useEffect(() => { if (page > totalPages) setPage(totalPages) }, [totalPages, page])

  function resetFilters() {
    setSearchQuery('')
    setFilters(DEFAULT_RESTAURANT_FILTER_VALUES)
    setPage(1)
  }

  return (
    <div className="flex flex-1 min-h-0 flex-col gap-8 px-20 pt-6 pb-10 bg-background">
      <div className="flex flex-1 min-h-0 flex-col gap-5">
        <div className="shrink-0 flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <div className="relative w-full sm:w-80">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Tìm theo tên, địa chỉ hoặc SĐT..."
                value={searchQuery}
                onChange={(e) => { setSearchQuery(e.target.value); setPage(1) }}
                className="pl-10 h-10"
              />
            </div>
            <AdminFilterPopover
              sections={RESTAURANT_FILTER_SECTIONS}
              values={filters}
              defaultValues={DEFAULT_RESTAURANT_FILTER_VALUES}
              onApply={setFilters}
            />
          </div>
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />Thêm quán ăn
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Thêm quán ăn</DialogTitle>
              </DialogHeader>
              <Form {...form}>
                <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
                  <FormField
                    control={form.control}
                    name="name"
                    rules={{ required: "Vui lòng nhập tên quán" }}
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Tên quán ăn</FormLabel>
                        <FormControl>
                          <Input placeholder="Nhập tên quán" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="address"
                    rules={{ required: "Vui lòng nhập địa chỉ" }}
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Địa chỉ</FormLabel>
                        <FormControl>
                          <Textarea placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành" rows={3} {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="phone"
                    rules={{
                      required: "Vui lòng nhập số điện thoại",
                      pattern: {
                        value: /^\+?\d{9,15}$/,
                        message: "Số điện thoại không hợp lệ",
                      },
                    }}
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Số điện thoại</FormLabel>
                        <FormControl>
                          <Input placeholder="VD: 0901234567" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <DialogFooter>
                    <Button type="button" variant="outline" onClick={() => setOpen(false)}>
                      Hủy
                    </Button>
                    <Button type="submit" disabled={creating}>{creating ? "Đang lưu..." : "Lưu"}</Button>
                  </DialogFooter>
                </form>
              </Form>
            </DialogContent>
          </Dialog>
        </div>
        {error && !loading ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon" className="bg-destructive/10 text-destructive">
                  <AlertTriangle />
                </EmptyMedia>
                <EmptyTitle>Không thể tải danh sách quán ăn</EmptyTitle>
                <EmptyDescription>{error}</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button variant="outline" onClick={refresh}>Thử lại</Button>
              </EmptyContent>
            </Empty>
          ) : loading ? (
            null
          ) : restaurants.length === 0 ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <Store />
                </EmptyMedia>
                <EmptyTitle>Chưa có quán ăn nào</EmptyTitle>
                <EmptyDescription>Chưa có quán ăn nào trong hệ thống. Bắt đầu bằng cách thêm quán ăn đầu tiên.</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button onClick={() => setOpen(true)}>
                  <Plus className="mr-2 h-4 w-4" />Thêm quán ăn
                </Button>
              </EmptyContent>
            </Empty>
          ) : filtered.length === 0 ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <FilterX />
                </EmptyMedia>
                <EmptyTitle>Không có quán ăn phù hợp</EmptyTitle>
                <EmptyDescription>Không có quán ăn phù hợp với bộ lọc hiện tại.</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button variant="outline" onClick={resetFilters}>Xoá bộ lọc</Button>
              </EmptyContent>
            </Empty>
          ) : (
        <AdminTableFrame
          className="flex-1"
          page={page}
          totalPages={totalPages}
          onPageChange={setPage}
          caption={
            <>Hiển thị {(filtered.length === 0) ? 0 : (page - 1) * pageSize + 1}
            –{Math.min(page * pageSize, filtered.length)} trong tổng {filtered.length}</>
          }
        >
          <Table className="[&_th]:py-4 [&_td]:py-4 [&_th]:px-6 [&_td]:px-6 [&_td:last-child]:py-2">
            <TableHeader className="sticky top-0 z-10 bg-background">
              <TableRow>
                <TableHead className="w-[26%] min-w-[220px]">Tên quán ăn</TableHead>
                <TableHead className="w-[34%] min-w-[260px]">Địa chỉ</TableHead>
                <TableHead className="w-[18%] min-w-[160px]">Số điện thoại</TableHead>
                <TableHead className="w-[12%]">Trạng thái</TableHead>
                <TableHead className="w-[10%] text-right">Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
                {paged.map((r, idx) => (
                  <TableRow className={cn("hover:bg-muted/40", idx === paged.length - 1 && "!border-b")} key={r.id}>
                    <TableCell className="max-w-[260px]">
                      <div className="flex items-center gap-3">
                        <span className="truncate" title={r.name}>{r.name}</span>
                      </div>
                    </TableCell>
                    <TableCell className="max-w-[360px]">
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <MapPin className="h-4 w-4 shrink-0" />
                        <span className="truncate" title={r.address}>{r.address}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <Phone className="h-4 w-4 shrink-0" />
                        <span>{r.phone}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      {r.isActive ? (
                        <span className="inline-flex items-center rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-200">Đang mở</span>
                      ) : (
                        <span className="inline-flex items-center rounded-full bg-rose-100 px-2.5 py-1 text-xs font-medium text-rose-700 ring-1 ring-inset ring-rose-200">Tạm ngưng</span>
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
                          <DropdownMenuItem onClick={() => setDetailRestaurant(r)}>
                            <Eye className="h-4 w-4" />Xem chi tiết
                          </DropdownMenuItem>
                          <DropdownMenuItem onClick={() => { setEditing(r); setEditOpen(true) }}>
                            <Pencil className="h-4 w-4" />Chỉnh sửa
                          </DropdownMenuItem>
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <DropdownMenuItem onSelect={(e) => e.preventDefault()}>
                                {r.isActive ? <Ban className="h-4 w-4" /> : <CheckCircle2 className="h-4 w-4" />}
                                {r.isActive ? "Tạm ngưng bán" : "Mở bán lại"}
                              </DropdownMenuItem>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>
                                  {r.isActive ? "Tạm ngưng bán quán" : "Mở lại bán quán"} “{r.name}”?
                                </AlertDialogTitle>
                                <AlertDialogDescription>
                                  {r.isActive
                                    ? "Quán sẽ tạm ngưng nhận đơn cho đến khi bạn mở lại."
                                    : "Quán sẽ được mở lại và hiển thị cho khách đặt."
                                  }
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Hủy</AlertDialogCancel>
                                <AlertDialogAction
                                  disabled={blockingId === r.id}
                                  onClick={async () => {
                                    const next = !r.isActive
                                    const type = next ? 1 : 0
                                    setBlockingId(r.id)
                                    try {
                                      await setRestaurantBlocking(normalizeId(r.id), type)
                                      setRestaurants((prev) => prev.map((item) => item.id === r.id ? { ...item, isActive: next } : item))
                                      toast({
                                        title: (
                                          <div className="flex items-center gap-3">
                                            <CheckCircle2 className="h-5 w-5 text-green-500" />
                                            <span className="font-medium">{next ? "Đã mở bán lại" : "Đã tạm ngưng bán"} quán "{r.name}"</span>
                                          </div>
                                        )
                                      })
                                    } catch (e) {
                                      toast({ variant: "destructive", title: "Cập nhật thất bại", description: "Vui lòng thử lại sau." })
                                    } finally {
                                      setBlockingId(null)
                                    }
                                  }}
                                >
                                  Xác nhận
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>
        </AdminTableFrame>
          )}
      </div>

      {/* Restaurant detail dialog */}
      <Dialog open={!!detailRestaurant} onOpenChange={(o) => { if (!o) setDetailRestaurant(null) }}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader className="items-center text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-muted">
              <Store className="h-8 w-8 text-muted-foreground" />
            </div>
            <DialogTitle className="text-center">{detailRestaurant?.name}</DialogTitle>
            {detailRestaurant?.isActive ? (
              <span className="inline-flex items-center rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-200">Đang mở</span>
            ) : (
              <span className="inline-flex items-center rounded-full bg-rose-100 px-2.5 py-1 text-xs font-medium text-rose-700 ring-1 ring-inset ring-rose-200">Tạm ngưng</span>
            )}
          </DialogHeader>
          <Separator className="my-4" />
          {detailRestaurant && (
            <InfoGrid className="grid-cols-1">
              <DetailRow label="Địa chỉ" value={detailRestaurant.address} />
              <DetailRow label="Số điện thoại" value={detailRestaurant.phone} />
              <DetailRow
                label="Ngày tạo"
                value={detailRestaurant.createdAt ? new Date(detailRestaurant.createdAt).toLocaleDateString("vi-VN") : "-"}
              />
            </InfoGrid>
          )}
        </DialogContent>
      </Dialog>

      {/* Edit Restaurant Dialog */}
      <Dialog open={editOpen} onOpenChange={(o) => { setEditOpen(o); if (!o) setEditing(null) }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Chỉnh sửa quán ăn</DialogTitle>
          </DialogHeader>
          <Form {...editForm}>
            <form className="space-y-4" onSubmit={editForm.handleSubmit(onEditSubmit)}>
              <FormField
                control={editForm.control}
                name="name"
                rules={{ required: "Vui lòng nhập tên quán" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tên quán ăn</FormLabel>
                    <FormControl>
                      <Input placeholder="Nhập tên quán" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={editForm.control}
                name="address"
                rules={{ required: "Vui lòng nhập địa chỉ" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Địa chỉ</FormLabel>
                    <FormControl>
                      <Textarea placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành" rows={3} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={editForm.control}
                name="phone"
                rules={{
                  required: "Vui lòng nhập số điện thoại",
                  pattern: { value: /^\+?\d{9,15}$/, message: "Số điện thoại không hợp lệ" },
                }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Số điện thoại</FormLabel>
                    <FormControl>
                      <Input placeholder="VD: 0901234567" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              {/* Status field removed to unify with create form */}
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setEditOpen(false)}>
                  Hủy
                </Button>
                <Button type="submit">Lưu</Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
