import type { Restaurant, Dish, Order, Review, DashboardStats, RevenueData, Category, Tag, User, Address, Bias } from "@/types"
import type { TopSelling } from "@/types"
const USER_ID = "user_1"


export const mockRestaurants: Restaurant[] = [
  {
    id: "rest_1",
    name: "Phở Hà Nội",
    address: "123 Nguyễn Huệ, Q.1, TP.HCM",
    phone: "0281234567",
    isActive: true,
  },
  {
    id: "rest_2",
    name: "Cơm Tấm Sài Gòn",
    address: "456 Lê Lợi, Q.1, TP.HCM",
    phone: "0281234568",
    isActive: true,
  },
  {
    id: "rest_3",
    name: "Bánh Mì 362",
    address: "362 Hai Bà Trưng, Q.3, TP.HCM",
    phone: "0281234569",
    isActive: true,
  },
  {
    id: "rest_4",
    name: "Bún Bò Huế Đông Ba",
    address: "110B Nguyễn Du, P. Bến Thành, Q.1, TP.HCM",
    phone: "0282345678",
    isActive: true,
  },
  {
    id: "rest_5",
    name: "Bánh Xèo Ăn Là Ghiền",
    address: "77 Hoàng Văn Thụ, P. 15, Q. Phú Nhuận, TP.HCM",
    phone: "0909876543",
    isActive: true,
  },
  {
    id: "rest_6",
    name: "Hủ Tiếu Nam Vang Nhân Quán",
    address: "A67 Nguyễn Trãi, P. Nguyễn Cư Trinh, Q.1, TP.HCM",
    phone: "0283456789",
    isActive: false,
  },
  {
    id: "rest_7",
    name: "Lẩu Nấm Ashima",
    address: "35A Nguyễn Đình Chiểu, P. Đa Kao, Q.1, TP.HCM",
    phone: "0284567890",
    isActive: true,
  },
  {
    id: "rest_8",
    name: "Hải Sản Tươi Sống Rạn Biển",
    address: "25 Kỳ Đồng, P. 9, Q.3, TP.HCM",
    phone: "0918765432",
    isActive: true,
  },
  {
    id: "rest_9",
    name: "Nhà Hàng Chay An Lạc",
    address: "15 Trần Hưng Đạo, P. Nguyễn Thái Bình, Q.1, TP.HCM",
    phone: "0285678901",
    isActive: true,
  },
  {
    id: "rest_10",
    name: "Gà Nướng Ò Ó O",
    address: "220/58 Lê Văn Sỹ, P. 14, Q.3, TP.HCM",
    phone: "0923456789",
    isActive: false,
  },
  // === 12 NHÀ HÀNG MỚI THÊM VÀO ===
  {
    id: "rest_11",
    name: "Sushi Hokkaido Sachi",
    address: "180 Pasteur, P. Bến Nghé, Q.1, TP.HCM",
    phone: "0286789012",
    isActive: true,
  },
  {
    id: "rest_12",
    name: "Gogi House - Nướng Hàn Quốc",
    address: "189 Nguyễn Thị Thập, P. Tân Phú, Q.7, TP.HCM",
    phone: "0287890123",
    isActive: true,
  },
  {
    id: "rest_13",
    name: "Pizza 4P's",
    address: "8/15 Lê Thánh Tôn, P. Bến Nghé, Q.1, TP.HCM",
    phone: "0288901234",
    isActive: true,
  },
  {
    id: "rest_14",
    name: "TukTuk Thai Bistro",
    address: "29 Ngô Thời Nhiệm, P. Võ Thị Sáu, Q.3, TP.HCM",
    phone: "0934567890",
    isActive: true,
  },
  {
    id: "rest_15",
    name: "San Fu Lou - Dimsum & Món Hoa",
    address: "76A Lê Lai, P. Bến Thành, Q.1, TP.HCM",
    phone: "0289012345",
    isActive: false,
  },
  {
    id: "rest_16",
    name: "The Coffee House Signature",
    address: "19B Phạm Ngọc Thạch, P. Võ Thị Sáu, Q.3, TP.HCM",
    phone: "0845678901",
    isActive: true,
  },
  {
    id: "rest_17",
    name: "El Gaucho Argentinian Steakhouse",
    address: "74/1 Hai Bà Trưng, P. Bến Nghé, Q.1, TP.HCM",
    phone: "0945678901",
    isActive: true,
  },
  {
    id: "rest_18",
    name: "Mì Quảng Phố Thị",
    address: "11 Phan Đình Phùng, P.2, Q.Tân Bình, TP.HCM",
    phone: "0956789012",
    isActive: true,
  },
  {
    id: "rest_19",
    name: "Baba's Kitchen - Món Ấn Độ",
    address: "164 Bùi Viện, P. Phạm Ngũ Lão, Q.1, TP.HCM",
    phone: "0285678912",
    isActive: true,
  },
  {
    id: "rest_20",
    name: "Gong Cha - Trà Sữa",
    address: "561 Sư Vạn Hạnh, P.13, Q.10, TP.HCM",
    phone: "0867890123",
    isActive: true,
  },
  {
    id: "rest_21",
    name: "McDonald's Đa Kao",
    address: "2-6 Bis Điện Biên Phủ, P. Đa Kao, Q.1, TP.HCM",
    phone: "0287891234",
    isActive: true,
  },
  {
    id: "rest_22",
    name: "Poke Saigon - Salad & Cơm Trộn",
    address: "42 Lý Tự Trọng, P. Bến Nghé, Q.1, TP.HCM",
    phone: "0978901234",
    isActive: false,
  },
];

