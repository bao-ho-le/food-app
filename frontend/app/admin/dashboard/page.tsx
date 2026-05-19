"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  DollarSign,
  Users,
  ShoppingBag,
  ThumbsUp,
  ArrowUp,
  ArrowDown,
  MoreVertical,
} from "lucide-react"
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area,
} from "recharts"
import Image from "next/image"
import { Dish, Restaurant, TopSelling } from "@/types"
import { mockDishes, mockRestaurants, mockTopSellingData } from "@/lib/mock-data"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Separator } from "@/components/ui/separator"

const summaryData = [
  { name: "Sunday", Earning: 2000 },
  { name: "Monday", Earning: 3000 },
  { name: "Tuesday", Earning: 2500 },
  { name: "Wednesday", Earning: 4500 },
  { name: "Thursday", Earning: 5900 },
  { name: "Friday", Earning: 3800 },
  { name: "Saturday", Earning: 4200 },
]


const topSellingItems = mockTopSellingData
  .map((item: TopSelling) => {
    const dish = mockDishes.find((d) => d.id === item.dishId)
    if (!dish) {
      return null
    }
    const restaurant = mockRestaurants.find((r) => r.id === dish.restaurantId)
    return {
      ...dish,
      restaurantName: restaurant?.name || "N/A",
      quantitySold: item.quantitySold,
    }
  })
  .filter((item): item is Dish & { restaurantName: string; quantitySold: number } => item !== null)

export default function DashboardPage() {
  return (
    <div className="flex-1 space-y-10 bg-background p-4 md:px-18 md:py-10">
      <div className="flex items-center justify-between">
        <h1 className="text-4xl font-bold">Dashboard</h1>
        <div className="flex items-center space-x-2">
          <Select defaultValue="week">
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder="Filter" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="day">Day</SelectItem>
              <SelectItem value="week">Week</SelectItem>
              <SelectItem value="month">Month</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid gap-10 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Total Earning"
          value="$10,236"
          change="3.01%"
          isPositive
          icon={<DollarSign className="h-6 w-6 text-white" />}
          iconBgColor="bg-indigo-500"
          chartColor="indigo"
        />
        <StatCard
          title="Daily Customers"
          value="36,531"
          change="3.01%"
          isPositive={false}
          icon={<Users className="h-6 w-6 text-white" />}
          iconBgColor="bg-yellow-500"
          chartColor="yellow"
        />
        <StatCard
          title="New Orders"
          value="52,416"
          change="3.01%"
          isPositive
          icon={<ShoppingBag className="h-6 w-6 text-white" />}
          iconBgColor="bg-blue-500"
          chartColor="blue"
        />
        <StatCard
          title="New Feedback"
          value="13,924"
          change="52.1%"
          isPositive
          icon={<ThumbsUp className="h-6 w-6 text-white" />}
          iconBgColor="bg-cyan-500"
          chartColor="cyan"
        />
      </div>

      <div className="grid grid-cols-1 gap-13 lg:grid-cols-3 py-4">
        {/* Summary Chart */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Summary</CardTitle>
              <Select defaultValue="last-week">
                <SelectTrigger className="w-[140px]">
                  <SelectValue placeholder="Filter" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="last-week">Last Week</SelectItem>
                  <SelectItem value="last-month">Last Month</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardHeader>
          <CardContent className="pl-2">
            <ResponsiveContainer width="100%" height={350}>
              <AreaChart data={summaryData}>
                <defs>
                  <linearGradient id="colorEarning" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#8884d8" stopOpacity={0.8} />
                    <stop offset="95%" stopColor="#8884d8" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tickLine={false} axisLine={false} />
                <YAxis tickLine={false} axisLine={false} tickFormatter={(value) => `${value / 1000}k`} />
                <Tooltip
                  contentStyle={{
                    borderRadius: "8px",
                    boxShadow: "0 2px 10px rgba(0,0,0,0.1)",
                    border: "none",
                  }}
                />
                <Area type="monotone" dataKey="Earning" stroke="#8884d8" fillOpacity={1} fill="url(#colorEarning)" />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Top Selling Items */}
        <Card className="rounded-3xl">
          <CardHeader className="flex flex-row items-center justify-between px-7 pt-2 pb-0">
            <CardTitle className="text-xl font-bold">Best Sellers</CardTitle>
            <Button variant="link" className="text-base">
              View all
            </Button>
          </CardHeader>
          <CardContent className="px-3">
            <ScrollArea className="h-[350px]">
              <div className="flex flex-col">
                {topSellingItems.map((item, index) => (
                  <div key={item.id}>
                    <div className="flex items-center justify-between pb-5 px-7">
                      <div className="flex items-center space-x-4">
                        <Image
                          src={item.image!}
                          alt={item.name!}
                          width={56}
                          height={56}
                          className="rounded-lg object-cover"
                        />
                        <div className="max-w-[150px]">
                          <p className="truncate font-semibold text-base">{item.name}</p>
                          <p className="truncate text-sm text-muted-foreground">{item.restaurantName}</p>
                        </div>
                      </div>
                      <div className="w-16 text-right">
                        <p className="font-bold text-lg">{item.quantitySold}</p>
                      </div>
                    </div>

                    {/* --- đường kẻ giữa các item --- */}
                    {index < topSellingItems.length - 1 && (
                        <div className="border-b border-gray-200 dark:border-gray-700 mx-7 mb-5"></div>
                    )}
                  </div>
                  
                ))}
              </div>
            </ScrollArea>
          </CardContent>

        </Card>
      </div>
    </div>
  )
}

