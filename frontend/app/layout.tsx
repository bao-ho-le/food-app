import type React from "react"
import type { Metadata } from "next"
import { Inter } from "next/font/google"
import "./globals.css"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "@/components/ui/toaster"

const inter = Inter({
  subsets: ["latin", "vietnamese"],
  variable: "--font-sans",
})

export const metadata: Metadata = {
  title: "Food Ordering - Đặt đồ ăn trực tuyến",
  description: "Hệ thống đặt đồ ăn trực tuyến với gợi ý món ăn cá nhân hóa",
  generator: 'v0.app'
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="vi" className={inter.variable}>
      <body className="bg-background font-sans antialiased text-foreground">
        <ThemeProvider>
          {children}
        </ThemeProvider>
        <Toaster />
      </body>
    </html>
  )
}