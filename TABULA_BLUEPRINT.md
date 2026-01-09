# TABULA PROJECT BLUEPRINT
> Context: This file defines the rules, tech stack, and design system for the "Tabula" Android App. 
> AI Instruction: Read this file before generating any code.

## 1. Project Identity
- **Name:** Tabula
- **Type:** Minimalist Photo Cleaner
- **Core Loop:** Load 15 photos -> Swipe Interaction -> Batch Delete -> Review.
- **Aesthetic:** Strict Black & White, "Art Gallery" feel, Rounded Corners (24dp).

## 2. Tech Stack (Strict Constraints)
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Image Loading:** Coil
- **Async:** Coroutines & Flow
- **Permissions:** Accompanist or AndroidX ActivityResultContracts
- **Min SDK:** 26 (Target SDK 34)

## 3. Coding Skills & Rules (The "How-To")
- **Scoped Storage Rule:** NEVER delete files using `File.delete()`. ALWAYS use `ContentResolver` and handle `RecoverableSecurityException` for Android 10+.
- **Compose Rule:** Use `@Preview` for every UI component. Separate UI (Screen) from Logic (ViewModel).
- **Style Rule:** - NO COLORS allowed except Black (#000000), White (#FFFFFF), and Dark Gray (#1A1A1A).
  - Use `Modifier.graphicsLayer` for swipe animations.
- **Simplicity Rule:** Keep code DRY. If a function exceeds 30 lines, refactor it.

## 4. Key Interaction Logic
- **Swipe Left/Right:** SKIP (Keep photo, move to next).
- **Swipe Up:** TRASH (Mark for deletion, animate up).
- **Session:** Only load 15 photos at a time.