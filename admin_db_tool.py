#!/usr/bin/env python3
"""
Admin GUI Tool - LUXURY EDITION v4.0 (FINAL STABLE)
----------------------------------
- Cố định kích thước 1500x850.
- Khôi phục đầy đủ các hàm xử lý Voucher (Add/Delete).
- Đảm bảo tính ổn định tối đa cho phần hình ảnh.
"""

import os
import sys
import uuid
import shutil
import tkinter as tk
from tkinter import ttk, messagebox, filedialog
from dataclasses import dataclass
from datetime import datetime
from typing import Dict, List, Optional
from io import BytesIO

# --- Dependencies Check ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
LOCAL_DEPS = os.path.join(BASE_DIR, ".pydeps")
if os.path.isdir(LOCAL_DEPS) and LOCAL_DEPS not in sys.path:
    sys.path.insert(0, LOCAL_DEPS)

try:
    import pymysql
    from pymysql.cursors import DictCursor
    from PIL import Image, ImageTk
    import requests
except ImportError:
    pass

# --- DB Config ---
@dataclass
class DbConfig:
    host: str; port: int; name: str; user: str; password: str

def load_db_config() -> DbConfig:
    env_path = os.path.join(BASE_DIR, ".env")
    data: Dict[str, str] = {}
    if os.path.exists(env_path):
        with open(env_path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1); data[k.strip()] = v.strip()
    return DbConfig(host=data.get("DB_HOST", "localhost"), port=int(data.get("DB_PORT", "3306")), name=data.get("DB_NAME", "ecommerce"), user=data.get("DB_USER", "root"), password=data.get("DB_PASSWORD", ""))

