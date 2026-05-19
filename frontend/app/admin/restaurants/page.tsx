"use client"

import { useMemo, useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
// Removed card wrapper for simpler layout
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Search, MoreVertical, Phone, MapPin, Plus, CheckCircle2 } from "lucide-react"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { useForm } from "react-hook-form"
import { Textarea } from "@/components/ui/textarea"
import type { Restaurant } from "@/types"
import { useRestaurants } from "@/hooks/restaurants/use-restaurants"
import { useCreateRestaurant } from "@/hooks/restaurants/use-create-restaurant"
import { setRestaurantBlocking, updateRestaurant } from "@/services/restaurants"
import { useToast } from "@/hooks/use-toast"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog"

export default function RestaurantsPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [page, setPage] = useState(1)
  const pageSize = 10
  const [open, setOpen] = useState(false)
  const { toast } = useToast()
  const { mutate: createRestaurant, loading: creating } = useCreateRestaurant()
  const [editOpen, setEditOpen] = useState(false)
  const [editing, setEditing] = useState<Restaurant | null>(null)
  const [blockingId, setBlockingId] = useState<string | null>(null)

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
      if (!q) return true
      return (
        r.name.toLowerCase().includes(q) ||
        r.address.toLowerCase().includes(q) ||
        r.phone.toLowerCase().includes(q)
      )
    })
  }, [searchQuery, restaurants])

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const paged = useMemo(() => {
    const safePage = Math.min(Math.max(1, page), totalPages)
    const start = (safePage - 1) * pageSize
    return filtered.slice(start, start + pageSize)
  }, [filtered, page, totalPages])

  useEffect(() => { setPage(1) }, [searchQuery])
  useEffect(() => { if (page > totalPages) setPage(totalPages) }, [totalPages, page])

  return (
    <div className="space-y-8 px-20 py-10 bg-background flex-1">
      <div className="text-center">
        <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight pb-3">Quản lý quán ăn</h1>
        <div className="mx-auto mt-2 h-1 w-24 rounded bg-foreground/80" />
      </div>

      <div className="flex flex-col gap-5">
        <div className="flex items-center justify-between gap-3">
          <div className="relative w-full sm:w-80">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm theo tên, địa chỉ hoặc SĐT..."
              value={searchQuery}
              onChange={(e) => { setSearchQuery(e.target.value); setPage(1) }}
              className="pl-10 h-10"
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
        <div className="overflow-x-auto rounded-lg border">
          <Table className="[&_th]:py-4 [&_td]:py-3 [&_th]:px-6 [&_td]:px-6">
            <TableHeader>
              <TableRow>
                <TableHead className="w-[26%] min-w-[220px]">Tên quán ăn</TableHead>
                <TableHead className="w-[34%] min-w-[260px]">Địa chỉ</TableHead>
                <TableHead className="w-[18%] min-w-[160px]">Số điện thoại</TableHead>
                <TableHead className="w-[12%]">Trạng thái</TableHead>
                <TableHead className="w-[10%] text-right">Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading && (
                <TableRow>
                  <TableCell colSpan={5} className="h-24 text-center text-muted-foreground">Đang tải danh sách...</TableCell>
                </TableRow>
              )}
              {error && !loading && (
                <TableRow>
                  <TableCell colSpan={5} className="h-24 text-center text-destructive">Lỗi tải dữ liệu: {error}</TableCell>
                </TableRow>
              )}
              {filtered.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="h-32 text-center text-muted-foreground">
                    Không có quán ăn phù hợp với bộ lọc hiện tại
                  </TableCell>
                </TableRow>
              ) : (
                paged.map((r) => (
                  <TableRow className="hover:bg-muted/40" key={r.id}>
                    <TableCell className="font-medium max-w-[260px]">
                      <div className="flex items-center gap-3">
                        <span className="text-sm sm:text-base font-semibold leading-tight truncate" title={r.name}>{r.name}</span>
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
                          <DropdownMenuItem>Xem chi tiết</DropdownMenuItem>
                          <DropdownMenuItem onClick={() => { setEditing(r); setEditOpen(true) }}>Chỉnh sửa</DropdownMenuItem>
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <DropdownMenuItem onSelect={(e) => e.preventDefault()}>
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
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>

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
