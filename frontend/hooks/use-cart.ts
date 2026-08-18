"use client"

import { useState, useEffect, useCallback } from "react"
import type { CartItem, Dish } from "@/types"

// Định nghĩa lại cấu trúc Cart đơn giản
export interface Cart {
  items: CartItem[]
}

const CART_KEY = "food_ordering_simple_cart" // Đổi key để tránh xung đột dữ liệu cũ

export function useCart() {
  const [cart, setCart] = useState<Cart>({ items: [] })

  useEffect(() => {
    const savedCartJson = localStorage.getItem(CART_KEY)
    if (savedCartJson) {
      const savedCart = JSON.parse(savedCartJson) as Cart
      setCart(savedCart)
    }
  }, [])

  const saveCart = (newCart: Cart) => {
    setCart(newCart)
    localStorage.setItem(CART_KEY, JSON.stringify(newCart))
  }

  const replaceCartItems = useCallback((items: CartItem[]) => {
    const newCart = { items }
    setCart(newCart)
    localStorage.setItem(CART_KEY, JSON.stringify(newCart))
  }, [])

  const addToCart = (dish: Dish, quantity = 1, note?: string) => {
    const newItems = [...cart.items]
    const existingItem = newItems.find(item => item.dish.id === dish.id)

    if (existingItem) {
      existingItem.quantity += quantity
    } else {
      newItems.push({ dish, quantity, note })
    }
    saveCart({ items: newItems })
  }

  const updateQuantity = (dishId: string, quantity: number) => {
    let newItems = [...cart.items]
    const itemIndex = newItems.findIndex(item => item.dish.id === dishId)

    if (itemIndex !== -1) {
      if (quantity <= 0) {
        newItems = newItems.filter(item => item.dish.id !== dishId) // Xóa món ăn
      } else {
        newItems[itemIndex].quantity = quantity
      }
    }
    saveCart({ items: newItems })
  }

  const removeFromCart = (dishId: string) => {
    updateQuantity(dishId, 0) // Tái sử dụng logic của updateQuantity
  }

  const clearCart = () => {
    saveCart({ items: [] })
  }

  const getTotalAmount = () => {
    return cart.items
      .filter(item => item.dish.isAvailable)
      .reduce((total, item) => total + item.dish.price * item.quantity, 0)
  }

  const getTotalItems = () => {
    return cart.items.reduce((total, item) => total + item.quantity, 0)
  }

  return {
    cart,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    getTotalAmount,
    getTotalItems,
    replaceCartItems,
  }
}
