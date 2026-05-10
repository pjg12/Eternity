from __future__ import annotations

import html
from pathlib import Path
import sys

import station_data


DEFAULT_OUTPUT_FILE = Path("medical_biology_explicit_tiles.svg")
DEFAULT_SECTION_NAME = "Medical & Biology"
CELL_SIZE = 36
GRID_STROKE = "#1f2937"
INVALID_FILL = "#111827"
EMPTY_FILL = "#dbeafe"
SEEDED_FILL = "#10b981"
TEXT_DARK = "#0f172a"
TEXT_LIGHT = "#f8fafc"
BG_FILL = "#f8fafc"


def svg_text(x: int, y: int, value: str, size: int = 14, fill: str = TEXT_DARK, anchor: str = "start") -> str:
    return (
        f'<text x="{x}" y="{y}" font-family="Segoe UI, Arial, sans-serif" '
        f'font-size="{size}" fill="{fill}" text-anchor="{anchor}">{html.escape(value)}</text>'
    )


def parse_args() -> tuple[str, Path, set[str]]:
    section_name = DEFAULT_SECTION_NAME
    output_file = DEFAULT_OUTPUT_FILE
    ignored_templates: set[str] = set()
    if len(sys.argv) >= 2:
        section_name = sys.argv[1]
    if len(sys.argv) >= 3:
        output_file = Path(sys.argv[2])
    if len(sys.argv) >= 4:
        ignored_templates = {value.strip() for value in sys.argv[3].split(",") if value.strip()}
    return section_name, output_file, ignored_templates


def main() -> None:
    section_name, output_file, ignored_templates = parse_args()
    station = station_data.create_demo_station()
    section = next(section for floor in station.floors for section in floor.sections if section.name == section_name)

    invalid_coordinates = set(section.invalid_coordinates)
    seeded_coordinates = {
        coordinate
        for building in section.buildings
        if building.template_id not in ignored_templates
        for coordinate in building.coordinates
    }

    rows = section.grid_rows or len(section.occupancy_grid)
    columns = section.grid_columns or max(len(row) for row in section.occupancy_grid)
    valid_coordinates = {
        (row_index, column_index)
        for row_index in range(rows)
        for column_index in range(columns)
        if (row_index, column_index) not in invalid_coordinates
    }

    margin_left = 84
    extra_header_height = 20 if ignored_templates else 0
    margin_top = 112 + extra_header_height
    legend_top = margin_top + rows * CELL_SIZE + 36
    width = margin_left + columns * CELL_SIZE + 48
    height = legend_top + 130

    parts: list[str] = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        f'<rect width="{width}" height="{height}" fill="{BG_FILL}" />',
        svg_text(margin_left, 44, f"{section_name} Explicit Building Tiles", size=28),
        svg_text(margin_left, 70, f"Valid tiles: {len(valid_coordinates)}   Explicitly seeded: {len(seeded_coordinates)}", size=16),
    ]
    if ignored_templates:
        ignored_text = ", ".join(sorted(ignored_templates))
        parts.append(svg_text(margin_left, 92, f"Treated as implicit fill: {ignored_text}", size=14))

    for column_index in range(columns):
        x = margin_left + column_index * CELL_SIZE + CELL_SIZE // 2
        parts.append(svg_text(x, margin_top - 16, str(column_index), size=14, anchor="middle"))

    for row_index in range(rows):
        y = margin_top + row_index * CELL_SIZE + CELL_SIZE // 2 + 5
        parts.append(svg_text(margin_left - 16, y, str(row_index), size=14, anchor="end"))

    for row_index in range(rows):
        for column_index in range(columns):
            coordinate = (row_index, column_index)
            x = margin_left + column_index * CELL_SIZE
            y = margin_top + row_index * CELL_SIZE
            if coordinate in invalid_coordinates:
                fill = INVALID_FILL
                label = ""
                text_fill = TEXT_LIGHT
            elif coordinate in seeded_coordinates:
                fill = SEEDED_FILL
                label = "X"
                text_fill = TEXT_LIGHT
            else:
                fill = EMPTY_FILL
                label = ""
                text_fill = TEXT_DARK
            parts.append(
                f'<rect x="{x}" y="{y}" width="{CELL_SIZE}" height="{CELL_SIZE}" '
                f'fill="{fill}" stroke="{GRID_STROKE}" stroke-width="1" />'
            )
            if label:
                parts.append(svg_text(x + CELL_SIZE // 2, y + CELL_SIZE // 2 + 5, label, size=18, fill=text_fill, anchor="middle"))

    legend_x = margin_left
    legend_items = [
        (INVALID_FILL, "Invalid tile"),
        (EMPTY_FILL, "Valid tile without explicit building seed"),
        (SEEDED_FILL, "Valid tile with explicit building seed"),
    ]
    for index, (fill, label) in enumerate(legend_items):
        y = legend_top + index * 28
        parts.append(f'<rect x="{legend_x}" y="{y - 14}" width="18" height="18" fill="{fill}" stroke="{GRID_STROKE}" stroke-width="1" />')
        parts.append(svg_text(legend_x + 28, y, label, size=14))

    parts.append("</svg>")
    output_file.write_text("\n".join(parts), encoding="utf-8")


if __name__ == "__main__":
    main()
