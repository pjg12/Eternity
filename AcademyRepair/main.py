from __future__ import annotations

import math

import pygame
from models import BUILDING_TILE_COST, Building, BuildingTemplate, Floor, GameClock, Section, Station, TileCoordinate
from save_system import SAVE_PATH, load_game, save_game
from station_data import create_building_templates, create_demo_station


SCREEN_WIDTH = 1280
SCREEN_HEIGHT = 720
SCREEN_SIZE = (SCREEN_WIDTH, SCREEN_HEIGHT)
FPS = 60

BACKGROUND = (10, 16, 24)
PANEL = (20, 28, 40)
PANEL_ALT = (26, 36, 52)
PANEL_ACTIVE = (41, 68, 96)
GRID_LINE = (44, 62, 82)
TEXT = (228, 235, 244)
TEXT_MUTED = (158, 173, 189)
ACCENT = (114, 235, 201)
WARNING = (255, 187, 92)

LEFT_PANEL_WIDTH = 240
RIGHT_PANEL_WIDTH = 320
HEADER_HEIGHT = 88
SECTION_GAP = 16
BUILD_PAGE_SIZE = 8
EXPLORE_ACTION_DURATION_MS = 3000
WORKER_ICON_PATH = "assets/icons/Worker.png"
TECHNICIAN_ICON_PATH = "assets/icons/Technician.png"
HERO_ICON_PATH = "assets/icons/Hero.png"
POWER_ICON_PATH = "assets/icons/Power.png"
AIR_ICON_PATH = "assets/icons/Air.png"
WATER_ICON_PATH = "assets/icons/Water.png"
DISPOSAL_ICON_PATH = "assets/icons/Trash.png"
IMAGE_CACHE: dict[tuple[str, tuple[int, int]], pygame.Surface] = {}

MAIN_LEVEL_INNER_SECTION_NAMES = [
    "Staff Residential 1",
    "Residential 1",
    "Student Residential 1",
    "Culinary",
    "Student Residential 2",
    "Residential 2",
    "Staff Residential 2",
    "Administration",
]

MAIN_LEVEL_OUTER_SECTION_NAMES = [
    "Engineering & Science",
    "Medical & Biology",
    "Athletics & Training",
    "Shopping & Recreation",
    "History & Geography",
    "Language & Communication",
    "Art & Philosophy",
    "Math & Business",
]

SECURITY_LEVEL_INNER_SECTION_NAME = "DefSec"
SECURITY_LEVEL_OUTER_SECTION_NAME = "Aura Forge"
DOCK_SMALL_SECTION_NAMES = [f"Shelf {index}" for index in range(1, 31)] + [
    "Dock A",
    "Dock B",
    "Dock C",
    "Dock D",
]
DOCK_LARGE_SECTION_NAME = "Dock Omega"
FLOOR_IMAGE_ZOOM = {
    "Main Level": 1.7,
    "Nature Level": 1.9,
    "Utility Level": 1.9,
    "Security Level": 2.2,
    "Docks": 2.4,
}

SEGMENT_COLORS = [
    (220, 180, 220),
    (240, 220, 190),
    (250, 180, 190),
    (240, 240, 200),
    (180, 200, 240),
    (180, 240, 180),
    (210, 210, 210),
    (180, 180, 200),
]


def load_scaled_image(path: str, size: tuple[int, int]) -> pygame.Surface | None:
    cache_key = (path, size)
    if cache_key in IMAGE_CACHE:
        return IMAGE_CACHE[cache_key]

    try:
        image = pygame.image.load(path).convert_alpha()
    except (pygame.error, FileNotFoundError):
        return None

    scaled_image = pygame.transform.smoothscale(image, size)
    IMAGE_CACHE[cache_key] = scaled_image
    return scaled_image


def draw_radial_segment(
    surface: pygame.Surface,
    color: tuple[int, int, int],
    center: tuple[int, int],
    inner_radius: int,
    outer_radius: int,
    start_angle: float,
    end_angle: float,
    point_count: int = 18,
) -> list[tuple[float, float]]:
    points: list[tuple[float, float]] = []
    for index in range(point_count + 1):
        angle = math.radians(start_angle + (end_angle - start_angle) * index / point_count)
        points.append(
            (
                center[0] + math.cos(angle) * outer_radius,
                center[1] + math.sin(angle) * outer_radius,
            )
        )

    if inner_radius > 0:
        for index in range(point_count, -1, -1):
            angle = math.radians(start_angle + (end_angle - start_angle) * index / point_count)
            points.append(
                (
                    center[0] + math.cos(angle) * inner_radius,
                    center[1] + math.sin(angle) * inner_radius,
                )
            )
    else:
        points.append(center)

    pygame.draw.polygon(surface, color, points)
    return points


def get_central_panel_bounds() -> tuple[int, int, int, int]:
    content_left = LEFT_PANEL_WIDTH + 24
    content_top = HEADER_HEIGHT + 28
    content_width = SCREEN_WIDTH - LEFT_PANEL_WIDTH - RIGHT_PANEL_WIDTH - 48
    content_height = SCREEN_HEIGHT - content_top - 24
    return content_left, content_top, content_width, content_height


def get_central_panel_center() -> tuple[int, int]:
    content_left, content_top, content_width, content_height = get_central_panel_bounds()
    return (
        content_left + content_width // 2,
        content_top + content_height // 2,
    )


def get_map_size() -> tuple[int, int]:
    _content_left, _content_top, content_width, content_height = get_central_panel_bounds()
    return content_width, content_height


def get_map_local_center() -> tuple[int, int]:
    content_width, content_height = get_map_size()
    return content_width // 2, content_height // 2


def get_floor_section_index_by_name(floor: Floor, section_name: str) -> int:
    for index, section in enumerate(floor.sections):
        if section.name == section_name:
            return index
    raise ValueError(f"Section {section_name} not found on {floor.name}.")


def get_main_level_segment_indices(floor: Floor) -> list[int]:
    indices: list[int] = []
    for section_name in MAIN_LEVEL_OUTER_SECTION_NAMES:
        indices.append(get_floor_section_index_by_name(floor, section_name))
    for section_name in MAIN_LEVEL_INNER_SECTION_NAMES:
        indices.append(get_floor_section_index_by_name(floor, section_name))
    return indices


