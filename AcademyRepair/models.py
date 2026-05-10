from __future__ import annotations

from dataclasses import dataclass, field
import math


OccupancyGrid = list[list[int]]
ExplorationGrid = list[list[bool]]
LockGrid = list[list[bool]]
RuinsGrid = list[list[int]]
PowerGrid = list[list[int]]
AirGrid = list[list[int]]
WaterGrid = list[list[int]]
DisposalGrid = list[list[int]]
SecurityGrid = list[list[int]]
PersonnelGrid = list[list[int]]
HullGrid = list[list[int]]
StructureGrid = list[list[int]]
BuildingGrid = list[list[bool]]
TileCoordinate = tuple[int, int]
BUILDING_TILE_COST = 1


@dataclass
class FloorTile:
    valid: bool
    building_id: int = 0


FloorTileGrid = list[list[FloorTile]]


def create_occupancy_grid(
    tile_count: int,
    rows: int | None = None,
    columns: int | None = None,
) -> OccupancyGrid:
    if rows is not None or columns is not None:
        if rows is None or columns is None:
            raise ValueError("Explicit occupancy grid dimensions need both rows and columns.")
        if rows <= 0 or columns <= 0:
            raise ValueError("Occupancy grid dimensions must be positive.")
        if rows * columns < tile_count:
            raise ValueError("Occupancy grid dimensions cannot hold the section tile count.")
        return [[0] * columns for _ in range(rows)]

    columns = max(1, math.ceil(math.sqrt(tile_count)))
    full_rows, remainder = divmod(tile_count, columns)
    grid = [[0] * columns for _ in range(full_rows)]
    if remainder:
        grid.append([0] * remainder)
    return grid


def create_floor_tile_grid(rows: int, columns: int, valid_tile_count: int) -> FloorTileGrid:
    if rows <= 0 or columns <= 0:
        raise ValueError("Floor tile grids need positive dimensions.")
    if valid_tile_count < 0 or valid_tile_count > rows * columns:
        raise ValueError("Floor tile grid valid tile count is out of bounds.")

    remaining_valid_tiles = valid_tile_count
    grid: FloorTileGrid = []
    for _row_index in range(rows):
        row: list[FloorTile] = []
        for _column_index in range(columns):
            is_valid = remaining_valid_tiles > 0
            row.append(FloorTile(valid=is_valid))
            if is_valid:
                remaining_valid_tiles -= 1
        grid.append(row)
    return grid


def create_exploration_grid(occupancy_grid: OccupancyGrid) -> ExplorationGrid:
    return [[False] * len(row) for row in occupancy_grid]


def create_lock_grid(occupancy_grid: OccupancyGrid) -> LockGrid:
    return [[True] * len(row) for row in occupancy_grid]


def create_ruins_grid(occupancy_grid: OccupancyGrid) -> RuinsGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_power_grid(occupancy_grid: OccupancyGrid) -> PowerGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_air_grid(occupancy_grid: OccupancyGrid) -> AirGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_water_grid(occupancy_grid: OccupancyGrid) -> WaterGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_disposal_grid(occupancy_grid: OccupancyGrid) -> DisposalGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_security_grid(occupancy_grid: OccupancyGrid) -> SecurityGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_personnel_grid(occupancy_grid: OccupancyGrid) -> PersonnelGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_hull_grid(occupancy_grid: OccupancyGrid) -> HullGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_structure_grid(occupancy_grid: OccupancyGrid) -> StructureGrid:
    return [[50] * len(row) for row in occupancy_grid]


def create_building_grid(occupancy_grid: OccupancyGrid) -> BuildingGrid:
    return [[False] * len(row) for row in occupancy_grid]


@dataclass(frozen=True)
class BuildingTemplate:
    template_id: str
    name: str
    category: str
    facility: int
    prod: str
    prod_amount: int
    power_draw: int
    air_draw: int
    water_draw: int
    disposal_draw: int
    crew: int
    technician: int
    worker: int
    image: str


