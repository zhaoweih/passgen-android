# Password Generator — Product Requirements

| | |
| --- | --- |
| Product | Password Generator (`dev.passgen.app`) |
| Platform | Android — minSdk 24 (Android 7.0), targetSdk / compileSdk 36 |
| Version | 1.0 (versionCode 1) |
| Status | Written against the shipped v1.0 code (an as-built PRD) |
| Last updated | 2026-09-10 |

> This document describes **what v1.0 actually does**. Every requirement below corresponds to something in the code, so it can be used to align on product behaviour, to plan the next iteration, and as a regression baseline. The known issues in §8.2 and the roadmap in §9 are the parts still open for a decision.

---

## 1. Positioning

### 1.1 In one sentence

A **fully offline** Android password generator: open it and you have a strong random string, generated on the phone and kept there.

### 1.2 The problem

Password managers solve storage. Plenty of moments only call for **a strong string of random characters**:

- signing up for a new account and needing a password nobody else has;
- resetting the default credential on a router, NAS or printer;
- handing a colleague a one-off password.

Comparable tools tend to raise the same three objections: they want a network connection, they want an account, and there is no way to check whether the password was uploaded. This product answers that by **declaring no permissions at all — `INTERNET` included** — and saying so on screen.

### 1.3 Who it is for

| Audience | Characteristics | What they need |
| --- | --- | --- |
| Privacy-minded general users | Distrust cloud password tools | A privacy claim they can verify |
| Developers and ops | Create test accounts and device credentials often | Speed, adjustable character sets, one-tap copy |
| Anyone reading a password aloud or writing it down | Dictating over the phone, copying by hand | Look-alike characters removed |

### 1.4 Design principles

1. **Useful with zero input.** A password is generated on cold start; nothing has to be configured first.
2. **Verifiable privacy.** The privacy promise rests on a technical constraint — no declared permissions — not on copy.
3. **Strength is computed, not asserted.** Shannon entropy and an estimated crack time, rather than a subjective weak/medium/strong rule.
4. **One screen.** Everything fits on a single screen: no second page, no settings screen, no onboarding.

### 1.5 Explicit non-goals

- No password storage, no vault, no autofill service;
- No accounts, no cloud sync, no cross-device anything;
- No analytics, no crash reporting, no ads, no in-app purchases;
- No password history (see the trade-off in §8.1);
- No passphrase / word-list generation.

---

## 2. Scope

A single screen (`MainScreen`) made of five regions, top to bottom:

| # | Region | Components | Purpose |
| --- | --- | --- | --- |
| 1 | Header | Title, theme button | Switch between light and dark |
| 2 | Password card | Password text, strength meter, detail line, copy / refresh buttons | Show the password and its strength; copy; regenerate |
| 3 | Length section | Custom slider, current value | Adjust length between 6 and 48 |
| 4 | Options card | Five toggle rows | Character sets, and dropping look-alikes |
| 5 | Privacy note | Static block | State that the app has no network access |

The navigation layer (Navigation 3) currently registers a single route, `Main`, leaving the structure in place for later screens.

---

## 3. Functional requirements

### FR-1 Generation

| ID | Requirement |
| --- | --- |
| FR-1.1 | The randomness source must be `java.security.SecureRandom`. `Math.random()`, timestamp seeds and any other predictable source are prohibited. |
| FR-1.2 | Passwords are sampled position by position, independently and uniformly: `pool[random.nextInt(pool.length)]`, repeated to the target length. |
| FR-1.3 | A password is generated on cold start **without** the shuffle animation, so it is ready to copy the moment the app opens. |
| FR-1.4 | Regeneration is triggered by: tapping refresh (**with** the animation), changing the length (without), and toggling any character set (without). |
| FR-1.5 | Each generation is independent — nothing is recycled and no history is consulted for de-duplication. |

### FR-2 Character sets and the pool

