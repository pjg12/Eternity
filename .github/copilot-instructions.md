**Purpose**
- **Goal:** Help AI coding agents be immediately productive in this Java Swing TTRPG project.

**Quick Build & Run**
- **Compile:** Use Maven (Java 17):

  ```powershell
  mvn -q -DskipTests=true compile
  ```
- **Run (dev):** copy dependencies and run the main class from project root:

  ```powershell
  mvn dependency:copy-dependencies -DoutputDirectory=target/dependency
  java -cp target/classes;target/dependency/* eternity.EternityMain
  ```
- **IDE:** You can also run `eternity.EternityMain` directly from your IDE (recommended for GUI debugging).

**Where runtime data lives (important)**
- **Data files:** `DataStore` loads JSON from a hardcoded path: `c:\Eternity\Eternity\data\` (see `src/main/java/eternity/DataStore.java`). The project repository contains a top-level `Data/` folder with the JSON files — ensure the runtime path points to the same directory or update `DataStore` to use a configurable path.
- **Character list:** `CharacterDataManager` reads/writes `charStore.json` via an absolute Windows path: `c:/Eternity/Eternity/Data/charStore.json` (see `CharacterDataManager.java`). Changes here affect portability — prefer updating these constants when running elsewhere.
- **Images:** UI classes load image files via relative file paths such as `eternity/images/<Name>1.png` (example: `FrameNewClass.java`). Confirm the working directory or update the image paths to use classpath resources.

**Big-picture architecture & data flow**
- **UI layer:** Swing frames under `src/main/java/eternity/*Frame*.java` (examples: `FrameSheet`, `FrameNew`, `FrameNewClass`). Frames are thin and call into `DataQuery`/domain objects.
- **Data access:** `DataQuery` is the read-only query API for game data. It delegates to `DataStore`, which eagerly loads JSON files via `DataBuilder` at construction.
- **JSON loader:** `DataBuilder` wraps Jackson `ObjectMapper` and provides `loadList` / `loadObject` helpers. Examples:
  - `new DataBuilder(Paths.get("c:\\Eternity\\Eternity\\data\\"))` in `DataStore`
  - `builder.loadList("classdata.json", DataClass[].class)`
- **Domain classes:** Many `Data*` classes model JSON arrays (e.g., `DataClass`, `DataRace`, `DataColor`) and are used throughout `DataQuery`.
- **Character persistence:** `CharStore` objects are persisted to `charStore.json` by `CharacterDataManager`.

**Project-specific conventions & gotchas**
- **Absolute paths are used:** Search for `c:\Eternity` and `c:/Eternity` in `DataStore` and `CharacterDataManager`. When changing code, update these or make paths configurable to avoid environment-specific bugs.
- **Filesystem image loading (not classpath):** Image loading uses `new ImageIcon("eternity/images/...")` — these are file paths, not resource streams. Running from different working directories will break UI images.
- **Graceful failure model:** Data load errors are generally caught and written to `System.err` (see `DataStore.safeLoad` and `CharacterDataManager`). Agents should preserve that style unless intentionally improving error handling.
- **Jackson dependency:** pom.xml includes `com.fasterxml.jackson.core:jackson-databind:2.9.0.pr2`. Keep Jackson usage consistent with existing data classes.

**Where to look for examples**
- **Entry point:** `src/main/java/eternity/EternityMain.java` — default app start-up and initial frame handling.
- **Data loader pattern:** `src/main/java/eternity/DataBuilder.java` and `DataStore.java`.
- **Query API usage:** `src/main/java/eternity/DataQuery.java` (search and getter patterns used by UI).
- **UI examples:** `FrameNewClass.java` shows image/button layout and how class selection updates `CharData`.

**Actionable rules for code edits**
- **If editing data file names/locations:** update `DataStore` and `CharacterDataManager` constants, and add a single configurable entry (prefer environment variable or system property).
- **If adding new JSON-backed types:** add `DataX.java` model, include the file name in `DataStore` and expose accessors in `DataQuery`.
- **If touching UI images:** prefer loading from `getResource("/eternity/images/...")` and include images in resources so the app is portable.

**When to ask the repo owner**
- Confirm preferred runtime working directory and whether absolute paths should remain or be refactored to configurable paths.

Please review — tell me if you want stricter rules (e.g., a coding-style section) or automated fixes (update paths to use config). I can iterate.