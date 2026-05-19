"use client"

import { useEffect, useMemo, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Search, MoreVertical, CheckCircle2 } from "lucide-react"
import type { User } from "@/types"
import { useToast } from "@/hooks/use-toast"
import { useAdminUsers } from "@/hooks/users/use-admin-users"
import { setUserBlocking } from "@/services/users"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog"
// Đã bỏ bộ lọc vai trò/trạng thái theo yêu cầu
// Bỏ avatar theo yêu cầu

export default function UsersPage() {
  const [searchQuery, setSearchQuery] = useState("")
  // Bỏ filter, chỉ giữ search
  const [page, setPage] = useState(1)
  const [quickFilter, setQuickFilter] = useState<"all"|"customers"|"admins"|"active"|"locked">("all")
  const pageSize = 10

  const { toast } = useToast()
  const { data: users, loading, error, setData: setUsers } = useAdminUsers()
  const [confirmUser, setConfirmUser] = useState<User | null>(null)
  const [confirmRoleUser, setConfirmRoleUser] = useState<User | null>(null)
  const [blockingId, setBlockingId] = useState<string | null>(null)

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
      return matchesQuery && matchesQuick
    })
  }, [searchQuery, quickFilter, users])

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
  }, [quickFilter, searchQuery])

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
    <div className="space-y-8 px-18 pt-10 bg-background flex-1">
      <div className="text-center">
        <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight pb-3">Quản lý người dùng</h1>
        <div className="mx-auto mt-2 h-1 w-24 rounded bg-foreground/80" />
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5 pt-7">
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

      <Card className="border-muted/40 px-3">
        <CardHeader className="pb-0">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div className="space-y-1">
              <CardTitle className="text-2xl font-semibold tracking-tight">Danh sách người dùng</CardTitle>
            </div>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
              <div className="relative w-full sm:w-64">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Tìm tên hoặc email..."
                  value={searchQuery}
                  onChange={(e) => { setSearchQuery(e.target.value); setPage(1) }}
                  className="pl-10 h-10"
                />
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent className="pt-0">
          <div className="overflow-x-auto rounded-lg border">
            <Table className="[&_th]:py-4 [&_td]:py-3 [&_th]:px-6 [&_td]:px-6">
              <TableHeader>
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
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-32 text-center text-muted-foreground">
                      Đang tải danh sách người dùng...
                    </TableCell>
                  </TableRow>
                ) : error ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-32 text-center text-destructive">
                      {error}
                    </TableCell>
                  </TableRow>
                ) : filteredUsers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-32 text-center text-muted-foreground">
                      Không có người dùng phù hợp với bộ lọc hiện tại
                    </TableCell>
                  </TableRow>
                ) : (
                  pagedUsers.map((user) => (
                  <TableRow className="hover:bg-muted/40" key={user.id}>
                    <TableCell className="font-medium max-w-[240px]">
                      <div className="flex items-center gap-3">
                        <span className="text-sm sm:text-base font-semibold leading-tight truncate" title={user.name}>{user.name}</span>
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
                          <DropdownMenuItem>Xem chi tiết</DropdownMenuItem>
                          {/* Bỏ nút Chỉnh sửa theo yêu cầu */}
                          <DropdownMenuItem onClick={() => setConfirmRoleUser(user)}>
                            {user.role === 'admin' ? 'Chuyển thành User' : 'Cấp quyền Admin'}
                          </DropdownMenuItem>
                          <DropdownMenuItem onClick={() => setConfirmUser(user)}>
                            {user.isActive ? 'Khóa tài khoản' : 'Mở khóa'}
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                )))}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      <div className="flex items-center justify-between gap-2">
        <p className="text-sm text-muted-foreground">
          Hiển thị {(filteredUsers.length === 0) ? 0 : (page - 1) * pageSize + 1}
          –{Math.min(page * pageSize, filteredUsers.length)} trong tổng {filteredUsers.length}
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