export const mockDishes: Dish[] = [
  {
    id: "dish_1",
    restaurantId: "rest_1",
    name: "Phở Bò Tái",
    description: "Beef pho with rare beef slices",
    price: 55000,
    image: "/vietnamese-beef-pho-bowl.jpg",
    category: "Noodles",
    rating: 4.6,
    totalReviews: 89,
    isAvailable: false,
    spicyLevel: "none",
    tags: ["Popular", "Bò", "Món nước", "Đậm đà"],
  },
  {
    id: "dish_2",
    restaurantId: "rest_1",
    name: "Phở Gà",
    description: "Chicken pho with tender chicken",
    price: 50000,
    image: "/vietnamese-chicken-pho.jpg",
    category: "Noodles",
    rating: 4.4,
    totalReviews: 67,
    isAvailable: false,
    spicyLevel: "none",
    tags: ["Gà", "Món nước", "Thanh nhẹ"],
  },
  {
    id: "dish_3",
    restaurantId: "rest_2",
    name: "Cơm Tấm Sườn Bì Chả",
    description: "Broken rice with grilled pork, shredded pork skin, and egg meatloaf",
    price: 45000,
    image: "/vietnamese-broken-rice-with-grilled-pork.jpg",
    category: "Rice",
    rating: 4.7,
    totalReviews: 123,
    isAvailable: false,
    spicyLevel: "mild",
    tags: ["Popular", "Heo", "Món Việt", "Nướng"],
  },
  {
    id: "dish_4",
    restaurantId: "rest_2",
    name: "Cơm Tấm Sườn Nướng",
    description: "Broken rice with grilled pork chop",
    price: 40000,
    image: "/grilled-pork-chop-with-rice.jpg",
    category: "Rice",
    rating: 4.5,
    totalReviews: 98,
    isAvailable: false,
    spicyLevel: "none",
    tags: ["Heo", "Món Việt", "Nướng"],
  },
  {
    id: "dish_5",
    restaurantId: "rest_3",
    name: "Bánh Mì Thịt Nguội",
    description: "Vietnamese baguette with cold cuts",
    price: 25000,
    image: "/vietnamese-banh-mi-sandwich.jpg",
    category: "Sandwiches",
    rating: 4.8,
    totalReviews: 234,
    isAvailable: true,
    spicyLevel: "mild",
    tags: ["Popular", "Bánh mì", "Nhanh"],
  },
  {
    id: "dish_6",
    restaurantId: "rest_3",
    name: "Bánh Mì Xíu Mại",
    description: "Vietnamese baguette with meatballs",
    price: 30000,
    image: "/banh-mi-with-meatballs.jpg",
    category: "Sandwiches",
    rating: 4.6,
    totalReviews: 156,
    isAvailable: true,
    spicyLevel: "medium",
    tags: ["Xíu mại", "Bánh mì", "Nhanh"],
  },
  {
    id: "dish_wagyu_pho",
    restaurantId: "rest_1",
    name: "Phở Bò Wagyu",
    description:
      "Sợi phở mềm thơm kết hợp với thịt bò wagyu thái mỏng, nước dùng hầm 18 giờ cùng gừng nướng và quế hồi.",
    price: 95000,
    image: "/vietnamese-beef-pho-bowl.jpg",
    category: "Noodles",
    rating: 4.9,
    totalReviews: 142,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Signature", "Đậm đà", "Bò"],
  },
  {
    id: "dish_golden_chicken_pho",
    restaurantId: "rest_1",
    name: "Phở Gà Thả Vườn",
    description:
      "Gà thả vườn xé tay, nước dùng thanh ngọt từ xương ống và thảo mộc, thêm gừng rang tạo cảm giác ấm bụng.",
    price: 78000,
    image: "/vietnamese-chicken-pho.jpg",
    category: "Noodles",
    rating: 4.7,
    totalReviews: 95,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Thanh nhẹ", "Gà", "Buổi sáng"],
  },
  {
    id: "dish_crispy_short_rib",
    restaurantId: "rest_2",
    name: "Cơm Tấm Sườn Non Giòn",
    description:
      "Sườn non ướp mật ong áp chảo giòn cạnh, dưa chua làm thủ công và mỡ hành béo ngậy.",
    price: 63000,
    image: "/vietnamese-broken-rice-with-grilled-pork.jpg",
    category: "Rice",
    rating: 4.8,
    totalReviews: 210,
    isAvailable: true,
    spicyLevel: "mild",
    tags: ["Popular", "Heo", "Nướng"],
  },
  {
    id: "dish_salted_egg_rice",
    restaurantId: "rest_2",
    name: "Cơm Tấm Trứng Muối Sốt Bơ",
    description:
      "Thịt ba chỉ nướng sốt trứng muối béo nhưng không ngậy, ăn kèm cơm tấm hạt tơi và tóp mỡ cháy tỏi.",
    price: 69000,
    image: "/grilled-pork-chop-with-rice.jpg",
    category: "Rice",
    rating: 4.6,
    totalReviews: 132,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Sáng tạo", "Trứng muối", "Đậm đà"],
  },
  {
    id: "dish_sweety_pork_rice",
    restaurantId: "rest_2",
    name: "Cơm Tấm Sườn Rim Dừa",
    description:
      "Sườn rim nước dừa Bến Tre, thơm mùi lá chanh kèm đồ chua giấm táo cân bằng vị ngọt.",
    price: 60000,
    image: "/vietnamese-broken-rice-with-grilled-pork.jpg",
    category: "Rice",
    rating: 4.5,
    totalReviews: 88,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Ngọt dịu", "Heo", "Gia đình"],
  },
  {
    id: "dish_banh_mi_pate_foie",
    restaurantId: "rest_3",
    name: "Bánh Mì Pate Gan Ngỗng",
    description:
      "Ổ bánh mì vỏ giòn nhân pate gan ngỗng béo ngậy, dưa leo ngâm giấm và rau thơm cắt tay.",
    price: 55000,
    image: "/vietnamese-banh-mi-sandwich.jpg",
    category: "Sandwiches",
    rating: 4.9,
    totalReviews: 167,
    isAvailable: true,
    spicyLevel: "mild",
    tags: ["Signature", "Bánh mì", "Ăn sáng"],
  },
  {
    id: "dish_banh_mi_grilled_chicken",
    restaurantId: "rest_3",
    name: "Bánh Mì Gà Nướng Lá Chanh",
    description:
      "Gà nướng lá chanh thơm ngát, sốt mayonnaise tỏi và đồ chua cắt sợi tạo nên chiếc bánh mì cân bằng vị.",
    price: 42000,
    image: "/vietnamese-banh-mi-sandwich.jpg",
    category: "Sandwiches",
    rating: 4.7,
    totalReviews: 143,
    isAvailable: true,
    spicyLevel: "mild",
    tags: ["Gà", "Đường phố", "Nhanh"],
  },
  {
    id: "dish_banh_mi_caramel_pork",
    restaurantId: "rest_3",
    name: "Bánh Mì Thịt Kho Trứng",
    description:
      "Thịt ba chỉ kho kẹo, trứng vịt thấm nước dừa ăn cùng pate và đồ chua trong ổ bánh mì giòn.",
    price: 45000,
    image: "/banh-mi-with-meatballs.jpg",
    category: "Sandwiches",
    rating: 4.5,
    totalReviews: 98,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Heo", "Comfort", "Gia truyền"],
  },
  {
    id: "dish_spicy_pho_sate",
    restaurantId: "rest_1",
    name: "Phở Bò Sa Tế Đà Nẵng",
    description:
      "Nước dùng đậm vị sa tế, gân bò hầm mềm, rau thơm và hành phi giòn rụm kích thích vị giác.",
    price: 82000,
    image: "/vietnamese-beef-pho-bowl.jpg",
    category: "Noodles",
    rating: 4.8,
    totalReviews: 176,
    isAvailable: true,
    spicyLevel: "hot",
    tags: ["Cay", "Bò", "Đặc biệt"],
  },
  {
    id: "dish_crab_pho",
    restaurantId: "rest_1",
    name: "Phở Riêu Cua Đồng",
    description:
      "Nước phở trong thanh kết hợp riêu cua đồng thơm lừng, thêm cà chua chưng và hành thì là.",
    price: 76000,
    image: "/vietnamese-chicken-pho.jpg",
    category: "Noodles",
    rating: 4.5,
    totalReviews: 74,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Thanh", "Cua", "Đặc sắc"],
  },
  {
    id: "dish_pandan_rice",
    restaurantId: "rest_2",
    name: "Cơm Tấm Lá Dứa Hải Sản",
    description:
      "Cơm tấm hấp lá dứa, topping tôm sú sốt bơ tỏi và chả cua dùng kèm sốt me cay nhẹ.",
    price: 78000,
    image: "/grilled-pork-chop-with-rice.jpg",
    category: "Rice",
    rating: 4.7,
    totalReviews: 121,
    isAvailable: true,
    spicyLevel: "medium",
    tags: ["Hải sản", "Thơm lừng", "Đặc biệt"],
  },
  {
    id: "dish_veggie_banh_mi",
    restaurantId: "rest_3",
    name: "Bánh Mì Nấm Đậu Phụ",
    description:
      "Nhân nấm đùi gà xào xì dầu, đậu phụ chiên giòn, sốt tương đen và rau củ ngâm hài hòa hương vị.",
    price: 39000,
    image: "/banh-mi-with-meatballs.jpg",
    category: "Sandwiches",
    rating: 4.4,
    totalReviews: 67,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Chay", "Healthy", "Bánh mì"],
  },
  {
    id: "dish_truffle_pho",
    restaurantId: "rest_1",
    name: "Phở Bò Sốt Nấm Truffle",
    description:
      "Thịt bắp bò hầm mềm chan sốt nấm truffle Ý, hành tây nướng caramel và tương đậu phộng.",
    price: 115000,
    image: "/vietnamese-beef-pho-bowl.jpg",
    category: "Noodles",
    rating: 4.9,
    totalReviews: 189,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Cao cấp", "Bò", "Truffle"],
  },
  {
    id: "dish_oxtail_pho",
    restaurantId: "rest_1",
    name: "Phở Đuôi Bò Hầm Than",
    description:
      "Đuôi bò hầm trên than hồng 6 giờ, nước phở đậm hương quế hồi, hành nướng và gừng già tạo vị ngọt.",
    price: 88000,
    image: "/vietnamese-beef-pho-bowl.jpg",
    category: "Noodles",
    rating: 4.7,
    totalReviews: 132,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Đặc biệt", "Bò", "Đậm đà"],
  },
  {
    id: "dish_scallop_rice",
    restaurantId: "rest_2",
    name: "Cơm Tấm Sò Điệp Nướng",
    description:
      "Sò điệp nướng bơ tỏi đặt trên cơm tấm hạt tơi, ăn cùng mỡ hành, tóp mỡ và nước mắm gừng.",
    price: 76000,
    image: "/vietnamese-broken-rice-with-grilled-pork.jpg",
    category: "Rice",
    rating: 4.6,
    totalReviews: 118,
    isAvailable: true,
    spicyLevel: "mild",
    tags: ["Hải sản", "Bơ tỏi", "Đặc sắc"],
  },
  {
    id: "dish_herbal_chicken_noodles",
    restaurantId: "rest_1",
    name: "Phở Gà Thảo Mộc",
    description:
      "Nước dùng gà hầm táo đỏ và kỳ tử, sợi phở dai cùng thịt gà xé thơm, thêm lá tía tô ấm bụng.",
    price: 36000,
    image: "/vietnamese-chicken-pho.jpg",
    category: "Noodles",
    rating: 4.3,
    totalReviews: 54,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Thanh", "Gà", "Buổi sáng"],
  },
  {
    id: "dish_mushroom_noodles",
    restaurantId: "rest_1",
    name: "Phở Nấm Rừng",
    description:
      "Phở chay với nước dùng ngọt từ nấm hương và củ cải, topping nấm đùi gà xào mè rang.",
    price: 34000,
    image: "/vietnamese-chicken-pho.jpg",
    category: "Noodles",
    rating: 4.2,
    totalReviews: 47,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Chay", "Nấm", "Thanh nhẹ"],
  },
  {
    id: "dish_sate_chicken_noodles",
    restaurantId: "rest_1",
    name: "Phở Gà Sa Tế",
    description:
      "Nước dùng gà cay nồng với sa tế Huế, gà xé thấm vị ăn kèm rau răm và giá trụng.",
    price: 38000,
    image: "/vietnamese-chicken-pho.jpg",
    category: "Noodles",
    rating: 4.4,
    totalReviews: 63,
    isAvailable: true,
    spicyLevel: "medium",
    tags: ["Cay", "Gà", "Đậm đà"],
  },
  {
    id: "dish_fried_rice_egg",
    restaurantId: "rest_2",
    name: "Cơm Chiên Trứng Muối",
    description:
      "Cơm chiên với lòng đỏ trứng muối, tôm khô và bắp ngọt, ăn kèm sốt tương ớt và rau củ hạt lựu.",
    price: 32000,
    image: "/grilled-pork-chop-with-rice.jpg",
    category: "Rice",
    rating: 4.3,
    totalReviews: 58,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Nhanh", "Trứng muối", "Tiện lợi"],
  },
  {
    id: "dish_rice_fish_caramel",
    restaurantId: "rest_2",
    name: "Cơm Cá Kho Tiêu",
    description:
      "Cá bống kho tiêu chưng trong nồi đất, nước sốt sánh đậm, ăn kèm cơm tấm nóng và dưa leo giòn.",
    price: 35000,
    image: "/vietnamese-broken-rice-with-grilled-pork.jpg",
    category: "Rice",
    rating: 4.1,
    totalReviews: 48,
    isAvailable: true,
    spicyLevel: "mild",
    tags: ["Gia đình", "Đậm đà", "Tiêu"],
  },
  {
    id: "dish_rice_chicken_crispy",
    restaurantId: "rest_2",
    name: "Cơm Gà Xối Mỡ",
    description:
      "Đùi gà chiên xối mỡ giòn rụm, cơm tấm rang bơ hành và nước mắm chua ngọt cân bằng vị.",
    price: 39000,
    image: "/grilled-pork-chop-with-rice.jpg",
    category: "Rice",
    rating: 4.2,
    totalReviews: 62,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Gà", "Giòn", "Đường phố"],
  },
  {
    id: "dish_rice_toasted_garlic",
    restaurantId: "rest_2",
    name: "Cơm Tấm Tỏi Phi",
    description:
      "Cơm tấm trộn tỏi phi vàng ruộm, tóp mỡ và hành lá, ăn kèm chả trứng và dưa chua.",
    price: 30000,
    image: "/vietnamese-broken-rice-with-grilled-pork.jpg",
    category: "Rice",
    rating: 4,
    totalReviews: 37,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Tiết kiệm", "Nhanh", "Đậm hương"],
  },
  {
    id: "dish_banh_mi_fish_cake",
    restaurantId: "rest_3",
    name: "Bánh Mì Chả Cá Nha Trang",
    description:
      "Chả cá chiên vàng, rau răm, dưa leo và sốt sa tế trong ổ bánh mì giòn rụm vừa nướng.",
    price: 28000,
    image: "/vietnamese-banh-mi-sandwich.jpg",
    category: "Sandwiches",
    rating: 4.4,
    totalReviews: 73,
    isAvailable: true,
    spicyLevel: "mild",
    tags: ["Biển", "Nhanh", "Đặc sản"],
  },
  {
    id: "dish_banh_mi_quail_egg",
    restaurantId: "rest_3",
    name: "Bánh Mì Trứng Cút Sốt Bơ",
    description:
      "Trứng cút chiên lòng đào, sốt bơ hành, pate béo và đồ chua cân bằng vị cho bữa sáng nhanh gọn.",
    price: 32000,
    image: "/vietnamese-banh-mi-sandwich.jpg",
    category: "Sandwiches",
    rating: 4.1,
    totalReviews: 42,
    isAvailable: true,
    spicyLevel: "none",
    tags: ["Buổi sáng", "Trứng", "Nhanh"],
  },
  {
    id: "dish_banh_mi_spicy_beef",
    restaurantId: "rest_3",
    name: "Bánh Mì Bò Lá Lốt",
    description:
      "Bò lá lốt nướng than thơm phức, đồ chua giòn và sốt me cay nhẹ khiến chiếc bánh mì khó cưỡng.",
    price: 35000,
    image: "/banh-mi-with-meatballs.jpg",
    category: "Sandwiches",
    rating: 4.3,
    totalReviews: 59,
    isAvailable: true,
    spicyLevel: "mild",
    tags: ["Bò", "Nướng", "Cay nhẹ"],
  },
]

