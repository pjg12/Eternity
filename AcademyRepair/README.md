# Academy Repair

Small Python strategy game prototype built with Pygame.

## Current Prototype

The current build is a station-management scaffold focused on structure:

- The station is divided into floors
- Each floor contains sections
- Each sector has a tile budget
- Each building consumes tiles inside its sector
- Each sector now maintains a 2D binary occupancy grid with explicit tile coordinates
- The Main Level now also has a floor-level `48x48` tile grid with per-tile `valid` and `building_id` data
- You can inspect floors and sections, then add or remove buildings

## Project Structure

- `main.py` contains the Pygame loop and UI rendering
- `models.py` contains the station data model
- `station_data.py` contains the seeded station layout and building templates

## Station Floors

- Main Level
- Nature Level
- Utility Level
- Security Level
- Docks

## Controls

- Click floors on the left to switch decks
- Click sections in the center to inspect them
- Press `Q` / `E` to cycle floors
- Press `1` through `8` to choose a building from the current build page
- In placement mode, click exact tiles on the grid to place the building
- Press `Z` / `X` to cycle building pages
- Press `C`, `Backspace`, or right click to cancel placement mode
- Press `Backspace` to remove the most recent building when not in placement mode
- Press `Esc` to quit

## Available Buildings

- Housing Block
- Apartment Complex
- Main Administration
- Administration Building
- Cafeteria
- Learning Hall
- Large Learning Hall
- Grand Learning Hall
- Conference Hall
- Courtyard
- Cultural District
- Engineering Complex
- Interchange Building
- Hospital
- Large Hospital
- Medical Clinic
- Library
- Large Library
- Grand Library
- Medical Complex
- Restaurant
- Stock Exchange
- Tram
- Research Lab
- Training Hall
- Large Training Hall
- Training Dome
- Sports Auditorium
- Workshop
- Med Bay
- Training Hall
- Fusion Node
- Hydro Farm
- Market Arcade
- Air Scrubber
- Water Recycler
- Disposal Processor
- Security Post

## Run

```powershell
python -m pip install -r requirements.txt
python main.py
```

## Project Goal

This is the first layer of infrastructure for a space-station strategy game. The current version is meant to establish the core hierarchy and UI so systems like resources, staffing, construction, and events can be added cleanly.
