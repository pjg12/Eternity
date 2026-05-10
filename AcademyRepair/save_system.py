from __future__ import annotations

import json
from pathlib import Path

from models import Building, Floor, GameClock, Section, Station, TileCoordinate
from station_data import create_building_templates, create_demo_station


SAVE_VERSION = 1
SAVE_PATH = Path("saves/savegame.json")


def _serialize_building(building: Building) -> dict[str, object]:
    return {
        "template_id": building.template_id,
        "building_id": building.building_id,
        "level": building.level,
        "status": building.status,
        "coordinates": [list(coordinate) for coordinate in building.coordinates],
    }


def _serialize_section(section: Section) -> dict[str, object]:
    return {
        "section_id": section.section_id,
        "integrity": section.integrity,
        "buildings": [_serialize_building(building) for building in section.buildings],
        "exploration_grid": section.exploration_grid,
        "lock_grid": section.lock_grid,
        "ruins_grid": section.ruins_grid,
        "power_grid": section.power_grid,
        "air_grid": section.air_grid,
        "water_grid": section.water_grid,
        "disposal_grid": section.disposal_grid,
        "security_grid": section.security_grid,
        "personnel_grid": section.personnel_grid,
        "hull_grid": section.hull_grid,
        "structure_grid": section.structure_grid,
    }


def _serialize_floor(floor: Floor) -> dict[str, object]:
    tile_grid = None
    if floor.tile_grid:
        tile_grid = [
            [{"valid": tile.valid, "building_id": tile.building_id} for tile in row]
            for row in floor.tile_grid
        ]
    return {
        "floor_id": floor.floor_id,
        "sections": [_serialize_section(section) for section in floor.sections],
        "tile_grid": tile_grid,
    }


def _apply_grid(target_grid: list[list[object]], saved_grid: object) -> None:
    if not isinstance(saved_grid, list):
        return
    if len(saved_grid) != len(target_grid):
        return
    for row_index, target_row in enumerate(target_grid):
        saved_row = saved_grid[row_index]
        if not isinstance(saved_row, list) or len(saved_row) != len(target_row):
            return
    for row_index, target_row in enumerate(target_grid):
        saved_row = saved_grid[row_index]
        for column_index in range(len(target_row)):
            target_row[column_index] = saved_row[column_index]


def _apply_floor_tile_grid(floor: Floor, saved_tile_grid: object) -> None:
    if not floor.tile_grid or not isinstance(saved_tile_grid, list):
        return
    if len(saved_tile_grid) != len(floor.tile_grid):
        return
    for row_index, floor_row in enumerate(floor.tile_grid):
        saved_row = saved_tile_grid[row_index]
        if not isinstance(saved_row, list) or len(saved_row) != len(floor_row):
            return
    for row_index, floor_row in enumerate(floor.tile_grid):
        saved_row = saved_tile_grid[row_index]
        for column_index, tile in enumerate(floor_row):
            saved_tile = saved_row[column_index]
            if not isinstance(saved_tile, dict):
                continue
            tile.valid = bool(saved_tile.get("valid", tile.valid))
            tile.building_id = int(saved_tile.get("building_id", tile.building_id))


def _deserialize_building(data: dict[str, object]) -> Building:
    coordinates = [
        (int(coordinate[0]), int(coordinate[1]))
        for coordinate in data.get("coordinates", [])
        if isinstance(coordinate, list) and len(coordinate) == 2
    ]
    return Building(
        template_id=str(data["template_id"]),
        building_id=int(data.get("building_id", 0)),
        level=int(data.get("level", 1)),
        status=str(data.get("status", "online")),
        coordinates=coordinates,
    )


def _apply_section_state(section: Section, data: dict[str, object]) -> None:
    templates = create_building_templates()
    section.integrity = int(data.get("integrity", section.integrity))
    section.buildings = [
        _deserialize_building(building_data)
        for building_data in data.get("buildings", [])
        if isinstance(building_data, dict) and "template_id" in building_data
    ]
    section.sync_occupancy_grid(templates)
    _apply_grid(section.exploration_grid, data.get("exploration_grid"))
    _apply_grid(section.lock_grid, data.get("lock_grid"))
    _apply_grid(section.ruins_grid, data.get("ruins_grid"))
    _apply_grid(section.power_grid, data.get("power_grid"))
    _apply_grid(section.air_grid, data.get("air_grid"))
    _apply_grid(section.water_grid, data.get("water_grid"))
    _apply_grid(section.disposal_grid, data.get("disposal_grid"))
    _apply_grid(section.security_grid, data.get("security_grid"))
    _apply_grid(section.personnel_grid, data.get("personnel_grid"))
    _apply_grid(section.hull_grid, data.get("hull_grid"))
    _apply_grid(section.structure_grid, data.get("structure_grid"))


