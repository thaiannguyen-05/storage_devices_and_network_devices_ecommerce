(function () {
    const storageKey = "storeit-theme";
    const root = document.documentElement;
    let memoryTheme = "dark";

    function readStoredTheme() {
        try {
            return window.localStorage.getItem(storageKey);
        } catch (error) {
            return memoryTheme;
        }
    }

    function writeStoredTheme(theme) {
        memoryTheme = theme;
        try {
            window.localStorage.setItem(storageKey, theme);
        } catch (error) {
            // Ignore storage failures and keep the in-memory value.
        }
    }

    function getPreferredTheme() {
        const storedTheme = readStoredTheme();
        if (storedTheme === "light" || storedTheme === "dark") {
            return storedTheme;
        }
        return window.matchMedia && window.matchMedia("(prefers-color-scheme: light)").matches
            ? "light"
            : "dark";
    }

    function applyTheme(theme) {
        root.setAttribute("data-theme", theme);
        writeStoredTheme(theme);

        const toggle = document.querySelector(".home-theme-toggle");
        if (!toggle) {
            return;
        }

        const nextTheme = theme === "dark" ? "light" : "dark";
        toggle.setAttribute("aria-label", nextTheme === "dark" ? "Switch to dark mode" : "Switch to light mode");
        toggle.setAttribute("title", nextTheme === "dark" ? "Dark mode" : "Light mode");
    }

    function buildToggleButton() {
        const toggle = document.createElement("button");
        toggle.type = "button";
        toggle.className = "home-theme-toggle";
        toggle.innerHTML =
            '<span class="home-theme-icon home-theme-icon--moon" aria-hidden="true">' +
            '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" focusable="false" aria-hidden="true"><path d="M20.7 14.4A8.8 8.8 0 0 1 9.6 3.3a.5.5 0 0 0-.6-.6A9.8 9.8 0 1 0 21.3 15a.5.5 0 0 0-.6-.6z"></path></svg>' +
            "</span>" +
            '<span class="home-theme-icon home-theme-icon--sun" aria-hidden="true">' +
            '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" focusable="false" aria-hidden="true"><circle cx="12" cy="12" r="4.2"></circle><path d="M11.25 1.8h1.5v3h-1.5zm0 17.4h1.5v3h-1.5zM1.8 11.25h3v1.5h-3zm17.4 0h3v1.5h-3zM4.58 5.64l1.06-1.06 2.12 2.12-1.06 1.06zm11.66 11.66 1.06-1.06 2.12 2.12-1.06 1.06zM16.3 7.76l2.12-2.12 1.06 1.06-2.12 2.12zM4.58 18.36l2.12-2.12 1.06 1.06-2.12 2.12z"></path></svg>' +
            "</span>";
        toggle.addEventListener("click", function () {
            const currentTheme = root.getAttribute("data-theme") === "light" ? "light" : "dark";
            applyTheme(currentTheme === "light" ? "dark" : "light");
        });
        return toggle;
    }

    function mountThemeToggle() {
        document.querySelectorAll(".home-header-top").forEach(function (headerTop) {
            if (headerTop.querySelector(".home-theme-toggle")) {
                return;
            }

            let rightGroup = headerTop.querySelector(".home-header-right");
            if (!rightGroup) {
                rightGroup = document.createElement("div");
                rightGroup.className = "home-header-right";
                headerTop.appendChild(rightGroup);
            }

            rightGroup.appendChild(buildToggleButton());
        });

        applyTheme(root.getAttribute("data-theme") || getPreferredTheme());
    }

    applyTheme(getPreferredTheme());

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", mountThemeToggle);
    } else {
        mountThemeToggle();
    }
})();
