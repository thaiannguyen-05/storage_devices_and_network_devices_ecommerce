(function () {
    function formatCurrency(value) {
        var amount = Number(value || 0);
        return amount.toLocaleString("vi-VN") + " VND";
    }

    function renderChart(points) {
        var chart = document.getElementById("adminRevenueChart");
        if (!chart) {
            return;
        }
        var max = 0;
        points.forEach(function (point) {
            var amount = Number(point.amount || 0);
            if (amount > max) {
                max = amount;
            }
        });
        chart.innerHTML = "";
        points.forEach(function (point) {
            var amount = Number(point.amount || 0);
            var percent = max <= 0 ? 6 : Math.max(6, Math.round((amount / max) * 100));
            var bar = document.createElement("div");
            bar.className = "admin-chart-bar";
            bar.innerHTML =
                '<span class="admin-chart-column" style="height:' + percent + '%"></span>' +
                '<strong>' + (point.date || "").substring(5) + '</strong>' +
                '<small>' + formatCurrency(amount) + '</small>';
            chart.appendChild(bar);
        });
    }

    function renderRecentOrders(orders) {
        var tbody = document.getElementById("adminRecentOrdersBody");
        if (!tbody) {
            return;
        }
        tbody.innerHTML = "";
        orders.forEach(function (order) {
            var row = document.createElement("tr");
            row.innerHTML =
                "<td>" + order.orderId + "</td>" +
                "<td>" + (order.customerName || "") + "</td>" +
                "<td>" + formatCurrency(order.totalAmount) + "</td>" +
                "<td><span class=\"badge\">" + order.status + "</span></td>" +
                "<td>" + (order.createdAt || "") + "</td>";
            tbody.appendChild(row);
        });
    }

    function renderTopProducts(products) {
        var tbody = document.getElementById("adminTopProductsBody");
        if (!tbody) {
            return;
        }
        tbody.innerHTML = "";
        products.forEach(function (product) {
            var row = document.createElement("tr");
            row.innerHTML =
                "<td>" + product.productName + "</td>" +
                "<td>" + product.totalSold + "</td>" +
                "<td>" + formatCurrency(product.revenue) + "</td>";
            tbody.appendChild(row);
        });
    }

    function renderStats(stats) {
        var ids = {
            totalUsers: "statTotalUsers",
            totalProducts: "statTotalProducts",
            totalOrders: "statTotalOrders",
            totalRevenue: "statTotalRevenue",
            activeOrders: "statActiveOrders"
        };
        Object.keys(ids).forEach(function (key) {
            var node = document.getElementById(ids[key]);
            if (!node) {
                return;
            }
            node.textContent = key === "totalRevenue" ? formatCurrency(stats[key]) : (stats[key] || 0);
        });
    }

    function refreshDashboard() {
        var source = document.body.getAttribute("data-dashboard-api");
        if (!source) {
            return;
        }
        fetch(source, { headers: { "Accept": "application/json" } })
            .then(function (response) { return response.json(); })
            .then(function (payload) {
                if (!payload || !payload.success) {
                    return;
                }
                renderStats(payload.dashboardStats || {});
                renderChart(payload.revenueTrend || []);
                renderRecentOrders(payload.recentOrders || []);
                renderTopProducts(payload.topProducts || []);
            })
            .catch(function () {
            });
    }

    document.addEventListener("DOMContentLoaded", function () {
        if (!document.body.classList.contains("admin-dashboard-page")) {
            return;
        }
        refreshDashboard();
        window.setInterval(refreshDashboard, 30000);
    });
}());
