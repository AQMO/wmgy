# -*- coding: utf-8 -*-
"""裁掉各效果图底部留白并纵向拼接为一张长图"""
import sys
from PIL import Image

SRC = r"D:\dev\orchard-platform\docs\mockups\png"
files = [
    ("01-admin-dashboard.png", u"后台 PC 端 · 数据概览 / 商家管理"),
    ("02-admin-monitor.png", u"后台 PC 端 · 聊天监控 / 流量监控"),
    ("03-mini-home-product.png", u"小程序 · 首页 / 商品详情"),
    ("04-mini-cart-confirm.png", u"小程序 · 购物车 / 确认订单"),
]

def trim(im, bg=(255, 255, 255), tol=6, pad=24):
    """裁掉四周纯色留白"""
    rgb = im.convert("RGB")
    w, h = rgb.size
    px = rgb.load()
    def row_blank(y):
        return all(abs(px[x, y][i] - bg[i]) <= tol for x in range(0, w, 4) for i in range(3))
    def col_blank(x):
        return all(abs(px[x, y][i] - bg[i]) <= tol for y in range(0, h, 4) for i in range(3))
    top, bot, left, right = 0, h - 1, 0, w - 1
    while top < bot and row_blank(top): top += 1
    while bot > top and row_blank(bot): bot -= 1
    while left < right and col_blank(left): left += 1
    while right > left and col_blank(right): right -= 1
    return rgb.crop((max(0, left - pad), max(0, top - pad),
                     min(w, right + 1 + pad), min(h, bot + 1 + pad)))

images = []
for fname, _ in files:
    im = trim(Image.open(SRC + "\\" + fname))
    print("trimmed:", fname, im.size)
    images.append(im)

# 统一宽度为最大宽度，窄的按比例放大太糊——改为居中放在白底上
max_w = max(im.width for im in images)
gap, header_h = 40, 56
total_h = sum(im.height for im in images) + gap * (len(images) - 1) + header_h * len(images)
canvas = Image.new("RGB", (max_w, total_h), (255, 255, 255))

try:
    from PIL import ImageDraw, ImageFont
    font = ImageFont.truetype("msyh.ttc", 26)
except Exception:
    font = None

from PIL import ImageDraw
draw = ImageDraw.Draw(canvas)
y = 0
for (fname, title), im in zip(files, images):
    if font:
        draw.text((24, y + 14), title, fill=(59, 109, 17), font=font)
    y += header_h
    x = (max_w - im.width) // 2
    canvas.paste(im, (x, y))
    y += im.height + gap

out = SRC + u"\\00-效果图汇总长图.png"
canvas.save(out, optimize=True)
print("saved:", out, canvas.size)
