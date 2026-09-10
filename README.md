# Password Generator

<p align="center">
  <img src="docs/images/feature-graphic.png" alt="Password generator — Strong passwords, generated on your phone. No account, no network permission." width="100%">
</p>

A fully offline random password generator for Android. Written in Kotlin and Jetpack Compose, it declares no permissions at all — `INTERNET` included — so passwords are generated on your phone and stay there.

> **🤖 This project is 100% AI-written.** The app code, the tests, the visual design, the store assets and this README were all produced by Claude; the human side was limited to asking for things and signing off on them. See [About "entirely AI-written"](#about-entirely-ai-written).

---

## What it does

Password managers are great, but sometimes you just want **a strong string of random characters** — a new account, a router password to reset, a temporary credential to hand a colleague. This app does that one thing, with nothing questionable going on behind it:

- **A usable password the moment it opens.** One is generated on cold start; tap refresh for another. No sign-up, no login, no network.
- **Strength is computed, not guessed.** Every password shows its Shannon entropy in bits and an estimated crack time at 100 billion guesses per second, graded across four levels from *Weak* to *Very strong*.
- **You pick the alphabet.** Uppercase, lowercase, digits and symbols toggle independently, length slides anywhere from 6 to 48, and strength updates as you change them.
- **Look-alikes can be dropped.** Turn on *Avoid look-alikes* and `l 1 I O 0 o B 8 S 5 Z 2` are removed — useful for passwords that get copied by hand or read out loud.
- **Randomness comes from `SecureRandom`.** Not `Math.random()`, not a timestamp seed. Every password is cryptographically random: nothing recycled, nothing predictable.
- **No network permission.** `AndroidManifest.xml` declares none, and the app says so on screen — it is technically incapable of sending your passwords anywhere.
- **Light and dark themes**, one tap apart.

---

## Screens

| One tap, new password | Adjustable length |
| :---: | :---: |
| <img src="docs/images/screenshot-1-generate.png" alt="A new password in one tap, with live strength and crack time" width="300"> | <img src="docs/images/screenshot-2-length.png" alt="Six to forty-eight characters on a single slider" width="300"> |
| **A new password in one tap** — cryptographically random every time, nothing recycled, nothing predictable. | **Six to forty-eight characters** — drag once, and strength and crack time update as you go. |

| Character sets | Fully offline |
| :---: | :---: |
| <img src="docs/images/screenshot-3-charset.png" alt="Turn off symbols and skip look-alike characters" width="300"> | <img src="docs/images/screenshot-4-offline.png" alt="No network permission; passwords never leave the device" width="300"> |
| **Exactly the characters the site allows** — turn off symbols, skip look-alike letters like `l`, `1`, `O` and `0`. | **No network permission** — every password is made and kept on your phone, and nothing ever leaves the device. |

> These mockups come from a [Claude Design file](https://claude.ai/design/p/c7e5da90-9ca3-4e0e-9a4a-eefa9653114c?file=Play+Store+Assets.dc.html) that doubles as the Google Play listing: a 1024 × 500 feature graphic and four 1080 × 1920 screenshots.

---

## How strength is calculated

Entropy is computed for a uniformly random string:

```
entropy = length × log2(poolSize)
```

`poolSize` is the size of the enabled character sets after look-alikes are filtered out. Crack time assumes an attacker exhausts half the keyspace at 10¹¹ guesses per second:

```
seconds = 2^(entropy − 1) / 1e11
```

The thresholds (`Strength.of`) are `< 45` Weak, `< 65` Fair, `< 90` Strong, `≥ 90` Very strong.

The default — 20 characters with all four sets enabled — draws from a pool of 86 characters for roughly 129 bits, which lands in Very strong.

---

## Built with

| | |
| --- | --- |
| Language | Kotlin (JVM toolchain 17) |
| UI | Jetpack Compose + Material 3, via the Compose BOM |
| Architecture | `ViewModel` + `StateFlow`, unidirectional data flow |
| Navigation | Navigation 3 |
| Randomness | `java.security.SecureRandom` |
| SDK levels | minSdk 24 (Android 7.0), targetSdk / compileSdk 36 |
| Permissions | None |

The code that matters:

```
app/src/main/java/dev/passgen/app/
├── data/PasswordGenerator.kt      # character sets, generation, entropy, crack time
├── ui/main/MainScreen.kt          # Compose UI
├── ui/main/MainScreenViewModel.kt # state and the shuffle animation
└── theme/                         # colors and type
```

---

## Building and running

```bash
# Install a debug build on a connected device
./gradlew installDebug

# Unit tests
./gradlew test

# Instrumented tests (needs a device or emulator)
./gradlew connectedAndroidTest

# Release build
./gradlew assembleRelease
```

Release signing credentials are read from `keystore.properties` in the repository root, which is not committed:

```properties
storeFile=passgen-release.jks
storePassword=…
keyAlias=…
keyPassword=…
```

When that file is absent the release build simply produces unsigned output, so a fresh clone and CI both configure and build without it.

---

## About "entirely AI-written"

Not a line of this repository was written by hand. Every step from an empty directory to a signed, shippable build was done by [Claude](https://claude.com/claude-code):

| Output | Details |
| --- | --- |
| App code | Generation and entropy math in `PasswordGenerator.kt`, the Compose UI, the `ViewModel` state flow, the theme palette |
| Tests | The `PasswordGeneratorTest` and `MainScreenViewModelTest` unit tests and the `MainScreenTest` instrumented test |
| Visual design | Screen mockups, the app icon, the Play feature graphic and the four store screenshots, all made in [Claude Design](https://claude.ai/design/p/c7e5da90-9ca3-4e0e-9a4a-eefa9653114c?file=Play+Store+Assets.dc.html) |
| Build setup | Gradle scripts, the version catalog, signing configuration, `.gitignore` |
| Docs | This README, and every commit message |

The human role was two things: **asking** and **accepting** — deciding what to build, checking whether the result was right, and pointing out what to change. How it was implemented, which architecture it used, and what the screens look like were the model's calls.

Which carries one caveat worth stating plainly: **none of this code has had a line-by-line human review.** Passwords come from `java.security.SecureRandom`, and the entropy and crack-time formulas are written out above for you to check — but if you plan to use this anywhere high-stakes, read `app/src/main/java/dev/passgen/app/data/PasswordGenerator.kt` yourself first. It is a little over a hundred lines.

---

## Privacy

No account, no analytics, no crash reporting, no network permission. A generated password exists only in the app's memory and wherever you deliberately paste it.
