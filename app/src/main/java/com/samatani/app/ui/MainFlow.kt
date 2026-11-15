@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.samatani.app.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samatani.app.data.OfferStatus
import com.samatani.app.data.OrderStatus
import com.samatani.app.data.Role
import com.samatani.app.data.User
import com.samatani.app.ui.account.AccountScreen
import com.samatani.app.ui.account.EditProfileScreen
import com.samatani.app.ui.account.MarketPriceScreen
import com.samatani.app.ui.buyer.CartScreen
import com.samatani.app.ui.buyer.CheckoutScreen
import com.samatani.app.ui.buyer.MarketScreen
import com.samatani.app.ui.buyer.ProductDetailScreen
import com.samatani.app.ui.farmer.FarmerHomeScreen
import com.samatani.app.ui.farmer.MyProductsScreen
import com.samatani.app.ui.farmer.ProductFormScreen
import com.samatani.app.ui.orders.OrderDetailScreen
import com.samatani.app.ui.orders.OrdersScreen
import com.samatani.app.ui.requests.RequestDetailScreen
import com.samatani.app.ui.requests.RequestFormScreen
import com.samatani.app.ui.requests.RequestsScreen

private data class TabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
)

private fun tabsFor(role: Role): List<TabItem> = if (role == Role.PETANI) {
    listOf(
        TabItem(Routes.FARMER_HOME, "Beranda", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem(Routes.MY_PRODUCTS, "Produk", Icons.Filled.ShoppingBasket, Icons.Outlined.ShoppingBasket),
        TabItem(Routes.ORDERS, "Pesanan", Icons.Filled.Receipt, Icons.Outlined.Receipt),
        TabItem(Routes.REQUESTS, "Permintaan", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        TabItem(Routes.ACCOUNT, "Akun", Icons.Filled.Person, Icons.Outlined.Person),
    )
} else {
    listOf(
        TabItem(Routes.MARKET, "Pasar", Icons.Filled.Store, Icons.Outlined.Store),
        TabItem(Routes.REQUESTS, "Permintaan", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        TabItem(Routes.CART, "Keranjang", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
        TabItem(Routes.ORDERS, "Pesanan", Icons.Filled.Receipt, Icons.Outlined.Receipt),
        TabItem(Routes.ACCOUNT, "Akun", Icons.Filled.Person, Icons.Outlined.Person),
    )
}

/** Pindah tab dengan menyimpan & memulihkan posisi tiap tab. */
private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun MainFlow(user: User) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val nav = rememberNavController()
    val tabs = remember(user.role) { tabsFor(user.role) }
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(repository) {
        repository.messages.collect { message -> snackbarHostState.showSnackbar(message) }
    }

    fun badgeCount(route: String): Int = when (route) {
        Routes.CART -> data.cart.size
        Routes.ORDERS -> if (user.role == Role.PETANI) {
            data.orders.count { it.sellerId == user.id && it.status == OrderStatus.MENUNGGU } +
                data.offers.count { it.sellerId == user.id && it.status == OfferStatus.MENUNGGU }
        } else {
            data.orders.count { it.buyerId == user.id && it.status == OrderStatus.DIKIRIM } +
                data.offers.count { it.buyerId == user.id && it.status == OfferStatus.DITAWAR_BALIK }
        }
        else -> 0
    }

    val openTab: (String) -> Unit = { route -> nav.openTab(route) }
    val back: () -> Unit = { nav.popBackStack() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = if (showBottomBar) Modifier else Modifier.navigationBarsPadding(),
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        val count = badgeCount(tab.route)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { if (!selected) nav.openTab(tab.route) },
                            icon = {
                                BadgedBox(badge = {
                                    if (count > 0) Badge { Text(if (count > 99) "99+" else count.toString()) }
                                }) {
                                    Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = tabs.first().route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            // ---------- Tab petani ----------
            composable(Routes.FARMER_HOME) {
                FarmerHomeScreen(
                    user = user,
                    onAddProduct = { nav.navigate(Routes.productForm()) },
                    onOpenOrder = { nav.navigate(Routes.order(it)) },
                    onOpenRequest = { nav.navigate(Routes.request(it)) },
                    onOpenTab = openTab,
                    onOpenMarketPrices = { nav.navigate(Routes.MARKET_PRICES) },
                )
            }
            composable(Routes.MY_PRODUCTS) {
                MyProductsScreen(
                    user = user,
                    onAdd = { nav.navigate(Routes.productForm()) },
                    onEdit = { nav.navigate(Routes.productForm(it)) },
                    onOpen = { nav.navigate(Routes.product(it)) },
                )
            }

            // ---------- Tab pembeli ----------
            composable(Routes.MARKET) {
                MarketScreen(
                    user = user,
                    onOpenProduct = { nav.navigate(Routes.product(it)) },
                    onOpenCart = { openTab(Routes.CART) },
                    onOpenMarketPrices = { nav.navigate(Routes.MARKET_PRICES) },
                    onCreateRequest = { nav.navigate(Routes.REQUEST_FORM) },
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    onCheckout = { nav.navigate(Routes.CHECKOUT) },
                    onOpenProduct = { nav.navigate(Routes.product(it)) },
                    onBrowse = { openTab(Routes.MARKET) },
                )
            }

            // ---------- Tab bersama ----------
            composable(Routes.ORDERS) {
                OrdersScreen(user = user, onOpenOrder = { nav.navigate(Routes.order(it)) })
            }
            composable(Routes.REQUESTS) {
                RequestsScreen(
                    user = user,
                    onOpenRequest = { nav.navigate(Routes.request(it)) },
                    onCreateRequest = { nav.navigate(Routes.REQUEST_FORM) },
                )
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(
                    user = user,
                    onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                    onOpenMarketPrices = { nav.navigate(Routes.MARKET_PRICES) },
                )
            }

            // ---------- Halaman detail ----------
            composable(Routes.PRODUCT) { entry ->
                ProductDetailScreen(
                    user = user,
                    productId = entry.arguments?.getString("id").orEmpty(),
                    onBack = back,
                    onEdit = { nav.navigate(Routes.productForm(it)) },
                    onOpenCart = { openTab(Routes.CART) },
                )
            }
            composable(
                route = Routes.PRODUCT_FORM,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { entry ->
                ProductFormScreen(
                    user = user,
                    productId = entry.arguments?.getString("id"),
                    onBack = back,
                )
            }
            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    user = user,
                    onBack = back,
                    onDone = {
                        nav.navigate(Routes.ORDERS) {
                            popUpTo(nav.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.ORDER) { entry ->
                OrderDetailScreen(
                    user = user,
                    orderId = entry.arguments?.getString("id").orEmpty(),
                    onBack = back,
                )
            }
            composable(Routes.REQUEST) { entry ->
                RequestDetailScreen(
                    user = user,
                    requestId = entry.arguments?.getString("id").orEmpty(),
                    onBack = back,
                    onOpenOrder = { nav.navigate(Routes.order(it)) },
                )
            }
            composable(Routes.REQUEST_FORM) {
                RequestFormScreen(user = user, onBack = back)
            }
            composable(Routes.MARKET_PRICES) {
                MarketPriceScreen(onBack = back)
            }
            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(user = user, onBack = back)
            }
        }
    }
}
