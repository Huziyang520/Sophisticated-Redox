#!/usr/bin/env python3
"""Deterministic pixel-art generator for Sophisticated Redox.

Everything is rasterised by hand (no anti-aliasing, no image library) so the output is pixel
exact: the GUI atlas must land on an exact 16x16 UV grid or the buttons in the settings page
blit the wrong glyph, and the item textures must be exactly 16x16.

Style direction (locked from the generated reference piece): an isometric copper block with a
bevelled top, corner rivets and a near-black outline; oxidation moves the palette towards
verdigris green, reduction back towards copper, waxing adds amber honeycomb.

Usage:  python tools/gen_art.py
"""

import os
import struct
import sys
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ITEM_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "sophisticated_redox", "textures", "item")
GUI_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "sophisticated_redox", "textures", "gui")

# --------------------------------------------------------------------------- canvas

OUTLINE = (43, 29, 20, 255)


class Canvas:
    def __init__(self, w, h):
        self.w = w
        self.h = h
        self.px = [(0, 0, 0, 0)] * (w * h)

    def put(self, x, y, color):
        if len(color) == 3:
            color = (color[0], color[1], color[2], 255)
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y * self.w + x] = color

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[y * self.w + x]
        return (0, 0, 0, 0)

    def fill_poly(self, pts, color):
        """Scanline fill, even-odd rule, sampling at pixel centres (y + 0.5)."""
        ys = [p[1] for p in pts]
        y0, y1 = int(min(ys)), int(max(ys)) + 1
        for y in range(y0, y1):
            sy = y + 0.5
            xs = []
            n = len(pts)
            for i in range(n):
                ax, ay = pts[i]
                bx, by = pts[(i + 1) % n]
                if (ay <= sy < by) or (by <= sy < ay):
                    xs.append(ax + (sy - ay) * (bx - ax) / (by - ay))
            xs.sort()
            for i in range(0, len(xs) - 1, 2):
                for x in range(int(round(xs[i])), int(round(xs[i + 1]))):
                    self.put(x, y, color)

    def line(self, x0, y0, x1, y1, color):
        x0, y0, x1, y1 = int(round(x0)), int(round(y0)), int(round(x1)), int(round(y1))
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx + dy
        while True:
            self.put(x0, y0, color)
            if x0 == x1 and y0 == y1:
                return
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def rect(self, x, y, w, h, color):
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                self.put(xx, yy, color)


def write_png(path, canvas):
    raw = bytearray()
    for y in range(canvas.h):
        raw.append(0)
        for x in range(canvas.w):
            raw.extend(canvas.px[y * canvas.w + x])
    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", canvas.w, canvas.h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


# --------------------------------------------------------------------------- palette

def shade(rgb, factor):
    return (max(0, min(255, int(rgb[0] * factor))),
            max(0, min(255, int(rgb[1] * factor))),
            max(0, min(255, int(rgb[2] * factor))),
            255)


def blend(a, b, t):
    return (int(a[0] + (b[0] - a[0]) * t),
            int(a[1] + (b[1] - a[1]) * t),
            int(a[2] + (b[2] - a[2]) * t),
            255)


COPPER = (196, 121, 74)
VERDIGRIS = (110, 143, 81)
AMBER = (232, 196, 100)
GOLD = (240, 209, 122)
GOLD_DARK = (122, 84, 26)
DIM = (128, 118, 104)


def face_colors(base):
    """Top face is the brightest, the right face is the darkest - Minecraft's own block convention."""
    return {
        "top": shade(base, 1.18),
        "top_edge": shade(base, 1.45),
        "left": shade(base, 0.88),
        "right": shade(base, 0.60),
    }


# --------------------------------------------------------------------------- shapes

