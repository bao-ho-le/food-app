"use client"

import { useEffect, useState } from "react"
import {
  User,
  MapPin,
  Heart,
  Edit,
  Trash2,
  PlusCircle,
  CalendarIcon,
  KeyRound,
  Star,
  Search,
  CheckCircle2,
  XCircle,
} from "lucide-react"
import { format } from "date-fns"
import { vi } from "date-fns/locale"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
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
import { Badge } from "@/components/ui/badge"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { useToast } from "@/hooks/use-toast"
import { Calendar } from "@/components/ui/calendar"
import { mockUsers } from "@/lib/mock-data"
import { AddressDialog } from "@/components/address-dialog"
import { ChangePasswordDialog } from "@/components/change-password-dialog"
import { cn } from "@/lib/utils"
import { fetchUserProfile, updateUserProfile } from "@/services/users"
import {
  fetchUserAddresses,
  deleteUserAddress,
  createUserAddress,
  updateUserAddress,
} from "@/services/addresses"
import type { UserAddressResponse } from "@/services/addresses"
import { fetchAllTags } from "@/services/tags"
import type { TagResponse } from "@/services/tags"
import { fetchUserBiases, updateUserBias } from "@/services/biases"
import type { UserBiasResponse } from "@/services/biases"
import type { Address, Bias, Category, Tag, User } from "@/types"

const DEFAULT_USER: User =
  (Array.isArray(mockUsers) && mockUsers.length > 0
    ? mockUsers[0]
    : {
        id: "user_fallback",
        email: "",
        name: "",
        phone: "",
        gender: "other",
        role: "user",
        birthdate: "",
        roleName: "USER",
        createdAt: "",
        isActive: true,
        bias: [],
        address: [],
      })

const mapApiAddressToClient = (
  apiAddress: UserAddressResponse,
  fallbackUserId: string,
  fallbackId?: string,
): Address => ({
  id:
    apiAddress.id !== undefined && apiAddress.id !== null
      ? String(apiAddress.id)
      : fallbackId ?? `addr-${Date.now()}`,
  address: apiAddress.address,
  isDefault: Boolean(apiAddress.isDefault),
  userId: apiAddress.user?.id ? String(apiAddress.user.id) : fallbackUserId,
})

const UNCATEGORIZED_CATEGORY: Category = { id: "uncategorized", name: "Khác" }

const mapApiTagToClient = (tag: TagResponse) => {
  const categoryId = tag.category?.id ? String(tag.category.id) : UNCATEGORIZED_CATEGORY.id
  const categoryName = tag.category?.name?.trim() || UNCATEGORIZED_CATEGORY.name

  return {
    tag: {
      id: String(tag.id),
      name: tag.name,
      categoryId,
    } as Tag,
    category: { id: categoryId, name: categoryName } as Category,
  }
}

const mapApiBiasToClient = (bias: UserBiasResponse, fallbackUserId: string): Bias | null => {
  if (!bias.tag?.id) {
    return null
  }
  return {
    id:
      bias.id !== undefined && bias.id !== null
        ? String(bias.id)
        : `bias-${bias.tag.id}-${Date.now()}`,
    userId: bias.user?.id ? String(bias.user.id) : fallbackUserId,
    tagId: String(bias.tag.id),
    score: typeof bias.score === "number" ? bias.score : 3,
  }
}