def save_game(
    station: Station,
    game_clock: GameClock,
    selected_floor_index: int,
    selected_section_index: int | None,
    selected_tile_coordinate: TileCoordinate | None,
    path: Path = SAVE_PATH,
) -> Path:
    selected_floor = station.floors[selected_floor_index]
    selected_section_id = None
    if selected_section_index is not None:
        selected_section_id = selected_floor.sections[selected_section_index].section_id

    data = {
        "version": SAVE_VERSION,
        "station_name": station.name,
        "station_stats": {
            "workers": station.workers,
            "technicians": station.technicians,
            "heroes": station.heroes,
            "workers_needed": station.workers_needed,
            "technicians_needed": station.technicians_needed,
            "heroes_needed": station.heroes_needed,
            "power_needed": station.power_needed,
            "power_produced": station.power_produced,
            "air_needed": station.air_needed,
            "air_produced": station.air_produced,
            "water_needed": station.water_needed,
            "water_produced": station.water_produced,
            "disposal_needed": station.disposal_needed,
            "disposal_produced": station.disposal_produced,
        },
        "game_clock": {
            "day": game_clock.day,
            "minute_of_cycle": game_clock.minute_of_cycle,
        },
        "ui_state": {
            "selected_floor_id": selected_floor.floor_id,
            "selected_section_id": selected_section_id,
            "selected_tile_coordinate": list(selected_tile_coordinate) if selected_tile_coordinate is not None else None,
        },
        "floors": [_serialize_floor(floor) for floor in station.floors],
    }

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2), encoding="utf-8")
    return path


def load_game(path: Path = SAVE_PATH) -> tuple[Station, GameClock, int, int | None, TileCoordinate | None]:
    if not path.exists():
        raise FileNotFoundError(path)

    data = json.loads(path.read_text(encoding="utf-8"))
    if int(data.get("version", 0)) != SAVE_VERSION:
        raise ValueError("Unsupported save version.")

    station = create_demo_station()
    station.name = str(data.get("station_name", station.name))
    station_stats = data.get("station_stats", {})
    station.workers = int(station_stats.get("workers", station.workers))
    station.technicians = int(station_stats.get("technicians", station.technicians))
    station.heroes = int(station_stats.get("heroes", station.heroes))
    station.workers_needed = int(station_stats.get("workers_needed", station.workers_needed))
    station.technicians_needed = int(station_stats.get("technicians_needed", station.technicians_needed))
    station.heroes_needed = int(station_stats.get("heroes_needed", station.heroes_needed))
    station.power_needed = int(station_stats.get("power_needed", station.power_needed))
    station.power_produced = int(station_stats.get("power_produced", station.power_produced))
    station.air_needed = int(station_stats.get("air_needed", station.air_needed))
    station.air_produced = int(station_stats.get("air_produced", station.air_produced))
    station.water_needed = int(station_stats.get("water_needed", station.water_needed))
    station.water_produced = int(station_stats.get("water_produced", station.water_produced))
    station.disposal_needed = int(station_stats.get("disposal_needed", station.disposal_needed))
    station.disposal_produced = int(station_stats.get("disposal_produced", station.disposal_produced))
    game_clock_data = data.get("game_clock", {})
    game_clock = GameClock(
        day=int(game_clock_data.get("day", 1)),
        minute_of_cycle=int(game_clock_data.get("minute_of_cycle", 0)),
    )

    floors_by_id = {floor.floor_id: floor for floor in station.floors}
    for floor_data in data.get("floors", []):
        if not isinstance(floor_data, dict):
            continue
        floor_id = floor_data.get("floor_id")
        if floor_id not in floors_by_id:
            continue
        floor = floors_by_id[str(floor_id)]
        sections_by_id = {section.section_id: section for section in floor.sections}
        for section_data in floor_data.get("sections", []):
            if not isinstance(section_data, dict):
                continue
            section_id = section_data.get("section_id")
            if section_id not in sections_by_id:
                continue
            _apply_section_state(sections_by_id[str(section_id)], section_data)
        _apply_floor_tile_grid(floor, floor_data.get("tile_grid"))

    ui_state = data.get("ui_state", {})
    selected_floor_id = ui_state.get("selected_floor_id")
    selected_floor_index = next(
        (index for index, floor in enumerate(station.floors) if floor.floor_id == selected_floor_id),
        0,
    )

    selected_section_index = None
    selected_tile_coordinate = None
    selected_section_id = ui_state.get("selected_section_id")
    if selected_section_id is not None:
        floor = station.floors[selected_floor_index]
        selected_section_index = next(
            (index for index, section in enumerate(floor.sections) if section.section_id == selected_section_id),
            None,
        )
        saved_tile_coordinate = ui_state.get("selected_tile_coordinate")
        if (
            selected_section_index is not None
            and isinstance(saved_tile_coordinate, list)
            and len(saved_tile_coordinate) == 2
        ):
            coordinate = (int(saved_tile_coordinate[0]), int(saved_tile_coordinate[1]))
            if floor.sections[selected_section_index].is_valid_coordinate(coordinate):
                selected_tile_coordinate = coordinate

    return station, game_clock, selected_floor_index, selected_section_index, selected_tile_coordinate
