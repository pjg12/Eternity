# Chrome Extension Beta Distribution

This project's Chrome extension source currently lives in:

- [`src/main/java/websocket/manifest.json`](/C:/Eternity/Eternity/src/main/java/websocket/manifest.json)
- [`src/main/java/websocket/content.js`](/C:/Eternity/Eternity/src/main/java/websocket/content.js)
- [`src/main/java/websocket/pageBridge.js`](/C:/Eternity/Eternity/src/main/java/websocket/pageBridge.js)

For testing, the recommended GitHub flow is:

1. Commit extension changes to a branch.
2. Let GitHub Actions build a beta ZIP artifact.
3. Have testers download that ZIP from the workflow run.
4. Testers extract it and use `Load unpacked` in `chrome://extensions`.

## Local Packaging

From the repo root:

```powershell
./scripts/package-chrome-extension.ps1
```

This creates:

- `target/chrome-extension/roll20-bridge-v<version>.zip`

## GitHub Testing Flow

The workflow file is:

- [`.github/workflows/chrome-extension-beta.yml`](/C:/Eternity/Eternity/.github/workflows/chrome-extension-beta.yml)

It runs on:

- manual dispatch
- pushes that change the extension source files

The workflow uploads an artifact named:

- `roll20-bridge-chrome-extension-beta`

## Tester Instructions

1. Download the ZIP artifact from GitHub Actions.
2. Extract it somewhere local.
3. Open `chrome://extensions`.
4. Enable `Developer mode`.
5. Click `Load unpacked`.
6. Select the extracted extension folder.

## Recommended GitHub Structure

For beta testing, I recommend:

- Keep extension source in this repo for now.
- Use GitHub Actions artifacts for private/early testing.
- When the extension stabilizes, move to either:
  - GitHub Releases with attached ZIP assets
  - Chrome Web Store publishing

## Versioning

The ZIP name is based on the extension `manifest.json` version. Increment that value whenever you want a new tester build to be clearly distinguishable.
