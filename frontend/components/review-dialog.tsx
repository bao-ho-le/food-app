"use client"

import { useState, useEffect } from "react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Star } from "lucide-react"
import { cn } from "@/lib/utils"
import type { Dish } from "@/types"

interface ReviewDialogProps {
  dish: Dish | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (rating: number, comment: string) => void
}

export function ReviewDialog({ dish, open, onOpenChange, onSubmit }: ReviewDialogProps) {
  const [rating, setRating] = useState(0)
  const [hoverRating, setHoverRating] = useState(0)
  const [comment, setComment] = useState("")

  useEffect(() => {
    if (open) {
      setRating(0)
      setComment("")
      setHoverRating(0)
    }
  }, [open])

  const handleSubmit = () => {
    if (dish) {
      onSubmit(rating, comment)
    }
  }

  if (!dish) return null

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Đánh giá món ăn</DialogTitle>
          <DialogDescription>
            Bạn đang đánh giá cho món <span className="font-semibold text-primary">{dish.name}</span>.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="flex items-center justify-center gap-2">
            {[1, 2, 3, 4, 5].map((star) => (
              <Star
                key={star}
                className={cn(
                  "h-8 w-8 cursor-pointer transition-colors",
                  (hoverRating || rating) >= star
                    ? "text-yellow-400 fill-yellow-400"
                    : "text-gray-300",
                )}
                onClick={() => setRating(star)}
                onMouseEnter={() => setHoverRating(star)}
                onMouseLeave={() => setHoverRating(0)}
              />
            ))}
          </div>
          <Textarea
            placeholder="Chia sẻ cảm nhận của bạn về món ăn này..."
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            rows={4}
          />
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Hủy
          </Button>
          <Button
            type="submit"
            onClick={handleSubmit}
            disabled={rating === 0 || comment.trim() === ""}
          >
            Gửi đánh giá
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}