@dataclass
class Building:
    template_id: str
    building_id: int = 0
    level: int = 1
    status: str = "online"
    coordinates: list[TileCoordinate] = field(default_factory=list)


@dataclass
class GameClock:
    day: int = 1
    minute_of_cycle: int = 0

    START_HOUR: int = 9
    END_HOUR: int = 21

    @property
    def cycle_minutes(self) -> int:
        return (self.END_HOUR - self.START_HOUR) * 60

    @property
    def hour_24(self) -> int:
        return self.START_HOUR + self.minute_of_cycle // 60

    @property
    def minute(self) -> int:
        return self.minute_of_cycle % 60

    def advance_minutes(self, minutes: int) -> None:
        if minutes < 0:
            raise ValueError("Game clock cannot advance by a negative number of minutes.")
        total_minutes = self.minute_of_cycle + minutes
        while total_minutes >= self.cycle_minutes:
            total_minutes -= self.cycle_minutes
            self.day += 1
        self.minute_of_cycle = total_minutes

    def display_label(self) -> str:
        hour_12 = self.hour_24 % 12
        if hour_12 == 0:
            hour_12 = 12
        suffix = "AM" if self.hour_24 < 12 else "PM"
        return f"Day {self.day}, {hour_12}:{self.minute:02d} {suffix}"


