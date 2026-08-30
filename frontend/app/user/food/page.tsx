"use client"

import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { createPortal } from "react-dom"
import Image from "next/image"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useCart } from "@/hooks/use-cart"
import { mockRestaurants } from "@/lib/mock-data"
import { fetchAllTags } from "@/services/tags"
import { fetchDishesRaw, type DishApiResponse } from "@/services/dishes"
import { fetchDishReviews, type DishReviewResponse } from "@/services/reviews"
import { addUserCartItem } from "@/services/user-cart"
import { Search, Star, Plus, Flame, MessageCircle, MapPin, Phone, CheckCircle2 } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import type { Dish, Tag } from "@/types"
import { SearchBar } from "@/components/search-bar"
import {
  FilterPopover,
  DEFAULT_DISH_FILTER_VALUES,
  PRICE_RANGE_OPTIONS,
  type DishFilterValues,
} from "@/components/dish-filters"

export default function FoodPage() {
  const [dishes, setDishes] = useState<Dish[]>([])
  const [restaurantInfoMap, setRestaurantInfoMap] = useState<
    Record<string, { name: string; address?: string; phone?: string }>
  >({})
  const [dishesLoading, setDishesLoading] = useState(false)
  const [dishesError, setDishesError] = useState<string | null>(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [committedFilters, setCommittedFilters] = useState<DishFilterValues>(DEFAULT_DISH_FILTER_VALUES)
  const [tagNameById, setTagNameById] = useState<Map<string, string>>(new Map())
  const [selectedDishId, setSelectedDishId] = useState<string | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const { addToCart } = useCart()
  const { toast } = useToast()
  const [navSlot, setNavSlot] = useState<HTMLElement | null>(null)

  useEffect(() => {
    setNavSlot(document.getElementById("user-nav-search-slot"))
  }, [])

  const ITEMS_PER_PAGE = 12

  const [selectedRatingFilter, setSelectedRatingFilter] = useState<number>(0)
  const REVIEWS_PER_PAGE = 3
  const [visibleReviewCount, setVisibleReviewCount] = useState(REVIEWS_PER_PAGE)
  const [reviewsByDish, setReviewsByDish] = useState<Record<string, DishReviewResponse[]>>({})
  const [reviewsLoading, setReviewsLoading] = useState(false)
  const [reviewsError, setReviewsError] = useState<string | null>(null)
  const pendingAdditionsRef = useRef<
    Record<string, { timeout: ReturnType<typeof setTimeout>; quantity: number }>
  >({})
  const flushPendingAdditions = useCallback(async () => {
    const entries = Object.entries(pendingAdditionsRef.current)
    pendingAdditionsRef.current = {}
    await Promise.all(
      entries.map(async ([dishId, data]) => {
        clearTimeout(data.timeout)
        if (data.quantity <= 0) return
        const numericId = Number(dishId)
        const payloadDishId = Number.isNaN(numericId) ? dishId : numericId
        try {
          await addUserCartItem(payloadDishId, data.quantity)
        } catch (error) {
          toast({
            variant: "destructive",
            title: "Không thể cập nhật giỏ hàng",
            description: error instanceof Error ? error.message : "Vui lòng thử lại sau.",
          })
        }
      }),
    )
  }, [toast])

  const scheduleAddSync = useCallback(
    (dishId: string) => {
      const current = pendingAdditionsRef.current[dishId]
      const nextQuantity = (current?.quantity ?? 0) + 1
      if (current) {
        clearTimeout(current.timeout)
      }
      const timeout = setTimeout(async () => {
        const pending = pendingAdditionsRef.current[dishId]
        if (!pending) return
        delete pendingAdditionsRef.current[dishId]
        if (pending.quantity <= 0) return
        const numericId = Number(dishId)
        const payloadDishId = Number.isNaN(numericId) ? dishId : numericId
        try {
          await addUserCartItem(payloadDishId, pending.quantity)
        } catch (error) {
          toast({
            variant: "destructive",
            title: "Không thể thêm vào giỏ hàng",
            description: error instanceof Error ? error.message : "Vui lòng thử lại sau.",
          })
        }
      }, 1000)
      pendingAdditionsRef.current[dishId] = { timeout, quantity: nextQuantity }
    },
    [toast],
  )

  useEffect(() => {
    return () => {
      flushPendingAdditions()
    }
  }, [flushPendingAdditions])

  useEffect(() => {
    let isMounted = true
    const loadDishes = async () => {
      setDishesLoading(true)
      setDishesError(null)
      try {
        const data = await fetchDishesRaw()
        if (!isMounted) return
        const mappedDishes: Dish[] = data.map((dish) => {
          const restaurantId =
            dish.restaurant?.id !== undefined && dish.restaurant?.id !== null
              ? String(dish.restaurant.id)
              : `rest-${dish.id}`
          return {
            id: String(dish.id),
            restaurantId,
            name: dish.name,
            description: dish.description ?? "",
            price: Number(dish.price ?? 0),
            image: dish.url || "/placeholder.svg",
            category: dish.restaurant?.name ?? "Món ăn",
            rating: typeof dish.rating === "number" ? dish.rating : 0,
            totalReviews: dish.totalReviews ?? 0,
            isAvailable: Boolean(dish.available ?? true),
            stockQuantity: Number(dish.stockQuantity ?? 0),
            spicyLevel: "none",
            tags: (dish.tags ?? []).map((tag) => tag.name.trim()),
          }
        })

        const restaurantMap: Record<string, { name: string; address?: string; phone?: string }> = {}

        data.forEach((dish) => {
          const dishId = String(dish.id)
          if (dish.restaurant?.name) {
            restaurantMap[dishId] = {
              name: dish.restaurant.name,
              address: dish.restaurant.address,
              phone: dish.restaurant.phoneNumber,
            }
          }
        })

        setDishes(mappedDishes)
        setRestaurantInfoMap(restaurantMap)
      } catch (error) {
        if (!isMounted) return
        setDishesError("Không thể tải danh sách món ăn. Đang hiển thị dữ liệu mẫu.")
      } finally {
        if (isMounted) setDishesLoading(false)
      }
    }
    loadDishes()
    return () => {
      isMounted = false
    }
  }, [])

  const handleTagsLoaded = useCallback((tags: Tag[]) => {
    setTagNameById(new Map(tags.map((tag) => [tag.id, tag.name])))
  }, [])

  const filteredDishes = useMemo(() => {
    const { priceRange, ratingMin, tagIds, sortOrder } = committedFilters

    const filtered = dishes.filter((dish) => {
      const matchesSearch = dish.name.toLowerCase().includes(searchQuery.toLowerCase())

      let matchesPrice = true
      if (priceRange) {
        const range = PRICE_RANGE_OPTIONS.find((option) => option.value === priceRange)
        if (range) {
          matchesPrice = dish.price >= range.min && (range.max === undefined || dish.price <= range.max)
        }
      }

      const matchesRating = ratingMin === "" || dish.rating >= Number(ratingMin)

      const matchesTags = tagIds.every((tagId) => {
        const tagName = tagNameById.get(tagId)
        return tagName !== undefined && dish.tags.includes(tagName)
      })

      return matchesSearch && matchesPrice && matchesRating && matchesTags && dish.isAvailable
    })

    if (sortOrder === "asc") {
      return [...filtered].sort((a, b) => a.price - b.price)
    }
    if (sortOrder === "desc") {
      return [...filtered].sort((a, b) => b.price - a.price)
    }
    return filtered
  }, [dishes, searchQuery, committedFilters, tagNameById])

  useEffect(() => {
    setCurrentPage(1)
  }, [searchQuery, committedFilters])

  useEffect(() => {
    if (!filteredDishes.length) {
      setSelectedDishId(null)
      return
    }

    const isSelectedVisible = filteredDishes.some((dish) => dish.id === selectedDishId)
    if (!isSelectedVisible || selectedDishId === null) {
      setSelectedDishId(filteredDishes[0].id)
    }
  }, [filteredDishes, selectedDishId])

  useEffect(() => {
    setSelectedRatingFilter(0)
    setVisibleReviewCount(REVIEWS_PER_PAGE)
  }, [selectedDishId])

  const totalPages = filteredDishes.length ? Math.ceil(filteredDishes.length / ITEMS_PER_PAGE) : 1

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages)
    }
  }, [currentPage, totalPages])

  const paginatedDishes = useMemo(() => {
    if (!filteredDishes.length) return []
    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE
    return filteredDishes.slice(startIndex, startIndex + ITEMS_PER_PAGE)
  }, [currentPage, filteredDishes])

  useEffect(() => {
    if (!paginatedDishes.length) return
    if (!selectedDishId || !paginatedDishes.some((dish) => dish.id === selectedDishId)) {
      setSelectedDishId(paginatedDishes[0]?.id ?? null)
    }
  }, [paginatedDishes, selectedDishId])

  const selectedDish = useMemo(
    () => filteredDishes.find((dish) => dish.id === selectedDishId),
    [filteredDishes, selectedDishId],
  )

  useEffect(() => {
    if (!selectedDishId) return
    if (reviewsByDish[selectedDishId]) return
    let cancelled = false
    setReviewsLoading(true)
    setReviewsError(null)
    fetchDishReviews(selectedDishId)
      .then((data) => {
        if (cancelled) return
        setReviewsByDish((prev) => ({ ...prev, [selectedDishId]: data }))
      })
      .catch((error) => {
        if (cancelled) return
        setReviewsError(
          error instanceof Error ? error.message : "Không thể tải đánh giá. Vui lòng thử lại.",
        )
      })
      .finally(() => {
        if (!cancelled) setReviewsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [selectedDishId, reviewsByDish])

  const getDishReviews = (dishId: string) => reviewsByDish[dishId] ?? []

  const filteredReviews = useMemo(() => {
    if (!selectedDish) return []
    const reviews = getDishReviews(selectedDish.id)

    if (selectedRatingFilter === 0) {
      return reviews
    }

    // Lọc theo số sao chính xác
    return reviews.filter((review) => Math.floor(review.rating ?? 0) === selectedRatingFilter)
  }, [selectedDish, selectedRatingFilter, reviewsByDish])

  const visibleReviews = useMemo(() => {
    return filteredReviews.slice(0, visibleReviewCount)
  }, [filteredReviews, visibleReviewCount])

  const overallRating = useMemo(() => {
    if (!selectedDish) return null
    const reviews = getDishReviews(selectedDish.id)
    if (!reviews.length) return null
    const total = reviews.reduce((sum, review) => sum + (review.rating ?? 0), 0)
    return (total / reviews.length).toFixed(1)
  }, [selectedDish, reviewsByDish])

  const handleAddToCart = (dish: Dish) => {
    addToCart(dish)
    scheduleAddSync(dish.id)
    toast({
      title: (
        <div className="flex items-center gap-3">
          <CheckCircle2 className="h-5 w-5 text-green-500" />
          <span className="font-medium">
            "{dish.name}" đã được thêm vào giỏ hàng!
          </span>
        </div>
      ),
    })
  }

  const handleApplyFilters = (values: DishFilterValues) => {
    setCommittedFilters(values)
  }

  const getRestaurant = (dish: Dish) => {
    if (restaurantInfoMap[dish.id]) {
      return restaurantInfoMap[dish.id]
    }
    return mockRestaurants.find((r) => r.id === dish.restaurantId)
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {navSlot &&
        createPortal(
          <div className="flex w-full max-w-md items-center gap-2">
            <SearchBar
              value={searchQuery}
              onChange={setSearchQuery}
              placeholder="Tìm kiếm món ăn..."
              leftSlot={<Search className="h-4 w-4" />}
              className="h-10 flex-1"
            />
            <FilterPopover
              filters={committedFilters}
              onApply={handleApplyFilters}
              onTagsLoaded={handleTagsLoaded}
            />
          </div>,
          navSlot,
        )}
      {dishesError && (
        <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {dishesError}
        </div>
      )}

      <div className="grid gap-8 lg:grid-cols-[minmax(0,1fr)_320px]">
        {/* Dishes Grid */}
        <div className="flex flex-col gap-6">
          <div className="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
            {dishesLoading && paginatedDishes.length === 0 && (
              <div className="col-span-full rounded-xl border border-dashed border-muted-foreground/40 p-8 text-center text-sm text-muted-foreground">
                Đang tải danh sách món ăn...
              </div>
            )}
            {paginatedDishes.map((dish) => {
              const isSelected = selectedDish?.id === dish.id
              return (
                <Card
                  key={dish.id}
                  tabIndex={0}
                  role="button"
                  onClick={() => setSelectedDishId(dish.id)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" || event.key === " ") {
                      event.preventDefault()
                      setSelectedDishId(dish.id)
                    }
                  }}
                  className={`flex h-full flex-col gap-0 overflow-hidden border-2 py-0 transition-all duration-200 focus:outline-none ${isSelected
                    ? "border-primary shadow-xl"
                    : "border-border/50 hover:border-primary/70 hover:shadow-lg hover:-translate-y-1"
                    }`}
                >
                  <CardContent className="flex flex-1 flex-col p-0">
                    <div className="relative aspect-[4/3] overflow-hidden bg-muted">
                      <Image src={dish.image || "/placeholder.svg"} alt={dish.name} fill className="object-cover" />
                      {dish.tags.includes("Popular") && (
                        <Badge className="absolute right-2 top-2 rounded-full bg-amber-500 px-3 py-1 text-xs font-semibold text-white shadow-sm">
                          Phổ biến
                        </Badge>
                      )}
                    </div>
                    <div className="flex flex-1 flex-col gap-3 px-4 pb-4 pt-4">
                      <div className="flex items-start justify-between gap-3">
                        <h3 className="line-clamp-1 text-base font-bold text-card-foreground">{dish.name}</h3>
                        <div className="flex items-center gap-1 text-base font-semibold text-amber-500 flex-shrink-0">
                          <Star className="h-4 w-4 fill-current" />
                          <span>{dish.rating.toFixed(1)}</span>
                        </div>
                      </div>
                      <div className="mt-auto flex w-full flex-nowrap gap-2 overflow-x-auto">
                        {dish.tags.map((tag) => (
                          <Badge
                            key={tag}
                            variant="outline"
                            className="rounded-full border-primary/20 bg-primary/5 px-2 text-xs font-medium text-primary whitespace-nowrap"
                          >
                            {tag}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  </CardContent>
                  <CardFooter className="flex items-center justify-between border-t border-border bg-muted/20 p-4">
                    <div className="flex flex-col gap-0.5">
                      <span className="text-xl font-semibold text-primary">{dish.price.toLocaleString("vi-VN")}đ</span>
                      {dish.stockQuantity > 0 ? (
                        <span className="text-xs text-muted-foreground">Còn {dish.stockQuantity} phần</span>
                      ) : (
                        <span className="text-xs font-medium text-rose-600">Hết hàng</span>
                      )}
                    </div>
                    <Button
                      size="sm"
                      disabled={dish.stockQuantity <= 0}
                      className="min-w-[110px] rounded-full bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground shadow-[0_8px_16px_rgba(34,197,94,0.2)] hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-50"
                      onClick={(e) => { e.stopPropagation(); handleAddToCart(dish); }}
                    >
                      <Plus className="mr-1 h-4 w-4" />
                      Thêm
                    </Button>
                  </CardFooter>
                </Card>
              )
            })}
          </div>

          {totalPages > 1 && (
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    href="#"
                    onClick={(event) => {
                      event.preventDefault()
                      setCurrentPage((prev) => Math.max(1, prev - 1))
                    }}
                    aria-disabled={currentPage === 1}
                    className={currentPage === 1 ? "pointer-events-none opacity-50" : undefined}
                  />
                </PaginationItem>
                {Array.from({ length: totalPages }, (_, index) => {
                  const pageNumber = index + 1
                  return (
                    <PaginationItem key={pageNumber}>
                      <PaginationLink
                        href="#"
                        isActive={pageNumber === currentPage}
                        onClick={(event) => {
                          event.preventDefault()
                          setCurrentPage(pageNumber)
                        }}
                      >
                        {pageNumber}
                      </PaginationLink>
                    </PaginationItem>
                  )
                })}
                <PaginationItem>
                  <PaginationNext
                    href="#"
                    onClick={(event) => {
                      event.preventDefault()
                      setCurrentPage((prev) => Math.min(totalPages, prev + 1))
                    }}
                    aria-disabled={currentPage === totalPages}
                    className={currentPage === totalPages ? "pointer-events-none opacity-50" : undefined}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          )}
        </div>

        <div className="sticky top-20 flex max-h-[calc(100vh-6rem)] flex-col gap-8 overflow-y-auto">
          {selectedDish ? (
            <>
              {/* Card 1: Thông tin món ăn & nhà hàng */}
              <aside className="h-fit rounded-xl border border-border bg-card shadow-sm overflow-hidden">
                <div className="p-4">
                  <div className="mb-4">
                    <h2 className="text-xl font-bold text-foreground mb-1">{selectedDish.name}</h2>
                    <p className="text-sm text-muted-foreground mb-3">{selectedDish.description}</p>
                    <div className="flex flex-wrap gap-2">
                      {selectedDish.tags.map((tag) => (
                        <Badge key={tag} variant="secondary" className="font-normal">
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </div>
                  {(() => {
                    const restaurant = getRestaurant(selectedDish)
                    if (!restaurant) return null
                    return (
                      <div className="space-y-2 rounded-lg border border-border bg-muted/20 p-3">
                        <p className="text-base font-semibold text-foreground">{restaurant.name}</p>
                        <div className="flex items-start gap-2 text-xs text-muted-foreground">
                          <MapPin className="mt-0.5 h-4 w-4 flex-shrink-0 text-primary" />
                          <span>{restaurant.address}</span>
                        </div>
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                          <Phone className="h-4 w-4 flex-shrink-0 text-primary" />
                          <span>{restaurant.phone}</span>
                        </div>
                      </div>
                    )
                  })()}
                </div>
              </aside>

              {/* Card 2: Đánh giá với bộ lọc và nút "Xem thêm" */}
              <aside className="h-fit rounded-xl border border-border bg-card shadow-sm overflow-hidden">
                <div className="px-4 py-3 flex justify-between items-center border-b border-border">
                  <h3 className="text-lg font-semibold">
                    Đánh giá
                    {selectedDish && (
                      <span className="ml-2 text-sm font-normal text-muted-foreground">
                        ({getDishReviews(selectedDish.id).length} lượt)
                      </span>
                    )}
                  </h3>

                  <Select
                    value={selectedRatingFilter.toString()}
                    onValueChange={(value) => {
                      setSelectedRatingFilter(Number(value))
                      setVisibleReviewCount(REVIEWS_PER_PAGE) // Reset khi lọc
                    }}
                  >
                    <SelectTrigger className="w-auto h-9">
                      <SelectValue placeholder="Lọc theo sao" />
                    </SelectTrigger>
                    <SelectContent>
                      {[0, 5, 4, 3, 2, 1].map((rating) => (
                        <SelectItem key={rating} value={rating.toString()}>
                          {rating === 0 ? "Tất cả" : (
                            <span className="flex items-center gap-1.5">
                              {rating} <Star className="h-3 w-3 fill-amber-400 text-amber-400" />
                            </span>
                          )}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {reviewsError && (
                  <div className="border-b border-destructive/40 bg-destructive/10 px-4 py-2 text-xs text-destructive">
                    {reviewsError}
                  </div>
                )}
                <div className="max-h-[450px] overflow-y-auto p-4">
                  <div className="flex flex-col gap-3">
                    {reviewsLoading && (
                      <div className="py-6 text-center text-sm text-muted-foreground">
                        Đang tải đánh giá...
                      </div>
                    )}
                    {!reviewsLoading && visibleReviews.length > 0 ? (
                      visibleReviews.map((review, index) => (
                        <div
                          key={`${selectedDish.id}-${index}`}
                          className="rounded-lg border border-border bg-muted/30 p-3"
                        >
                          <div className="mb-2 flex items-center justify-between">
                            <p className="font-medium text-card-foreground">
                              {review.userName ?? `Người dùng ${index + 1}`}
                            </p>
                            <div className="flex items-center gap-1 text-sm font-semibold text-amber-500">
                              <Star className="h-4 w-4 fill-current" />
                              <span>{(review.rating ?? 0).toFixed(1)}</span>
                            </div>
                          </div>
                          <p className="text-sm text-muted-foreground">
                            {review.comment ?? "Không có nội dung."}
                          </p>
                          <p className="mt-3 text-xs text-muted-foreground">
                            {review.createdAt
                              ? new Intl.DateTimeFormat("vi-VN", {
                                dateStyle: "short",
                                timeStyle: "short",
                              }).format(new Date(review.createdAt))
                              : "Thời gian không xác định"}
                          </p>
                        </div>
                      ))
                    ) : (
                      !reviewsLoading && (
                        <div className="py-8 text-center text-sm text-muted-foreground">
                          Không có đánh giá nào phù hợp.
                        </div>
                      )
                    )}
                  </div>

                  {visibleReviewCount < filteredReviews.length && (
                    <div className="mt-4 text-center">
                      <Button
                        variant="ghost"
                        onClick={() => setVisibleReviewCount((prev) => prev + REVIEWS_PER_PAGE)}
                      >
                        Xem thêm{" "}
                        {Math.min(REVIEWS_PER_PAGE, filteredReviews.length - visibleReviewCount)} đánh giá
                      </Button>
                    </div>
                  )}
                </div>
              </aside>
            </>
          ) : (
            <div className="h-fit rounded-xl border border-border bg-card shadow-sm p-8 text-center text-muted-foreground">
              Chọn một món ăn để xem chi tiết và đánh giá.
            </div>
          )}
        </div>
      </div>

      {filteredDishes.length === 0 && (
        <div className="py-12 text-center">
          <p className="text-muted-foreground">Không tìm thấy món ăn phù hợp</p>
        </div>
      )}
    </div>
  )
}
