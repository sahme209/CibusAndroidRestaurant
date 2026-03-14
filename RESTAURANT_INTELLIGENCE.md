# Restaurant Intelligence Platform — Phase 83

Intelligent insights to help restaurants grow on the Cibus platform.

---

## Restaurant Apps (Android & iOS)

### A — Restaurant Analytics Dashboard

Metrics displayed:
- **Daily Orders** — count vs yesterday
- **Avg Prep Time** — target comparison
- **Delivery Time** — average ETA
- **Revenue** — today's total

### B — Dish Popularity Signals

Automatically detected categories:
- **Best sellers** — top-performing dishes
- **Trending** — rising in orders
- **Underperforming** — low engagement, consider promotions or removal

### C — Restaurant Suggestions

Actionable recommendations:
- **Promote this dish** — trending items deserving spotlight deals
- **Improve prep time** — category-level prep insights
- **Add combo meals** — combo deals drive higher order volume

---

## Customer Apps (Android & iOS)

### D — Customer Visibility

Food cards display popularity badges:
- **Best seller** — when `isPopular` and `reviewCount >= 800`
- **Popular today** — when `isPopular`

Badges appear on `FoodCard` and `FoodCardHorizontal` in customer apps.

---

## Files Modified

### Android
- **CibusAndroid:** `FoodCard.kt` — auto "Best seller" / "Popular today" from item
- **CibusAndroidRestaurant:** `RestaurantAnalyticsContent.kt` — Dish Popularity Signals section, PopularityChip

### iOS
- **CibusIOS:** `FoodCard.swift` — popularity badge; `CibusModels.swift` — `popularityDisplayLabel`
- **CibusIOSRestaurant:** `RestaurantMainView.swift` — full analytics DashboardView