def iso_block(c, x0, y0, w, h, base):
    """An isometric block filling a w x h box at (x0, y0)."""
    mx = x0 + w / 2.0
    top = y0
    mid = y0 + h * 4.0 / 15.0
    low = y0 + h * 8.0 / 15.0
    bot = y0 + h

    top_face = [(mx, top), (x0 + w, mid), (mx, low), (x0, mid)]
    left_face = [(x0, mid), (mx, low), (mx, bot), (x0, bot - (low - mid))]
    right_face = [(mx, low), (x0 + w, mid), (x0 + w, bot - (low - mid)), (mx, bot)]

    col = face_colors(base)
    c.fill_poly(left_face, col["left"])
    c.fill_poly(right_face, col["right"])
    c.fill_poly(top_face, col["top"])

    # silhouette + the separating edges between the three faces
    edges = [(( mx, top), (x0 + w, mid)), ((x0 + w, mid), (x0 + w, bot - (low - mid))),
             ((x0 + w, bot - (low - mid)), (mx, bot)), ((mx, bot), (x0, bot - (low - mid))),
             ((x0, bot - (low - mid)), (x0, mid)), ((x0, mid), (mx, top)),
             ((x0, mid), (mx, low)), ((mx, low), (x0 + w, mid)), ((mx, low), (mx, bot))]
    for (ax, ay), (bx, by) in edges:
        c.line(ax, ay, bx, by, OUTLINE)

    # bevel highlight along the top-left edge of the top face
    steps = max(2, int(w))
    for i in range(steps):
        t = i / float(steps)
        c.put(int(round(x0 + (mx - x0) * t)), int(round(mid + (top - mid) * t)), col["top_edge"])

    # corner rivets, inset by one pixel
    for rx, ry in ((x0 + 1, int(mid)), (int(mx), int(top) + 1),
                   (x0 + w - 2, int(mid)), (int(mx), int(low) - 1)):
        c.put(rx, ry, OUTLINE)


def chevron(c, cx, cy, half, down, color, thick=False):
    """A ^ or v marker, inset-outlined into the face it sits on so it stays crisp at 16x16."""
    points = []
    for i in range(half + 1):
        y = cy + i if down else cy - i
        points.append((cx - i, y))
        points.append((cx + i, y))
    if thick:
        points = [(x, y + t) for (x, y) in points for t in range(2)]
    for (x, y) in points:
        c.put(x, y, color)
    for (x, y) in points:
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            # only darken pixels that belong to the block face - never paint a halo outside the sprite
            if (nx, ny) not in points and c.get(nx, ny)[3] != 0:
                c.put(nx, ny, OUTLINE)


FONT = {
    "I": ["1", "1", "1", "1", "1"],
    "V": ["101", "101", "101", "101", "010"],
}


def roman(c, text, base_x, base_y, scale, color):
    """Draws a Roman numeral with a 1px dark drop shadow, centred on base_x."""
    widths = [len(FONT[ch][0]) for ch in text]
    total = sum(widths) + (len(text) - 1)
    width = total * scale
    x = base_x - width // 2
    for index, ch in enumerate(text):
        glyph = FONT[ch]
        for gy, row in enumerate(glyph):
            for gx, bit in enumerate(row):
                if bit != "1":
                    continue
                for sy in range(scale):
                    for sx in range(scale):
                        px = x + gx * scale + sx
                        py = base_y + gy * scale + sy
                        c.put(px + 1, py + 1, GOLD_DARK)
                        c.put(px, py, color)
        x += (widths[index] + 1) * scale


def hexagon(c, cx, cy, r, fill, edge=None):
    pts = []
    for i in range(6):
        import math
        ang = math.pi / 180 * (60 * i - 30)
        pts.append((cx + r * math.cos(ang), cy + r * math.sin(ang)))
    c.fill_poly(pts, fill)
    if edge:
        for i in range(6):
            ax, ay = pts[i]
            bx, by = pts[(i + 1) % 6]
            c.line(ax, ay, bx, by, edge)


def honeycomb(c, cx, cy, base):
    """A single amber honeycomb cell - the visual shorthand for "waxed"."""
    hexagon(c, cx, cy, 3.2, base, OUTLINE)
    hexagon(c, cx, cy, 1.4, shade(base, 0.72), OUTLINE)


# --------------------------------------------------------------------------- item textures

def draw_item(base, marker, advanced):
    """One upgrade sprite: an isometric copper block plus a direction marker on its top face."""
    c = Canvas(16, 16)
    iso_block(c, 1, 0, 15, 15, base)
    if marker == "up":
        chevron(c, 8, 5, 3, False, GOLD, thick=advanced)
    elif marker == "down":
        chevron(c, 8, 2, 3, True, GOLD, thick=advanced)
    elif marker == "wax":
        honeycomb(c, 8, 4, AMBER)
    return c


