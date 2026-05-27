(function () {
    "use strict";

    const StoreIT = {
        formatCurrency(amount) {
            const number = Number(amount || 0);
            return new Intl.NumberFormat("vi-VN", {
                style: "currency",
                currency: "VND"
            }).format(number);
        },
        debounce(fn, delay) {
            let timer;
            return function (...args) {
                window.clearTimeout(timer);
                timer = window.setTimeout(() => fn.apply(this, args), delay);
            };
        },
        validateEmail(email) {
            return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email || "").trim());
        },
        validatePhone(phone) {
            return /^[0-9]{9,11}$/.test(String(phone || "").trim());
        }
    };

    function initHeader() {
        document.querySelectorAll("[data-filter-toggle]").forEach((button) => {
            button.addEventListener("click", () => document.body.classList.toggle("filter-open"));
        });

        document.addEventListener("click", (event) => {
            const sidebar = document.querySelector(".left-menu");
            if (sidebar && !sidebar.contains(event.target)
                && !event.target.hasAttribute("data-filter-toggle")
                && document.body.classList.contains("filter-open")) {
                document.body.classList.remove("filter-open");
            }
        });

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                document.body.classList.remove("filter-open");
            }
        });

        const search = document.querySelector("[data-search]");
        const suggestionsBox = document.querySelector("[data-search-suggestions]");
        if (search && suggestionsBox) {
            const form = search.closest("form");
            const formAction = form ? form.getAttribute("action") : "";
            const ctx = formAction.substring(0, formAction.lastIndexOf("/"));
            const apiUrl = ctx + "/products?action=autocomplete";

            const fetchSuggestions = StoreIT.debounce(async (keyword) => {
                const term = String(keyword || "").trim();
                if (!term) {
                    suggestionsBox.innerHTML = "";
                    suggestionsBox.style.display = "none";
                    return;
                }

                try {
                    const res = await fetch(apiUrl + "&keyword=" + encodeURIComponent(term));
                    if (!res.ok) throw new Error("API error");
                    const data = await res.json();
                    
                    if (data && data.length > 0) {
                        suggestionsBox.innerHTML = data.map(item => {
                            const formattedPrice = StoreIT.formatCurrency(item.price);
                            const detailUrl = ctx + "/product?id=" + item.id;
                            const imageHtml = item.imageUrl 
                                ? `<img src="${item.imageUrl}" alt="${item.name}">`
                                : `<div style="width:40px; height:40px; border-radius:4px; background:var(--color-gray-100); display:flex; align-items:center; justify-content:center; color:var(--color-text-muted); font-size:0.75rem;">No image</div>`;
                            
                            return `
                                <a href="${detailUrl}" class="suggestion-item">
                                    ${imageHtml}
                                    <div class="suggestion-info">
                                        <div class="suggestion-name">${item.name}</div>
                                        <div class="suggestion-price">${formattedPrice}</div>
                                    </div>
                                </a>
                            `;
                        }).join("");
                        suggestionsBox.style.display = "block";
                    } else {
                        suggestionsBox.innerHTML = `<div class="suggestion-empty">Không tìm thấy sản phẩm nào</div>`;
                        suggestionsBox.style.display = "block";
                    }
                } catch (err) {
                    console.error("Autocomplete failed:", err);
                    suggestionsBox.style.display = "none";
                }
            }, 250);

            search.addEventListener("input", (event) => {
                const keyword = event.target.value;
                document.documentElement.style.setProperty("--search-length", keyword.length);
                fetchSuggestions(keyword);
            });

            search.addEventListener("focus", () => {
                if (search.value.trim()) {
                    suggestionsBox.style.display = "block";
                }
            });

            document.addEventListener("click", (event) => {
                if (!search.contains(event.target) && !suggestionsBox.contains(event.target)) {
                    suggestionsBox.style.display = "none";
                }
            });

            search.addEventListener("keydown", (event) => {
                if (event.key === "Escape") {
                    suggestionsBox.style.display = "none";
                    search.blur();
                }
            });
        }
    }

    function initFilters() {
        const filterForm = document.querySelector(".left-menu form");
        if (!filterForm) return;

        const urlParams = new URLSearchParams(window.location.search);

        // Restore checkboxes
        filterForm.querySelectorAll("input[type='checkbox']").forEach(cb => {
            const name = cb.name;
            const value = cb.value;
            const values = urlParams.getAll(name);
            if (values.includes(value)) {
                cb.checked = true;
            }
        });

        // Restore radios
        filterForm.querySelectorAll("input[type='radio']").forEach(radio => {
            const name = radio.name;
            const value = radio.value;
            const val = urlParams.get(name);
            if (val === value) {
                radio.checked = true;
            }
        });

        // Restore selects
        filterForm.querySelectorAll("select").forEach(select => {
            const name = select.name;
            const val = urlParams.get(name);
            if (val) {
                select.value = val;
            }
        });

        // Preserve active search parameters (q, keyword) in the filter form so they aren't lost on filter submission
        ["q", "keyword"].forEach(paramName => {
            const val = urlParams.get(paramName);
            if (val) {
                let hiddenInput = filterForm.querySelector(`input[type='hidden'][name='${paramName}']`);
                if (!hiddenInput) {
                    hiddenInput = document.createElement("input");
                    hiddenInput.type = "hidden";
                    hiddenInput.name = paramName;
                    filterForm.appendChild(hiddenInput);
                }
                hiddenInput.value = val;
            }
        });

        // Only auto-submit on change for sort select
        filterForm.addEventListener("change", (event) => {
            const isSelect = event.target.tagName === "SELECT";
            if (isSelect) {
                filterForm.submit();
            }
        });

        // Scroll to products if any filter/search is active
        const hasFilters = urlParams.has("category") || urlParams.has("brand") || urlParams.has("price") || urlParams.has("status") || urlParams.has("sort") || urlParams.has("q") || urlParams.has("keyword");
        if (hasFilters) {
            const productsSection = document.getElementById("products");
            if (productsSection) {
                setTimeout(() => {
                    productsSection.scrollIntoView({ behavior: "smooth", block: "start" });
                }, 150);
            }
        }
    }

    function initForms() {
        document.querySelectorAll("[data-validate]").forEach((form) => {
            form.addEventListener("submit", (event) => {
                let ok = true;
                form.querySelectorAll("[required]").forEach((field) => {
                    const wrapper = field.closest(".field");
                    const error = wrapper ? wrapper.querySelector(".error") : null;
                    let message = "";

                    if (!String(field.value || "").trim()) {
                        message = "Vui lòng nhập thông tin này.";
                    } else if (field.type === "email" && !StoreIT.validateEmail(field.value)) {
                        message = "Email không đúng định dạng.";
                    } else if (field.dataset.phone === "true" && !StoreIT.validatePhone(field.value)) {
                        message = "Số điện thoại cần 9-11 chữ số.";
                    }

                    if (message) {
                        ok = false;
                        wrapper && wrapper.classList.add("invalid");
                        if (error) {
                            error.textContent = message;
                        }
                    } else {
                        wrapper && wrapper.classList.remove("invalid");
                        if (error) {
                            error.textContent = "";
                        }
                    }
                });

                const password = form.querySelector("[data-password]");
                const confirm = form.querySelector("[data-password-confirm]");
                if (password && confirm && password.value !== confirm.value) {
                    ok = false;
                    const wrapper = confirm.closest(".field");
                    wrapper && wrapper.classList.add("invalid");
                    const error = wrapper ? wrapper.querySelector(".error") : null;
                    if (error) {
                        error.textContent = "Mật khẩu nhập lại không khớp.";
                    }
                }

                if (!ok) {
                    event.preventDefault();
                }
            });
        });
    }

    function initProductDetail() {
        const variantSelect = document.querySelector("[data-variant-select]");
        if (variantSelect) {
            const price = document.querySelector("[data-variant-price]");
            const stock = document.querySelector("[data-variant-stock]");
            const image = document.querySelector("[data-main-image]");
            const qty = document.querySelector("[data-quantity]");
            const variantInput = document.querySelector("[data-variant-input]");

            variantSelect.addEventListener("change", () => {
                const option = variantSelect.selectedOptions[0];
                if (!option) {
                    return;
                }
                if (variantInput) {
                    variantInput.value = option.value;
                }
                if (price) {
                    price.textContent = StoreIT.formatCurrency(option.dataset.price);
                }
                if (stock) {
                    stock.textContent = Number(option.dataset.quantity || 0) > 0
                        ? "Còn " + option.dataset.quantity + " sản phẩm"
                        : "Hết hàng";
                }
                if (image && option.dataset.image) {
                    image.src = option.dataset.image;
                }
                if (qty) {
                    qty.max = option.dataset.quantity || 1;
                    qty.value = Math.min(Number(qty.value || 1), Number(qty.max || 1));
                }
            });
            // Trigger change on initial load to sync price/quantity
            variantSelect.dispatchEvent(new Event("change"));
        }

        document.querySelectorAll("[data-thumb]").forEach((button) => {
            button.addEventListener("click", () => {
                const image = document.querySelector("[data-main-image]");
                if (image) {
                    image.src = button.dataset.thumb;
                }
            });
        });

        document.querySelectorAll("[data-tab]").forEach((button) => {
            button.addEventListener("click", () => {
                document.querySelectorAll("[data-tab]").forEach((item) => item.classList.remove("active"));
                document.querySelectorAll("[data-tab-panel]").forEach((item) => item.classList.remove("active"));
                button.classList.add("active");
                const panel = document.querySelector("[data-tab-panel='" + button.dataset.tab + "']");
                panel && panel.classList.add("active");
            });
        });
    }

    function initCart() {
        function recalc(scope) {
            let total = 0;
            scope.querySelectorAll("[data-cart-row]").forEach((row) => {
                const price = Number(row.dataset.price || 0);
                const quantity = Number(row.querySelector("[data-cart-quantity]")?.value || 0);
                const line = price * quantity;
                const lineEl = row.querySelector("[data-line-total]");
                if (lineEl) {
                    lineEl.textContent = StoreIT.formatCurrency(line);
                }
                const checked = row.querySelector("[data-item-select]")?.checked !== false;
                if (checked) {
                    total += line;
                }
            });
            const totalEl = scope.querySelector("[data-cart-total]");
            if (totalEl) {
                totalEl.textContent = StoreIT.formatCurrency(total);
            }
        }

        document.querySelectorAll("[data-cart]").forEach((cart) => {
            const debouncedSubmit = StoreIT.debounce((form) => {
                form.submit();
            }, 500);

            cart.addEventListener("input", (event) => {
                if (event.target.matches("[data-cart-quantity]")) {
                    recalc(cart);
                    const form = event.target.closest("form");
                    if (form) {
                        debouncedSubmit(form);
                    }
                }
            });

            cart.addEventListener("change", (event) => {
                if (event.target.matches("[data-item-select]")) {
                    recalc(cart);
                    const allCb = cart.querySelector("[data-select-all]");
                    if (allCb) {
                        const items = Array.from(cart.querySelectorAll("[data-item-select]"));
                        allCb.checked = items.every(cb => cb.checked);
                    }
                } else if (event.target.matches("[data-select-all]")) {
                    const checked = event.target.checked;
                    cart.querySelectorAll("[data-item-select]").forEach(cb => {
                        cb.checked = checked;
                    });
                    recalc(cart);
                }
            });

            cart.addEventListener("click", (event) => {
                const btn = event.target.closest("[data-qty-change]");
                if (btn) {
                    const change = Number(btn.dataset.qtyChange || 0);
                    const row = btn.closest("[data-cart-row]");
                    const input = row ? row.querySelector("[data-cart-quantity]") : null;
                    if (input) {
                        const min = Number(input.min || 1);
                        const max = Number(input.max || 9999);
                        let val = Number(input.value || 1) + change;
                        if (val >= min && val <= max) {
                            input.value = val;
                            input.dispatchEvent(new Event("input", { bubbles: true }));
                        }
                    }
                }
            });

            recalc(cart);
        });
    }

    function initQuantityControls() {
        document.addEventListener("click", (event) => {
            const btn = event.target.closest("[data-qty-change]");
            if (!btn) return;
            if (btn.closest("[data-cart]")) return;

            const change = Number(btn.dataset.qtyChange || 0);
            const container = btn.closest(".quantity-control");
            const input = container ? container.querySelector("input") : null;
            if (input) {
                const min = Number(input.min || 1);
                const max = Number(input.max || 9999);
                let val = Number(input.value || 1) + change;
                if (val >= min && val <= max) {
                    input.value = val;
                    input.dispatchEvent(new Event("change", { bubbles: true }));
                    input.dispatchEvent(new Event("input", { bubbles: true }));
                }
            }
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        initHeader();
        initFilters();
        initForms();
        initProductDetail();
        initCart();
        initQuantityControls();
    });

    window.StoreIT = StoreIT;
})();