export default function ProfilePage() {
  const [user, setUser] = useState<User>(DEFAULT_USER)
  const [birthdate, setBirthdate] = useState<Date | undefined>(
    DEFAULT_USER.birthdate ? new Date(DEFAULT_USER.birthdate) : undefined,
  )
  const [searchTerm, setSearchTerm] = useState("")
  const { toast } = useToast()
  const [profileData, setProfileData] = useState({
    name: DEFAULT_USER.name,
    phone: DEFAULT_USER.phone,
  })
  const [gender, setGender] = useState<User["gender"]>(DEFAULT_USER.gender)
  const [isProfileLoading, setIsProfileLoading] = useState(false)
  const [isSavingProfile, setIsSavingProfile] = useState(false)
  const [profileError, setProfileError] = useState<string | null>(null)
  const [isAddressDialogOpen, setAddressDialogOpen] = useState(false)
  const [selectedAddress, setSelectedAddress] = useState<Address | null>(null)
  const [isDeleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [addressToDelete, setAddressToDelete] = useState<Address | null>(null)
  const [isPasswordDialogOpen, setPasswordDialogOpen] = useState(false)
  const [isAddressesLoading, setIsAddressesLoading] = useState(false)
  const [addressesError, setAddressesError] = useState<string | null>(null)
  const [preferenceTags, setPreferenceTags] = useState<Tag[]>([])
  const [preferenceCategories, setPreferenceCategories] = useState<Category[]>([UNCATEGORIZED_CATEGORY])
  const [isPreferencesLoading, setIsPreferencesLoading] = useState(false)
  const [preferencesError, setPreferencesError] = useState<string | null>(null)
  const [isBiasLoading, setIsBiasLoading] = useState(false)
  const [biasError, setBiasError] = useState<string | null>(null)
  const [initialBiasScores, setInitialBiasScores] = useState<Record<string, number>>({})
  const [pendingBiasChanges, setPendingBiasChanges] = useState<Record<string, number>>({})
  const [isSavingPreferences, setIsSavingPreferences] = useState(false)

  useEffect(() => {
    let isMounted = true

    const loadProfile = async () => {
      setIsProfileLoading(true)
      setProfileError(null)

      try {
        const token = typeof window !== "undefined" ? localStorage.getItem("token") : null

        if (!token) {
          throw new Error("Bạn cần đăng nhập để xem thông tin tài khoản.")
        }

        const data = await fetchUserProfile({ token })
        if (!isMounted) return

        const normalizedGender = data.gender?.toLowerCase() as User["gender"] | undefined
        const nextName = data.fullName ?? ""
        const nextPhone = data.phoneNumber ?? ""
        const nextEmail = data.email ?? ""
        const nextBirthdate = data.birthday ?? ""

        setUser((currentUser) => ({
          ...currentUser,
          name: nextName || currentUser.name,
          phone: nextPhone || currentUser.phone,
          email: nextEmail || currentUser.email,
          birthdate: nextBirthdate || currentUser.birthdate,
          gender: normalizedGender ?? currentUser.gender,
        }))

        setProfileData({
          name: nextName || "",
          phone: nextPhone || "",
        })
        setBirthdate(nextBirthdate ? new Date(nextBirthdate) : undefined)
        setGender(normalizedGender ?? DEFAULT_USER.gender)
        setProfileError(null)
      } catch (error) {
        if (!isMounted) return
        const message =
          error instanceof Error
            ? error.message
            : "Không thể tải thông tin tài khoản. Vui lòng thử lại."
        setProfileError(message)
        toast({
          variant: "destructive",
          title: "Không thể tải thông tin",
          description: message,
        })
      } finally {
        if (isMounted) {
          setIsProfileLoading(false)
        }
      }
    }

    loadProfile()
    const loadAddresses = async () => {
      setIsAddressesLoading(true)
      setAddressesError(null)
      try {
        const token = typeof window !== "undefined" ? localStorage.getItem("token") : null
        if (!token) {
          throw new Error("Vui lòng đăng nhập để xem sổ địa chỉ.")
        }
        const addresses = await fetchUserAddresses(token)
        if (!isMounted) return

        setUser((currentUser) => ({
          ...currentUser,
          address: addresses.map((addr) => mapApiAddressToClient(addr, currentUser.id)),
        }))
      } catch (error) {
        if (!isMounted) return
        const message =
          error instanceof Error
            ? error.message
            : "Không thể tải sổ địa chỉ. Vui lòng thử lại sau."
        setAddressesError(message)
      } finally {
        if (isMounted) {
          setIsAddressesLoading(false)
        }
      }
    }

    loadAddresses()

    const loadTags = async () => {
      setIsPreferencesLoading(true)
      setPreferencesError(null)
      try {
        const data = await fetchAllTags()
        if (!isMounted) return
        const categoryMap = new Map<string, string>()
        const normalizedTags: Tag[] = data.map((tag) => {
          const mapped = mapApiTagToClient(tag)
          categoryMap.set(mapped.category.id, mapped.category.name)
          return mapped.tag
        })

        if (categoryMap.size === 0) {
          categoryMap.set(UNCATEGORIZED_CATEGORY.id, UNCATEGORIZED_CATEGORY.name)
        }

        setPreferenceTags(normalizedTags)
        setPreferenceCategories(
          Array.from(categoryMap.entries()).map(([id, name]) => ({ id, name })),
        )
      } catch (error) {
        if (!isMounted) return
        const message =
          error instanceof Error
            ? error.message
            : "Không thể tải danh sách khẩu vị. Vui lòng thử lại."
        setPreferencesError(message)
      } finally {
        if (isMounted) {
          setIsPreferencesLoading(false)
        }
      }
    }

    const loadBiases = async () => {
      setIsBiasLoading(true)
      setBiasError(null)
      try {
        const token = typeof window !== "undefined" ? localStorage.getItem("token") : null
        if (!token) {
          throw new Error("Vui lòng đăng nhập để xem tùy chọn ẩm thực.")
        }
        const data = await fetchUserBiases(token)
        if (!isMounted) return
        const fallbackUserId = user.id || DEFAULT_USER.id
        const normalizedBiases = data
          .map((bias) => mapApiBiasToClient(bias, fallbackUserId))
          .filter((bias): bias is Bias => Boolean(bias))
        const baselineMap = normalizedBiases.reduce<Record<string, number>>((acc, bias) => {
          acc[bias.tagId] = bias.score
          return acc
        }, {})
        setInitialBiasScores(baselineMap)
        setPendingBiasChanges({})
        setUser((currentUser) => ({
          ...currentUser,
          bias: normalizedBiases,
        }))
      } catch (error) {
        if (!isMounted) return
        const message =
          error instanceof Error
            ? error.message
            : "Không thể tải dữ liệu khẩu vị. Vui lòng thử lại."
        setBiasError(message)
      } finally {
        if (isMounted) {
          setIsBiasLoading(false)
        }
      }
    }

    loadTags()
    loadBiases()

    return () => {
      isMounted = false
    }
  }, [toast])

  const handleScoreChange = (tagId: string, newScore: number) => {
    setUser((currentUser) => {
      const newBias = [...currentUser.bias]
      const biasIndex = newBias.findIndex((b) => b.tagId === tagId)
      if (biasIndex > -1) {
        newBias[biasIndex] = { ...newBias[biasIndex], score: newScore }
      } else {
        newBias.push({
          id: `bias-${Date.now()}-${tagId}`,
          userId: currentUser.id,
          tagId: tagId,
          score: newScore,
        })
      }
      return { ...currentUser, bias: newBias }
    })

    const baselineScore = initialBiasScores[tagId] ?? 3
    setPendingBiasChanges((prev) => {
      const next = { ...prev }
      if (baselineScore === newScore) {
        delete next[tagId]
      } else {
        next[tagId] = newScore
      }
      return next
    })
  }

  const handleSaveAddress = async (addressData: Omit<Address, "id" | "userId"> & { id?: string }) => {
    const token = typeof window !== "undefined" ? localStorage.getItem("token") : null
    if (!token) {
      toast({
        variant: "destructive",
        title: "Không thể lưu địa chỉ",
        description: "Vui lòng đăng nhập lại để tiếp tục.",
      })
      return
    }

    if (!addressData.address.trim()) {
      toast({
        variant: "destructive",
        title: "Lỗi",
        description: "Địa chỉ không được để trống.",
      })
      return
    }

    const payload = {
      address: addressData.address.trim(),
      isDefault: Boolean(addressData.isDefault),
    }

    try {
      const response = addressData.id
        ? await updateUserAddress(token, addressData.id, payload)
        : await createUserAddress(token, payload)

      setUser((currentUser) => {
        const normalizedAddress = mapApiAddressToClient(response, currentUser.id, addressData.id)
        const isEdit = Boolean(addressData.id)

        let newAddresses = isEdit
          ? currentUser.address.map((addr) =>
              addr.id === normalizedAddress.id ? normalizedAddress : addr,
            )
          : [...currentUser.address, normalizedAddress]

        if (normalizedAddress.isDefault) {
          newAddresses = newAddresses.map((addr) =>
            addr.id === normalizedAddress.id ? normalizedAddress : { ...addr, isDefault: false },
          )
        }

        return { ...currentUser, address: newAddresses }
      })

      toast({
        variant: "success",
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">Đã {addressData.id ? "cập nhật" : "thêm"} địa chỉ thành công!</span>
          </div>
        ),
      })

      setAddressDialogOpen(false)
      setSelectedAddress(null)
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "Không thể lưu địa chỉ. Vui lòng thử lại."
      toast({
        variant: "destructive",
        title: "Lỗi",
        description: message,
      })
    }
  }

  const handleDeleteAddress = async (addressId: string) => {
    const token = typeof window !== "undefined" ? localStorage.getItem("token") : null
    if (!token) {
      toast({
        variant: "destructive",
        title: "Không thể xóa",
        description: "Vui lòng đăng nhập lại để tiếp tục.",
      })
      return
    }

    try {
      await deleteUserAddress(token, addressId)
      setUser((currentUser) => {
        const newAddresses = currentUser.address.filter((addr) => addr.id !== addressId)
        return { ...currentUser, address: newAddresses }
      })
      toast({
        variant: "success",
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">Đã xóa địa chỉ thành công!</span>
          </div>
        ),
      })
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "Không thể xóa địa chỉ. Vui lòng thử lại."
      toast({
        variant: "destructive",
        title: "Lỗi",
        description: message,
      })
    } finally {
      setDeleteDialogOpen(false)
      setAddressToDelete(null)
    }
  }

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setProfileData({ ...profileData, [e.target.name]: e.target.value })
  }

  const handleProfileSave = async () => {
    const { name, phone } = profileData
    if (!name.trim() || !phone.trim()) {
      toast({
        variant: "destructive",
        title: "Lỗi",
        description: "Vui lòng điền đầy đủ họ tên và số điện thoại.",
      })
      return
    }
    if (!/^\d{10}$/.test(phone)) {
      toast({
        variant: "destructive",
        title: "Lỗi",
        description: "Số điện thoại phải có đúng 10 chữ số.",
      })
      return
    }
    const oneYearAgo = new Date()
    oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 10)

    if (birthdate && birthdate > oneYearAgo) {
      toast({
        variant: "destructive",
        title: "Lỗi",
        description: "Ngày sinh không hợp lệ. Bạn phải lớn hơn 10 tuổi.",
      })
      return
    }
    const token = typeof window !== "undefined" ? localStorage.getItem("token") : null
    if (!token) {
      toast({
        variant: "destructive",
        title: "Không thể cập nhật",
        description: "Vui lòng đăng nhập lại để tiếp tục.",
      })
      return
    }

    const payload = {
      fullName: name.trim(),
      birthday: birthdate ? format(birthdate, "dd-MM-yyyy") : null,
      gender: gender.toUpperCase() as "MALE" | "FEMALE" | "OTHER",
      phoneNumber: phone.trim(),
      email: user.email,
    }

    try {
      setIsSavingProfile(true)
      const response = await updateUserProfile(payload, { token })

      const responseGender = response?.gender?.toLowerCase() as User["gender"] | undefined
      const nextGender = responseGender ?? gender
      const nextName = response?.fullName ?? payload.fullName
      const nextPhone = response?.phoneNumber ?? payload.phoneNumber
      const nextEmail = response?.email ?? payload.email
      const nextBirthdateString =
        response && "birthday" in response ? response.birthday : payload.birthday
      const nextBirthdateDate = nextBirthdateString ? new Date(nextBirthdateString) : undefined

      setUser((currentUser) => ({
        ...currentUser,
        name: nextName,
        phone: nextPhone,
        email: nextEmail,
        birthdate: nextBirthdateString ?? "",
        gender: nextGender,
      }))
      setProfileData({
        name: nextName,
        phone: nextPhone,
      })
      setBirthdate(nextBirthdateDate)
      setGender(nextGender)

      toast({
        variant: "success",
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">Đã cập nhật thông tin thành công!</span>
          </div>
        ),
      })
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Không thể cập nhật thông tin. Vui lòng thử lại sau."
      toast({
        variant: "destructive",
        title: "Lỗi",
        description: message,
      })
    } finally {
      setIsSavingProfile(false)
    }
  }

  const handleSavePreferences = async () => {
    const pendingEntries = Object.entries(pendingBiasChanges)
    if (pendingEntries.length === 0) {
      toast({
        title: "Không có thay đổi",
        description: "Bạn chưa điều chỉnh khẩu vị nào.",
      })
      return
    }

    const token = typeof window !== "undefined" ? localStorage.getItem("token") : null
    if (!token) {
      toast({
        variant: "destructive",
        title: "Không thể lưu khẩu vị",
        description: "Vui lòng đăng nhập lại để tiếp tục.",
      })
      return
    }

    setIsSavingPreferences(true)
    try {
      const apiResponses: Record<string, UserBiasResponse | undefined> = {}
      for (const [tagId, score] of pendingEntries) {
        const numericTagId = Number(tagId)
        const payloadTagId = Number.isNaN(numericTagId) ? tagId : numericTagId
        const response = await updateUserBias(token, { tagId: payloadTagId, score })
        apiResponses[tagId] = response
      }

      setInitialBiasScores((prev) => {
        const updated = { ...prev }
        for (const [tagId, score] of pendingEntries) {
          updated[tagId] = score
        }
        return updated
      })
      setPendingBiasChanges({})

      setUser((currentUser) => {
        const updatedBiases = currentUser.bias.map((bias) => {
          const response = apiResponses[bias.tagId]
          if (response?.id !== undefined && response?.id !== null) {
            return { ...bias, id: String(response.id) }
          }
          return bias
        })
        return { ...currentUser, bias: updatedBiases }
      })

      toast({
        variant: "success",
        title: (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-500" />
            <span className="font-medium">Đã lưu tùy chọn thành công!</span>
          </div>
        ),
      })
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Không thể cập nhật khẩu vị. Vui lòng thử lại."
      toast({
        variant: "destructive",
        title: "Lỗi",
        description: message,
      })
    } finally {
      setIsSavingPreferences(false)
    }
  }

  const normalizedSearchTerm = searchTerm.trim().toLowerCase()
  const filteredCategories = preferenceCategories.filter((category) =>
    preferenceTags.some(
      (tag) =>
        tag.categoryId === category.id &&
        tag.name.toLowerCase().includes(normalizedSearchTerm),
    ),
  )

  return (
    <div className="container mx-auto max-w-5xl px-4 py-8">
      <Tabs defaultValue="profile" className="w-full">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="profile" className="py-2.5 text-sm md:text-base">
            <User className="mr-2 h-4 w-4" />
            Thông tin tài khoản
          </TabsTrigger>
          <TabsTrigger value="addresses" className="py-2.5 text-sm md:text-base">
            <MapPin className="mr-2 h-4 w-4" />
            Sổ địa chỉ
          </TabsTrigger>
          <TabsTrigger value="preferences" className="py-2.5 text-sm md:text-base">
            <Heart className="mr-2 h-4 w-4" />
            Tùy chọn ẩm thực
          </TabsTrigger>
        </TabsList>

        <TabsContent value="profile" className="mt-8">
          <div className="rounded-lg border bg-card p-8 text-card-foreground shadow-sm">
            <div className="mb-8 flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-bold">Thông tin cá nhân</h2>
                <p className="text-muted-foreground">Quản lý thông tin cá nhân của bạn.</p>
              </div>
              <Button variant="outline" onClick={() => setPasswordDialogOpen(true)}>
                <KeyRound className="mr-2 h-4 w-4" />
                Đổi mật khẩu
              </Button>
            </div>
            {profileError && (
              <div className="mb-6 rounded-md border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {profileError}
              </div>
            )}
            {isProfileLoading && !profileError && (
              <div className="mb-6 rounded-md border border-dashed border-muted px-4 py-3 text-sm text-muted-foreground">
                Đang tải thông tin tài khoản...
              </div>
            )}
            <div className="space-y-8">
              <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="name">Họ và tên</Label>
                  <Input
                    id="name"
                    name="name"
                    value={profileData.name}
                    onChange={handleProfileChange}
                    disabled={isProfileLoading || isSavingProfile}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Số điện thoại</Label>
                  <Input
                    id="phone"
                    name="phone"
                    value={profileData.phone}
                    onChange={handleProfileChange}
                    disabled={isProfileLoading || isSavingProfile}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input id="email" type="email" value={user.email} disabled readOnly />
                </div>
                <div className="space-y-2">
                  <Label>Ngày sinh</Label>
                  <Popover>
                    <PopoverTrigger asChild>
                      <Button
                        variant={"outline"}
                        className={cn(
                          "w-full justify-start text-left font-normal",
                          !birthdate && "text-muted-foreground",
                        )}
                        disabled={isProfileLoading || isSavingProfile}
                      >
                        <CalendarIcon className="mr-2 h-4 w-4" />
                        {birthdate ? format(birthdate, "PPP", { locale: vi }) : <span>Chọn ngày</span>}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0">
                      <Calendar
                        mode="single"
                        selected={birthdate}
                        onSelect={setBirthdate}
                        initialFocus
                        captionLayout="dropdown"
                        fromYear={1950}
                        toYear={new Date().getFullYear()}
                      />
                    </PopoverContent>
                  </Popover>
                </div>
              </div>
              <div className="space-y-3">
                <Label>Giới tính</Label>
                <RadioGroup
                  value={gender}
                  onValueChange={(value) => setGender(value as User["gender"])}
                  className="flex gap-6"
                >
                  <div className="flex items-center space-x-2">
                    <RadioGroupItem value="male" id="male" disabled={isProfileLoading || isSavingProfile} />
                    <Label htmlFor="male">Nam</Label>
                  </div>
                  <div className="flex items-center space-x-2">
                    <RadioGroupItem
                      value="female"
                      id="female"
                      disabled={isProfileLoading || isSavingProfile}
                    />
                    <Label htmlFor="female">Nữ</Label>
                  </div>
                  <div className="flex items-center space-x-2">
                    <RadioGroupItem value="other" id="other" disabled={isProfileLoading || isSavingProfile} />
                    <Label htmlFor="other">Khác</Label>
                  </div>
                </RadioGroup>
              </div>
              <div className="border-t pt-6">
                <Button
                  onClick={handleProfileSave}
                  className="bg-green-700 hover:bg-green-800"
                  disabled={isProfileLoading || isSavingProfile}
                >
                  {isSavingProfile ? "Đang lưu..." : "Lưu thay đổi"}
                </Button>
              </div>
            </div>
          </div>
        </TabsContent>

        <TabsContent value="addresses" className="mt-8">
          <div className="rounded-lg border bg-card p-8 text-card-foreground shadow-sm">
            <div className="mb-6 flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-bold">Sổ địa chỉ</h2>
                <p className="text-muted-foreground">Quản lý địa chỉ nhận hàng của bạn.</p>
              </div>
              <Button
                variant="outline"
                onClick={() => {
                  setSelectedAddress(null)
                  setAddressDialogOpen(true)
                }}
              >
                <PlusCircle className="mr-2 h-4 w-4" />
                Thêm địa chỉ mới
              </Button>
            </div>
            {addressesError && (
              <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {addressesError}
              </div>
            )}
            {isAddressesLoading && (
              <div className="mb-4 rounded-md border border-dashed border-muted px-4 py-3 text-sm text-muted-foreground">
                Đang tải sổ địa chỉ...
              </div>
            )}
            <div className="space-y-4">
              {user.address.map((addr: Address) => (
                <div
                  key={addr.id}
                  className="flex items-center justify-between rounded-lg border p-4 transition-colors hover:bg-muted/50"
                >
                  <div className="flex items-start gap-4">
                    <MapPin className="mt-1 h-5 w-5 shrink-0 text-muted-foreground" />
                    <span className="font-medium">{addr.address}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    {addr.isDefault && (
                      <Badge className="bg-green-100 text-green-800 hover:bg-green-200">
                        Mặc định
                      </Badge>
                    )}
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8"
                      onClick={() => {
                        setSelectedAddress(addr)
                        setAddressDialogOpen(true)
                      }}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 text-destructive hover:text-destructive"
                      onClick={() => {
                        setAddressToDelete(addr)
                        setDeleteDialogOpen(true)
                      }}
                      disabled={addr.isDefault}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </TabsContent>

        <TabsContent value="preferences" className="mt-8">
          <div className="rounded-lg border bg-card p-8 text-card-foreground shadow-sm">
            <div className="mb-8 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div>
                <h2 className="text-2xl font-bold">Tùy chọn ẩm thực</h2>
                <p className="text-muted-foreground">
                  Giúp chúng tôi hiểu rõ hơn về khẩu vị của bạn để đưa ra những gợi ý tốt nhất.
                </p>
              </div>
              <div className="relative w-full md:w-auto">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Tìm kiếm sở thích..."
                  className="w-full pl-8 md:w-[250px]"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>

            {preferencesError && (
              <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {preferencesError}
              </div>
            )}
            {biasError && (
              <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {biasError}
              </div>
            )}
            {(isPreferencesLoading || isBiasLoading) && (
              <div className="mb-4 rounded-md border border-dashed border-muted px-4 py-3 text-sm text-muted-foreground">
                Đang tải dữ liệu khẩu vị...
              </div>
            )}

            <div className="space-y-8">
              {filteredCategories.length === 0 && !isPreferencesLoading ? (
                <p className="text-sm text-muted-foreground">Chưa có dữ liệu khẩu vị để hiển thị.</p>
              ) : (
                filteredCategories.map((category) => {
                  const tagsInCategory = preferenceTags.filter(
                    (tag) =>
                      tag.categoryId === category.id &&
                      tag.name.toLowerCase().includes(normalizedSearchTerm),
                  )
                  if (tagsInCategory.length === 0) return null
                  return (
                    <div key={category.id}>
                      <Label className="text-lg font-semibold">{category.name}</Label>
                      <div className="mt-4 flex flex-wrap gap-3">
                        {tagsInCategory.map((tag) => {
                          const userBias = user.bias.find((b: Bias) => b.tagId === tag.id)
                          const score = userBias ? userBias.score : 3
                          const getFillPercentage = (value: number): number => {
                            const scoreMap: { [key: number]: number } = {
                              1: 0,
                              2: 25,
                              3: 50,
                              4: 75,
                              5: 100,
                            }
                            return scoreMap[value] ?? 50
                          }
                          const fillPercentage = getFillPercentage(score)

                          return (
                            <Popover key={tag.id}>
                              <PopoverTrigger asChild>
                                <div className="relative inline-flex cursor-pointer items-center justify-center overflow-hidden rounded-full border border-border bg-background px-4 py-1.5 text-sm font-medium transition-colors hover:bg-amber-50">
                                  <div
                                    className="absolute left-0 top-0 h-full bg-amber-200 transition-all duration-300"
                                    style={{ width: `${fillPercentage}%` }}
                                  />
                                  <span className="relative z-10 text-amber-900">{tag.name}</span>
                                </div>
                              </PopoverTrigger>
                              <PopoverContent className="w-auto p-2">
                                <div className="flex items-center gap-1">
                                  {[1, 2, 3, 4, 5].map((star) => (
                                    <Star
                                      key={star}
                                      className={cn(
                                        "h-6 w-6 cursor-pointer transition-colors",
                                        score >= star
                                          ? "fill-yellow-400 text-yellow-400"
                                          : "text-gray-300 hover:text-yellow-300",
                                      )}
                                      onClick={() => handleScoreChange(tag.id, star)}
                                    />
                                  ))}
                                </div>
                              </PopoverContent>
                            </Popover>
                          )
                        })}
                      </div>
                    </div>
                  )
                })
              )}
            </div>
            <div className="mt-8 border-t pt-6">
              <Button
                className="bg-green-700 hover:bg-green-800"
                onClick={handleSavePreferences}
                disabled={isSavingPreferences}
              >
                {isSavingPreferences ? "Đang lưu..." : "Lưu tùy chọn"}
              </Button>
            </div>
          </div>
        </TabsContent>
      </Tabs>

      <AddressDialog
        open={isAddressDialogOpen}
        onOpenChange={setAddressDialogOpen}
        address={selectedAddress}
        onSave={handleSaveAddress}
      />

      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Bạn có chắc chắn muốn xóa?</AlertDialogTitle>
            <AlertDialogDescription>
              Hành động này không thể hoàn tác. Địa chỉ này sẽ bị xóa vĩnh viễn khỏi sổ địa chỉ của
              bạn.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction onClick={() => addressToDelete && handleDeleteAddress(addressToDelete.id)}>
              Tiếp tục
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <ChangePasswordDialog open={isPasswordDialogOpen} onOpenChange={setPasswordDialogOpen} />
    </div>
  )
}