def get_main_level_selected_section_index(floor: Floor, mouse_pos: tuple[int, int]) -> int | None:
    center_x, center_y = get_map_local_center()
    dx = mouse_pos[0] - center_x
    dy = mouse_pos[1] - center_y
    distance = math.hypot(dx, dy)
    if distance > 240 or distance == 0:
        return None

    angle = math.degrees(math.atan2(dy, dx))
    relative_angle = (angle + 90) % 360
    wedge_index = int(relative_angle // 45) % 8

    section_names = MAIN_LEVEL_INNER_SECTION_NAMES if distance <= 160 else MAIN_LEVEL_OUTER_SECTION_NAMES
    section_name = section_names[wedge_index]
    return get_floor_section_index_by_name(floor, section_name)


def get_security_level_selected_section_index(floor: Floor, mouse_pos: tuple[int, int]) -> int | None:
    center_x, center_y = get_map_local_center()
    dx = mouse_pos[0] - center_x
    dy = mouse_pos[1] - center_y
    distance = math.hypot(dx, dy)
    if distance > 100:
        return None
    if distance <= 50:
        return get_floor_section_index_by_name(floor, SECURITY_LEVEL_INNER_SECTION_NAME)
    return get_floor_section_index_by_name(floor, SECURITY_LEVEL_OUTER_SECTION_NAME)


def get_docks_section_rects(floor: Floor) -> dict[int, pygame.Rect]:
    circle_center = get_map_local_center()
    rects: dict[int, pygame.Rect] = {}

    for index, section_name in enumerate(DOCK_SMALL_SECTION_NAMES):
        section_index = get_floor_section_index_by_name(floor, section_name)
        rects[section_index] = pygame.Rect(
            circle_center[0] - 50,
            circle_center[1] - 255 + index * 15,
            100,
            15,
        )

    omega_index = get_floor_section_index_by_name(floor, DOCK_LARGE_SECTION_NAME)
    rects[omega_index] = pygame.Rect(circle_center[0] - 100, circle_center[1] + 255, 200, 30)
    return rects


def get_docks_selected_section_index(floor: Floor, mouse_pos: tuple[int, int]) -> int | None:
    for section_index, rect in get_docks_section_rects(floor).items():
        if rect.collidepoint(mouse_pos):
            return section_index
    return None


def get_quadrant_selected_section_index(
    floor: Floor,
    mouse_pos: tuple[int, int],
    radius: int,
) -> int | None:
    center_x, center_y = get_map_local_center()
    dx = mouse_pos[0] - center_x
    dy = mouse_pos[1] - center_y
    distance = math.hypot(dx, dy)
    if distance > radius:
        return None

    angle = math.degrees(math.atan2(dy, dx))
    relative_angle = (angle + 90) % 360
    quadrant_index = int(relative_angle // 90) % 4
    return quadrant_index if quadrant_index < len(floor.sections) else None


def get_floor_map_selected_section_index(floor: Floor, mouse_pos: tuple[int, int]) -> int | None:
    if floor.name == "Main Level":
        return get_main_level_selected_section_index(floor, mouse_pos)
    if floor.name == "Security Level":
        return get_security_level_selected_section_index(floor, mouse_pos)
    if floor.name == "Docks":
        return get_docks_selected_section_index(floor, mouse_pos)
    if floor.name == "Nature Level":
        return get_quadrant_selected_section_index(floor, mouse_pos, 200)
    if floor.name == "Utility Level":
        return get_quadrant_selected_section_index(floor, mouse_pos, 150)
    return None


def get_main_level_focus_point(floor: Floor, selected_section_index: int) -> tuple[int, int]:
    center_x, center_y = get_map_local_center()
    if selected_section_index in [get_floor_section_index_by_name(floor, name) for name in MAIN_LEVEL_INNER_SECTION_NAMES]:
        ring_names = MAIN_LEVEL_INNER_SECTION_NAMES
        radius = 90
    else:
        ring_names = MAIN_LEVEL_OUTER_SECTION_NAMES
        radius = 200
    section_name = floor.sections[selected_section_index].name
    wedge_index = ring_names.index(section_name)
    angle = math.radians(-67.5 + wedge_index * 45)
    return (
        int(center_x + math.cos(angle) * radius),
        int(center_y + math.sin(angle) * radius),
    )


def get_quadrant_focus_point(selected_section_index: int, radius: int) -> tuple[int, int]:
    center_x, center_y = get_map_local_center()
    angle = math.radians(-45 + selected_section_index * 90)
    focus_radius = radius * 0.45
    return (
        int(center_x + math.cos(angle) * focus_radius),
        int(center_y + math.sin(angle) * focus_radius),
    )


def get_docks_focus_point(floor: Floor, selected_section_index: int) -> tuple[int, int]:
    rects = get_docks_section_rects(floor)
    return rects[selected_section_index].center


def get_floor_map_focus_point(floor: Floor, selected_section_index: int | None) -> tuple[int, int]:
    if selected_section_index is None:
        return get_map_local_center()
    if floor.name == "Main Level":
        return get_main_level_focus_point(floor, selected_section_index)
    if floor.name == "Nature Level":
        return get_quadrant_focus_point(selected_section_index, 200)
    if floor.name == "Utility Level":
        return get_quadrant_focus_point(selected_section_index, 150)
    if floor.name == "Security Level":
        center_x, center_y = get_map_local_center()
        if floor.sections[selected_section_index].name == SECURITY_LEVEL_INNER_SECTION_NAME:
            return center_x, center_y
        return center_x + 75, center_y
    if floor.name == "Docks":
        return get_docks_focus_point(floor, selected_section_index)
    return get_map_local_center()


def get_floor_map_zoom(floor: Floor, selected_section_index: int | None) -> float:
    if selected_section_index is None:
        return 1.0
    return FLOOR_IMAGE_ZOOM.get(floor.name, 1.0)


def map_screen_to_local_point(
    floor: Floor,
    selected_section_index: int | None,
    mouse_pos: tuple[int, int],
) -> tuple[float, float] | None:
    content_left, content_top, content_width, content_height = get_central_panel_bounds()
    if not pygame.Rect(content_left, content_top, content_width, content_height).collidepoint(mouse_pos):
        return None

    zoom = get_floor_map_zoom(floor, selected_section_index)
    focus_x, focus_y = get_floor_map_focus_point(floor, selected_section_index)
    local_x = (mouse_pos[0] - content_left - content_width / 2) / zoom + focus_x
    local_y = (mouse_pos[1] - content_top - content_height / 2) / zoom + focus_y
    return local_x, local_y


def split_map_label(text: str, max_chars: int) -> list[str]:
    words = text.split()
    lines: list[str] = []
    current_line = ""
    for word in words:
        candidate = word if not current_line else f"{current_line} {word}"
        if len(candidate) <= max_chars:
            current_line = candidate
        else:
            if current_line:
                lines.append(current_line)
            current_line = word
    if current_line:
        lines.append(current_line)
    return lines


def draw_map_label(
    surface: pygame.Surface,
    font: pygame.font.Font,
    text: str,
    center: tuple[int, int],
    max_chars: int,
    color: tuple[int, int, int] = BACKGROUND,
) -> None:
    lines = split_map_label(text, max_chars)
    line_height = font.get_linesize()
    total_height = line_height * len(lines)
    start_y = center[1] - total_height // 2
    for index, line in enumerate(lines):
        rendered = font.render(line, True, color)
        rect = rendered.get_rect(center=(center[0], start_y + index * line_height + line_height // 2))
        surface.blit(rendered, rect)


def draw_floor_map_labels(
    surface: pygame.Surface,
    floor: Floor,
    fonts: dict[str, pygame.font.Font],
) -> None:
    circle_center = get_map_local_center()

    if floor.name == "Main Level":
        for section_name in MAIN_LEVEL_INNER_SECTION_NAMES:
            section_index = get_floor_section_index_by_name(floor, section_name)
            draw_map_label(surface, fonts["map"], floor.sections[section_index].name, get_main_level_focus_point(floor, section_index), 12)
        for section_name in MAIN_LEVEL_OUTER_SECTION_NAMES:
            section_index = get_floor_section_index_by_name(floor, section_name)
            draw_map_label(surface, fonts["map_small"], floor.sections[section_index].name, get_main_level_focus_point(floor, section_index), 14)
        return

    if floor.name == "Nature Level":
        for index, section in enumerate(floor.sections):
            draw_map_label(surface, fonts["map"], section.name, get_quadrant_focus_point(index, 200), 12)
        return

    if floor.name == "Utility Level":
        for index, section in enumerate(floor.sections):
            draw_map_label(surface, fonts["map"], section.name, get_quadrant_focus_point(index, 150), 12)
        return

    if floor.name == "Security Level":
        inner_index = get_floor_section_index_by_name(floor, SECURITY_LEVEL_INNER_SECTION_NAME)
        outer_index = get_floor_section_index_by_name(floor, SECURITY_LEVEL_OUTER_SECTION_NAME)
        draw_map_label(surface, fonts["map"], floor.sections[inner_index].name, circle_center, 10)
        draw_map_label(surface, fonts["map"], floor.sections[outer_index].name, (circle_center[0], circle_center[1] - 76), 10)
        return

    if floor.name == "Docks":
        for section_index, rect in get_docks_section_rects(floor).items():
            font_key = "map_tiny" if rect.height <= 18 else "map_small"
            draw_map_label(surface, fonts[font_key], floor.sections[section_index].name, rect.center, 12)


def get_selected_section_tile_color(
    floor: Floor,
    selected_section_index: int,
) -> tuple[int, int, int]:
    section_name = floor.sections[selected_section_index].name

    if floor.name == "Main Level":
        if section_name in MAIN_LEVEL_OUTER_SECTION_NAMES:
            return SEGMENT_COLORS[MAIN_LEVEL_OUTER_SECTION_NAMES.index(section_name)]
        if section_name in MAIN_LEVEL_INNER_SECTION_NAMES:
            return SEGMENT_COLORS[MAIN_LEVEL_INNER_SECTION_NAMES.index(section_name)]

    if floor.name == "Nature Level":
        return SEGMENT_COLORS[selected_section_index + 3]

    if floor.name == "Utility Level":
        return SEGMENT_COLORS[selected_section_index + 2]

    if floor.name == "Security Level":
        if section_name == SECURITY_LEVEL_INNER_SECTION_NAME:
            return (250, 180, 190)
        return (180, 180, 200)

    if floor.name == "Docks":
        if section_name == DOCK_LARGE_SECTION_NAME:
            return TEXT
        return SEGMENT_COLORS[DOCK_SMALL_SECTION_NAMES.index(section_name) % len(SEGMENT_COLORS)]

    return TEXT


def render_floor_map(
    surface: pygame.Surface,
    floor: Floor,
    selected_section_index: int | None,
    fonts: dict[str, pygame.font.Font],
) -> None:
    circle_center = get_map_local_center()
    if floor.name == "Docks":
        dock_rects = get_docks_section_rects(floor)
        for index, section_name in enumerate(DOCK_SMALL_SECTION_NAMES):
            section_index = get_floor_section_index_by_name(floor, section_name)
            rect = dock_rects[section_index]
            color = SEGMENT_COLORS[index % 8]
            if section_index == selected_section_index:
                color = tuple(min(channel + 28, 255) for channel in color)
            pygame.draw.rect(surface, color, rect)
            pygame.draw.rect(surface, BACKGROUND, rect, 1)

        omega_index = get_floor_section_index_by_name(floor, DOCK_LARGE_SECTION_NAME)
        omega_rect = dock_rects[omega_index]
        omega_color = TEXT if omega_index != selected_section_index else (255, 255, 255)
        pygame.draw.rect(surface, omega_color, omega_rect)
        pygame.draw.rect(surface, BACKGROUND, omega_rect, 1)
        if selected_section_index is None:
            draw_floor_map_labels(surface, floor, fonts)
        return

    if floor.name == "Security Level":
        inner_index = get_floor_section_index_by_name(floor, SECURITY_LEVEL_INNER_SECTION_NAME)
        outer_index = get_floor_section_index_by_name(floor, SECURITY_LEVEL_OUTER_SECTION_NAME)
        inner_color = (250, 180, 190)
        outer_color = (180, 180, 200)
        if selected_section_index == inner_index:
            inner_color = tuple(min(channel + 20, 255) for channel in inner_color)
        if selected_section_index == outer_index:
            outer_color = tuple(min(channel + 20, 255) for channel in outer_color)

        pygame.draw.circle(surface, inner_color, circle_center, 50)
        pygame.draw.circle(surface, BACKGROUND, circle_center, 50, 3)
        draw_radial_segment(surface, outer_color, circle_center, 50, 100, 0, 360)
        if selected_section_index is None:
            draw_floor_map_labels(surface, floor, fonts)
        return

    if floor.name == "Main Level":
        angle_step = 45
        start_angle = -90
        segment_indices = get_main_level_segment_indices(floor)
        for index in range(8):
            segment_start = start_angle + index * angle_step
            segment_end = segment_start + angle_step
            outer_section_index = segment_indices[index]
            inner_section_index = segment_indices[index + 8]
            outer_color = SEGMENT_COLORS[index]
            inner_color = SEGMENT_COLORS[index]
            if outer_section_index == selected_section_index:
                outer_color = tuple(min(channel + 28, 255) for channel in outer_color)
            if inner_section_index == selected_section_index:
                inner_color = tuple(min(channel + 28, 255) for channel in inner_color)

            draw_radial_segment(surface, outer_color, circle_center, 160, 240, segment_start, segment_end)
            draw_radial_segment(surface, inner_color, circle_center, 0, 160, segment_start, segment_end)
        pygame.draw.circle(surface, BACKGROUND, circle_center, 160, 3)
        pygame.draw.line(surface, BACKGROUND, (circle_center[0] - 240, circle_center[1]), (circle_center[0] + 240, circle_center[1]), 3)
        pygame.draw.line(surface, BACKGROUND, (circle_center[0], circle_center[1] - 240), (circle_center[0], circle_center[1] + 240), 3)
        diagonal_offset = int(240 / 1.41421356237)
        pygame.draw.line(
            surface,
            BACKGROUND,
            (circle_center[0] - diagonal_offset, circle_center[1] - diagonal_offset),
            (circle_center[0] + diagonal_offset, circle_center[1] + diagonal_offset),
            3,
        )
        pygame.draw.line(
            surface,
            BACKGROUND,
            (circle_center[0] - diagonal_offset, circle_center[1] + diagonal_offset),
            (circle_center[0] + diagonal_offset, circle_center[1] - diagonal_offset),
            3,
        )
        if selected_section_index is None:
            draw_floor_map_labels(surface, floor, fonts)
        return

    radius_by_floor = {
        "Nature Level": 200,
        "Utility Level": 150,
    }
    i_by_floor = {
        "Nature Level": 3,
        "Utility Level": 2,
    }

    floor_radius = radius_by_floor.get(floor.name, 50)
    color_offset = i_by_floor.get(floor.name, 0)
    angle_step = 90
    start_angle = -90
    for index in range(4):
        segment_start = start_angle + index * angle_step
        segment_end = segment_start + angle_step
        color = SEGMENT_COLORS[index + color_offset]
        if index == selected_section_index:
            color = tuple(min(channel + 28, 255) for channel in color)
        draw_radial_segment(surface, color, circle_center, 0, floor_radius, segment_start, segment_end)
    pygame.draw.line(surface, BACKGROUND, (circle_center[0] - floor_radius, circle_center[1]), (circle_center[0] + floor_radius, circle_center[1]), 3)
    pygame.draw.line(surface, BACKGROUND, (circle_center[0], circle_center[1] - floor_radius), (circle_center[0], circle_center[1] + floor_radius), 3)
    if selected_section_index is None:
        draw_floor_map_labels(surface, floor, fonts)


def blit_zoomed_map(
    surface: pygame.Surface,
    map_surface: pygame.Surface,
    floor: Floor,
    selected_section_index: int | None,
) -> None:
    content_left, content_top, content_width, content_height = get_central_panel_bounds()
    panel_rect = pygame.Rect(content_left, content_top, content_width, content_height)
    zoom = get_floor_map_zoom(floor, selected_section_index)
    focus_x, focus_y = get_floor_map_focus_point(floor, selected_section_index)
    scaled_width = max(int(content_width * zoom), 1)
    scaled_height = max(int(content_height * zoom), 1)
    scaled_surface = pygame.transform.smoothscale(map_surface, (scaled_width, scaled_height))
    destination = (
        int(content_left + content_width / 2 - focus_x * zoom),
        int(content_top + content_height / 2 - focus_y * zoom),
    )
    previous_clip = surface.get_clip()
    surface.set_clip(panel_rect)
    surface.blit(scaled_surface, destination)
    surface.set_clip(previous_clip)


def should_render_selected_section_tile_view(
    floor: Floor,
    selected_section_index: int | None,
) -> bool:
    return selected_section_index is not None


def draw_selected_section_tile_view(
    surface: pygame.Surface,
    section: Section,
    color: tuple[int, int, int],
    selected_tile_coordinate: TileCoordinate | None = None,
) -> None:
    for coordinate, rect in get_selected_section_tile_rects(section).items():
        if not section.is_valid_coordinate(coordinate):
            continue
        cell_size = rect.width
        pygame.draw.rect(surface, color, rect, border_radius=3)
        pygame.draw.rect(surface, BACKGROUND, rect, 1, border_radius=3)
        if not section.is_explored_coordinate(coordinate):
            inset = max(cell_size // 4, 2)
            inner_rect = rect.inflate(-inset * 2, -inset * 2)
            if inner_rect.width > 0 and inner_rect.height > 0:
                pygame.draw.rect(surface, BACKGROUND, inner_rect, border_radius=2)
        elif section.is_locked_coordinate(coordinate):
            inset = max(cell_size // 4, 2)
            inner_rect = rect.inflate(-inset * 2, -inset * 2)
            if inner_rect.width > 0 and inner_rect.height > 0:
                pygame.draw.rect(surface, WARNING, inner_rect, border_radius=2)
        elif section.get_ruins_coordinate(coordinate) not in (0, 100):
            inset = max(cell_size // 4, 2)
            inner_rect = rect.inflate(-inset * 2, -inset * 2)
            if inner_rect.width > 0 and inner_rect.height > 0:
                pygame.draw.rect(surface, (220, 72, 72), inner_rect, border_radius=2)
        if coordinate == selected_tile_coordinate:
            pygame.draw.rect(surface, ACCENT, rect, 3, border_radius=4)


def get_selected_section_tile_rects(section: Section) -> dict[TileCoordinate, pygame.Rect]:
    content_left, content_top, content_width, content_height = get_central_panel_bounds()
    rows, columns = section.grid_dimensions()
    if rows <= 0 or columns <= 0:
        return {}

    grid_area = pygame.Rect(
        content_left + 40,
        content_top + 40,
        content_width - 80,
        content_height - 80,
    )
    gap = 4
    cell_width = (grid_area.width - gap * max(columns - 1, 0)) // max(columns, 1)
    cell_height = (grid_area.height - gap * max(rows - 1, 0)) // max(rows, 1)
    cell_size = max(min(cell_width, cell_height), 8)

    grid_width = columns * cell_size + gap * max(columns - 1, 0)
    grid_height = rows * cell_size + gap * max(rows - 1, 0)
    start_x = grid_area.x + max((grid_area.width - grid_width) // 2, 0)
    start_y = grid_area.y + max((grid_area.height - grid_height) // 2, 0)

    cell_rects: dict[TileCoordinate, pygame.Rect] = {}
    for row_index in range(rows):
        for column_index in range(columns):
            x = start_x + column_index * (cell_size + gap)
            y = start_y + row_index * (cell_size + gap)
            cell_rects[(row_index, column_index)] = pygame.Rect(x, y, cell_size, cell_size)
    return cell_rects


def get_floor_rects(station: Station) -> list[pygame.Rect]:
    available_height = SCREEN_HEIGHT - (HEADER_HEIGHT + 96) - 24
    gap = 10
    floor_count = max(len(station.floors), 1)
    card_height = min(84, (available_height - gap * (floor_count - 1)) // floor_count)

    rects: list[pygame.Rect] = []
    y = HEADER_HEIGHT + 96
    for _floor in station.floors:
        rects.append(pygame.Rect(18, y, LEFT_PANEL_WIDTH - 36, card_height))
        y += card_height + gap
    return rects


def draw_text(
    surface: pygame.Surface,
    font: pygame.font.Font,
    text: str,
    position: tuple[int, int],
    color: tuple[int, int, int] = TEXT,
    center: bool = False,
) -> pygame.Rect:
    rendered = font.render(text, True, color)
    rect = rendered.get_rect()
    if center:
        rect.center = position
    else:
        rect.topleft = position
    surface.blit(rendered, rect)
    return rect


def draw_header(
    surface: pygame.Surface,
    station: Station,
    game_clock: GameClock,
    templates: dict[str, BuildingTemplate],
    fonts: dict[str, pygame.font.Font],
) -> None:
    header_rect = pygame.Rect(0, 0, SCREEN_WIDTH, HEADER_HEIGHT)
    pygame.draw.rect(surface, PANEL, header_rect)
    pygame.draw.line(surface, GRID_LINE, (0, HEADER_HEIGHT), (SCREEN_WIDTH, HEADER_HEIGHT), 2)

    draw_text(surface, fonts["title"], game_clock.display_label(), (24, 24))
    power_icon = load_scaled_image(POWER_ICON_PATH, (20, 20))
    if power_icon is not None:
        surface.blit(power_icon, (SCREEN_WIDTH - 260, 20))
        draw_text(
            surface,
            fonts["small"],
            f"{station.power_needed}/{station.power_produced}",
            (SCREEN_WIDTH - 234, 30),
            TEXT_MUTED,
        )
    else:
        draw_text(
            surface,
            fonts["small"],
            f"Power: {station.power_needed}/{station.power_produced}",
            (SCREEN_WIDTH - 260, 30),
            TEXT_MUTED,
        )
    air_icon = load_scaled_image(AIR_ICON_PATH, (20, 20))
    if air_icon is not None:
        surface.blit(air_icon, (SCREEN_WIDTH - 420, 20))
        draw_text(
            surface,
            fonts["small"],
            f"{station.air_needed}/{station.air_produced}",
            (SCREEN_WIDTH - 394, 30),
            TEXT_MUTED,
        )
    else:
        draw_text(
            surface,
            fonts["small"],
            f"Air: {station.air_needed}/{station.air_produced}",
            (SCREEN_WIDTH - 420, 30),
            TEXT_MUTED,
        )
    water_icon = load_scaled_image(WATER_ICON_PATH, (20, 20))
    if water_icon is not None:
        surface.blit(water_icon, (SCREEN_WIDTH - 590, 20))
        draw_text(
            surface,
            fonts["small"],
            f"{station.water_needed}/{station.water_produced}",
            (SCREEN_WIDTH - 564, 30),
            TEXT_MUTED,
        )
    else:
        draw_text(
            surface,
            fonts["small"],
            f"Water: {station.water_needed}/{station.water_produced}",
            (SCREEN_WIDTH - 590, 30),
            TEXT_MUTED,
        )
    disposal_icon = load_scaled_image(DISPOSAL_ICON_PATH, (20, 20))
    if disposal_icon is not None:
        surface.blit(disposal_icon, (SCREEN_WIDTH - 800, 20))
        draw_text(
            surface,
            fonts["small"],
            f"{station.disposal_needed}/{station.disposal_produced}",
            (SCREEN_WIDTH - 774, 30),
            TEXT_MUTED,
        )
    else:
        draw_text(
            surface,
            fonts["small"],
            f"Disposal: {station.disposal_needed}/{station.disposal_produced}",
            (SCREEN_WIDTH - 800, 30),
            TEXT_MUTED,
        )
    worker_icon = load_scaled_image(WORKER_ICON_PATH, (20, 20))
    technician_icon = load_scaled_image(TECHNICIAN_ICON_PATH, (20, 20))
    hero_icon = load_scaled_image(HERO_ICON_PATH, (20, 20))
    if worker_icon is not None and technician_icon is not None and hero_icon is not None:
        surface.blit(worker_icon, (24, 53))
        draw_text(surface, fonts["small"], f"{station.workers_needed}/{station.workers}", (50, 56), TEXT_MUTED)
        surface.blit(technician_icon, (116, 53))
        draw_text(
            surface,
            fonts["small"],
            f"{station.technicians_needed}/{station.technicians}",
            (142, 56),
            TEXT_MUTED,
        )
        surface.blit(hero_icon, (236, 53))
        draw_text(surface, fonts["small"], f"{station.heroes_needed}/{station.heroes}", (262, 56), TEXT_MUTED)
    elif worker_icon is not None:
        surface.blit(worker_icon, (24, 53))
        header_status = (
            f"{station.workers_needed}/{station.workers}  "
            f"Technicians: {station.technicians_needed}/{station.technicians}  "
            f"Heroes: {station.heroes_needed}/{station.heroes}"
        )
        draw_text(surface, fonts["small"], header_status, (50, 56), TEXT_MUTED)
    else:
        header_status = (
            f"Workers: {station.workers_needed}/{station.workers}  "
            f"Technicians: {station.technicians_needed}/{station.technicians}  "
            f"Heroes: {station.heroes_needed}/{station.heroes}"
        )
        draw_text(surface, fonts["small"], header_status, (24, 56), TEXT_MUTED)


def draw_floor_list(
    surface: pygame.Surface,
    station: Station,
    selected_floor_index: int,
    fonts: dict[str, pygame.font.Font],
) -> list[pygame.Rect]:
    panel_rect = pygame.Rect(0, HEADER_HEIGHT, LEFT_PANEL_WIDTH, SCREEN_HEIGHT - HEADER_HEIGHT)
    pygame.draw.rect(surface, PANEL_ALT, panel_rect)
    pygame.draw.line(surface, GRID_LINE, (LEFT_PANEL_WIDTH, HEADER_HEIGHT), (LEFT_PANEL_WIDTH, SCREEN_HEIGHT), 2)

    draw_text(surface, fonts["label"], "FLOORS", (24, HEADER_HEIGHT + 20), ACCENT)

    floor_rects = get_floor_rects(station)
    for index, (floor, rect) in enumerate(zip(station.floors, floor_rects)):
        color = PANEL_ACTIVE if index == selected_floor_index else PANEL
        pygame.draw.rect(surface, color, rect, border_radius=12)
        pygame.draw.rect(surface, GRID_LINE, rect, 2, border_radius=12)
        draw_text(surface, fonts["body"], floor.name, (rect.x + 14, rect.y + 12))

    return floor_rects


def draw_sections(
    surface: pygame.Surface,
    floor: Floor,
    selected_section_index: int | None,
    templates: dict[str, BuildingTemplate],
    fonts: dict[str, pygame.font.Font],
    selected_tile_coordinate: TileCoordinate | None = None,
) -> list[pygame.Rect]:
    content_left, content_top, content_width, content_height = get_central_panel_bounds()
    panel_rect = pygame.Rect(content_left, content_top, content_width, content_height)

    pygame.draw.rect(surface, PANEL_ALT, panel_rect, border_radius=18)
    pygame.draw.rect(surface, GRID_LINE, panel_rect, 2, border_radius=18)
    if should_render_selected_section_tile_view(floor, selected_section_index):
        draw_selected_section_tile_view(
            surface,
            floor.sections[selected_section_index],
            get_selected_section_tile_color(floor, selected_section_index),
            selected_tile_coordinate,
        )
        return []
    map_surface = pygame.Surface((content_width, content_height), pygame.SRCALPHA)
    render_floor_map(map_surface, floor, selected_section_index, fonts)
    blit_zoomed_map(surface, map_surface, floor, selected_section_index)
    return []


def draw_building_legend(
    surface: pygame.Surface,
    templates: list[BuildingTemplate],
    fonts: dict[str, pygame.font.Font],
    build_page_index: int,
    pending_template: BuildingTemplate | None,
    selected_count: int,
) -> None:
    x = SCREEN_WIDTH - RIGHT_PANEL_WIDTH + 24
    y = SCREEN_HEIGHT - 286

    draw_text(surface, fonts["label"], "BUILD MENU", (x, y), ACCENT)
    page_count = max((len(templates) + BUILD_PAGE_SIZE - 1) // BUILD_PAGE_SIZE, 1)
    draw_text(surface, fonts["small"], "Press 1-8 to choose building", (x, y + 28), TEXT_MUTED)
    draw_text(surface, fonts["small"], "Press Z / X to change page", (x, y + 48), TEXT_MUTED)
    draw_text(surface, fonts["small"], f"Page {build_page_index + 1}/{page_count}", (x, y + 68), ACCENT)
    if pending_template is not None:
        draw_text(
            surface,
            fonts["small"],
            f"Placing: {pending_template.name} {selected_count}/{BUILDING_TILE_COST}",
            (x, y + 88),
            WARNING,
        )

    start = build_page_index * BUILD_PAGE_SIZE
    entries = templates[start : start + BUILD_PAGE_SIZE]
    y += 122
    for index, template in enumerate(entries, start=1):
        chip = pygame.Rect(x, y, 22, 22)
        pygame.draw.rect(surface, PANEL_ACTIVE if template.image else PANEL_ALT, chip, border_radius=6)
        pygame.draw.rect(surface, ACCENT if template.image else GRID_LINE, chip, 2, border_radius=6)
        if pending_template is not None and pending_template.template_id == template.template_id:
            pygame.draw.rect(surface, ACCENT, pygame.Rect(x - 8, y - 4, RIGHT_PANEL_WIDTH - 32, 30), 2, border_radius=8)
        draw_text(
            surface,
            fonts["small"],
            f"{index} {template.name} ({BUILDING_TILE_COST}t)",
            (x + 34, y - 1),
        )
        y += 28


def draw_section_details(
    surface: pygame.Surface,
    section: Section | None,
    templates: dict[str, BuildingTemplate],
    template_catalog: list[BuildingTemplate],
    fonts: dict[str, pygame.font.Font],
    notice: str,
    build_page_index: int,
    pending_template: BuildingTemplate | None,
    selected_count: int,
    selected_tile_coordinate: TileCoordinate | None,
    explore_progress: float = 0.0,
    explore_active: bool = False,
    unlock_active: bool = False,
) -> None:
    panel_rect = pygame.Rect(
        SCREEN_WIDTH - RIGHT_PANEL_WIDTH,
        HEADER_HEIGHT,
        RIGHT_PANEL_WIDTH,
        SCREEN_HEIGHT - HEADER_HEIGHT,
    )
    pygame.draw.rect(surface, PANEL_ALT, panel_rect)
    pygame.draw.line(
        surface,
        GRID_LINE,
        (SCREEN_WIDTH - RIGHT_PANEL_WIDTH, HEADER_HEIGHT),
        (SCREEN_WIDTH - RIGHT_PANEL_WIDTH, SCREEN_HEIGHT),
        2,
    )

    x = panel_rect.x + 24
    y = HEADER_HEIGHT + 20
    if section is None:
        draw_text(surface, fonts["label"], "SECTION", (x, y), ACCENT)
        draw_text(surface, fonts["title"], "No Selection", (x, y + 26))
    else:
        draw_text(surface, fonts["label"], "SECTION", (x, y), ACCENT)
        draw_text(surface, fonts["title"], section.name, (x, y + 26))

        details = [
            f"Tile Count: {section.tile_count}",
            f"Explored Tiles: {sum(1 for row in range(section.grid_dimensions()[0]) for col in range(section.grid_dimensions()[1]) if section.is_explored_coordinate((row, col)))}",
            f"Unlocked Tiles: {sum(1 for row in range(section.grid_dimensions()[0]) for col in range(section.grid_dimensions()[1]) if section.is_valid_coordinate((row, col)) and not section.is_locked_coordinate((row, col)))}",
            f"Open Tiles: {sum(1 for row in range(section.grid_dimensions()[0]) for col in range(section.grid_dimensions()[1]) if section.is_valid_coordinate((row, col)) and not section.is_building_coordinate((row, col)) and section.get_ruins_coordinate((row, col)) == 0)}",
        ]
        y += 64
        for detail in details:
            draw_text(surface, fonts["body"], detail, (x, y))
            y += 30

        draw_text(surface, fonts["label"], "TILE", (x, y + 10), ACCENT)
        y += 42
        if selected_tile_coordinate is None:
            draw_text(surface, fonts["title"], "No Selection", (x, y))
        else:
            selected_building = section.get_building_at_coordinate(selected_tile_coordinate)
            selected_template = (
                templates.get(selected_building.template_id)
                if selected_building is not None
                else None
            )
            draw_text(surface, fonts["title"], str(selected_tile_coordinate), (x, y))
            y += 38
            if not section.is_explored_coordinate(selected_tile_coordinate):
                draw_text(surface, fonts["body"], "Not Explored", (x, y), TEXT_MUTED)
            elif section.is_locked_coordinate(selected_tile_coordinate):
                draw_text(surface, fonts["body"], "Explored", (x, y))
                y += 28
                draw_text(surface, fonts["body"], "Locked", (x, y))
            elif section.get_ruins_coordinate(selected_tile_coordinate) > 0:
                tile_details = [
                    (
                        "Building: None"
                        if selected_template is None
                        else f"Ruined Building: {selected_template.name}"
                    ),
                    f"Ruins: {section.get_ruins_coordinate(selected_tile_coordinate)}",
                ]
                for detail in tile_details:
                    draw_text(surface, fonts["body"], detail, (x, y))
                    y += 28
            else:
                tile_details = [
                    (
                        "Building: None"
                        if selected_template is None
                        else f"Building: {selected_template.name}"
                    ),
                    f"Hull: {section.get_hull_coordinate(selected_tile_coordinate)}",
                    f"Structure: {section.get_structure_coordinate(selected_tile_coordinate)}",
                    f"Power: {section.get_power_coordinate(selected_tile_coordinate)}",
                    f"Air: {section.get_air_coordinate(selected_tile_coordinate)}",
                    f"Water: {section.get_water_coordinate(selected_tile_coordinate)}",
                    f"Disposal: {section.get_disposal_coordinate(selected_tile_coordinate)}",
                    f"Personnel: {section.get_personnel_coordinate(selected_tile_coordinate)}",
                ]
                for detail in tile_details:
                    draw_text(surface, fonts["body"], detail, (x, y))
                    y += 28

    if (
        section is not None
        and selected_tile_coordinate is not None
        and not section.is_explored_coordinate(selected_tile_coordinate)
    ):
        explore_button_rect = get_explore_button_rect()
        pygame.draw.rect(surface, PANEL_ACTIVE, explore_button_rect, border_radius=12)
        if explore_active:
            fill_rect = pygame.Rect(
                explore_button_rect.x,
                explore_button_rect.y,
                int(explore_button_rect.width * max(0.0, min(explore_progress, 1.0))),
                explore_button_rect.height,
            )
            if fill_rect.width > 0:
                pygame.draw.rect(surface, ACCENT, fill_rect, border_radius=12)
        pygame.draw.rect(surface, GRID_LINE, explore_button_rect, 2, border_radius=12)
        draw_text(surface, fonts["body"], "Explore", explore_button_rect.center, center=True)

    if (
        section is not None
        and selected_tile_coordinate is not None
        and section.is_explored_coordinate(selected_tile_coordinate)
        and section.is_locked_coordinate(selected_tile_coordinate)
    ):
        unlock_button_rect = get_unlock_button_rect()
        pygame.draw.rect(surface, PANEL_ACTIVE, unlock_button_rect, border_radius=12)
        if unlock_active:
            fill_rect = pygame.Rect(
                unlock_button_rect.x,
                unlock_button_rect.y,
                int(unlock_button_rect.width * max(0.0, min(explore_progress, 1.0))),
                unlock_button_rect.height,
            )
            if fill_rect.width > 0:
                pygame.draw.rect(surface, ACCENT, fill_rect, border_radius=12)
        pygame.draw.rect(surface, GRID_LINE, unlock_button_rect, 2, border_radius=12)
        draw_text(surface, fonts["body"], "Unlock", unlock_button_rect.center, center=True)


def get_explore_button_rect() -> pygame.Rect:
    return pygame.Rect(
        SCREEN_WIDTH - RIGHT_PANEL_WIDTH + 24,
        SCREEN_HEIGHT - 78,
        RIGHT_PANEL_WIDTH - 48,
        46,
    )


def get_unlock_button_rect() -> pygame.Rect:
    return pygame.Rect(
        SCREEN_WIDTH - RIGHT_PANEL_WIDTH + 24,
        SCREEN_HEIGHT - 78,
        RIGHT_PANEL_WIDTH - 48,
        46,
    )


def get_placement_overlay_rect() -> pygame.Rect:
    return pygame.Rect(
        LEFT_PANEL_WIDTH + 24,
        HEADER_HEIGHT + 44,
        SCREEN_WIDTH - LEFT_PANEL_WIDTH - RIGHT_PANEL_WIDTH - 48,
        SCREEN_HEIGHT - HEADER_HEIGHT - 68,
    )


def get_placement_grid_rects(section: Section) -> tuple[pygame.Rect, dict[TileCoordinate, pygame.Rect]]:
    overlay_rect = get_placement_overlay_rect()
    rows, columns = section.grid_dimensions()
    grid_area = pygame.Rect(
        overlay_rect.x + 28,
        overlay_rect.y + 94,
        overlay_rect.width - 56,
        overlay_rect.height - 126,
    )
    gap = 2
    cell_width = (grid_area.width - gap * max(columns - 1, 0)) // max(columns, 1)
    cell_height = (grid_area.height - gap * max(rows - 1, 0)) // max(rows, 1)
    cell_size = max(min(cell_width, cell_height), 6)

    max_grid_width = columns * cell_size + gap * max(columns - 1, 0)
    max_grid_height = rows * cell_size + gap * max(rows - 1, 0)
    start_x = grid_area.x + max((grid_area.width - max_grid_width) // 2, 0)
    start_y = grid_area.y + max((grid_area.height - max_grid_height) // 2, 0)

    cell_rects: dict[TileCoordinate, pygame.Rect] = {}
    for row_index, row in enumerate(section.occupancy_grid):
        row_width = len(row) * cell_size + gap * max(len(row) - 1, 0)
        row_start_x = start_x + max((max_grid_width - row_width) // 2, 0)
        for column_index, _cell in enumerate(row):
            x = row_start_x + column_index * (cell_size + gap)
            y = start_y + row_index * (cell_size + gap)
            cell_rects[(row_index, column_index)] = pygame.Rect(x, y, cell_size, cell_size)

    return overlay_rect, cell_rects


def draw_placement_overlay(
    surface: pygame.Surface,
    section: Section,
    template: BuildingTemplate,
    selected_coordinates: list[TileCoordinate],
    fonts: dict[str, pygame.font.Font],
) -> dict[TileCoordinate, pygame.Rect]:
    overlay_rect, cell_rects = get_placement_grid_rects(section)
    pygame.draw.rect(surface, PANEL_ALT, overlay_rect, border_radius=18)
    pygame.draw.rect(surface, GRID_LINE, overlay_rect, 2, border_radius=18)

    draw_text(surface, fonts["label"], "PLACEMENT MODE", (overlay_rect.x + 24, overlay_rect.y + 18), ACCENT)
    draw_text(surface, fonts["title"], template.name, (overlay_rect.x + 24, overlay_rect.y + 42))
    draw_text(
        surface,
        fonts["small"],
        f"Select {BUILDING_TILE_COST} tile with left click. Right click or C cancels.",
        (overlay_rect.x + 24, overlay_rect.y + 72),
        TEXT_MUTED,
    )
    draw_text(
        surface,
        fonts["small"],
        f"Selected: {len(selected_coordinates)}/{BUILDING_TILE_COST}",
        (overlay_rect.right - 170, overlay_rect.y + 18),
        WARNING,
    )

    selected_lookup = set(selected_coordinates)
    for coordinate, rect in cell_rects.items():
        if coordinate in selected_lookup:
            color = ACCENT
        elif not section.is_valid_coordinate(coordinate):
            color = BACKGROUND
        elif section.is_occupied_coordinate(coordinate):
            color = (96, 60, 60)
        else:
            color = PANEL
        pygame.draw.rect(surface, color, rect, border_radius=4)
        pygame.draw.rect(surface, GRID_LINE, rect, 1, border_radius=4)

    return cell_rects


def add_building_to_section(
    station: Station,
    section: Section,
    template_id: str,
    templates: dict[str, BuildingTemplate],
    coordinates: list[TileCoordinate] | None = None,
) -> str:
    template = templates[template_id]
    if section.available_tiles(templates) < BUILDING_TILE_COST:
        return f"{section.name} needs {BUILDING_TILE_COST} tile for {template.name}."

    building = Building(template_id, building_id=station.next_building_id(), coordinates=coordinates or [])
    section.add_building(building, templates)
    placed_coordinates = section.buildings[-1].coordinates
    return f"Added {template.name} to {section.name} at {placed_coordinates[0]}."


def remove_building_from_section(section: Section, templates: dict[str, BuildingTemplate]) -> str:
    if not section.buildings:
        return f"{section.name} has no building to remove."

    removed = section.buildings.pop()
    section.sync_occupancy_grid(templates)
    return f"Removed {templates[removed.template_id].name} from {section.name}."


def main(max_frames: int | None = None) -> None:
    pygame.init()
    screen = pygame.display.set_mode(SCREEN_SIZE)
    pygame.display.set_caption("Academy Station")
    frame_clock = pygame.time.Clock()

    fonts = {
        "title": pygame.font.SysFont("consolas", 28, bold=True),
        "label": pygame.font.SysFont("consolas", 20, bold=True),
        "body": pygame.font.SysFont("consolas", 22),
        "small": pygame.font.SysFont("consolas", 16),
        "map": pygame.font.SysFont("consolas", 14, bold=True),
        "map_small": pygame.font.SysFont("consolas", 12, bold=True),
        "map_tiny": pygame.font.SysFont("consolas", 10, bold=True),
    }

    templates = create_building_templates()
    template_catalog = list(templates.values())
    station = create_demo_station()
    game_clock = GameClock()
    selected_floor_index = 0
    selected_section_index: int | None = None
    selected_tile_coordinate: TileCoordinate | None = None
    build_page_index = 0
    pending_template_id: str | None = None
    pending_coordinates: list[TileCoordinate] = []
    pending_tile_action: str | None = None
    pending_explore_floor_index: int | None = None
    pending_explore_section_index: int | None = None
    pending_explore_tile_coordinate: TileCoordinate | None = None
    pending_explore_elapsed_ms = 0.0
    notice = ""
    frame_count = 0
    running = True

    build_hotkeys = [
        pygame.K_1,
        pygame.K_2,
        pygame.K_3,
        pygame.K_4,
        pygame.K_5,
        pygame.K_6,
        pygame.K_7,
        pygame.K_8,
    ]

    while running:
        delta_ms = frame_clock.tick(FPS)
        frame_count += 1
        current_floor = station.floors[selected_floor_index]
        current_section = current_floor.sections[selected_section_index] if selected_section_index is not None else None
        pending_template = templates[pending_template_id] if pending_template_id is not None else None
        placement_rects = (
            get_placement_grid_rects(current_section)[1]
            if pending_template is not None and current_section is not None
            else {}
        )
        tile_action_active = (
            pending_tile_action is not None
            and
            pending_explore_floor_index == selected_floor_index
            and pending_explore_section_index == selected_section_index
            and pending_explore_tile_coordinate == selected_tile_coordinate
            and current_section is not None
            and selected_tile_coordinate is not None
            and (
                (
                    pending_tile_action == "explore"
                    and not current_section.is_explored_coordinate(selected_tile_coordinate)
                )
                or (
                    pending_tile_action == "unlock"
                    and current_section.is_explored_coordinate(selected_tile_coordinate)
                    and current_section.is_locked_coordinate(selected_tile_coordinate)
                )
            )
        )
        explore_active = tile_action_active and pending_tile_action == "explore"
        unlock_active = tile_action_active and pending_tile_action == "unlock"
        if tile_action_active:
            pending_explore_elapsed_ms += delta_ms
            if pending_explore_elapsed_ms >= EXPLORE_ACTION_DURATION_MS:
                if pending_tile_action == "explore":
                    current_section.set_explored_coordinate(selected_tile_coordinate, True)
                    game_clock.advance_minutes(60)
                    notice = f"Explored tile {selected_tile_coordinate}."
                elif pending_tile_action == "unlock":
                    current_section.set_locked_coordinate(selected_tile_coordinate, False)
                    notice = f"Unlocked tile {selected_tile_coordinate}."
                pending_tile_action = None
                pending_explore_floor_index = None
                pending_explore_section_index = None
                pending_explore_tile_coordinate = None
                pending_explore_elapsed_ms = 0.0
                explore_active = False
                unlock_active = False
        elif pending_explore_tile_coordinate is not None:
            pending_tile_action = None
            pending_explore_floor_index = None
            pending_explore_section_index = None
            pending_explore_tile_coordinate = None
            pending_explore_elapsed_ms = 0.0

        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_ESCAPE:
                    if pending_template is not None:
                        pending_template_id = None
                        pending_coordinates = []
                        notice = "Placement cancelled."
                    else:
                        running = False
                elif event.key == pygame.K_s and event.mod & pygame.KMOD_CTRL:
                    save_path = save_game(
                        station,
                        game_clock,
                        selected_floor_index,
                        selected_section_index,
                        selected_tile_coordinate,
                    )
                    notice = f"Saved to {save_path}."
                elif event.key == pygame.K_l and event.mod & pygame.KMOD_CTRL:
                    try:
                        (
                            station,
                            game_clock,
                            selected_floor_index,
                            selected_section_index,
                            selected_tile_coordinate,
                        ) = load_game()
                        pending_template_id = None
                        pending_coordinates = []
                        pending_explore_floor_index = None
                        pending_explore_section_index = None
                        pending_explore_tile_coordinate = None
                        pending_explore_elapsed_ms = 0.0
                        pending_tile_action = None
                        notice = f"Loaded {SAVE_PATH}."
                    except FileNotFoundError:
                        notice = f"No save file at {SAVE_PATH}."
                    except ValueError as exc:
                        notice = str(exc)
                elif event.key == pygame.K_q:
                    selected_floor_index = (selected_floor_index - 1) % len(station.floors)
                    selected_section_index = None
                    selected_tile_coordinate = None
                    pending_explore_floor_index = None
                    pending_explore_section_index = None
                    pending_explore_tile_coordinate = None
                    pending_explore_elapsed_ms = 0.0
                    pending_tile_action = None
                    pending_template_id = None
                    pending_coordinates = []
                    notice = f"Viewing {station.floors[selected_floor_index].name}."
                elif event.key == pygame.K_e:
                    selected_floor_index = (selected_floor_index + 1) % len(station.floors)
                    selected_section_index = None
                    selected_tile_coordinate = None
                    pending_explore_floor_index = None
                    pending_explore_section_index = None
                    pending_explore_tile_coordinate = None
                    pending_explore_elapsed_ms = 0.0
                    pending_tile_action = None
                    pending_template_id = None
                    pending_coordinates = []
                    notice = f"Viewing {station.floors[selected_floor_index].name}."
                elif event.key == pygame.K_z:
                    page_count = max((len(template_catalog) + BUILD_PAGE_SIZE - 1) // BUILD_PAGE_SIZE, 1)
                    build_page_index = (build_page_index - 1) % page_count
                    notice = f"Build menu page {build_page_index + 1}."
                elif event.key == pygame.K_x:
                    page_count = max((len(template_catalog) + BUILD_PAGE_SIZE - 1) // BUILD_PAGE_SIZE, 1)
                    build_page_index = (build_page_index + 1) % page_count
                    notice = f"Build menu page {build_page_index + 1}."
                elif event.key in build_hotkeys:
                    if selected_section_index is None or current_section is None:
                        notice = "Select a sector before placing a building."
                        continue
                    slot_index = build_hotkeys.index(event.key)
                    template_index = build_page_index * BUILD_PAGE_SIZE + slot_index
                    if template_index >= len(template_catalog):
                        notice = "No building is assigned to that slot on this page."
                        continue
                    template_id = template_catalog[template_index].template_id
                    pending_template_id = template_id
                    pending_coordinates = []
                    notice = f"Placing {templates[template_id].name}: select {BUILDING_TILE_COST} tile."
                elif event.key == pygame.K_c and pending_template is not None:
                    pending_template_id = None
                    pending_coordinates = []
                    notice = "Placement cancelled."
                elif event.key == pygame.K_BACKSPACE:
                    if pending_template is not None:
                        pending_template_id = None
                        pending_coordinates = []
                        notice = "Placement cancelled."
                    else:
                        if selected_section_index is None or current_section is None:
                            notice = "Select a sector before removing a building."
                        else:
                            current_floor = station.floors[selected_floor_index]
                            current_section = current_floor.sections[selected_section_index]
                            notice = remove_building_from_section(current_section, templates)
            elif event.type == pygame.MOUSEBUTTONDOWN and event.button == 3 and pending_template is not None:
                pending_template_id = None
                pending_coordinates = []
                notice = "Placement cancelled."
            elif event.type == pygame.MOUSEBUTTONDOWN and event.button == 3:
                if pending_tile_action is not None:
                    cancelled_action = pending_tile_action
                    pending_tile_action = None
                    pending_explore_floor_index = None
                    pending_explore_section_index = None
                    pending_explore_tile_coordinate = None
                    pending_explore_elapsed_ms = 0.0
                    notice = f"{cancelled_action.title()} cancelled."
                    continue
                if selected_section_index is not None:
                    selected_section_index = None
                    selected_tile_coordinate = None
                    pending_coordinates = []
                    notice = ""
            elif event.type == pygame.MOUSEBUTTONDOWN and event.button == 1 and pending_template is not None:
                clicked_coordinate = None
                for coordinate, rect in placement_rects.items():
                    if rect.collidepoint(event.pos):
                        clicked_coordinate = coordinate
                        break

                if clicked_coordinate is None:
                    continue

                if clicked_coordinate in pending_coordinates:
                    pending_coordinates.remove(clicked_coordinate)
                    notice = f"Selection updated: {len(pending_coordinates)}/{BUILDING_TILE_COST} tile."
                    continue

                if current_section is None:
                    continue

                if not current_section.is_valid_coordinate(clicked_coordinate):
                    notice = f"{clicked_coordinate} is not a valid tile."
                    continue

                if current_section.is_occupied_coordinate(clicked_coordinate):
                    notice = f"{clicked_coordinate} is already occupied."
                    continue

                if len(pending_coordinates) >= BUILDING_TILE_COST:
                    notice = f"{pending_template.name} already has all required tiles selected."
                    continue

                pending_coordinates.append(clicked_coordinate)
                if len(pending_coordinates) == BUILDING_TILE_COST:
                    try:
                        notice = add_building_to_section(
                            station,
                            current_section,
                            pending_template.template_id,
                            templates,
                            pending_coordinates.copy(),
                        )
                        pending_template_id = None
                        pending_coordinates = []
                    except ValueError as exc:
                        notice = str(exc)
                else:
                    notice = f"Selection updated: {len(pending_coordinates)}/{BUILDING_TILE_COST} tile."
            elif event.type == pygame.MOUSEBUTTONDOWN and event.button == 1:
                mouse_pos = event.pos
                if (
                    current_section is not None
                    and selected_tile_coordinate is not None
                    and not current_section.is_explored_coordinate(selected_tile_coordinate)
                    and get_explore_button_rect().collidepoint(mouse_pos)
                ):
                    if explore_active:
                        pending_tile_action = None
                        pending_explore_floor_index = None
                        pending_explore_section_index = None
                        pending_explore_tile_coordinate = None
                        pending_explore_elapsed_ms = 0.0
                        notice = "Explore cancelled."
                    else:
                        pending_tile_action = "explore"
                        pending_explore_floor_index = selected_floor_index
                        pending_explore_section_index = selected_section_index
                        pending_explore_tile_coordinate = selected_tile_coordinate
                        pending_explore_elapsed_ms = 0.0
                        notice = f"Exploring tile {selected_tile_coordinate}..."
                    continue
                if (
                    current_section is not None
                    and selected_tile_coordinate is not None
                    and current_section.is_explored_coordinate(selected_tile_coordinate)
                    and current_section.is_locked_coordinate(selected_tile_coordinate)
                    and get_unlock_button_rect().collidepoint(mouse_pos)
                ):
                    if unlock_active:
                        pending_tile_action = None
                        pending_explore_floor_index = None
                        pending_explore_section_index = None
                        pending_explore_tile_coordinate = None
                        pending_explore_elapsed_ms = 0.0
                        notice = "Unlock cancelled."
                    else:
                        pending_tile_action = "unlock"
                        pending_explore_floor_index = selected_floor_index
                        pending_explore_section_index = selected_section_index
                        pending_explore_tile_coordinate = selected_tile_coordinate
                        pending_explore_elapsed_ms = 0.0
                        notice = f"Unlocking tile {selected_tile_coordinate}..."
                    continue
                floor_rects = get_floor_rects(station)
                for index, rect in enumerate(floor_rects):
                    if rect.collidepoint(mouse_pos):
                        selected_floor_index = index
                        selected_section_index = None
                        selected_tile_coordinate = None
                        pending_tile_action = None
                        pending_explore_floor_index = None
                        pending_explore_section_index = None
                        pending_explore_tile_coordinate = None
                        pending_explore_elapsed_ms = 0.0
                        pending_template_id = None
                        pending_coordinates = []
                        notice = f"Viewing {station.floors[selected_floor_index].name}."
                        break
                else:
                    current_floor = station.floors[selected_floor_index]
                    if selected_section_index is not None:
                        current_section = current_floor.sections[selected_section_index]
                        clicked_tile = None
                        for coordinate, rect in get_selected_section_tile_rects(current_section).items():
                            if rect.collidepoint(mouse_pos):
                                clicked_tile = coordinate
                                break
                        if clicked_tile is not None and current_section.is_valid_coordinate(clicked_tile):
                            selected_tile_coordinate = clicked_tile
                            pending_tile_action = None
                            pending_explore_floor_index = None
                            pending_explore_section_index = None
                            pending_explore_tile_coordinate = None
                            pending_explore_elapsed_ms = 0.0
                            notice = f"Selected tile {clicked_tile}."
                    else:
                        local_point = map_screen_to_local_point(current_floor, selected_section_index, mouse_pos)
                        if local_point is not None:
                            selected_index = get_floor_map_selected_section_index(current_floor, local_point)
                            if selected_index is not None:
                                selected_section_index = selected_index
                                selected_tile_coordinate = None
                                pending_tile_action = None
                                pending_explore_floor_index = None
                                pending_explore_section_index = None
                                pending_explore_tile_coordinate = None
                                pending_explore_elapsed_ms = 0.0
                                pending_template_id = None
                                pending_coordinates = []
                                notice = f"Selected {current_floor.sections[selected_index].name}."

        screen.fill(BACKGROUND)
        draw_header(screen, station, game_clock, templates, fonts)
        floor_rects = draw_floor_list(screen, station, selected_floor_index, fonts)
        current_floor = station.floors[selected_floor_index]
        section_rects = draw_sections(screen, current_floor, selected_section_index, templates, fonts, selected_tile_coordinate)
        current_section = current_floor.sections[selected_section_index] if selected_section_index is not None else None
        draw_section_details(
            screen,
            current_section,
            templates,
            template_catalog,
            fonts,
            notice,
            build_page_index,
            pending_template,
            len(pending_coordinates),
            selected_tile_coordinate,
            pending_explore_elapsed_ms / EXPLORE_ACTION_DURATION_MS if (explore_active or unlock_active) else 0.0,
            explore_active,
            unlock_active,
        )
        if pending_template is not None and current_section is not None:
            draw_placement_overlay(screen, current_section, pending_template, pending_coordinates, fonts)
        pygame.display.flip()

        if max_frames is not None and frame_count >= max_frames:
            running = False

    pygame.quit()


def get_section_rects(floor: Floor) -> list[pygame.Rect]:
    content_left, content_top, card_width, card_height, section_columns = get_section_layout(floor)

    rects: list[pygame.Rect] = []
    for index, _section in enumerate(floor.sections):
        row = index // section_columns
        column = index % section_columns
        x = content_left + column * (card_width + SECTION_GAP)
        y = content_top + row * (card_height + SECTION_GAP)
        rects.append(pygame.Rect(x, y, card_width, card_height))
    return rects


def get_section_layout(floor: Floor) -> tuple[int, int, int, int, int]:
    content_left = LEFT_PANEL_WIDTH + 24
    content_top = HEADER_HEIGHT + 28
    content_width = SCREEN_WIDTH - LEFT_PANEL_WIDTH - RIGHT_PANEL_WIDTH - 48
    content_height = SCREEN_HEIGHT - content_top - 24
    section_count = len(floor.sections)
    if section_count <= 8:
        section_columns = 3
    elif section_count <= 16:
        section_columns = 4
    else:
        section_columns = 5
    row_count = max((section_count + section_columns - 1) // section_columns, 1)
    card_width = (content_width - SECTION_GAP * (section_columns - 1)) // section_columns
    card_height = min(160, (content_height - SECTION_GAP * (row_count - 1)) // row_count)
    return content_left, content_top, card_width, card_height, section_columns


if __name__ == "__main__":
    main()