| ID | Requirement |
| --- | --- |
| FR-2.1 | Four independently switchable sets:<br>· uppercase `A–Z` (26)<br>· lowercase `a–z` (26)<br>· digits `0–9` (10)<br>· symbols `!@#$%^&*()-_=+[]{};:,.?/` (24) |
| FR-2.2 | A fifth toggle, *Avoid look-alikes*, removes the twelve characters `l 1 I O 0 o B 8 S 5 Z 2` from the pool. |
| FR-2.3 | **Fallback:** when all four sets are switched off the pool falls back to lowercase, so a valid password is always produced — no empty password, no error state. |
| FR-2.4 | Look-alike filtering runs **after** the sets are merged, so it applies to every enabled set, including the lowercase fallback. |
| FR-2.5 | Enabled by default: uppercase, lowercase, digits, symbols. Off by default: avoid look-alikes. The default pool is 86 characters. |

Pool sizes, for verification:

| Configuration | Pool | Entropy at 20 chars | Tier |
| --- | --- | --- | --- |
| Default (all four sets, look-alikes kept) | 86 | 129 bits | Very strong |
| All four sets, look-alikes dropped | 74 | 124 bits | Very strong |
| Lowercase only | 26 | 94 bits | Very strong |
| Everything off (fallback) | 26 | 94 bits | Very strong |

### FR-3 Length

| ID | Requirement |
| --- | --- |
| FR-3.1 | Length ranges from 6 to 48; the default is 20. |
| FR-3.2 | The slider is custom: it supports dragging and tapping anywhere on the track, with the landing position rounded to the nearest integer. |
| FR-3.3 | Any incoming length is clamped to `[6, 48]`. If the value has not changed, no regeneration occurs — this is what keeps a drag from regenerating repeatedly. |
| FR-3.4 | The current length appears in large monospace above the right end of the slider, with `6` and `48` marking the ends of the track. |

### FR-4 Strength

| ID | Requirement |
| --- | --- |
| FR-4.1 | Entropy is computed for a uniformly random string: `entropy = round(length × log2(poolSize))`, in bits. It is 0 when `length ≤ 0` or `poolSize ≤ 1`. |
| FR-4.2 | Entropy is derived from **the pool implied by the current settings**, not from statistics over the generated string, so strength updates the instant a length or toggle changes. |
| FR-4.3 | Four tiers: `< 45` Weak, `< 65` Fair, `< 90` Strong, `≥ 90` Very strong. |
| FR-4.4 | The meter fills to 25% / 50% / 78% / 100% by tier, with a colour bound to the tier; both width and colour animate over 300 ms. |
| FR-4.5 | Crack time assumes an attacker exhausting half the keyspace at 10¹¹ guesses per second: `seconds = 2^(entropy − 1) / 1e11`. |
| FR-4.6 | Duration formatting: `< 1 second` renders as "an instant"; more than a billion years as "billions of years"; otherwise the largest fitting unit from seconds / minutes / hours / days / months / years, with one decimal place below 10 and locale-aware thousands separators at 1000 and above. |
| FR-4.7 | The detail line reads `Takes about {duration} to crack · {bits} bits of entropy`, with the duration emphasised in bold. |

### FR-5 Copying

| ID | Requirement |
| --- | --- |
| FR-5.1 | Copy writes the **settled** password to the system clipboard, never an intermediate animation frame. |
| FR-5.2 | The clip must carry `android.content.extra.IS_SENSITIVE` so the system keeps the password out of clipboard previews and history (API 24+). |
| FR-5.3 | A successful copy fires one `LongPress` haptic. |
| FR-5.4 | The button label becomes "Copied ✓" and reverts after 1600 ms; tapping again restarts the timer. |
| FR-5.5 | An empty password is never written to the clipboard (defensive branch). |
| FR-5.6 | Any regeneration clears the copied state, so a new password is never mistaken for one already on the clipboard. |

### FR-6 Shuffle animation

