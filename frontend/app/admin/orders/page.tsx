"use client"

import { useEffect, useMemo, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Search, MoreVertical, RefreshCw, CheckCircle2, Package, AlertTriangle, FilterX } from "lucide-react"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { useToast } from "@/hooks/use-toast"
import type { AdminOrder, AdminOrderItem, AdminOrderStatus } from "@/types"
import { fetchAdminOrderItems, fetchAdminOrders, updateOrderStatus } from "@/services/admin-orders"

const STATUS_LABEL: Record<AdminOrderStatus, string> = {
  PENDING: "Chờ xử lý",
  PREPARING: "Đang chuẩn bị",
  DELIVERED: "Đã giao",
  CANCELLED: "Đã huỷ",
}

const STATUS_BADGE_CLASS: Record<AdminOrderStatus, string> = {
  PENDING: "bg-amber-100 text-amber-700 ring-amber-200",
  PREPARING: "bg-blue-100 text-blue-700 ring-blue-200",
  DELIVERED: "bg-emerald-100 text-emerald-700 ring-emerald-200",
  CANCELLED: "bg-rose-100 text-rose-700 ring-rose-200",
}

type PendingAction = {
  order: AdminOrder
  nextStatus: AdminOrderStatus
  title: string
  description: string
}

export default function AdminOrdersPage() {
  const { toast } = useToast()
  const [orders, setOrders] = useState<AdminOrder[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<"all" | AdminOrderStatus>("all")
  const [sortBy, setSortBy] = useState<"newest" | "oldest" | "total_asc" | "total_desc">("newest")
  const [page, setPage] = useState(1)
  const pageSize = 10

  const [detailOrder, setDetailOrder] = useState<AdminOrder | null>(null)
  const [detailItems, setDetailItems] = useState<AdminOrderItem[]>([])
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<string | null>(null)

  const [pendingAction, setPendingAction] = useState<PendingAction | null>(null)
  const [updatingId, setUpdatingId] = useState<string | null>(null)

  const [reloadTick, setReloadTick] = useState(0)
  const reloadOrders = () => setReloadTick((v) => v + 1)

  useEffect(() => {
    let isMounted = true
    setIsLoading(true)
    setLoadError(null)
    fetchAdminOrders()
      .then((data) => {
        if (isMounted) setOrders(data)
      })
      .catch((error) => {
        if (isMounted) setLoadError(error instanceof Error ? error.message : "Không thể tải danh sách đơn hàng.")
      })
      .finally(() => {
        if (isMounted) setIsLoading(false)
      })
    return () => {
      isMounted = false
    }
  }, [reloadTick])

  const filtered = useMemo(() => {
    const q = searchQuery.toLowerCase().trim()
    return orders.filter((o) => {
      const matchesText =
        !q ||
        o.id.toLowerCase().includes(q) ||
        o.customerName.toLowerCase().includes(q) ||
        o.deliveryAddress.toLowerCase().includes(q)
      const matchesStatus = statusFilter === "all" || o.status === statusFilter
      return matchesText && matchesStatus
    })
  }, [orders, searchQuery, statusFilter])

  const sorted = useMemo(() => {
    const list = [...filtered]
    switch (sortBy) {
      case "oldest":
        list.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
        break
      case "total_asc":
        list.sort((a, b) => a.totalPrice - b.totalPrice)
        break
      case "total_desc":
        list.sort((a, b) => b.totalPrice - a.totalPrice)
        break
      default:
        list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    }
    return list
  }, [filtered, sortBy])

  const totalPages = Math.max(1, Math.ceil(sorted.length / pageSize))
  const paged = useMemo(() => {
    const safePage = Math.min(Math.max(1, page), totalPages)
    const start = (safePage - 1) * pageSize
    return sorted.slice(start, start + pageSize)
  }, [sorted, page, totalPages])

  useEffect(() => { setPage(1) }, [searchQuery, statusFilter])
  useEffect(() => { if (page > totalPages) setPage(totalPages) }, [totalPages, page])

  function resetFilters() {
    setSearchQuery("")
    setStatusFilter("all")
    setSortBy("newest")
    setPage(1)
  }

  async function openDetail(order: AdminOrder) {
    setDetailOrder(order)
    setDetailItems([])
    setDetailError(null)
    setDetailLoading(true)
    try {
      const items = await fetchAdminOrderItems(order.id)
      setDetailItems(items)
    } catch (error) {
      setDetailError(error instanceof Error ? error.message : "Không thể tải chi tiết đơn hàng.")
    } finally {
      setDetailLoading(false)
    }
  }

  async function confirmAction() {
    if (!pendingAction) return
    const { order, nextStatus } = pendingAction
    setUpdatingId(order.id)
    try {
      const updated = await updateOrderStatus(order.id, nextStatus)
      setOrders((prev) => prev.map((o) => (o.id === order.id ? updated : o)))
      toast({
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">Đã cập nhật trạng thái đơn hàng</span>
          </div>
        ),
      })
      setPendingAction(null)
    } catch (e) {
      toast({ variant: "destructive", title: "Cập nhật thất bại", description: "Vui lòng thử lại sau." })
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <div className="space-y-8 px-18 py-10 bg-background flex-1">
      <div className="text-center">
        <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight pb-3">Quản lý đơn hàng</h1>
        <div className="mx-auto mt-2 h-1 w-24 rounded bg-foreground/80" />
      </div>

      <div className="flex flex-wrap items-center gap-3 text-[15px] sm:text-base">
        <div className="relative flex-1 sm:max-w-[22rem]">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Tìm theo mã đơn, khách hàng hoặc địa chỉ..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10 h-10"
          />
        </div>
        <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as typeof statusFilter)}>
          <SelectTrigger className="w-44 h-10">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="PENDING">Chờ xử lý</SelectItem>
            <SelectItem value="PREPARING">Đang chuẩn bị</SelectItem>
            <SelectItem value="DELIVERED">Đã giao</SelectItem>
            <SelectItem value="CANCELLED">Đã huỷ</SelectItem>
          </SelectContent>
        </Select>
        <Select value={sortBy} onValueChange={(v) => setSortBy(v as typeof sortBy)}>
          <SelectTrigger className="h-10 w-40 justify-between">
            <SelectValue placeholder="Sắp xếp" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="newest">Mới nhất</SelectItem>
            <SelectItem value="oldest">Cũ nhất</SelectItem>
            <SelectItem value="total_desc">Giá trị đơn ↓</SelectItem>
            <SelectItem value="total_asc">Giá trị đơn ↑</SelectItem>
          </SelectContent>
        </Select>
        <Button variant="outline" size="icon" className="h-10 w-10" title="Đặt lại bộ lọc" onClick={resetFilters}>
          <RefreshCw className="h-4 w-4" />
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Danh sách đơn hàng</CardTitle>
        </CardHeader>
        <CardContent>
        {loadError ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon" className="bg-destructive/10 text-destructive">
                <AlertTriangle />
              </EmptyMedia>
              <EmptyTitle>Không thể tải danh sách đơn hàng</EmptyTitle>
              <EmptyDescription>{loadError}</EmptyDescription>
            </EmptyHeader>
            <EmptyContent>
              <Button variant="outline" onClick={reloadOrders}>Thử lại</Button>
            </EmptyContent>
          </Empty>
        ) : isLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <Skeleton key={i} className="h-14 w-full" />
            ))}
          </div>
        ) : orders.length === 0 ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Package />
              </EmptyMedia>
              <EmptyTitle>Chưa có đơn hàng nào</EmptyTitle>
              <EmptyDescription>Đơn hàng sẽ xuất hiện ở đây khi khách hàng bắt đầu đặt món.</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : sorted.length === 0 ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <FilterX />
              </EmptyMedia>
              <EmptyTitle>Không có đơn hàng phù hợp</EmptyTitle>
              <EmptyDescription>Không có đơn hàng phù hợp với bộ lọc hiện tại.</EmptyDescription>
            </EmptyHeader>
            <EmptyContent>
              <Button variant="outline" onClick={resetFilters}>Đặt lại bộ lọc</Button>
            </EmptyContent>
          </Empty>
        ) : (
      <div className="overflow-x-auto rounded-lg border text-[15px] sm:text-base">
        <Table className="[&_th]:py-4 [&_td]:py-3 [&_th]:px-6 [&_td]:px-6">
          <TableHeader>
            <TableRow>
              <TableHead className="w-[10%] min-w-[100px]">Mã đơn</TableHead>
              <TableHead className="w-[18%] min-w-[160px]">Khách hàng</TableHead>
              <TableHead className="w-[26%] min-w-[220px]">Địa chỉ giao</TableHead>
              <TableHead className="w-[12%] min-w-[120px]">Tổng tiền</TableHead>
              <TableHead className="w-[12%]">Trạng thái</TableHead>
              <TableHead className="w-[12%]">Ngày đặt</TableHead>
              <TableHead className="w-[10%] text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
              {paged.map((o) => (
                <TableRow className="hover:bg-muted/40" key={o.id}>
                  <TableCell className="font-medium">#{o.id}</TableCell>
                  <TableCell className="max-w-[200px]">
                    <span className="truncate" title={o.customerName}>{o.customerName}</span>
                  </TableCell>
                  <TableCell className="max-w-[300px]">
                    <span className="truncate" title={o.deliveryAddress}>{o.deliveryAddress}</span>
                  </TableCell>
                  <TableCell>{o.totalPrice.toLocaleString("vi-VN")}₫</TableCell>
                  <TableCell>
                    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset ${STATUS_BADGE_CLASS[o.status]}`}>
                      {STATUS_LABEL[o.status]}
                    </span>
                  </TableCell>
                  <TableCell>{new Date(o.createdAt).toLocaleDateString("vi-VN")}</TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => openDetail(o)}>Xem chi tiết</DropdownMenuItem>
                        {o.status === "PENDING" && (
                          <>
                            <DropdownMenuItem
                              onClick={() =>
                                setPendingAction({
                                  order: o,
                                  nextStatus: "PREPARING",
                                  title: "Xác nhận đơn hàng?",
                                  description: `Đơn #${o.id} sẽ chuyển sang trạng thái "Đang chuẩn bị".`,
                                })
                              }
                            >
                              Xác nhận đơn (→ Đang chuẩn bị)
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() =>
                                setPendingAction({
                                  order: o,
                                  nextStatus: "CANCELLED",
                                  title: "Huỷ đơn hàng?",
                                  description: `Đơn #${o.id} sẽ bị huỷ và không thể hoàn tác.`,
                                })
                              }
                            >
                              Huỷ đơn
                            </DropdownMenuItem>
                          </>
                        )}
                        {o.status === "PREPARING" && (
                          <>
                            <DropdownMenuItem
                              onClick={() =>
                                setPendingAction({
                                  order: o,
                                  nextStatus: "DELIVERED",
                                  title: "Đánh dấu đã giao?",
                                  description: `Đơn #${o.id} sẽ chuyển sang trạng thái "Đã giao".`,
                                })
                              }
                            >
                              Đánh dấu đã giao
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() =>
                                setPendingAction({
                                  order: o,
                                  nextStatus: "CANCELLED",
                                  title: "Huỷ đơn hàng?",
                                  description: `Đơn #${o.id} sẽ bị huỷ và không thể hoàn tác.`,
                                })
                              }
                            >
                              Huỷ đơn
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </div>
        )}
        </CardContent>
      </Card>

      {/* Order detail dialog */}
      <Dialog open={!!detailOrder} onOpenChange={(o) => { if (!o) setDetailOrder(null) }}>
        <DialogContent>
          <DialogHeader className="items-center text-center">
            <DialogTitle className="text-center">Chi tiết đơn #{detailOrder?.id}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            {detailLoading && <p className="text-center text-sm text-muted-foreground py-6">Đang tải chi tiết...</p>}
            {detailError && !detailLoading && (
              <p className="text-center text-sm text-destructive py-6">{detailError}</p>
            )}
            {!detailLoading && !detailError && detailItems.length === 0 && (
              <p className="text-center text-sm text-muted-foreground py-6">Đơn hàng không có món nào.</p>
            )}
            {!detailLoading &&
              !detailError &&
              detailItems.map((item, idx) => (
                <div key={idx} className="flex items-center gap-3 border-b pb-3 last:border-b-0">
                  {item.imageUrl && (
                    <img src={item.imageUrl} alt={item.dishName} className="h-12 w-16 object-cover rounded-md border" />
                  )}
                  <div className="flex-1">
                    <p className="font-medium text-sm">{item.dishName}</p>
                    <p className="text-xs text-muted-foreground">
                      {item.quantity} × {item.price.toLocaleString("vi-VN")}₫
                    </p>
                  </div>
                  <p className="text-sm font-medium">
                    {(item.quantity * item.price).toLocaleString("vi-VN")}₫
                  </p>
                </div>
              ))}
          </div>
        </DialogContent>
      </Dialog>

      {/* Confirm status change */}
      <AlertDialog open={!!pendingAction} onOpenChange={(o) => { if (!o) setPendingAction(null) }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{pendingAction?.title}</AlertDialogTitle>
            <AlertDialogDescription>{pendingAction?.description}</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setPendingAction(null)}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              disabled={!pendingAction || updatingId === pendingAction?.order.id}
              onClick={confirmAction}
            >
              Xác nhận
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <div className="flex items-center justify-between gap-2">
        <p className="text-sm text-muted-foreground">
          Hiển thị {(sorted.length === 0) ? 0 : (page - 1) * pageSize + 1}
          –{Math.min(page * pageSize, sorted.length)} trong tổng {sorted.length}
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