@dataclass
class Section:
    section_id: str
    name: str
    purpose: str
    tile_count: int
    grid_rows: int | None = None
    grid_columns: int | None = None
    invalid_coordinates: set[TileCoordinate] = field(default_factory=set)
    integrity: int = 100
    buildings: list[Building] = field(default_factory=list)
    occupancy_grid: OccupancyGrid = field(init=False)
    exploration_grid: ExplorationGrid = field(init=False)
    lock_grid: LockGrid = field(init=False)
    ruins_grid: RuinsGrid = field(init=False)
    power_grid: PowerGrid = field(init=False)
    air_grid: AirGrid = field(init=False)
    water_grid: WaterGrid = field(init=False)
    disposal_grid: DisposalGrid = field(init=False)
    security_grid: SecurityGrid = field(init=False)
    personnel_grid: PersonnelGrid = field(init=False)
    hull_grid: HullGrid = field(init=False)
    structure_grid: StructureGrid = field(init=False)
    building_grid: BuildingGrid = field(init=False)

    def __post_init__(self) -> None:
        self.occupancy_grid = create_occupancy_grid(self.tile_count, self.grid_rows, self.grid_columns)
        self.exploration_grid = create_exploration_grid(self.occupancy_grid)
        self.lock_grid = create_lock_grid(self.occupancy_grid)
        self.ruins_grid = create_ruins_grid(self.occupancy_grid)
        self.power_grid = create_power_grid(self.occupancy_grid)
        self.air_grid = create_air_grid(self.occupancy_grid)
        self.water_grid = create_water_grid(self.occupancy_grid)
        self.disposal_grid = create_disposal_grid(self.occupancy_grid)
        self.security_grid = create_security_grid(self.occupancy_grid)
        self.personnel_grid = create_personnel_grid(self.occupancy_grid)
        self.hull_grid = create_hull_grid(self.occupancy_grid)
        self.structure_grid = create_structure_grid(self.occupancy_grid)
        self.building_grid = create_building_grid(self.occupancy_grid)
        for coordinate in list(self.invalid_coordinates):
            self._validate_grid_bounds(coordinate)

    def used_tiles(self, templates: dict[str, BuildingTemplate]) -> int:
        return len(self.buildings) * BUILDING_TILE_COST

    def available_tiles(self, templates: dict[str, BuildingTemplate]) -> int:
        return self.tile_count - self.used_tiles(templates)

    def occupied_tiles(self) -> int:
        return sum(sum(row) for row in self.occupancy_grid)

    def grid_dimensions(self) -> tuple[int, int]:
        rows = len(self.occupancy_grid)
        columns = max((len(row) for row in self.occupancy_grid), default=0)
        return rows, columns

    def clear_occupancy_grid(self) -> None:
        for row_index, row in enumerate(self.occupancy_grid):
            self.occupancy_grid[row_index] = [0] * len(row)

    def clear_exploration_grid(self) -> None:
        for row_index, row in enumerate(self.exploration_grid):
            self.exploration_grid[row_index] = [False] * len(row)

    def clear_lock_grid(self) -> None:
        for row_index, row in enumerate(self.lock_grid):
            self.lock_grid[row_index] = [True] * len(row)

    def clear_ruins_grid(self) -> None:
        for row_index, row in enumerate(self.ruins_grid):
            self.ruins_grid[row_index] = [50] * len(row)

    def clear_power_grid(self) -> None:
        for row_index, row in enumerate(self.power_grid):
            self.power_grid[row_index] = [50] * len(row)

    def clear_air_grid(self) -> None:
        for row_index, row in enumerate(self.air_grid):
            self.air_grid[row_index] = [50] * len(row)

    def clear_water_grid(self) -> None:
        for row_index, row in enumerate(self.water_grid):
            self.water_grid[row_index] = [50] * len(row)

    def clear_disposal_grid(self) -> None:
        for row_index, row in enumerate(self.disposal_grid):
            self.disposal_grid[row_index] = [50] * len(row)

    def clear_security_grid(self) -> None:
        for row_index, row in enumerate(self.security_grid):
            self.security_grid[row_index] = [50] * len(row)

    def clear_personnel_grid(self) -> None:
        for row_index, row in enumerate(self.personnel_grid):
            self.personnel_grid[row_index] = [50] * len(row)

    def clear_hull_grid(self) -> None:
        for row_index, row in enumerate(self.hull_grid):
            self.hull_grid[row_index] = [50] * len(row)

    def clear_structure_grid(self) -> None:
        for row_index, row in enumerate(self.structure_grid):
            self.structure_grid[row_index] = [50] * len(row)

    def clear_building_grid(self) -> None:
        for row_index, row in enumerate(self.building_grid):
            self.building_grid[row_index] = [False] * len(row)

    def _validate_grid_bounds(self, coordinate: TileCoordinate) -> None:
        row_index, column_index = coordinate
        if row_index < 0 or row_index >= len(self.occupancy_grid):
            raise ValueError(f"Section {self.section_id} received an out-of-bounds coordinate.")
        if column_index < 0 or column_index >= len(self.occupancy_grid[row_index]):
            raise ValueError(f"Section {self.section_id} received an out-of-bounds coordinate.")

    def is_valid_coordinate(self, coordinate: TileCoordinate) -> bool:
        row_index, column_index = coordinate
        if row_index < 0 or row_index >= len(self.occupancy_grid):
            return False
        if column_index < 0 or column_index >= len(self.occupancy_grid[row_index]):
            return False
        return coordinate not in self.invalid_coordinates

    def invalidate_coordinate(self, coordinate: TileCoordinate) -> None:
        self._validate_grid_bounds(coordinate)
        self.invalid_coordinates.add(coordinate)
        row_index, column_index = coordinate
        self.occupancy_grid[row_index][column_index] = 0
        self.exploration_grid[row_index][column_index] = False
        self.lock_grid[row_index][column_index] = False
        self.ruins_grid[row_index][column_index] = 0
        self.power_grid[row_index][column_index] = 0
        self.air_grid[row_index][column_index] = 0
        self.water_grid[row_index][column_index] = 0
        self.disposal_grid[row_index][column_index] = 0
        self.security_grid[row_index][column_index] = 0
        self.personnel_grid[row_index][column_index] = 0
        self.hull_grid[row_index][column_index] = 0
        self.structure_grid[row_index][column_index] = 0
        self.building_grid[row_index][column_index] = False

    def invalidate_span(self, row_index: int, start_column: int, tile_count: int) -> None:
        if tile_count < 0:
            raise ValueError("Section invalid span tile count cannot be negative.")
        for offset in range(tile_count):
            self.invalidate_coordinate((row_index, start_column + offset))

    def is_occupied_coordinate(self, coordinate: TileCoordinate) -> bool:
        if not self.is_valid_coordinate(coordinate):
            return False
        row_index, column_index = coordinate
        return self.occupancy_grid[row_index][column_index] == 1

    def is_explored_coordinate(self, coordinate: TileCoordinate) -> bool:
        if not self.is_valid_coordinate(coordinate):
            return False
        row_index, column_index = coordinate
        return self.exploration_grid[row_index][column_index]

    def set_explored_coordinate(self, coordinate: TileCoordinate, explored: bool = True) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid exploration coordinate.")
        row_index, column_index = coordinate
        self.exploration_grid[row_index][column_index] = explored

    def is_locked_coordinate(self, coordinate: TileCoordinate) -> bool:
        if not self.is_valid_coordinate(coordinate):
            return False
        row_index, column_index = coordinate
        return self.lock_grid[row_index][column_index]

    def set_locked_coordinate(self, coordinate: TileCoordinate, locked: bool = True) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid lock coordinate.")
        row_index, column_index = coordinate
        self.lock_grid[row_index][column_index] = locked

    def get_ruins_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.ruins_grid[row_index][column_index]

    def set_ruins_coordinate(self, coordinate: TileCoordinate, ruins: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid ruins coordinate.")
        if ruins < 0 or ruins > 100:
            raise ValueError("Ruins value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.ruins_grid[row_index][column_index] = ruins

    def get_power_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.power_grid[row_index][column_index]

    def set_power_coordinate(self, coordinate: TileCoordinate, power: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid power coordinate.")
        if power < 0 or power > 100:
            raise ValueError("Power value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.power_grid[row_index][column_index] = power

    def get_air_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.air_grid[row_index][column_index]

    def set_air_coordinate(self, coordinate: TileCoordinate, air: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid air coordinate.")
        if air < 0 or air > 100:
            raise ValueError("Air value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.air_grid[row_index][column_index] = air

    def get_water_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.water_grid[row_index][column_index]

    def set_water_coordinate(self, coordinate: TileCoordinate, water: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid water coordinate.")
        if water < 0 or water > 100:
            raise ValueError("Water value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.water_grid[row_index][column_index] = water

    def get_disposal_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.disposal_grid[row_index][column_index]

    def set_disposal_coordinate(self, coordinate: TileCoordinate, disposal: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid disposal coordinate.")
        if disposal < 0 or disposal > 100:
            raise ValueError("Disposal value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.disposal_grid[row_index][column_index] = disposal

    def get_security_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.security_grid[row_index][column_index]

    def set_security_coordinate(self, coordinate: TileCoordinate, security: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid security coordinate.")
        if security < 0 or security > 100:
            raise ValueError("Security value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.security_grid[row_index][column_index] = security

    def get_personnel_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.personnel_grid[row_index][column_index]

    def set_personnel_coordinate(self, coordinate: TileCoordinate, personnel: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid personnel coordinate.")
        if personnel < 0 or personnel > 100:
            raise ValueError("Personnel value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.personnel_grid[row_index][column_index] = personnel

    def get_hull_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.hull_grid[row_index][column_index]

    def set_hull_coordinate(self, coordinate: TileCoordinate, hull: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid hull coordinate.")
        if hull < 0 or hull > 100:
            raise ValueError("Hull value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.hull_grid[row_index][column_index] = hull

    def get_structure_coordinate(self, coordinate: TileCoordinate) -> int:
        if not self.is_valid_coordinate(coordinate):
            return 0
        row_index, column_index = coordinate
        return self.structure_grid[row_index][column_index]

    def set_structure_coordinate(self, coordinate: TileCoordinate, structure: int) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid structure coordinate.")
        if structure < 0 or structure > 100:
            raise ValueError("Structure value must be between 0 and 100.")
        row_index, column_index = coordinate
        self.structure_grid[row_index][column_index] = structure

    def is_building_coordinate(self, coordinate: TileCoordinate) -> bool:
        if not self.is_valid_coordinate(coordinate):
            return False
        row_index, column_index = coordinate
        return self.building_grid[row_index][column_index]

    def get_building_at_coordinate(self, coordinate: TileCoordinate) -> Building | None:
        if not self.is_valid_coordinate(coordinate):
            return None
        for building in self.buildings:
            if coordinate in building.coordinates:
                return building
        return None

    def set_building_coordinate(self, coordinate: TileCoordinate, building: bool = True) -> None:
        if not self.is_valid_coordinate(coordinate):
            raise ValueError(f"Section {self.section_id} received an invalid building coordinate.")
        row_index, column_index = coordinate
        self.building_grid[row_index][column_index] = building

    def open_coordinates(self) -> list[TileCoordinate]:
        coordinates: list[TileCoordinate] = []
        for row_index, row in enumerate(self.occupancy_grid):
            for column_index, value in enumerate(row):
                coordinate = (row_index, column_index)
                if coordinate in self.invalid_coordinates:
                    continue
                if value == 0:
                    coordinates.append(coordinate)
        return coordinates

    def find_first_open_coordinates(self) -> list[TileCoordinate]:
        coordinates = self.open_coordinates()[:BUILDING_TILE_COST]
        if len(coordinates) != BUILDING_TILE_COST:
            raise ValueError(f"Section {self.section_id} does not have enough open tiles.")
        return coordinates

    def mark_coordinates(self, coordinates: list[TileCoordinate]) -> None:
        for row_index, column_index in coordinates:
            self.occupancy_grid[row_index][column_index] = 1
            self.building_grid[row_index][column_index] = True

    def validate_coordinates(self, coordinates: list[TileCoordinate]) -> None:
        if len(coordinates) != BUILDING_TILE_COST:
            raise ValueError(f"Section {self.section_id} expected {BUILDING_TILE_COST} coordinates.")
        if len(set(coordinates)) != len(coordinates):
            raise ValueError(f"Section {self.section_id} received duplicate coordinates.")
        for coordinate in coordinates:
            if not self.is_valid_coordinate(coordinate):
                raise ValueError(f"Section {self.section_id} received an out-of-bounds coordinate.")
            if self.is_occupied_coordinate(coordinate):
                raise ValueError(f"Section {self.section_id} received an occupied coordinate.")

    def add_building(self, building: Building, templates: dict[str, BuildingTemplate]) -> None:
        if building.coordinates:
            coordinates = list(building.coordinates)
            self.validate_coordinates(coordinates)
        else:
            coordinates = self.find_first_open_coordinates()
        building.coordinates = coordinates
        self.mark_coordinates(coordinates)
        self.buildings.append(building)

    def sync_occupancy_grid(self, templates: dict[str, BuildingTemplate]) -> None:
        existing_buildings = list(self.buildings)
        self.buildings = []
        self.clear_occupancy_grid()
        self.clear_building_grid()
        for building in existing_buildings:
            self.add_building(building, templates)


@dataclass
class Floor:
    floor_id: str
    name: str
    role: str
    sections: list[Section]
    tile_grid: FloorTileGrid = field(default_factory=list)

    @property
    def total_tiles(self) -> int:
        return sum(section.tile_count for section in self.sections)

    def initialize_tile_grid(self, rows: int, columns: int, valid_tile_count: int | None = None) -> None:
        if valid_tile_count is None:
            valid_tile_count = self.total_tiles
        self.tile_grid = create_floor_tile_grid(rows, columns, valid_tile_count)

    def tile_grid_dimensions(self) -> tuple[int, int]:
        if not self.tile_grid:
            return 0, 0
        return len(self.tile_grid), len(self.tile_grid[0])

    def valid_tile_count(self) -> int:
        return sum(1 for row in self.tile_grid for tile in row if tile.valid)

    def occupied_tile_count(self) -> int:
        return sum(1 for row in self.tile_grid for tile in row if tile.valid and tile.building_id != 0)

    def clear_tile_grid_buildings(self) -> None:
        for row in self.tile_grid:
            for tile in row:
                tile.building_id = 0

    def is_valid_tile_coordinate(self, coordinate: TileCoordinate) -> bool:
        row_index, column_index = coordinate
        if row_index < 0 or row_index >= len(self.tile_grid):
            return False
        if column_index < 0 or column_index >= len(self.tile_grid[row_index]):
            return False
        return True

    def invalidate_tile(self, coordinate: TileCoordinate) -> None:
        if not self.is_valid_tile_coordinate(coordinate):
            raise ValueError(f"Floor {self.floor_id} received an out-of-bounds tile coordinate.")
        row_index, column_index = coordinate
        self.tile_grid[row_index][column_index].valid = False
        self.tile_grid[row_index][column_index].building_id = 0

    def invalidate_reflected_span(self, row_index: int, start_column: int, tile_count: int) -> None:
        rows, columns = self.tile_grid_dimensions()
        if rows == 0 or columns == 0:
            raise ValueError(f"Floor {self.floor_id} does not have a tile grid.")
        if tile_count < 0:
            raise ValueError("Tile count cannot be negative.")

        coordinates: set[TileCoordinate] = set()
        for offset in range(tile_count):
            column_index = start_column + offset
            if column_index >= columns:
                raise ValueError(f"Floor {self.floor_id} reflected span exceeded grid width.")
            mirrored_row = rows - 1 - row_index
            mirrored_column = columns - 1 - column_index
            coordinates.add((row_index, column_index))
            coordinates.add((row_index, mirrored_column))
            coordinates.add((mirrored_row, column_index))
            coordinates.add((mirrored_row, mirrored_column))

        for coordinate in coordinates:
            self.invalidate_tile(coordinate)


@dataclass
class Station:
    name: str
    floors: list[Floor]
    workers: int = 0
    technicians: int = 0
    heroes: int = 0
    workers_needed: int = 0
    technicians_needed: int = 0
    heroes_needed: int = 0
    power_needed: int = 0
    power_produced: int = 0
    air_needed: int = 0
    air_produced: int = 0
    water_needed: int = 0
    water_produced: int = 0
    disposal_needed: int = 0
    disposal_produced: int = 0

    def total_sections(self) -> int:
        return sum(len(floor.sections) for floor in self.floors)

    def total_buildings(self) -> int:
        return sum(len(section.buildings) for floor in self.floors for section in floor.sections)

    def total_tiles(self) -> int:
        return sum(section.tile_count for floor in self.floors for section in floor.sections)

    def used_tiles(self, templates: dict[str, BuildingTemplate]) -> int:
        return sum(section.used_tiles(templates) for floor in self.floors for section in floor.sections)

    def sync_occupancy_grids(self, templates: dict[str, BuildingTemplate]) -> None:
        for floor in self.floors:
            for section in floor.sections:
                section.sync_occupancy_grid(templates)

    def total_power_draw(self, templates: dict[str, BuildingTemplate]) -> int:
        total = 0
        for floor in self.floors:
            for section in floor.sections:
                for building in section.buildings:
                    total += templates[building.template_id].power_draw * building.level
        return total

    def total_crew(self, templates: dict[str, BuildingTemplate]) -> int:
        total = 0
        for floor in self.floors:
            for section in floor.sections:
                for building in section.buildings:
                    total += templates[building.template_id].crew * building.level
        return total

    def next_building_id(self) -> int:
        max_building_id = 0
        for floor in self.floors:
            for section in floor.sections:
                for building in section.buildings:
                    max_building_id = max(max_building_id, building.building_id)
        return max_building_id + 1

    def assign_missing_building_ids(self) -> None:
        next_building_id = 1
        for floor in self.floors:
            for section in floor.sections:
                for building in section.buildings:
                    if building.building_id <= 0:
                        building.building_id = next_building_id
                        next_building_id += 1
                    else:
                        next_building_id = max(next_building_id, building.building_id + 1)
