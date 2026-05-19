"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { useCart } from "@/hooks/use-cart";
import { authService } from "@/lib/auth";
import { mockRestaurants } from "@/lib/mock-data";
import type { Order } from "@/types";
import { MapPin, CreditCard, Wallet } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

export const dynamic = "force-dynamic";

export default function CheckoutPage() {
  const router = useRouter();
  const { cart, getTotalAmount, clearCart } = useCart();
  const { toast } = useToast();
  const [user, setUser] = useState<any>(null);
  const [note, setNote] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("cash");
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    setUser(authService.getCurrentUser());
  }, []);

  const restaurant = mockRestaurants.find((r) => r.id === cart.restaurantId);
  const deliveryFee = 15000;
  const totalAmount = getTotalAmount() + deliveryFee;

  if (cart.items.length === 0) {
    router.push("/user/cart");
    return null;
  }

  const handlePlaceOrder = async () => {
    setIsProcessing(true);

    // Simulate order processing
    await new Promise((resolve) => setTimeout(resolve, 1500));

    const newOrder: Order = {
      id: `order_${Date.now()}`,
      userId: user?.id || "",
      restaurantId: cart.restaurantId,
      items: cart.items.map((item) => ({
        dishId: item.dish.id,
        dishName: item.dish.name,
        dishImage: item.dish.image,
        quantity: item.quantity,
        price: item.dish.price,
        note: item.note,
      })),
      status: "pending",
      totalAmount,
      deliveryAddress: {
        id: "addr_1",
        userId: user?.id || "",
        label: "Home",
        street: "789 Nguyễn Trãi",
        city: "TP.HCM",
        district: "Q.5",
        isDefault: true,
      },
      note,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    // Save order to localStorage
    const orders = JSON.parse(localStorage.getItem("orders") || "[]");
    orders.push(newOrder);
    localStorage.setItem("orders", JSON.stringify(orders));

    clearCart();
    setIsProcessing(false);

    toast({
      title: "Đặt hàng thành công!",
      description: "Đơn hàng của bạn đang được xử lý",
    });

    router.push("/user/orders");
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="mb-8 text-3xl font-bold text-foreground">Thanh toán</h1>

      <div className="grid gap-8 lg:grid-cols-3">
        {/* Checkout Form */}
        <div className="space-y-6 lg:col-span-2">
          {/* Delivery Address */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="h-5 w-5" />
                Địa chỉ giao hàng
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="rounded-lg border border-border bg-muted/50 p-4">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-semibold">Nhà riêng</h3>
                    <p className="mt-1 text-sm text-muted-foreground">
                      789 Nguyễn Trãi, Q.5, TP.HCM
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {user?.phone}
                    </p>
                  </div>
                  <Button variant="outline" size="sm">
                    Thay đổi
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Order Items */}
          <Card>
            <CardHeader>
              <CardTitle>Món ăn từ {restaurant?.name}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {cart.items.map((item) => (
                <div key={item.dish.id} className="flex gap-4">
                  <div className="relative h-16 w-16 shrink-0 overflow-hidden rounded-lg bg-muted">
                    <Image
                      src={item.dish.image || "/placeholder.svg"}
                      alt={item.dish.name}
                      fill
                      className="object-cover"
                    />
                  </div>
                  <div className="flex flex-1 items-center justify-between">
                    <div>
                      <h3 className="font-medium text-card-foreground">
                        {item.dish.name}
                      </h3>
                      <p className="text-sm text-muted-foreground">
                        x{item.quantity}
                      </p>
                    </div>
                    <p className="font-semibold">
                      {(item.dish.price * item.quantity).toLocaleString(
                        "vi-VN",
                      )}
                      đ
                    </p>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>

          {/* Payment Method */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CreditCard className="h-5 w-5" />
                Phương thức thanh toán
              </CardTitle>
            </CardHeader>
            <CardContent>
              <RadioGroup
                value={paymentMethod}
                onValueChange={setPaymentMethod}
              >
                <div className="flex items-center space-x-3 rounded-lg border border-border p-4">
                  <RadioGroupItem value="cash" id="cash" />
                  <Label
                    htmlFor="cash"
                    className="flex flex-1 cursor-pointer items-center gap-3"
                  >
                    <Wallet className="h-5 w-5 text-muted-foreground" />
                    <div>
                      <p className="font-medium">Tiền mặt</p>
                      <p className="text-sm text-muted-foreground">
                        Thanh toán khi nhận hàng
                      </p>
                    </div>
                  </Label>
                </div>
                <div className="flex items-center space-x-3 rounded-lg border border-border p-4">
                  <RadioGroupItem value="card" id="card" />
                  <Label
                    htmlFor="card"
                    className="flex flex-1 cursor-pointer items-center gap-3"
                  >
                    <CreditCard className="h-5 w-5 text-muted-foreground" />
                    <div>
                      <p className="font-medium">Thẻ tín dụng/ghi nợ</p>
                      <p className="text-sm text-muted-foreground">
                        Thanh toán trực tuyến
                      </p>
                    </div>
                  </Label>
                </div>
              </RadioGroup>
            </CardContent>
          </Card>

          {/* Note */}
          <Card>
            <CardHeader>
              <CardTitle>Ghi chú</CardTitle>
            </CardHeader>
            <CardContent>
              <Textarea
                placeholder="Ghi chú cho người bán (tùy chọn)"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                rows={3}
              />
            </CardContent>
          </Card>
        </div>

        {/* Order Summary */}
        <div>
          <Card className="sticky top-20">
            <CardHeader>
              <CardTitle>Tổng đơn hàng</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Tạm tính</span>
                  <span className="font-medium">
                    {getTotalAmount().toLocaleString("vi-VN")}đ
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Phí giao hàng</span>
                  <span className="font-medium">
                    {deliveryFee.toLocaleString("vi-VN")}đ
                  </span>
                </div>
              </div>
              <div className="border-t border-border pt-4">
                <div className="flex justify-between">
                  <span className="font-semibold">Tổng cộng</span>
                  <span className="text-xl font-bold text-primary">
                    {totalAmount.toLocaleString("vi-VN")}đ
                  </span>
                </div>
              </div>
              <Button
                className="w-full"
                onClick={handlePlaceOrder}
                disabled={isProcessing}
              >
                {isProcessing ? "Đang xử lý..." : "Đặt hàng"}
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
