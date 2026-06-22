# Firefox Extension Beta Distribution

This project's Firefox testing package is built from the same extension source used for Chrome:

- [`src/main/java/websocket/manifest.json`](/C:/Eternity/Eternity/src/main/java/websocket/manifest.json)
- [`src/main/java/websocket/content.js`](/C:/Eternity/Eternity/src/main/java/websocket/content.js)
- [`src/main/java/websocket/pageBridge.js`](/C:/Eternity/Eternity/src/main/java/websocket/pageBridge.js)

The Firefox package step generates a Firefox-specific `manifest.json` during packaging and injects a stable Gecko add-on ID:

- `roll20-bridge@eternity.local`

That keeps the main source manifest shared while giving Firefox a stable identity for testing and later signing.

## Local Packaging

From the repo root:

```powershell
./scripts/package-firefox-extension.ps1
```

This creates:

- `target/firefox-extension/roll20-bridge-firefox-v<version>.zip`

## GitHub Testing Flow

The workflow file is:

- [`.github/workflows/firefox-extension-beta.yml`](/C:/Eternity/Eternity/.github/workflows/firefox-extension-beta.yml)

It runs on:

- manual dispatch
- pushes that change the extension source files

The workflow uploads an artifact named:

- `roll20-bridge-firefox-extension-beta`

## Tester Instructions

Mozilla’s current testing flow allows temporary installation from `about:debugging`, including selecting a packaged `.zip` file.

1. Download the ZIP artifact from GitHub Actions.
2. Open Firefox.
3. Open `about:debugging`.
4. Click `This Firefox`.
5. Click `Load Temporary Add-on`.
6. Select the downloaded ZIP, or extract it and select any file inside the extension folder.

Important:

- Temporary installs are removed when Firefox restarts.
- For broader end-user testing or normal installation, Firefox requires a Mozilla-signed package.

## Recommended GitHub Structure

For early testing, I recommend:

- Keep extension source in this repo for now.
- Use GitHub Actions artifacts for private/early Firefox testing.
- When you move beyond temporary testing, publish a signed Firefox package, typically through AMO or self-distribution of a Mozilla-signed build.

## Versioning

The ZIP name is based on the source `manifest.json` version. Increment that value whenever you want a new tester build to be clearly distinguishable.