| ID | Requirement |
| --- | --- |
| FR-6.1 | Plays only on an explicit refresh: 9 frames at 34 ms each, roughly 306 ms. |
| FR-6.2 | The password settles left to right: on frame n the first `round(len × n/9)` characters are final and the rest are noise. |
| FR-6.3 | Noise characters are drawn from the current pool plus all symbols, which gives the animation more visual movement without affecting the final password. |
| FR-6.4 | The final password is already decided and held in state while the animation runs; a new generation cancels any animation still in flight. |

### FR-7 Theming

| ID | Requirement |
| --- | --- |
| FR-7.1 | Follows the system light/dark setting by default. |
| FR-7.2 | The header button overrides it to light or dark, and the override survives configuration changes such as rotation. |
| FR-7.3 | Switching themes updates the status and navigation bar styling; the app draws edge to edge. |
| FR-7.4 | Password text is tinted by character class — letters, digits and symbols each get a colour — so the shape of the string is readable at a glance. |

### FR-8 Privacy note

| ID | Requirement |
| --- | --- |
| FR-8.1 | A permanent note at the bottom of the screen reads: "No internet permission — This app has no network access. Every password is generated and stored entirely on your phone — nothing ever leaves the device." |
| FR-8.2 | That copy must stay true to the permissions actually declared in `AndroidManifest.xml`. Introducing any permission requires changing it. |

---

## 4. State and data

### 4.1 UI state

`MainScreenUiState` is the single source of truth (`ViewModel` + `StateFlow`, unidirectional data flow):

| Field | Type | Default | Meaning |
| --- | --- | --- | --- |
| `length` | Int | 20 | Current length |
| `options` | Set&lt;PasswordOption&gt; | upper / lower / digits / symbols | Enabled options |
| `password` | String | generated | The settled password |
| `scramble` | String? | null | Animation frame; null once settled |
| `poolSize` | Int | 86 | Pool size used for this generation |
| `copied` | Boolean | false | Copy confirmation state |

Derived: `displayedPassword` (`scramble ?: password`), `entropy`, `strength`, `crackTime`.

### 4.2 Storage

| Item | Persisted | Notes |
| --- | --- | --- |
| Generated password | No | Lives in process memory and wherever the user deliberately pastes it |
| Length and character sets | No | Return to defaults on a fresh launch |
| Theme override | Process only (`rememberSaveable`) | Back to following the system on cold start |
| Identifiers, analytics | None | Nothing is collected |

No database, no `SharedPreferences`, no file writes, no network calls.

---

## 5. Non-functional requirements

| Area | Requirement |
| --- | --- |
| Permissions | `AndroidManifest.xml` must declare **no permissions**, and `INTERNET` in particular. This is the product's central promise; any new dependency requires re-checking the merged manifest. |
| Security | `SecureRandom` only; clipboard entries flagged sensitive; passwords never logged. |
| Performance | Cold start to a usable password within a frame; repeated generation during a drag is suppressed by the unchanged-value check. |
| Accessibility | Content descriptions on the password text, the copy / refresh / theme buttons and the length slider; all copy lives in `strings.xml`; RTL supported. |
| Compatibility | Android 7.0+. The `IS_SENSITIVE` flag is skipped below API 24 (minSdk is already 24). |
| Internationalisation | Crack-time numbers are formatted per `Locale`. The UI is English-only today, but the strings are extracted and ready to translate. |
| Size | No third-party SDKs; AndroidX and Compose only. |

---

## 6. Technical constraints

| | |
| --- | --- |
| Language | Kotlin (JVM toolchain 17) |
| UI | Jetpack Compose + Material 3, via the Compose BOM |
| Architecture | `ViewModel` + `StateFlow`, unidirectional data flow; the `data/` layer is plain Kotlin and unit-testable on its own |
| Navigation | Navigation 3 (one route today) |
| Randomness | `java.security.SecureRandom`, with an injectable `Random` for tests |
| Signing | Read from an uncommitted `keystore.properties`; absent that, the release build produces unsigned output so a fresh clone and CI both build |