def item_texture(name, base, stage, advanced, marker):
    """The five upgrade item textures are NOT generated any more.

    They were hand-made earlier and restored from the author's backup after a previous accidental
    redraw; the mod's style also has to follow the template the base mod ships
    (sophisticatedbackpacks:textures/item/upgrade_base.png), which this generator does not reproduce.
    Writing them from here again would silently clobber the originals, so it is gated behind an
    explicit flag and off by default.
    """
    if "--force-items" not in sys.argv:
        print("skip " + name + ".png (hand-made original; pass --force-items to overwrite anyway)")
        return
    print("OVERWRITING " + name + ".png (--force-items)")
    write_png(os.path.join(ITEM_DIR, name + ".png"), draw_item(base, marker, advanced))


# --------------------------------------------------------------------------- gui atlas

def cell(col, row):
    """Origin of a 16x16 atlas cell - must match the UVs in RedoxButtons."""
    return col * 16, row * 16


def draw_atlas(c):
    """Draws every used cell of the GUI atlas. Must stay in sync with the UVs in RedoxButtons."""

    # row 0: level Ⅰ Ⅱ Ⅲ Ⅳ -------------------------------------------------
    for i, text in enumerate(["I", "II", "III", "IV"]):
        ox, oy = cell(i, 0)
        roman(c, text, ox + 8, oy + 3, 2, GOLD)

    # row 1, col 0: dewax ON - an amber honeycomb cell cracked open, wax running off
    ox, oy = cell(0, 1)
    hexagon(c, ox + 8.5, oy + 7.0, 4.5, AMBER, OUTLINE)
    hexagon(c, ox + 8.5, oy + 7.0, 2.0, shade(AMBER, 0.78), OUTLINE)
    for i in range(4):
        c.put(ox + 6 + i, oy + 3 + i, OUTLINE)
    c.put(ox + 8, oy + 12, shade(AMBER, 1.30))
    c.put(ox + 8, oy + 13, shade(AMBER, 1.30))

    # row 1, col 1: dewax OFF - the same cell, whole and dim
    ox, oy = cell(1, 1)
    hexagon(c, ox + 8.5, oy + 7.5, 4.5, DIM, OUTLINE)
    hexagon(c, ox + 8.5, oy + 7.5, 2.0, shade(DIM, 0.78), OUTLINE)

    # row 1, col 2: shape match ON - a cluster of same-shape blocks in every stage colour
    ox, oy = cell(2, 1)
    iso_block(c, ox + 0, oy + 7, 8, 8, COPPER)
    iso_block(c, ox + 8, oy + 7, 8, 8, VERDIGRIS)
    iso_block(c, ox + 4, oy + 1, 8, 8, AMBER)

    # row 1, col 3: shape match OFF - a single dim block
    ox, oy = cell(3, 1)
    iso_block(c, ox + 3, oy + 4, 10, 10, DIM)


def atlas():
    c = Canvas(256, 256)
    draw_atlas(c)
    write_png(os.path.join(GUI_DIR, "redox_icons.png"), c)


