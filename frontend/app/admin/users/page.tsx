"use client"

import { useEffect, useMemo, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { AdminTableFrame } from "@/components/admin/admin-table-frame"
import { AdminFilterPopover, type FilterSectionDef } from "@/components/admin/filter-popover"
import { DetailRow, DetailSection, InfoGrid } from "@/components/admin/detail-dialog"
import { Skeleton } from "@/components/ui/skeleton"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Separator } from "@/components/ui/separator"
import {
  Search, MoreVertical, CheckCircle2, Users as UsersIcon, AlertTriangle, FilterX,
  Eye, ShieldCheck, ShieldOff, Lock, Unlock, MapPin,
} from "lucide-react"
import type { Address, User } from "@/types"
import { cn } from "@/lib/utils"
import { useToast } from "@/hooks/use-toast"
import { useAdminUsers } from "@/hooks/users/use-admin-users"
import { fetchAdminUserAddresses, setUserBlocking } from "@/services/users"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog"

type UserFilterValues = { role: string; status: string }
const DEFAULT_USER_FILTER_VALUES: UserFilterValues = { role: "all", status: "all" }
const USER_FILTER_SECTIONS: FilterSectionDef[] = [
  {
    key: "role",
    label: "Vai trò",
    options: [
      { value: "all", label: "Tất cả" },
      { value: "user", label: "Khách hàng" },
      { value: "admin", label: "Quản trị viên" },
    ],
  },
  {
    key: "status",
    label: "Trạng thái",
    options: [
      { value: "all", label: "Tất cả" },
      { value: "active", label: "Đang hoạt động" },
      { value: "locked", label: "Bị khoá" },
    ],
  },
]

