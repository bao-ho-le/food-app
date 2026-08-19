"use client"

import { useEffect, useMemo, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import { AdminTableFrame } from "@/components/admin/admin-table-frame"
import { AdminFilterPopover, type FilterSectionDef } from "@/components/admin/filter-popover"
import { DetailRow, InfoGrid } from "@/components/admin/detail-dialog"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Search, MoreVertical, CheckCircle2, Package, AlertTriangle, FilterX, Eye, Ban } from "lucide-react"
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

type OrderFilterValues = { status: string; time: string; total: string }
const DEFAULT_ORDER_FILTER_VALUES: OrderFilterValues = { status: "all", time: "newest", total: "none" }
const ORDER_FILTER_SECTIONS: FilterSectionDef[] = [
  {
    key: "status",
    label: "Trạng thái",
    options: [
      { value: "all", label: "Tất cả" },
      { value: "PENDING", label: STATUS_LABEL.PENDING },
      { value: "PREPARING", label: STATUS_LABEL.PREPARING },
      { value: "DELIVERED", label: STATUS_LABEL.DELIVERED },
      { value: "CANCELLED", label: STATUS_LABEL.CANCELLED },
    ],
  },
  {
    key: "time",
    label: "Thời gian",
    options: [
      { value: "newest", label: "Mới nhất" },
      { value: "oldest", label: "Cũ nhất" },
    ],
  },
  {
    key: "total",
    label: "Giá trị đơn",
    options: [
      { value: "none", label: "Mặc định" },
      { value: "asc", label: "Tăng dần" },
      { value: "desc", label: "Giảm dần" },
    ],
  },
]

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
  const [filters, setFilters] = useState<OrderFilterValues>(DEFAULT_ORDER_FILTER_VALUES)
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
      const matchesStatus = filters.status === "all" || o.status === filters.status
      return matchesText && matchesStatus
    })
  }, [orders, searchQuery, filters.status])

  const sorted = useMemo(() => {
    const list = [...filtered]
    if (filters.total === "asc") {
      list.sort((a, b) => a.totalPrice - b.totalPrice)
    } else if (filters.total === "desc") {
      list.sort((a, b) => b.totalPrice - a.totalPrice)
    } else if (filters.time === "oldest") {
      list.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
    } else {
      list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    }
    return list
  }, [filtered, filters.total, filters.time])

  const totalPages = Math.max(1, Math.ceil(sorted.length / pageSize))
  const paged = useMemo(() => {
    const safePage = Math.min(Math.max(1, page), totalPages)
    const start = (safePage - 1) * pageSize
    return sorted.slice(start, start + pageSize)
  }, [sorted, page, totalPages])

  useEffect(() => { setPage(1) }, [searchQuery, filters])
  useEffect(() => { if (page > totalPages) setPage(totalPages) }, [totalPages, page])

  function resetFilters() {
    setSearchQuery("")
    setFilters(DEFAULT_ORDER_FILTER_VALUES)
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
    <div className="flex flex-1 min-h-0 flex-col gap-8 px-18 pt-6 pb-10 bg-background">
      <div className="shrink-0 flex items-center gap-2 text-[15px] sm:text-base">
        <div className="relative flex-1 sm:max-w-[22rem]">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Tìm theo mã đơn, khách hàng hoặc địa chỉ..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10 h-10"
          />
        </div>
        <AdminFilterPopover
          sections={ORDER_FILTER_SECTIONS}
          values={filters}
          defaultValues={DEFAULT_ORDER_FILTER_VALUES}
          onApply={setFilters}
        />
      </div>

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
      <AdminTableFrame
        className="flex-1 text-[15px] sm:text-base"
        page={page}
        totalPages={totalPages}
        onPageChange={setPage}
        caption={
          <>Hiển thị {(sorted.length === 0) ? 0 : (page - 1) * pageSize + 1}
          –{Math.min(page * pageSize, sorted.length)} trong tổng {sorted.length}</>
        }
      >
        <Table className="[&_th]:py-4 [&_td]:py-4 [&_th]:px-6 [&_td]:px-6 [&_td:last-child]:py-2">
          <TableHeader className="sticky top-0 z-10 bg-background">
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
                    <span className="block truncate" title={o.deliveryAddress}>{o.deliveryAddress}</span>
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
                        <DropdownMenuItem onClick={() => openDetail(o)}>
                          <Eye className="h-4 w-4" />Xem chi tiết
                        </DropdownMenuItem>
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
                              <CheckCircle2 className="h-4 w-4" />Xác nhận đơn (→ Đang chuẩn bị)
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
                              <Ban className="h-4 w-4" />Huỷ đơn
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
                              <Package className="h-4 w-4" />Đánh dấu đã giao
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
                              <Ban className="h-4 w-4" />Huỷ đơn
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
      </AdminTableFrame>
        )}

      {/* Order detail dialog */}
      <Dialog open={!!detailOrder} onOpenChange={(o) => { if (!o) setDetailOrder(null) }}>
        <DialogContent>
          <DialogHeader className="items-center text-center">
            <DialogTitle className="text-center">Chi tiết đơn #{detailOrder?.id}</DialogTitle>
            {detailOrder && (
              <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset ${STATUS_BADGE_CLASS[detailOrder.status]}`}>
                {STATUS_LABEL[detailOrder.status]}
              </span>
            )}
          </DialogHeader>
          {detailOrder && (
            <InfoGrid className="grid-cols-1 sm:grid-cols-2">
              <DetailRow label="Khách hàng" value={detailOrder.customerName} />
              <DetailRow label="Ngày đặt" value={new Date(detailOrder.createdAt).toLocaleString("vi-VN")} />
              {detailOrder.customerEmail && <DetailRow label="Email" value={detailOrder.customerEmail} />}
              {detailOrder.customerPhone && <DetailRow label="Số điện thoại" value={detailOrder.customerPhone} />}
              <DetailRow className="col-span-full" label="Địa chỉ giao hàng" value={detailOrder.deliveryAddress} />
            </InfoGrid>
          )}
          <div className="space-y-3 border-t pt-3">
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
    </div>
  )
}