def dump(canvas, cols, rows, label):
    """ASCII preview, so a misplaced glyph is obvious without eyeballing a PNG."""
    ramp = " .:-=+*#%@"
    print("---- " + label + " ----")
    for cy in range(rows):
        for y in range(16):
            line = ""
            for cx in range(cols):
                for x in range(16):
                    r, g, b, a = canvas.get(cx * 16 + x, cy * 16 + y)
                    if a == 0:
                        line += " "
                    else:
                        lum = (r * 30 + g * 59 + b * 11) // 100
                        line += ramp[min(len(ramp) - 1, lum * len(ramp) // 256)]
            print(line)
        print("." * (16 * cols))


def contact_sheet(cells, cols, scale, path):
    """Upscaled nearest-neighbour contact sheet: `cells` is a list of 16x16 Canvases."""
    sheet = Canvas(cols * 16 * scale, ((len(cells) + cols - 1) // cols) * 16 * scale)
    for index, src in enumerate(cells):
        ox = (index % cols) * 16 * scale
        oy = (index // cols) * 16 * scale
        for y in range(16):
            for x in range(16):
                color = src.get(x, y)
                for sy in range(scale):
                    for sx in range(scale):
                        sheet.put(ox + x * scale + sx, oy + y * scale + sy, color)
    write_png(path, sheet)


def main():
    # basic upgrades: one step, the block is caught mid-transition
    item_texture("oxidation_upgrade", blend(COPPER, VERDIGRIS, 0.45), 0, False, "up")
    item_texture("reduction_upgrade", blend(VERDIGRIS, COPPER, 0.45), 0, False, "down")
    # advanced upgrades: further along, gold rim, double marker
    item_texture("advanced_oxidation_upgrade", blend(COPPER, VERDIGRIS, 0.85), 0, True, "up")
    item_texture("advanced_reduction_upgrade", blend(VERDIGRIS, COPPER, 0.85), 0, True, "down")
    item_texture("waxing_upgrade", COPPER, 0, False, "wax")
    atlas()
    print("art written")

    if "--preview" in sys.argv:
        scale = 10
        art_dir = os.path.join(ROOT, "_ref", "art")
        names = ["oxidation_upgrade", "advanced_oxidation_upgrade", "reduction_upgrade",
                 "advanced_reduction_upgrade", "waxing_upgrade"]
        item_cells = []
        for name in names:
            base, marker, advanced = {
                "oxidation_upgrade": (blend(COPPER, VERDIGRIS, 0.45), "up", False),
                "advanced_oxidation_upgrade": (blend(COPPER, VERDIGRIS, 0.85), "up", True),
                "reduction_upgrade": (blend(VERDIGRIS, COPPER, 0.45), "down", False),
                "advanced_reduction_upgrade": (blend(VERDIGRIS, COPPER, 0.85), "down", True),
                "waxing_upgrade": (COPPER, "wax", False),
            }[name]
            item_cells.append(draw_item(base, marker, advanced))
        contact_sheet(item_cells, 5, scale, os.path.join(art_dir, "preview-items.png"))

        atlas_cells = []
        for cc, cr in [(0, 0), (1, 0), (2, 0), (3, 0), (0, 1), (1, 1), (2, 1), (3, 1)]:
            c = Canvas(16, 16)
            ox, oy = cell(cc, cr)
            # re-render the cell content at the origin of a standalone 16x16 canvas
            shifted = Canvas(256, 256)
            draw_atlas(shifted)
            for y in range(16):
                for x in range(16):
                    c.put(x, y, shifted.get(ox + x, oy + y))
            atlas_cells.append(c)
        contact_sheet(atlas_cells, 4, scale, os.path.join(art_dir, "preview-atlas.png"))
        print("previews written")

    if "--dump" in sys.argv:
        dump_atlas(Canvas(64, 48))


def dump_atlas(canvas):
    for i, text in enumerate(["I", "II", "III", "IV"]):
        ox, oy = cell(i, 0)
        roman(canvas, text, ox + 8, oy + 3, 2, GOLD)
    ox, oy = cell(0, 1)
    hexagon(canvas, ox + 8.5, oy + 7.0, 4.5, AMBER, OUTLINE)
    hexagon(canvas, ox + 8.5, oy + 7.0, 2.0, shade(AMBER, 0.78), OUTLINE)
    for i in range(4):
        canvas.put(ox + 6 + i, oy + 3 + i, OUTLINE)
    canvas.put(ox + 8, oy + 12, shade(AMBER, 1.30))
    canvas.put(ox + 8, oy + 13, shade(AMBER, 1.30))
    ox, oy = cell(1, 1)
    hexagon(canvas, ox + 8.5, oy + 7.5, 4.5, DIM, OUTLINE)
    hexagon(canvas, ox + 8.5, oy + 7.5, 2.0, shade(DIM, 0.78), OUTLINE)
    ox, oy = cell(2, 1)
    iso_block(canvas, ox + 0, oy + 6, 9, 9, COPPER)
    iso_block(canvas, ox + 7, oy + 6, 9, 9, VERDIGRIS)
    iso_block(canvas, ox + 3, oy + 0, 9, 9, AMBER)
    ox, oy = cell(3, 1)
    iso_block(canvas, ox + 3, oy + 4, 10, 10, DIM)
    dump(canvas, 4, 3, "atlas 4x3 cells")


if __name__ == "__main__":
    main()
