"use client"

import { useState, useEffect } from "react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"
import { cn } from "@/lib/utils"
import type { Address } from "@/types"

interface AddressDialogProps {
  address?: Address | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onSave: (address: Omit<Address, "id" | "userId"> & { id?: string }) => void
}

export function AddressDialog({ address, open, onOpenChange, onSave }: AddressDialogProps) {
  const [addressValue, setAddressValue] = useState("")
  const [isDefault, setIsDefault] = useState(false)

  useEffect(() => {
    if (open && address) {
      setAddressValue(address.address)
      setIsDefault(address.isDefault)
    } else {
      setAddressValue("")
      setIsDefault(false)
    }
  }, [open, address])

  const handleSave = () => {
    onSave({
      id: address?.id,
      address: addressValue,
      isDefault: isDefault,
    })
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{address ? "Chỉnh sửa địa chỉ" : "Thêm địa chỉ mới"}</DialogTitle>
          <DialogDescription>
            {address
              ? "Cập nhật thông tin địa chỉ của bạn."
              : "Thêm một địa chỉ mới vào sổ địa chỉ của bạn."}
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="address">
              Địa chỉ
            </Label>
            <Input
              id="address"
              value={addressValue}
              onChange={(e) => setAddressValue(e.target.value)}
              placeholder="Nhập địa chỉ chi tiết"
            />
          </div>
          <div className="flex items-center space-x-2">
            <Checkbox
              id="isDefault"
              checked={isDefault}
              onCheckedChange={() => setIsDefault(!isDefault)}
              disabled={address?.isDefault}
            />
            <Label
              htmlFor="isDefault"
              className={cn(address?.isDefault && "cursor-not-allowed opacity-50")}
            >
              Đặt làm địa chỉ mặc định
            </Label>
          </div>
        </div>
        <DialogFooter>
          <DialogClose asChild>
            <Button type="button" variant="outline">
              Hủy
            </Button>
          </DialogClose>
          <Button type="submit" onClick={handleSave} disabled={!addressValue.trim()}>
            Lưu địa chỉ
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}