---

## 7. Acceptance

### 7.1 Automated coverage

| Layer | File | Covers |
| --- | --- | --- |
| Generator unit tests | `PasswordGeneratorTest` | Pool assembly per option, the lowercase fallback, look-alike removal, length and pool constraints, two generations differing, the entropy formula, all four tier boundaries, duration formatting edges |
| State unit tests | `MainScreenViewModelTest` | A password on cold start, regeneration on length change, clamping, pool and entropy both shrinking when an option is switched off, strength tracking settings, the animation settling on the final password, the copy confirmation clearing itself |
| Instrumented tests | `MainScreenTest` | Password / length / options rendering, copy and refresh callbacks, option-row callbacks, theme-button callbacks |

### 7.2 Manual checklist

1. Cold start shows a 20-character password, Very strong, 129 bits.
2. Tap refresh: roughly 0.3 s of left-to-right settling, then a stable password.
3. Drag length to 6: strength drops to Weak and the crack time falls to a very short duration.
4. Switch off all four sets: the password becomes lowercase only — no blank, no crash.
5. Turn on *Avoid look-alikes*: new passwords contain none of `l 1 I O 0 o B 8 S 5 Z 2`.
6. Tap copy: the label becomes "Copied ✓" with haptic feedback, reverting after about 1.6 s; the password pastes in another app.
7. Tap refresh after copying: the label returns to "Copy password" immediately.
8. Switch themes: colours and system bars follow; the choice survives rotation.
9. Check app permissions in system settings: no permissions requested.

---

## 8. Trade-offs and known issues

### 8.1 Deliberate trade-offs

- **No guarantee that every enabled class appears.** Each position is sampled independently and uniformly, so enabling digits does not guarantee a digit. This keeps the entropy formula exactly true — forcing one character per class lowers real entropy and makes the displayed figure wrong — at the cost of an extra refresh on sites that mandate character classes.
- **Settings are not saved.** Every cold start returns to 20 characters and the default sets. The trade is for a complete zero-storage posture; saving them later means saying in the privacy copy what gets written.
- **No password history.** History means passwords on disk or resident in memory, which contradicts the positioning.

### 8.2 Defects to fix

| ID | Symptom | Impact |
| --- | --- | --- |
| BUG-1 | Crack-time pluralisation keys off the rounded integer, so a fractional value like 59 bits renders `1.1 month` instead of `1.1 months`. | Cosmetic |
| BUG-2 | Duration and tier strings are assembled in Kotlin rather than `strings.xml`. | Blocks localisation |
| BUG-3 | The custom length slider exposes no `setProgress` semantics, so TalkBack users cannot adjust the length. | Accessibility |

---

## 9. Roadmap (unscheduled)

| Priority | Item | Notes |
| --- | --- | --- |
| P1 | Fix BUG-1 / BUG-2 / BUG-3 | Copy quality and accessibility |
| P1 | Localisation, starting with Chinese | Strings are extracted; durations need templating |
| P2 | Optional "at least one of each class" | Requires restating how entropy is presented |
| P2 | Remember the last length and character sets | Introduces local storage; the privacy copy must follow |
| P3 | Passphrase mode (word lists) | For passwords that get spoken or memorised |
| P3 | Clear the clipboard after a timeout | Behaviour varies by Android version; needs feasibility work first |

---

## 10. Appendix: the formulas

```
poolSize = |union of enabled sets − look-alikes (when enabled)|   // 26 lowercase when all sets are off
entropy  = round(length × log2(poolSize))                          // bits
seconds  = 2^(entropy − 1) / 1e11                                  // half the keyspace at 10^11 guesses/second
strength = entropy < 45 ? Weak : entropy < 65 ? Fair : entropy < 90 ? Strong : Very strong
```