export type DishFilterOption = {
  label: string
  value: string
}

export const cuisineOptions: DishFilterOption[] = [
  { label: "Tất cả", value: "all" },
  { label: "Món Việt", value: "Món Việt" },
  { label: "Món Hàn", value: "Món Hàn" },
  { label: "Món Nhật", value: "Món Nhật" },
  { label: "Món Trung", value: "Món Trung" },
  { label: "Món Thái", value: "Món Thái" },
  { label: "Fusion", value: "Fusion" },
]

export const ingredientOptions: DishFilterOption[] = [
  { label: "Tất cả", value: "all" },
  { label: "Bò", value: "Bò" },
  { label: "Gà", value: "Gà" },
  { label: "Heo", value: "Heo" },
  { label: "Tôm", value: "Tôm" },
  { label: "Cua", value: "Cua" },
  { label: "Cá", value: "Cá" },
  { label: "Hải sản", value: "Hải sản" },
  { label: "Chay", value: "Chay" },
]

export const methodOptions: DishFilterOption[] = [
  { label: "Tất cả", value: "all" },
  { label: "Chiên", value: "Chiên" },
  { label: "Xào", value: "Xào" },
  { label: "Luộc", value: "Luộc" },
  { label: "Hấp", value: "Hấp" },
  { label: "Nướng", value: "Nướng" },
  { label: "Kho", value: "Kho" },
  { label: "Hầm", value: "Hầm" },
  { label: "Trộn", value: "Trộn" },
]