class AdminDbCore:
    def __init__(self, cfg: DbConfig):
        self.cfg = cfg; self.conn = None; self.connect(); self.ensure_tables()
    def connect(self):
        try: self.conn = pymysql.connect(host=self.cfg.host, port=self.cfg.port, user=self.cfg.user, password=self.cfg.password, database=self.cfg.name, charset="utf8mb4", cursorclass=DictCursor, autocommit=True)
        except Exception as e: raise ConnectionError(f"DB Error: {e}")
    def ensure_tables(self):
        with self.conn.cursor() as cur:
            cur.execute("CREATE TABLE IF NOT EXISTS voucher (id VARCHAR(64) PRIMARY KEY, code VARCHAR(64) NULL, userId VARCHAR(64) NOT NULL DEFAULT 'u1', expTime DATE NOT NULL, createdAt DATETIME NOT NULL, quantity INT NOT NULL DEFAULT 0)")
            cur.execute("SHOW COLUMNS FROM voucher")
            cols = [row['Field'] for row in cur.fetchall()]
            if 'discountType' not in cols: cur.execute("ALTER TABLE voucher ADD COLUMN discountType VARCHAR(20) NOT NULL DEFAULT 'PERCENT'")
            if 'discountValue' not in cols: cur.execute("ALTER TABLE voucher ADD COLUMN discountValue FLOAT NOT NULL DEFAULT 0")
            if 'percent' not in cols: cur.execute("ALTER TABLE voucher ADD COLUMN percent FLOAT NOT NULL DEFAULT 0")
            cur.execute("CREATE TABLE IF NOT EXISTS productreview (id BIGINT AUTO_INCREMENT PRIMARY KEY, productId VARCHAR(64) NOT NULL, reviewerName VARCHAR(255) NOT NULL, rating INT NOT NULL, comment TEXT NULL, createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, INDEX idx_productreview_productid(productId))")
        self.conn.commit()

    def check_id_exists(self, pid):
        with self.conn.cursor() as cur: cur.execute("SELECT id FROM product WHERE id = %s", (pid,)); return cur.fetchone() is not None
    def get_products(self):
        with self.conn.cursor() as cur: cur.execute("SELECT p.id, p.name, p.category, p.status, p.createdAt, v.id as variantId, v.price, v.quantity, v.imageUrl FROM product p LEFT JOIN productvariant v ON v.productId = p.id ORDER BY p.createdAt DESC"); return cur.fetchall()
    def add_product(self, p_data: dict, v_data: dict):
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        with self.conn.cursor() as cur:
            cur.execute("INSERT INTO product(id, name, description, brandId, status, userId, createdAt, updatedAt, category) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)", (p_data['id'], p_data['name'], p_data['description'], "b1", "ACTIVE", "u1", now, now, p_data['category']))
            cur.execute("INSERT INTO productvariant(id, productId, price, imageUrl, status, createdAt, updatedAt, sku, quantity) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)", (v_data['id'], p_data['id'], v_data['price'], v_data['imageUrl'], "ACTIVE", now, now, f"SKU-{v_data['id']}", v_data['quantity']))
        self.conn.commit()
    def update_product_and_variant(self, pid, vid, p_updates: dict, v_updates: dict):
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        with self.conn.cursor() as cur:
            if p_updates:
                sets = [f"{k}=%s" for k in p_updates.keys()]; vals = list(p_updates.values()) + [now, pid]
                cur.execute(f"UPDATE product SET {', '.join(sets)}, updatedAt=%s WHERE id=%s", tuple(vals))
            if v_updates:
                sets = [f"{k}=%s" for k in v_updates.keys()]; vals = list(v_updates.values()) + [now, vid]
                cur.execute(f"UPDATE productvariant SET {', '.join(sets)}, updatedAt=%s WHERE id=%s", tuple(vals))
        self.conn.commit()
    def delete_product(self, pid):
        with self.conn.cursor() as cur: cur.execute("DELETE FROM productvariant WHERE productId=%s", (pid,)); cur.execute("DELETE FROM product WHERE id=%s", (pid,))
        self.conn.commit()
    def get_vouchers(self):
        with self.conn.cursor() as cur: cur.execute("SELECT * FROM voucher ORDER BY createdAt DESC"); return cur.fetchall()
    def add_voucher(self, data: dict):
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S"); lp = data['val'] if data['type'] == 'PERCENT' else 0
        with self.conn.cursor() as cur: cur.execute("INSERT INTO voucher(id, code, discountType, discountValue, percent, userId, expTime, createdAt, quantity) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)", (str(uuid.uuid4())[:8], data['code'], data['type'], data['val'], lp, "u1", "2026-12-31", now, data['qty']))
        self.conn.commit()
    def delete_voucher(self, vid):
        with self.conn.cursor() as cur: cur.execute("DELETE FROM voucher WHERE id=%s", (vid,))
        self.conn.commit()
    def get_reviews(self):
        with self.conn.cursor() as cur: cur.execute("SELECT r.*, p.name as productName FROM productreview r JOIN product p ON r.productId = p.id ORDER BY r.createdAt DESC"); return cur.fetchall()
    def delete_review(self, rid):
        with self.conn.cursor() as cur: cur.execute("DELETE FROM productreview WHERE id=%s", (rid,))
        self.conn.commit()

class SidebarButton(tk.Button):
    def __init__(self, master, text, icon, command=None, **kwargs):
        self.active_bg = "#2563eb"; self.normal_bg = "#111318"
        super().__init__(master, text=f"  {icon}  {text}", command=command, bg=self.normal_bg, fg="#9ca3af", font=("Segoe UI Semibold", 11), bd=0, padx=20, pady=12, anchor="w", cursor="hand2", activebackground="#1e293b", activeforeground="white", **kwargs)
        self.bind("<Enter>", lambda e: self.config(bg="#1e293b", fg="white") if not self.is_active else None)
        self.bind("<Leave>", lambda e: self.config(bg=self.normal_bg) if not self.is_active else None)
        self.is_active = False
    def set_active(self, active=True):
        self.is_active = active; self.config(bg=self.active_bg if active else self.normal_bg, fg="white" if active else "#9ca3af")