interface StatCardProps {
  title: string
  value: string
  change: string
  isPositive: boolean
  icon: React.ReactNode
  iconBgColor: string
  chartColor: "indigo" | "yellow" | "blue" | "cyan"
}

const chartData = {
  indigo: [
    { x: 0, y: 12 }, { x: 1, y: 20 }, { x: 2, y: 16 }, { x: 3, y: 25 },
    { x: 4, y: 18 }, { x: 5, y: 28 }, { x: 6, y: 15 }, { x: 7, y: 27 },
  ],
  yellow: [
    { x: 0, y: 22 }, { x: 1, y: 14 }, { x: 2, y: 18 }, { x: 3, y: 12 },
    { x: 4, y: 16 }, { x: 5, y: 10 }, { x: 6, y: 15 }, { x: 7, y: 11 },
  ],
  blue: [
    { x: 0, y: 15 }, { x: 1, y: 25 }, { x: 2, y: 19 }, { x: 3, y: 28 },
    { x: 4, y: 22 }, { x: 5, y: 30 }, { x: 6, y: 20 }, { x: 7, y: 27 },
  ],
  cyan: [
    { x: 0, y: 20 }, { x: 1, y: 12 }, { x: 2, y: 18 }, { x: 3, y: 10 },
    { x: 4, y: 16 }, { x: 5, y: 11 }, { x: 6, y: 19 }, { x: 7, y: 13 },
  ],
}




const chartColors = {
  indigo: "#6366F1",
  yellow: "#FBBF24",
  blue: "#3B82F6",
  cyan: "#22D3EE",
}

function StatCard({ title, value, change, isPositive, icon, iconBgColor, chartColor }: StatCardProps) {
  const ChangeIcon = isPositive ? ArrowUp : ArrowDown
  const changeColor = isPositive ? "text-green-500" : "text-red-500"

  return (
    <Card className="rounded-3xl shadow-sm">
      <CardContent className="px-8">
        <div className="flex items-center justify-between">
          <div className={`flex h-12 w-12 items-center justify-center rounded-full ${iconBgColor}`}>
            {icon}
          </div>
          <div className="h-18 w-20">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData[chartColor]}>
                <Line
                  type="monotone"
                  dataKey="y"
                  stroke={chartColors[chartColor]}
                  strokeWidth={3}
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
        <div className="mt-3">
          <div className="flex items-baseline gap-2">
            <p className="text-3xl font-bold">{value}</p>
            <div className={`flex items-center text-base font-semibold ${changeColor}`}>
              <ChangeIcon className="mr-0.5 h-4 w-4" />
              {change}
            </div>
          </div>
          <p className="mt-2 text-1xl text-muted-foreground">{title}</p>
        </div>
      </CardContent>
    </Card>
  )
}