export const flavorOptions: DishFilterOption[] = [
  { label: "Tất cả", value: "all" },
  { label: "Thanh", value: "Thanh" },
  { label: "Cay", value: "Cay" },
  { label: "Mặn", value: "Mặn" },
  { label: "Ngọt", value: "Ngọt" },
  { label: "Chua", value: "Chua" },
  { label: "Đậm đà", value: "Đậm đà" },
  { label: "Béo", value: "Béo" },
]

export const priceOptions: DishFilterOption[] = [
  { label: "Tất cả", value: "all" },
  { label: "Dưới 40k", value: "under_40" },
  { label: "40k - 60k", value: "40_60" },
  { label: "Trên 60k", value: "over_60" },
]

type DishMeta = {
  cuisine: string
  mainIngredients: string[]
  cookMethods: string[]
  flavorProfiles: string[]
}

export const dishMetadata: Record<string, DishMeta> = {
  dish_1: {
    cuisine: "Món Việt",
    mainIngredients: ["Bò"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Đậm đà", "Thanh"],
  },
  dish_2: {
    cuisine: "Món Việt",
    mainIngredients: ["Gà"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Thanh", "Ngọt"],
  },
  dish_3: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Nướng", "Kho"],
    flavorProfiles: ["Đậm đà", "Mặn"],
  },
  dish_4: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Nướng"],
    flavorProfiles: ["Đậm đà", "Ngọt"],
  },
  dish_5: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Trộn"],
    flavorProfiles: ["Đậm đà", "Béo"],
  },
  dish_6: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Kho"],
    flavorProfiles: ["Đậm đà", "Ngọt"],
  },
  dish_wagyu_pho: {
    cuisine: "Fusion",
    mainIngredients: ["Bò"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Đậm đà", "Béo"],
  },
  dish_golden_chicken_pho: {
    cuisine: "Món Việt",
    mainIngredients: ["Gà"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Thanh", "Ngọt"],
  },
  dish_crispy_short_rib: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Nướng", "Chiên"],
    flavorProfiles: ["Đậm đà", "Mặn"],
  },
  dish_salted_egg_rice: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Nướng", "Chiên"],
    flavorProfiles: ["Béo", "Mặn"],
  },
  dish_sweety_pork_rice: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Kho"],
    flavorProfiles: ["Ngọt", "Đậm đà"],
  },
  dish_banh_mi_pate_foie: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Trộn"],
    flavorProfiles: ["Béo", "Đậm đà"],
  },
  dish_banh_mi_grilled_chicken: {
    cuisine: "Món Việt",
    mainIngredients: ["Gà"],
    cookMethods: ["Nướng"],
    flavorProfiles: ["Đậm đà", "Thanh"],
  },
  dish_banh_mi_caramel_pork: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Kho"],
    flavorProfiles: ["Ngọt", "Đậm đà"],
  },
  dish_spicy_pho_sate: {
    cuisine: "Món Thái",
    mainIngredients: ["Bò"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Cay", "Mặn"],
  },
  dish_crab_pho: {
    cuisine: "Món Việt",
    mainIngredients: ["Cua", "Hải sản"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Thanh", "Ngọt"],
  },
  dish_pandan_rice: {
    cuisine: "Fusion",
    mainIngredients: ["Tôm", "Hải sản"],
    cookMethods: ["Hấp", "Xào"],
    flavorProfiles: ["Thanh", "Đậm đà"],
  },
  dish_veggie_banh_mi: {
    cuisine: "Fusion",
    mainIngredients: ["Chay"],
    cookMethods: ["Xào", "Trộn"],
    flavorProfiles: ["Thanh", "Đậm đà"],
  },
  dish_truffle_pho: {
    cuisine: "Fusion",
    mainIngredients: ["Bò"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Đậm đà", "Béo"],
  },
  dish_oxtail_pho: {
    cuisine: "Món Việt",
    mainIngredients: ["Bò"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Đậm đà", "Béo"],
  },
  dish_scallop_rice: {
    cuisine: "Món Nhật",
    mainIngredients: ["Hải sản"],
    cookMethods: ["Nướng"],
    flavorProfiles: ["Béo", "Mặn"],
  },
  dish_herbal_chicken_noodles: {
    cuisine: "Món Việt",
    mainIngredients: ["Gà"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Thanh", "Ngọt"],
  },
  dish_mushroom_noodles: {
    cuisine: "Món Việt",
    mainIngredients: ["Chay"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Thanh"],
  },
  dish_sate_chicken_noodles: {
    cuisine: "Món Thái",
    mainIngredients: ["Gà"],
    cookMethods: ["Hầm"],
    flavorProfiles: ["Cay", "Đậm đà"],
  },
  dish_fried_rice_egg: {
    cuisine: "Món Việt",
    mainIngredients: ["Tôm"],
    cookMethods: ["Chiên"],
    flavorProfiles: ["Mặn", "Béo"],
  },
  dish_rice_fish_caramel: {
    cuisine: "Món Việt",
    mainIngredients: ["Cá"],
    cookMethods: ["Kho"],
    flavorProfiles: ["Mặn", "Đậm đà"],
  },
  dish_rice_chicken_crispy: {
    cuisine: "Món Việt",
    mainIngredients: ["Gà"],
    cookMethods: ["Chiên", "Nướng"],
    flavorProfiles: ["Đậm đà", "Béo"],
  },
  dish_rice_toasted_garlic: {
    cuisine: "Món Việt",
    mainIngredients: ["Heo"],
    cookMethods: ["Chiên"],
    flavorProfiles: ["Mặn", "Đậm đà"],
  },
  dish_banh_mi_fish_cake: {
    cuisine: "Món Việt",
    mainIngredients: ["Cá", "Hải sản"],
    cookMethods: ["Chiên"],
    flavorProfiles: ["Mặn", "Đậm đà"],
  },
  dish_banh_mi_quail_egg: {
    cuisine: "Món Việt",
    mainIngredients: ["Gà"],
    cookMethods: ["Chiên"],
    flavorProfiles: ["Béo", "Ngọt"],
  },
  dish_banh_mi_spicy_beef: {
    cuisine: "Món Việt",
    mainIngredients: ["Bò"],
    cookMethods: ["Nướng"],
    flavorProfiles: ["Cay", "Đậm đà"],
  },
}



// Thêm danh sách đơn hàng giả
// Dán để thay thế cho mảng mockOrders cũ của bạn

export const mockOrders: Order[] = [
  {
    id: "order_1",
    createdAt: "2024-10-26T10:00:00Z",
    deliveryAddress: "Tòa nhà XYZ, 456 Đường DEF, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh. Lầu 5, Phòng A",
    status: "completed",
    items: [
      {
        dishId: "dish_1", // Phở Bò Tái
        restaurantId: "rest_1",
        quantity: 2,
        price: 55000,
        isRated: true, // Đã đánh giá
      },
      {
        dishId: "dish_2", // Phở Gà
        restaurantId: "rest_1",
        quantity: 1,
        price: 50000,
        isRated: false, // Chưa đánh giá
      },
    ],
    totalAmount: 160000,
  },
  {
    id: "order_2",
    createdAt: "2024-10-25T18:30:00Z",
    deliveryAddress: "123 Đường ABC, Phường 1, Quận 3, TP. Hồ Chí Minh",
    status: "delivering", // Đơn hàng đang giao, giả định chưa đánh giá
    items: [
      {
        dishId: "dish_3", // Cơm Tấm Sườn Bì Chả
        restaurantId: "rest_2",
        quantity: 1,
        price: 45000,
        isRated: false,
      },
      {
        dishId: "dish_5", // Bánh Mì Thịt Nguội
        restaurantId: "rest_3",
        quantity: 2,
        price: 25000,
        isRated: false,
      },
    ],
    totalAmount: 95000,
  },
  {
    id: "order_3",
    createdAt: "2024-10-24T12:15:00Z",
    deliveryAddress: "789 Đường GHI, Phường Tân Phú, Quận 7, TP. Hồ Chí Minh",
    status: "cancelled", // Đơn hàng đã hủy, giả định chưa đánh giá
    items: [
      {
        dishId: "dish_6", // Bánh Mì Xíu Mại
        restaurantId: "rest_3",
        quantity: 3,
        price: 30000,
        isRated: false,
      },
    ],
    totalAmount: 90000,
  },
  // === DỮ LIỆU MỚI THÊM VÀO ===
  {
    id: "order_4",
    createdAt: "2024-10-23T09:05:00Z",
    deliveryAddress: "Tòa nhà XYZ, 456 Đường DEF, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh. Lầu 5, Phòng A",
    status: "cancelled", // Đơn hàng đã hủy, giả định chưa đánh giá
    items: [
      {
        dishId: "dish_crispy_short_rib", // Cơm Tấm Sườn Non Giòn
        restaurantId: "rest_2",
        quantity: 1,
        price: 63000,
        isRated: false,
      },
      {
        dishId: "dish_banh_mi_pate_foie", // Bánh Mì Pate Gan Ngỗng
        restaurantId: "rest_3",
        quantity: 2,
        price: 55000,
        isRated: false,
      },
    ],
    totalAmount: 173000,
  },
  {
    id: "order_5",
    createdAt: "2024-10-22T20:00:00Z",
    deliveryAddress: "123 Đường ABC, Phường 1, Quận 3, TP. Hồ Chí Minh",
    status: "completed",
    items: [
      {
        dishId: "dish_wagyu_pho", // Phở Bò Wagyu
        restaurantId: "rest_1",
        quantity: 2,
        price: 95000,
        isRated: true,
      },
      {
        dishId: "dish_salted_egg_rice", // Cơm Tấm Trứng Muối Sốt Bơ
        restaurantId: "rest_2",
        quantity: 1,
        price: 69000,
        isRated: false, // Chưa đánh giá
      },
      {
        dishId: "dish_banh_mi_grilled_chicken", // Bánh Mì Gà Nướng Lá Chanh
        restaurantId: "rest_3",
        quantity: 3,
        price: 42000,
        isRated: true,
      },
      {
        dishId: "dish_crispy_short_rib", // Cơm Tấm Sườn Non Giòn
        restaurantId: "rest_2",
        quantity: 1,
        price: 63000,
        isRated: false,
      },
      {
        dishId: "dish_banh_mi_pate_foie", // Bánh Mì Pate Gan Ngỗng
        restaurantId: "rest_3",
        quantity: 2,
        price: 55000,
        isRated: true,
      },
      {
        dishId: "dish_3", // Cơm Tấm Sườn Bì Chả
        restaurantId: "rest_2",
        quantity: 1,
        price: 45000,
        isRated: false,
      },
      {
        dishId: "dish_5", // Bánh Mì Thịt Nguội
        restaurantId: "rest_3",
        quantity: 2,
        price: 25000,
        isRated: true,
      },
    ],
    totalAmount: 385000,
  },
  {
    id: "order_6",
    createdAt: "2024-10-21T11:30:00Z",
    deliveryAddress: "789 Đường GHI, Phường Tân Phú, Quận 7, TP. Hồ Chí Minh",
    status: "completed",
    items: [
      {
        dishId: "dish_spicy_pho_sate", // Phở Bò Sa Tế Đà Nẵng
        restaurantId: "rest_1",
        quantity: 4,
        price: 82000,
        isRated: true,
      },
    ],
    totalAmount: 328000,
  },
  {
    id: "order_7",
    createdAt: "2024-10-20T14:00:00Z",
    deliveryAddress: "456 Đường JKL, Phường 10, Quận 5, TP. Hồ Chí Minh",
    status: "delivering", // Đơn hàng đang giao, giả định chưa đánh giá
    items: [
      {
        dishId: "dish_veggie_banh_mi", // Bánh Mì Nấm Đậu Phụ
        restaurantId: "rest_3",
        quantity: 2,
        price: 39000,
        isRated: false,
      },
      {
        dishId: "dish_truffle_pho", // Phở Bò Sốt Nấm Truffle
        restaurantId: "rest_1",
        quantity: 1,
        price: 115000,
        isRated: false,
      },
    ],
    totalAmount: 193000,
  },
]

export const mockReviews: Review[] = [
  {
    id: "review_1",
    userId: "user_1",
    userName: "Nguyễn Văn A",
    dishId: "dish_1",
    orderId: "order_1",
    rating: 5,
    comment: "Phở rất ngon, nước dùng đậm đà!",
    createdAt: "2024-10-10T12:00:00Z",
  },
  {
    id: "review_2",
    userId: "user_2",
    userName: "Lê Thu Trang",
    dishId: "dish_1",
    orderId: "order_2",
    rating: 4,
    comment: "Thịt bò mềm, nước dùng hơi mặn nhưng vẫn ổn.",
    createdAt: "2024-10-11T09:15:00Z",
  },
  {
    id: "review_3",
    userId: "user_3",
    userName: "Phạm Minh Đức",
    dishId: "dish_3",
    orderId: "order_3",
    rating: 5,
    comment: "Cơm tấm ngon tuyệt, sườn nướng thơm và mềm.",
    createdAt: "2024-10-09T18:45:00Z",
  },
  {
    id: "review_4",
    userId: "user_4",
    userName: "Trần Gia Huy",
    dishId: "dish_5",
    orderId: "order_4",
    rating: 5,
    comment: "Bánh mì giòn, nhân đầy đủ và tươi, rất đáng thử.",
    createdAt: "2024-10-08T14:20:00Z",
  },
  {
    id: "review_5",
    userId: "user_5",
    userName: "Vũ Ngọc Hà",
    dishId: "dish_3",
    orderId: "order_5",
    rating: 4,
    comment: "Khẩu phần nhiều, nước mắm pha vừa miệng.",
    createdAt: "2024-10-07T11:10:00Z",
  },
  {
    id: "review_6",
    userId: "user_6",
    userName: "Đinh Tuấn Kiệt",
    dishId: "dish_6",
    orderId: "order_6",
    rating: 3,
    comment: "Bánh mì xíu mại ngon nhưng hơi ít nước sốt.",
    createdAt: "2024-10-05T16:00:00Z",
  },
]

export const mockDashboardStats: DashboardStats = {
  totalRevenue: 125000000,
  totalOrders: 1234,
  totalUsers: 567,
  totalRestaurants: 45,
  revenueChange: 12.5,
  ordersChange: 8.3,
  usersChange: 15.2,
}

export const mockRevenueData: RevenueData[] = [
  { date: "2024-10-01", revenue: 3500000, orders: 45 },
  { date: "2024-10-02", revenue: 4200000, orders: 52 },
  { date: "2024-10-03", revenue: 3800000, orders: 48 },
  { date: "2024-10-04", revenue: 4500000, orders: 58 },
  { date: "2024-10-05", revenue: 5200000, orders: 65 },
  { date: "2024-10-06", revenue: 6100000, orders: 78 },
  { date: "2024-10-07", revenue: 5800000, orders: 72 },
]

const generateDynamicReviews = (dishes: Dish[]): Review[] => {
  if (!dishes.length) return []

  const reviewers = [
    { id: "user_quynh", name: "Lê Mỹ Quỳnh" },
    { id: "user_thinh", name: "Phạm Quốc Thịnh" },
    { id: "user_anhthu", name: "Ngô Ánh Thư" },
    { id: "user_danh", name: "Trần Hữu Danh" },
    { id: "user_tuananh", name: "Vũ Tuấn Anh" },
    { id: "user_minhanh", name: "Bùi Minh Ánh" },
    { id: "user_linh", name: "Đặng Diễm Linh" },
    { id: "user_vy", name: "Phạm Gia Vy" },
  ]

  const comments = [
    "Hương vị cực kỳ ấn tượng, nêm nếm vừa miệng và phần ăn rất đầy đặn.",
    "Giao hàng nhanh, món ăn tới nơi vẫn còn nóng hổi và thơm phức.",
    "Giá hơi cao nhưng chất lượng xứng đáng, sẽ đặt lại nhiều lần nữa.",
    "Rau ăn kèm tươi, nước sốt đậm đà, tổng thể rất hài lòng.",
    "Phần ăn trình bày bắt mắt, topping phong phú khiến ăn không bị ngán.",
    "Gia vị hài hòa, không quá mặn cũng không quá nhạt, dễ ăn cho cả nhà.",
    "Sốt đặc trưng rất ngon, nhưng mình mong thêm chút rau sống nữa là hoàn hảo.",
    "Ăn tới miếng cuối cùng vẫn thấy ngon, sẽ giới thiệu cho bạn bè thử.",
  ]

  const baseDate = Date.parse("2024-10-20T12:00:00Z")

  return dishes.flatMap((dish, dishIndex) => {
    const reviewCount = 3 // Tạo 3 review cho mỗi món
    return Array.from({ length: reviewCount }, (_, reviewIndex) => {
      const reviewer = reviewers[(dishIndex + reviewIndex) % reviewers.length]
      const comment = comments[(dishIndex * 2 + reviewIndex) % comments.length]
      const ratingBase = dish.rating
      const adjustment = reviewIndex === 0 ? 0.2 : reviewIndex === 1 ? -0.1 : 0
      const rating = Math.min(5, Math.max(3.5, Math.round((ratingBase + adjustment) * 10) / 10))
      const createdAt = new Date(baseDate - (dishIndex * 3 + reviewIndex) * 86400000).toISOString()

      return {
        id: `gen_review_${dish.id}_${reviewIndex}`,
        userId: reviewer.id,
        userName: reviewer.name,
        dishId: dish.id,
        orderId: `gen_order_${dish.id}_${reviewIndex}`,
        rating,
        comment,
        createdAt,
      }
    })
  })
}

// Gọi hàm để tạo review
const generatedReviews = generateDynamicReviews(mockDishes)

// Tạo một mảng tổng hợp chứa cả review gốc và review vừa tạo
export const allMockReviews: Review[] = [...mockReviews, ...generatedReviews]






export const mockCategories: Category[] = [
  {
    id: "cat_1",
    name: "Ẩm Thực",
  },
  {
    id: "cat_2",
    name: "Nguyên Liệu Chính",
  },
  {
    id: "cat_3",
    name: "Phương Pháp Chế Biến",
  },
  {
    id: "cat_4",
    name: "Hương Vị",
  },
];

export const mockTags: Tag[] = [
  // Category 1: Ẩm Thực
  { id: "tag_1_1", name: "Món Việt", categoryId: "cat_1" },
  { id: "tag_1_2", name: "Món Hàn", categoryId: "cat_1" },
  { id: "tag_1_3", name: "Món Nhật", categoryId: "cat_1" },
  { id: "tag_1_4", name: "Món Trung", categoryId: "cat_1" },
  { id: "tag_1_5", name: "Món Thái", categoryId: "cat_1" },
  { id: "tag_1_6", name: "Món Âu", categoryId: "cat_1" },

  // Category 2: Nguyên Liệu Chính
  { id: "tag_2_1", name: "Thịt Bò", categoryId: "cat_2" },
  { id: "tag_2_2", name: "Thịt Heo", categoryId: "cat_2" },
  { id: "tag_2_3", name: "Thịt Gà", categoryId: "cat_2" },
  { id: "tag_2_4", name: "Hải Sản", categoryId: "cat_2" }, // Gồm Tôm, Cá...
  { id: "tag_2_5", name: "Rau Củ", categoryId: "cat_2" },
  { id: "tag_2_6", name: "Bánh Mì/Bún/Phở", categoryId: "cat_2" }, // Gồm Phở, Bánh Mì...

  // Category 3: Phương Pháp Chế Biến
  { id: "tag_3_1", name: "Chiên", categoryId: "cat_3" },
  { id: "tag_3_2", name: "Xào", categoryId: "cat_3" },
  { id: "tag_3_3", name: "Nấu/Canh", categoryId: "cat_3" },
  { id: "tag_3_4", name: "Hấp", categoryId: "cat_3" },
  { id: "tag_3_5", name: "Nướng/Quay", categoryId: "cat_3" },
  { id: "tag_3_6", name: "Trộn/Gỏi", categoryId: "cat_3" },

  // Category 4: Hương Vị
  { id: "tag_4_1", name: "Chua", categoryId: "cat_4" },
  { id: "tag_4_2", name: "Cay", categoryId: "cat_4" },
  { id: "tag_4_3", name: "Mặn", categoryId: "cat_4" },
  { id: "tag_4_4", name: "Ngọt", categoryId: "cat_4" },
  { id: "tag_4_5", name: "Béo", categoryId: "cat_4" },
  { id: "tag_4_6", name: "Thanh/Nhạt", categoryId: "cat_4" },
];

export const mockAddresses: Address[] = [
  {
    id: "addr_1",
    userId: USER_ID,
    address: "99 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh",
    isDefault: true, // Địa chỉ mặc định
  },
  {
    id: "addr_2",
    userId: USER_ID,
    address: "Tòa nhà Bitexco, 2 Hải Triều, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh (Văn phòng)",
    isDefault: false,
  },
  {
    id: "addr_3",
    userId: USER_ID,
    address: "22/10 Đường Nguyễn Văn Linh, Quận 7, TP. Hồ Chí Minh (Nhà Ba Mẹ)",
    isDefault: false,
  },
];

export const mockBiases: Bias[] = [
  // Ẩm Thực (cat_1)
  { id: "bias_1", userId: USER_ID, tagId: "tag_1_1", score: 5 }, // Món Việt (Cực thích)
  { id: "bias_2", userId: USER_ID, tagId: "tag_1_2", score: 1 }, // Món Hàn (Không thích)
  { id: "bias_3", userId: USER_ID, tagId: "tag_1_3", score: 3 }, // Món Nhật (Bình thường)
  { id: "bias_4", userId: USER_ID, tagId: "tag_1_4", score: 2 }, // Món Trung (Ít thích)
  { id: "bias_5", userId: USER_ID, tagId: "tag_1_6", score: 4 }, // Món Âu (Thích)

  // Nguyên Liệu Chính (cat_2)
  { id: "bias_6", userId: USER_ID, tagId: "tag_2_1", score: 5 }, // Thịt Bò (Cực thích)
  { id: "bias_7", userId: USER_ID, tagId: "tag_2_2", score: 3 }, // Thịt Heo (Bình thường)
  { id: "bias_8", userId: USER_ID, tagId: "tag_2_3", score: 4 }, // Thịt Gà (Thích)
  { id: "bias_9", userId: USER_ID, tagId: "tag_2_4", score: 2 }, // Hải Sản (Ít thích)
  { id: "bias_10", userId: USER_ID, tagId: "tag_2_6", score: 5 }, // Bánh Mì/Bún/Phở (Cực thích)

  // Phương Pháp Chế Biến (cat_3)
  { id: "bias_11", userId: USER_ID, tagId: "tag_3_1", score: 4 }, // Chiên (Thích)
  { id: "bias_12", userId: USER_ID, tagId: "tag_3_5", score: 5 }, // Nướng/Quay (Cực thích)
  { id: "bias_13", userId: USER_ID, tagId: "tag_3_3", score: 3 }, // Nấu/Canh (Bình thường)
  { id: "bias_14", userId: USER_ID, tagId: "tag_3_4", score: 1 }, // Hấp (Không thích)

  // Hương Vị (cat_4)
  { id: "bias_15", userId: USER_ID, tagId: "tag_4_2", score: 5 }, // Cay (Cực thích)
  { id: "bias_16", userId: USER_ID, tagId: "tag_4_4", score: 2 }, // Ngọt (Ít thích)
  { id: "bias_17", userId: USER_ID, tagId: "tag_4_5", score: 4 }, // Béo (Thích)
  { id: "bias_18", userId: USER_ID, tagId: "tag_4_6", score: 1 }, // Thanh/Nhạt (Không thích)
];

// 3. Dữ liệu User
export const mockUsers: User[] = [
  {
    id: "user_1",
    email: "nguyenvanb@fakefoodapp.com",
    name: "Nguyễn Văn B",
    phone: "0901234567",
    gender: "male",
    birthdate: "1990-05-15",
    role: "user",
    roleName: "USER",
    createdAt: "2024-01-10T08:00:00Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_2",
    email: "lethutrang@fakefoodapp.com",
    name: "Lê Thị Thu Trang",
    phone: "0912345678",
    gender: "female",
    birthdate: "1995-11-20",
    role: "user",
    roleName: "USER",
    createdAt: "2024-02-22T14:30:00Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_3",
    email: "phamminhduc.admin@fakefoodapp.com",
    name: "Phạm Minh Đức",
    phone: "0987654321",
    gender: "male",
    birthdate: "1988-01-30",
    role: "admin",
    roleName: "ADMIN",
    createdAt: "2024-01-01T09:00:00Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_4",
    email: "vungochaofficial@fakefoodapp.com",
    name: "Vũ Ngọc Hà",
    phone: "0334455667",
    gender: "female",
    birthdate: "2001-07-12",
    role: "user",
    roleName: "USER",
    createdAt: "2024-03-15T11:20:00Z",
    isActive: false,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_5",
    email: "dinh.tuan.kiet@fakefoodapp.com",
    name: "Đinh Tuấn Kiệt",
    phone: "0778899001",
    gender: "male",
    birthdate: "1998-09-05",
    role: "user",
    roleName: "USER",
    createdAt: "2024-04-01T19:00:00Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
  // === DỮ LIỆU MỚI THÊM VÀO, GIỮ NGUYÊN CẤU TRÚC ===
  {
    id: "user_6",
    email: "trankhoa.dev@fakefoodapp.com",
    name: "Trần Anh Khoa",
    phone: "0938111222",
    gender: "male",
    birthdate: "1992-03-25",
    role: "user",
    roleName: "USER",
    createdAt: "2024-05-10T10:10:10Z",
    isActive: false,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_7",
    email: "myle.dang@fakefoodapp.com",
    name: "Đặng Thị Mỹ Lệ",
    phone: "0868333444",
    gender: "female",
    birthdate: "1997-08-19",
    role: "admin",
    roleName: "ADMIN",
    createdAt: "2024-01-15T09:00:00Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_8",
    email: "baohq@fakefoodapp.com",
    name: "Hoàng Quốc Bảo",
    phone: "0559555666",
    gender: "male",
    birthdate: "1985-12-01",
    role: "user",
    roleName: "USER",
    createdAt: "2024-06-01T15:00:00Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_9",
    email: "phuongthao.bui@fakefoodapp.com",
    name: "Bùi Phương Thảo",
    phone: "0944777888",
    gender: "female",
    birthdate: "2002-02-14",
    role: "user",
    roleName: "USER",
    createdAt: "2024-07-20T20:20:20Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_10",
    email: "huyngo.gia@fakefoodapp.com",
    name: "Ngô Gia Huy",
    phone: "0789999111",
    gender: "male",
    birthdate: "2000-10-10",
    role: "user",
    roleName: "USER",
    createdAt: "2024-08-08T08:08:08Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_11",
    email: "thuylinh.phan@fakefoodapp.com",
    name: "Phan Thuỳ Linh",
    phone: "0969123123",
    gender: "female",
    birthdate: "1999-06-28",
    role: "user",
    roleName: "USER",
    createdAt: "2024-09-01T00:00:00Z",
    isActive: false,
    bias: mockBiases,
    address: mockAddresses,
  },
  {
    id: "user_12",
    email: "v.minh.khang@fakefoodapp.com",
    name: "Võ Minh Khang",
    phone: "0925252525",
    gender: "male",
    birthdate: "1993-04-17",
    role: "user",
    roleName: "USER",
    createdAt: "2024-10-01T12:34:56Z",
    isActive: true,
    bias: mockBiases,
    address: mockAddresses,
  },
];

export const mockTopSellingData: TopSelling[] = [
  {
    dishId: "dish_1", 
    quantitySold: 215,
  },
  {
    dishId: "dish_2", 
    quantitySold: 189,
  },
  {
    dishId: "dish_3", 
    quantitySold: 172,
  },
  {
    dishId: "dish_4", 
    quantitySold: 158,
  },
  {
    dishId: "dish_5", 
    quantitySold: 141,
  },
  {
    dishId: "dish_6", 
    quantitySold: 130,    
  },
]