export default function UsersPage() {
  const [searchQuery, setSearchQuery] = useState("")
  // Bỏ filter, chỉ giữ search
  const [page, setPage] = useState(1)
  const [quickFilter, setQuickFilter] = useState<"all"|"customers"|"admins"|"active"|"locked">("all")
  const [filters, setFilters] = useState<UserFilterValues>(DEFAULT_USER_FILTER_VALUES)
  const pageSize = 10

  const { toast } = useToast()
  const { data: users, loading, error, setData: setUsers, refresh } = useAdminUsers()
  const [confirmUser, setConfirmUser] = useState<User | null>(null)
  const [confirmRoleUser, setConfirmRoleUser] = useState<User | null>(null)
  const [blockingId, setBlockingId] = useState<string | null>(null)
  const [detailUser, setDetailUser] = useState<User | null>(null)
  const [detailAddresses, setDetailAddresses] = useState<Address[]>([])
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<string | null>(null)

  async function openDetail(user: User) {
    setDetailUser(user)
    setDetailAddresses([])
    setDetailError(null)
    setDetailLoading(true)
    try {
      const addresses = await fetchAdminUserAddresses(user.id)
      setDetailAddresses(addresses)
    } catch (error) {
      setDetailError(error instanceof Error ? error.message : "Không thể tải sổ địa chỉ.")
    } finally {
      setDetailLoading(false)
    }
  }

  function normalizeId(value: string) {
    const trimmed = value.trim()
    return /^-?\d+$/.test(trimmed) ? Number(trimmed) : trimmed
  }

  const filteredUsers = useMemo(() => {
    const q = searchQuery.toLowerCase().trim()
    return users.filter((user) => {
      const matchesQuery = !q || user.name.toLowerCase().includes(q) || user.email.toLowerCase().includes(q)
      let matchesQuick = true
      switch (quickFilter) {
        case "customers":
          matchesQuick = user.role === "user"
          break
        case "admins":
          matchesQuick = user.role === "admin"
          break
        case "active":
          matchesQuick = user.isActive
          break
        case "locked":
          matchesQuick = !user.isActive
          break
        default:
          matchesQuick = true
      }
      const matchesRole = filters.role === "all" || user.role === filters.role
      const matchesStatus =
        filters.status === "all" || (filters.status === "active" ? user.isActive : !user.isActive)
      return matchesQuery && matchesQuick && matchesRole && matchesStatus
    })
  }, [searchQuery, quickFilter, filters, users])

  const totalPages = Math.max(1, Math.ceil(filteredUsers.length / pageSize))
  // Đảm bảo trang hợp lệ khi dữ liệu lọc thay đổi
  if (page > totalPages) {
    // eslint-disable-next-line no-console
    // console.debug('Adjust page due to filter change', { page, totalPages })
    // Soft sync back to last page
  }
  const pagedUsers = useMemo(() => {
    const safePage = Math.min(Math.max(1, page), totalPages)
    const start = (safePage - 1) * pageSize
    return filteredUsers.slice(start, start + pageSize)
  }, [filteredUsers, page, totalPages])

  // Reset về trang 1 mỗi khi điều kiện lọc thay đổi
  useEffect(() => {
    setPage(1)
  }, [quickFilter, searchQuery, filters])

  // Nếu tổng số trang thay đổi và nhỏ hơn trang hiện tại, kéo về trang cuối hợp lệ
  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages)
    }
  }, [totalPages, page])

  const stats = useMemo(() => {
    const total = users.length
    const admins = users.filter((u) => u.role === "admin").length
    const actives = users.filter((u) => u.isActive).length
    const customers = users.filter((u) => u.role === "user").length
    const locked = users.filter((u) => !u.isActive).length
    return { total, admins, actives, customers, locked }
  }, [users])

  // lock/unlock handling with toast
  const handleToggleActive = async (u: User) => {
    const next = !u.isActive
    const type = next ? 1 : 0
    setBlockingId(u.id)
    try {
      await setUserBlocking(normalizeId(u.id), type)
      setUsers(prev => prev.map(x => x.id === u.id ? { ...x, isActive: next } : x))
      toast({
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">{next ? 'Đã mở khóa tài khoản' : 'Đã khóa tài khoản'} {u.name}.</span>
          </div>
        )
      })
    } catch (e) {
      toast({ variant: "destructive", title: "Cập nhật thất bại", description: "Vui lòng thử lại sau." })
    } finally {
      setBlockingId(null)
    }
  }

  function resetFilters() {
    setSearchQuery("")
    setQuickFilter("all")
    setFilters(DEFAULT_USER_FILTER_VALUES)
    setPage(1)
  }

  const handleToggleRole = (u: User) => {
    const nextRole: User["role"] = u.role === 'admin' ? 'user' : 'admin'
    setUsers(prev => prev.map(x => x.id === u.id ? { ...x, role: nextRole } : x))
    toast({
      title: (
        <div className="flex items-center gap-3">
          <CheckCircle2 className="h-5 w-5 text-green-500" />
          <span className="font-medium">{nextRole === 'admin' ? 'Đã cấp quyền Admin' : 'Đã chuyển thành User'}</span>
        </div>
      ),
      duration: 2000
    })
  }

  return (
    <div className="flex flex-1 min-h-0 flex-col gap-8 px-18 pt-6 pb-6 bg-background">
      <div className="shrink-0 grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <button onClick={() => { setQuickFilter('all'); setPage(1) }}
          className={`rounded-xl  p-4 text-center transition-all hover:shadow-sm hover:-translate-y-[1px] ${quickFilter==='all' ? 'ring-3 ring-violet-400 bg-violet-100 text-violet-700' : 'border-violet-200 bg-violet-200/60 hover:bg-violet-100 text-violet-700'}`}>
          <div className="text-sm font-medium p-1">Tổng người dùng</div>
          <div className="text-3xl font-bold">{stats.total}</div>
        </button>
        <button onClick={() => { setQuickFilter('customers'); setPage(1) }}
          className={`rounded-xl  p-4 text-center transition-all hover:shadow-sm hover:-translate-y-[1px] ${quickFilter==='customers' ? 'ring-3 ring-amber-400 bg-amber-100 text-amber-700' : 'border-amber-200 bg-amber-200/60 hover:bg-amber-100 text-amber-700'}`}>
          <div className="text-sm font-medium p-1">Khách hàng</div>
          <div className="text-3xl font-bold">{stats.customers}</div>
        </button>
        <button onClick={() => { setQuickFilter('admins'); setPage(1) }}
          className={`rounded-xl py-4 p-4 text-center transition-all hover:shadow-sm hover:-translate-y-[1px] ${quickFilter==='admins' ? 'ring-3 ring-blue-400 bg-blue-100 text-blue-700' : 'border-blue-200 bg-blue-200/60 hover:bg-blue-100 text-blue-700'}`}>
          <div className="text-sm font-medium p-1">Quản trị viên</div>
          <div className="text-3xl font-bold">{stats.admins}</div>
        </button>
        <button onClick={() => { setQuickFilter('active'); setPage(1) }}
          className={`rounded-xl  p-4 text-center transition-all hover:shadow-sm hover:-translate-y-[1px] ${quickFilter==='active' ? 'ring-3 ring-emerald-400 bg-emerald-100 text-emerald-700' : 'border-emerald-200 bg-emerald-200/60 hover:bg-emerald-100 text-emerald-700'}`}>
          <div className="text-sm font-medium p-1">Đang hoạt động</div>
          <div className="text-3xl font-bold">{stats.actives}</div>
        </button>
        <button onClick={() => { setQuickFilter('locked'); setPage(1) }}
          className={`rounded-xl p-4 text-center transition-all hover:shadow-sm hover:-translate-y-[1px] ${quickFilter==='locked' ? 'ring-3 ring-rose-400 bg-rose-100 text-rose-700' : 'border-rose-200 bg-rose-200/60 hover:bg-rose-100 text-rose-700'}`}>
          <div className="text-sm font-medium p-1">Bị khóa</div>
          <div className="text-3xl font-bold">{stats.locked}</div>
        </button>
      </div>

      <div className="shrink-0 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-start">
        <div className="flex items-center gap-2">
          <div className="relative w-full sm:w-64">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm tên hoặc email..."
              value={searchQuery}
              onChange={(e) => { setSearchQuery(e.target.value); setPage(1) }}
              className="pl-10 h-10"
            />
          </div>
          <AdminFilterPopover
            sections={USER_FILTER_SECTIONS}
            values={filters}
            defaultValues={DEFAULT_USER_FILTER_VALUES}
            onApply={setFilters}
          />
        </div>
      </div>

      {error ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon" className="bg-destructive/10 text-destructive">
                  <AlertTriangle />
                </EmptyMedia>
                <EmptyTitle>Không thể tải danh sách người dùng</EmptyTitle>
                <EmptyDescription>{error}</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button variant="outline" onClick={refresh}>Thử lại</Button>
              </EmptyContent>
            </Empty>
          ) : loading ? (
            null
          ) : users.length === 0 ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <UsersIcon />
                </EmptyMedia>
                <EmptyTitle>Chưa có người dùng nào</EmptyTitle>
                <EmptyDescription>Chưa có người dùng nào trong hệ thống.</EmptyDescription>
              </EmptyHeader>
            </Empty>
          ) : filteredUsers.length === 0 ? (
            <Empty>
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <FilterX />
                </EmptyMedia>
                <EmptyTitle>Không có người dùng phù hợp</EmptyTitle>
                <EmptyDescription>Không có người dùng phù hợp với bộ lọc hiện tại.</EmptyDescription>
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
              <>Hiển thị {(filteredUsers.length === 0) ? 0 : (page - 1) * pageSize + 1}
              –{Math.min(page * pageSize, filteredUsers.length)} trong tổng {filteredUsers.length}</>
            }
          >
            <Table className="[&_th]:py-4 [&_td]:py-4 [&_th]:px-6 [&_td]:px-6 [&_td:last-child]:py-2">
              <TableHeader className="sticky top-0 z-10 bg-background">
                <TableRow>
                  <TableHead className="w-[28%] min-w-[200px]">Tên user</TableHead>
                  <TableHead className="w-[24%] min-w-[220px]">Email</TableHead>
                  <TableHead className="w-[12%]">Vai trò</TableHead>
                  <TableHead className="w-[20%] min-w-[200px]">Số điện thoại</TableHead>
                  <TableHead className="w-[12%]">Trạng thái</TableHead>
                  <TableHead className="w-[8%] text-right">Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                  {pagedUsers.map((user, idx) => (
                  <TableRow className={cn("hover:bg-muted/40", idx === pagedUsers.length - 1 && "!border-b")} key={user.id}>
                    <TableCell className="max-w-[240px]">
                      <div className="flex items-center gap-3">
                        <span className="truncate" title={user.name}>{user.name}</span>
                      </div>
                    </TableCell>
                    <TableCell className="max-w-[260px] text-muted-foreground">
                      <span className="block truncate" title={user.email}>{user.email}</span>
                    </TableCell>
                    <TableCell>
                      <Badge className={user.role === "admin" ? "bg-blue-100 text-blue-700 hover:bg-blue-100" : "bg-amber-100 text-amber-700 hover:bg-amber-100"}>
                        {user.role === "admin" ? "Admin" : "User"}
                      </Badge>
                    </TableCell>
                    <TableCell className="max-w-[220px] text-muted-foreground">
                      <span className="block truncate" title={user.phone || "Chưa có số điện thoại"}>
                        {user.phone || "Chưa có số điện thoại"}
                      </span>
                    </TableCell>
                    <TableCell>
                      {user.isActive ? (
                        <span className="inline-flex items-center rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-200">Hoạt động</span>
                      ) : (
                        <span className="inline-flex items-center rounded-full bg-rose-100 px-2.5 py-1 text-xs font-medium text-rose-700 ring-1 ring-inset ring-rose-200">Khóa</span>
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
                          <DropdownMenuItem onClick={() => openDetail(user)}>
                            <Eye className="h-4 w-4" />Xem chi tiết
                          </DropdownMenuItem>
                          <DropdownMenuItem onClick={() => setConfirmRoleUser(user)}>
                            {user.role === 'admin' ? <ShieldOff className="h-4 w-4" /> : <ShieldCheck className="h-4 w-4" />}
                            {user.role === 'admin' ? 'Chuyển thành User' : 'Cấp quyền Admin'}
                          </DropdownMenuItem>
                          <DropdownMenuItem onClick={() => setConfirmUser(user)}>
                            {user.isActive ? <Lock className="h-4 w-4" /> : <Unlock className="h-4 w-4" />}
                            {user.isActive ? 'Khóa tài khoản' : 'Mở khóa'}
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

      {/* User detail dialog */}
      <Dialog open={!!detailUser} onOpenChange={(o) => { if (!o) setDetailUser(null) }}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader className="items-center text-center">
            <Avatar className="h-16 w-16">
              <AvatarImage src={detailUser?.avatarUrl} alt={detailUser?.name} />
              <AvatarFallback className="text-lg">{detailUser?.name?.charAt(0)?.toUpperCase() || "?"}</AvatarFallback>
            </Avatar>
            <div className="flex items-center gap-2">
              <DialogTitle>{detailUser?.name}</DialogTitle>
              <Badge className={detailUser?.role === "admin" ? "bg-blue-100 text-blue-700 hover:bg-blue-100" : "bg-amber-100 text-amber-700 hover:bg-amber-100"}>
                {detailUser?.role === "admin" ? "Admin" : "User"}
              </Badge>
            </div>
          </DialogHeader>
          <Separator className="my-4" />
          <div className="space-y-4">
            <InfoGrid>
              <DetailRow label="Email" value={detailUser?.email || "-"} />
              <DetailRow label="Số điện thoại" value={detailUser?.phone || "Chưa có số điện thoại"} />
              <DetailRow label="Giới tính" value={detailUser?.gender === "male" ? "Nam" : detailUser?.gender === "female" ? "Nữ" : "Khác"} />
              <DetailRow label="Ngày sinh" value={detailUser?.birthdate ? new Date(detailUser.birthdate).toLocaleDateString("vi-VN") : "Chưa cập nhật"} />
              <DetailRow
                label="Trạng thái"
                value={detailUser?.isActive
                  ? <span className="inline-flex items-center rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-200">Đang hoạt động</span>
                  : <span className="inline-flex items-center rounded-full bg-rose-100 px-2.5 py-1 text-xs font-medium text-rose-700 ring-1 ring-inset ring-rose-200">Bị khoá</span>}
              />
              <DetailRow label="Ngày tham gia" value={detailUser?.createdAt ? new Date(detailUser.createdAt).toLocaleDateString("vi-VN") : "-"} />
            </InfoGrid>
            <DetailSection title="Sổ địa chỉ">
              {detailLoading ? (
                <p className="text-sm text-muted-foreground py-2">Đang tải sổ địa chỉ...</p>
              ) : detailError ? (
                <p className="text-sm text-destructive py-2">{detailError}</p>
              ) : detailAddresses.length === 0 ? (
                <p className="text-sm text-muted-foreground py-2">Chưa có địa chỉ</p>
              ) : (
                <div className="space-y-2">
                  {detailAddresses.map((addr) => (
                    <div key={addr.id} className="flex items-start gap-2 rounded-md border px-3 py-2">
                      <MapPin className="h-4 w-4 shrink-0 mt-0.5 text-muted-foreground" />
                      <span className="flex-1 text-sm">{addr.address}</span>
                      {addr.isDefault && <Badge variant="secondary">Mặc định</Badge>}
                    </div>
                  ))}
                </div>
              )}
            </DetailSection>
          </div>
        </DialogContent>
      </Dialog>

      {/* Global controlled confirm dialog */}
      <AlertDialog open={!!confirmUser} onOpenChange={(o)=>{ if(!o) setConfirmUser(null) }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {confirmUser?.isActive ? 'Xác nhận khóa tài khoản' : 'Xác nhận mở khóa tài khoản'}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {confirmUser?.isActive ? (
                <>Bạn có chắc muốn khóa tài khoản của <b>{confirmUser?.name}</b>?</>
              ) : (
                <>Bạn có chắc muốn mở khóa tài khoản của <b>{confirmUser?.name}</b>?</>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={()=>setConfirmUser(null)}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              disabled={!confirmUser || blockingId === confirmUser?.id}
              onClick={async ()=>{ if(confirmUser){ await handleToggleActive(confirmUser); setConfirmUser(null) } }}
            >
              Xác nhận
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Confirm role change dialog */}
      <AlertDialog open={!!confirmRoleUser} onOpenChange={(o)=>{ if(!o) setConfirmRoleUser(null) }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {confirmRoleUser?.role === 'admin' ? 'Xác nhận chuyển thành User' : 'Xác nhận cấp quyền Admin'}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {confirmRoleUser?.role === 'admin' ? (
                <>Bạn có chắc muốn chuyển <b>{confirmRoleUser?.name}</b> thành người dùng thường?</>
              ) : (
                <>Bạn có chắc muốn cấp quyền Admin cho <b>{confirmRoleUser?.name}</b>?</>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={()=>setConfirmRoleUser(null)}>Hủy</AlertDialogCancel>
            <AlertDialogAction onClick={()=>{ if(confirmRoleUser){ handleToggleRole(confirmRoleUser); setConfirmRoleUser(null) } }}>Xác nhận</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