class ModernAdminApp:
    def __init__(self, root):
        self.root = root; self.root.title("LinhNam Store - Admin Dashboard v2"); self.root.geometry("1500x850")
        self.CLR = {"bg":"#0b0e13", "sidebar":"#111318", "card":"#151922", "border":"#2b3240", "accent":"#2563eb", "accent_lt":"#60a5fa", "fg":"#e5e7eb", "fg_dim":"#9ca3af", "input_bg":"#1e293b", "danger":"#ef4444"}
        self.root.configure(bg=self.CLR["bg"]); self.img_cache = {}
        self.setup_styles()
        try: self.db = AdminDbCore(load_db_config())
        except Exception as e: messagebox.showerror("Error", str(e)); sys.exit(1)
        self.current_page = None; self.setup_layout(); self.show_page("products")

    def setup_styles(self):
        s = ttk.Style(); s.theme_use("clam")
        s.configure("Treeview", background=self.CLR["card"], foreground=self.CLR["fg"], fieldbackground=self.CLR["card"], rowheight=60, borderwidth=0, font=("Segoe UI", 10))
        s.configure("Treeview.Heading", background=self.CLR["sidebar"], foreground=self.CLR["fg"], padding=12, font=("Segoe UI Bold", 10), borderwidth=0)
        s.map("Treeview", background=[("selected", self.CLR["accent"])], foreground=[("selected", "white")])

    def setup_layout(self):
        self.sidebar = tk.Frame(self.root, bg=self.CLR["sidebar"], width=260); self.sidebar.pack(side="left", fill="y"); self.sidebar.pack_propagate(False)
        tk.Label(self.sidebar, text="LINHNAM", fg=self.CLR["accent_lt"], bg=self.CLR["sidebar"], font=("Segoe UI Black", 24), pady=40).pack()
        self.nav_btns = {}
        for k, l, i in [("products", "Sản phẩm", "📦"), ("vouchers", "Mã giảm giá", "🎫"), ("reviews", "Đánh giá", "⭐")]:
            btn = SidebarButton(self.sidebar, l, i, command=lambda x=k: self.show_page(x)); btn.pack(fill="x", padx=15, pady=4); self.nav_btns[k] = btn
        self.main_container = tk.Frame(self.root, bg=self.CLR["bg"]); self.main_container.pack(side="right", fill="both", expand=True)
        self.header = tk.Frame(self.main_container, bg=self.CLR["bg"], pady=25, padx=40); self.header.pack(fill="x")
        self.lbl_page_title = tk.Label(self.header, text="Dashboard", fg="white", bg=self.CLR["bg"], font=("Segoe UI Bold", 26)); self.lbl_page_title.pack(side="left")

    def show_page(self, name):
        if self.current_page: self.current_page.destroy()
        for b in self.nav_btns.values(): b.set_active(False)
        self.nav_btns[name].set_active(True)
        self.lbl_page_title.config(text=self.nav_btns[name]["text"].split("  ")[-1])
        f = tk.Frame(self.main_container, bg=self.CLR["bg"], padx=40); f.pack(fill="both", expand=True); self.current_page = f
        if name == "products": self.render_products()
        elif name == "vouchers": self.render_vouchers()
        elif name == "reviews": self.render_reviews()

    def create_btn(self, master, text, color, cmd, padx=20, pady=10):
        return tk.Button(master, text=text, bg=color, fg="white", font=("Segoe UI Bold", 10), bd=0, padx=padx, pady=pady, cursor="hand2", command=cmd)

    def render_products(self):
        tb = tk.Frame(self.current_page, bg=self.CLR["bg"], pady=10); tb.pack(fill="x")
        self.create_btn(tb, "+ Thêm sản phẩm", self.CLR["accent"], self.on_add_product).pack(side="left")
        self.create_btn(tb, "Sửa thông tin", self.CLR["card"], self.on_edit_product).pack(side="left", padx=15)
        self.create_btn(tb, "Xóa", self.CLR["danger"], self.on_del_product).pack(side="left")
        self.create_btn(tb, "🔄 Làm mới", self.CLR["bg"], self.load_products).pack(side="right")
        card = tk.Frame(self.current_page, bg=self.CLR["card"], highlightthickness=1, highlightbackground=self.CLR["border"]); card.pack(fill="both", expand=True, pady=15)
        cols = ("id", "name", "category", "price", "stock", "createdAt", "variant")
        self.tree = ttk.Treeview(card, columns=cols, show="tree headings")
        self.tree.column("#0", width=80, anchor="center"); self.tree.heading("#0", text="IMG")
        for c in cols: self.tree.heading(c, text=c.upper()); width = 300 if c == "name" else 120; self.tree.column(c, anchor="center", width=width)
        self.tree.pack(fill="both", expand=True); self.load_products()

    def get_thumbnail(self, path_or_url):
        if not path_or_url: return None
        if path_or_url in self.img_cache: return self.img_cache[path_or_url]
        try:
            if path_or_url.startswith("http"):
                headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'}
                resp = requests.get(path_or_url, timeout=5, headers=headers); img = Image.open(BytesIO(resp.content))
            else:
                full_path = os.path.join(BASE_DIR, "web", path_or_url)
                if not os.path.exists(full_path): return None
                img = Image.open(full_path)
            img.thumbnail((50, 50)); photo = ImageTk.PhotoImage(img); self.img_cache[path_or_url] = photo; return photo
        except: return None

    def load_products(self):
        for i in self.tree.get_children(): self.tree.delete(i)
        for r in self.db.get_products(): 
            thumb = self.get_thumbnail(r.get('imageUrl'))
            created_str = r['createdAt'].strftime("%H:%M %d/%m/%Y") if r['createdAt'] else "-"
            item_id = self.tree.insert("", "end", values=(r['id'], r['name'], r['category'], f"{r['price']:,.0f} đ", r['quantity'], created_str, r['variantId']))
            if thumb: self.tree.item(item_id, image=thumb)

    def render_vouchers(self):
        tb = tk.Frame(self.current_page, bg=self.CLR["bg"], pady=10); tb.pack(fill="x")
        self.create_btn(tb, "+ Tạo mã giảm giá", self.CLR["accent"], self.on_add_voucher).pack(side="left")
        self.create_btn(tb, "Xóa", self.CLR["danger"], self.on_del_voucher).pack(side="left", padx=15)
        card = tk.Frame(self.current_page, bg=self.CLR["card"], highlightthickness=1, highlightbackground=self.CLR["border"]); card.pack(fill="both", expand=True, pady=15)
        cols = ("code", "type", "value", "quantity", "exp")
        self.tree_v = ttk.Treeview(card, columns=cols, show="headings")
        for c in cols: self.tree_v.heading(c, text=c.upper()); self.tree_v.column(c, anchor="center")
        self.tree_v.pack(fill="both", expand=True); self.load_vouchers()

    def load_vouchers(self):
        try:
            for i in self.tree_v.get_children(): self.tree_v.delete(i)
            vouchers = self.db.get_vouchers()
            for r in vouchers:
                d_type = r.get('discountType', 'PERCENT')
                d_val = float(r.get('discountValue', 0))
                val_str = f"{d_val}%" if d_type == 'PERCENT' else f"{d_val:,.0f} đ"
                self.tree_v.insert("", "end", values=(r['code'], d_type, val_str, r['quantity'], r['expTime']))
        except Exception as e: print(f"Voucher Load Error: {e}")

    def render_reviews(self):
        tb = tk.Frame(self.current_page, bg=self.CLR["bg"], pady=10); tb.pack(fill="x")
        self.create_btn(tb, "Xóa đánh giá", self.CLR["danger"], self.on_del_review).pack(side="left")
        card = tk.Frame(self.current_page, bg=self.CLR["card"], highlightthickness=1, highlightbackground=self.CLR["border"]); card.pack(fill="both", expand=True, pady=15)
        cols = ("id", "product", "user", "rating", "comment")
        self.tree_r = ttk.Treeview(card, columns=cols, show="headings")
        for c in cols: self.tree_r.heading(c, text=c.upper()); self.tree_r.column(c, anchor="w" if c in ["product", "comment"] else "center", width=400 if c=="comment" else 200)
        self.tree_r.pack(fill="both", expand=True); self.load_reviews()

    def load_reviews(self):
        for i in self.tree_r.get_children(): self.tree_r.delete(i)
        for r in self.db.get_reviews(): self.tree_r.insert("", "end", values=(r['id'], r['productName'], r['reviewerName'], "⭐" * r['rating'], r['comment']))

    def create_dialog(self, title, size="500x750"):
        dlg = tk.Toplevel(self.root); dlg.title(title); dlg.geometry(size); dlg.configure(bg=self.CLR["bg"])
        tk.Label(dlg, text=title.upper(), bg=self.CLR["accent"], fg="white", pady=15, font=("Segoe UI Bold", 12)).pack(fill="x")
        dlg.transient(self.root); dlg.grab_set(); dlg.focus_force(); return dlg

    def create_input(self, master, label):
        tk.Label(master, text=label, bg=self.CLR["bg"], fg="white", font=("Segoe UI Semibold", 10)).pack(pady=(12, 0))
        e = tk.Entry(master, bg=self.CLR["input_bg"], fg="white", font=("Segoe UI", 11), bd=0, insertbackground="white"); e.pack(pady=5, padx=40, fill="x", ipady=8); return e

    def handle_image_select(self, dlg, entry_widget):
        file_path = filedialog.askopenfilename(parent=dlg, filetypes=[("Image Files", "*.jpg *.png *.jpeg *.webp")])
        if file_path:
            dest_dir = os.path.join(BASE_DIR, "web", "assets", "images"); os.makedirs(dest_dir, exist_ok=True)
            file_name = f"up_{uuid.uuid4().hex[:8]}{os.path.splitext(file_path)[1]}"; dest_path = os.path.join(dest_dir, file_name); shutil.copy2(file_path, dest_path); rel_path = f"assets/images/{file_name}"; entry_widget.delete(0, tk.END); entry_widget.insert(0, rel_path)
        dlg.focus_force()

    def on_add_product(self):
        dlg = self.create_dialog("Thêm sản phẩm", "500x750")
        inputs = {k: self.create_input(dlg, l) for l, k in [("ID", "id"), ("Tên", "name"), ("Giá", "price"), ("Kho", "stock"), ("Danh mục", "cat")]}
        tk.Label(dlg, text="Ảnh (URL hoặc từ Folder):", bg=self.CLR["bg"], fg="white", font=("Segoe UI Semibold", 10)).pack(pady=(12, 0))
        row = tk.Frame(dlg, bg=self.CLR["bg"]); row.pack(fill="x", padx=40, pady=5)
        e = tk.Entry(row, bg=self.CLR["input_bg"], fg="white", font=("Segoe UI", 11), bd=0, insertbackground="white"); e.pack(side="left", fill="x", expand=True, ipady=8); inputs['img']=e
        self.create_btn(row, "📂", self.CLR["card"], lambda: self.handle_image_select(dlg, e), padx=10, pady=5).pack(side="right", padx=(5, 0))
        def sv():
            try:
                pid = inputs['id'].get() or f"p_{uuid.uuid4().hex[:6]}"
                if self.db.check_id_exists(pid): messagebox.showerror("Lỗi", "ID sản phẩm đã tồn tại!", parent=dlg); return
                self.db.add_product({"id":pid, "name":inputs['name'].get(), "description":"", "category":inputs['cat'].get()}, {"id":f"pv_{uuid.uuid4().hex[:6]}", "price":float(inputs['price'].get()), "quantity":int(inputs['stock'].get()), "imageUrl":inputs['img'].get()}); self.load_products(); dlg.destroy()
            except Exception as e: messagebox.showerror("Lỗi", str(e), parent=dlg)
        self.create_btn(dlg, "LƯU NGAY", self.CLR["accent"], sv).pack(pady=30, padx=40, fill="x")

    def on_edit_product(self):
        sel = self.tree.selection()
        if not sel: return
        v = self.tree.item(sel[0])['values']
        pid, vid = v[0], v[6]
        with self.db.conn.cursor() as cur: cur.execute("SELECT imageUrl FROM productvariant WHERE id=%s", (vid,)); current_img = cur.fetchone()['imageUrl']
        dlg = self.create_dialog("Sửa thông tin", "500x650")
        inputs = {k: self.create_input(dlg, l) for l, k in [("Tên", "name"), ("Danh mục", "cat"), ("Giá", "price"), ("Số lượng", "stock")]}
        inputs['name'].insert(0, v[1]); inputs['cat'].insert(0, v[2]); inputs['price'].insert(0, str(v[3]).replace(" đ", "").replace(",", "")); inputs['stock'].insert(0, str(v[4]))
        tk.Label(dlg, text="Cập nhật ảnh:", bg=self.CLR["bg"], fg="white", font=("Segoe UI Semibold", 10)).pack(pady=(12, 0))
        row = tk.Frame(dlg, bg=self.CLR["bg"]); row.pack(fill="x", padx=40, pady=5)
        e = tk.Entry(row, bg=self.CLR["input_bg"], fg="white", font=("Segoe UI", 11), bd=0, insertbackground="white"); e.insert(0, current_img); e.pack(side="left", fill="x", expand=True, ipady=8); inputs['img']=e
        self.create_btn(row, "📂", self.CLR["card"], lambda: self.handle_image_select(dlg, e), padx=10, pady=5).pack(side="right", padx=(5, 0))
        def up():
            try: self.db.update_product_and_variant(pid, vid, {"name": inputs['name'].get(), "category": inputs['cat'].get()}, {"price": float(inputs['price'].get()), "quantity": int(inputs['stock'].get()), "imageUrl": inputs['img'].get()}); self.load_products(); dlg.destroy()
            except Exception as e: messagebox.showerror("Lỗi", str(e), parent=dlg)
        self.create_btn(dlg, "CẬP NHẬT", self.CLR["accent"], up).pack(pady=30, padx=40, fill="x")

    def on_add_voucher(self):
        dlg = self.create_dialog("Tạo Voucher", "450x550")
        tk.Label(dlg, text="Loại giảm giá:", bg=self.CLR["bg"], fg="white", font=("Segoe UI Semibold", 10)).pack(pady=(15,0))
        v_type = tk.StringVar(value="PERCENT"); mode_f = tk.Frame(dlg, bg=self.CLR["bg"]); mode_f.pack(pady=10)
        tk.Radiobutton(mode_f, text="Giảm %", variable=v_type, value="PERCENT", bg=self.CLR["bg"], fg="white", selectcolor=self.CLR["sidebar"]).pack(side="left", padx=20)
        tk.Radiobutton(mode_f, text="Tiền mặt", variable=v_type, value="FIXED", bg=self.CLR["bg"], fg="white", selectcolor=self.CLR["sidebar"]).pack(side="left", padx=20)
        inputs = {k: self.create_input(dlg, l) for l, k in [("Mã Voucher", "code"), ("Giá trị", "val"), ("Số lượng dùng", "qty")]}
        def sv():
            try: self.db.add_voucher({"type":v_type.get(), "code":inputs['code'].get(), "val":float(inputs['val'].get()), "qty":int(inputs['qty'].get())}); self.load_vouchers(); dlg.destroy()
            except Exception as e: messagebox.showerror("Lỗi", str(e), parent=dlg)
        self.create_btn(dlg, "TẠO MÃ", self.CLR["accent"], sv).pack(pady=30, padx=40, fill="x")

    def on_del_product(self):
        sel = self.tree.selection()
        if sel:
            pid = self.tree.item(sel[0])['values'][0]
            if messagebox.askyesno("Xác nhận", f"Xóa sản phẩm {pid}?"): self.db.delete_product(pid); self.load_products()

    def on_del_voucher(self):
        sel = self.tree_v.selection()
        if sel:
            code = self.tree_v.item(sel[0])['values'][0]
            with self.db.conn.cursor() as cur:
                cur.execute("SELECT id FROM voucher WHERE code=%s", (code,)); r = cur.fetchone()
                if r: self.db.delete_voucher(r['id']); self.load_vouchers()

    def on_del_review(self):
        sel = self.tree_r.selection()
        if sel: self.db.delete_review(self.tree_r.item(sel[0])['values'][0]); self.load_reviews()

if __name__ == "__main__":
    root = tk.Tk()
    try: from ctypes import windll; windll.shcore.SetProcessDpiAwareness(1)
    except: pass
    ModernAdminApp(root); root.mainloop()
