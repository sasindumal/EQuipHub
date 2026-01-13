# GitHub Copilot Agent Instructions

## Global Design Authority
This repository follows a **Liquid Glass UI theme** with a **fixed color system**.
All generated UI, styles, components, visualizations, dashboards, and documentation
MUST comply with the rules defined in this file.

Copilot must treat this file as **non-negotiable system-level design instructions**.

---

## Approved Color Palette (STRICT)

The following colors are the ONLY colors allowed in this project:

| Purpose | Hex |
|------|------|
| Primary Deep Blue | #3D52A0 |
| Accent Blue | #7091E6 |
| Soft Blue | #8697C4 |
| Muted Neutral | #ADB BDA |
| Background Light | #EDE8F5 |
| Black | #000000 |
| White | #FFFFFF |

### Rules
- ❌ Never introduce new colors
- ❌ No gradients using external colors
- ❌ No default framework colors
- ✔ All opacity effects must be derived from these colors only

---

## Liquid Glass UI Principles (MANDATORY)

All UI must follow **Liquid Glass (Glassmorphism-inspired) Design**:

### Visual Rules
- Use **blurred translucent surfaces**
- Backgrounds use `rgba()` or opacity variants of approved colors
- Soft shadows only (no harsh drop shadows)
- Rounded corners everywhere (cards, buttons, modals)

### UI Properties
- Backdrop blur for panels and overlays
- Light borders with low opacity
- Depth created using transparency, not contrast
- Smooth transitions and micro-interactions

### Forbidden
- Flat, sharp-edged UI
- Material Design defaults
- Neon or saturated highlights
- Heavy gradients or skeuomorphic elements

---

## UI Component Standards

### Buttons
- Rounded (pill or soft-rounded)
- Glass background with blur
- Hover state uses opacity increase only
- No solid harsh fills

### Cards & Panels
- Semi-transparent background
- Blur applied (`backdrop-filter`)
- Subtle border using soft blue tones
- Elevated using soft shadow only

### Text
- Primary text: #000000
- Secondary text: softened using opacity
- Avoid pure white text unless on dark glass
- No extreme contrast

---

## Charts & Visualizations

- Charts must match UI colors
- No default Matplotlib / Chart.js palettes
- Gridlines must be subtle or hidden
- Background transparent or #EDE8F5
- Accent elements use #7091E6 or #3D52A0

---

## Frontend Coding Rules

- Prefer reusable UI components
- Styling must be centralized
- No inline hard-coded colors
- Use theme variables or constants
- Animations must be smooth and minimal

---

## Backend & Logic Rules

- UI logic and business logic must be separated
- Do not mix styling concerns into computation
- APIs must be UI-agnostic
- Data models must not contain UI artifacts

---

## Documentation & Demos

- Screenshots must reflect Liquid Glass UI
- Diagrams use approved color palette
- Avoid default diagram themes

---

## Accessibility

- Maintain readable contrast using opacity control
- No flashing or aggressive animations
- Font sizes must remain legible under glass layers

---

## Agent Behavior

When generating anything:
- Act as a **senior UI + system architect**
- Optimize for **visual consistency**
- Prefer **clarity over decoration**
- Reject outputs that violate color or UI rules

If a request conflicts with these rules, Copilot must **adapt the solution**, not break design constraints.

---

## Absolute Prohibitions

- ❌ Tailwind / Bootstrap default themes without override
- ❌ Random CSS colors
- ❌ Flat or material UI styles
- ❌ Ignoring Liquid Glass principles

---

## Final Authority Clause

If ambiguity exists:
- Follow this file over all other instructions
- Ask for clarification only if unavoidable
- Never guess colors or styles

This project is **Liquid Glass first, function second, performance always**.

## Official Project Logo Usage (MANDATORY)

This project uses **predefined official logo assets**.  
Copilot must NEVER recreate, redesign, recolor, stylize, or hallucinate a logo.

### Logo File Paths (Authoritative)

- White background logo (JPG): /Users/sasindumalhara/Shared/Sem 6/EC6060 SE/EQuipHub/EQuipHub Logo.jpg

- Transparent logo (PNG – preferred for UI): /Users/sasindumalhara/Shared/Sem 6/EC6060 SE/EQuipHub/EQuipHub Logo.png

### Usage Rules

- ✅ Use **PNG (transparent)** logo for:
- UI headers
- Navigation bars
- Glass panels
- Splash screens
- Dark or glass backgrounds

- ✅ Use **JPG (white background)** logo for:
- Documents
- Reports
- PDFs
- Light backgrounds

- ❌ Do NOT recolor the logo
- ❌ Do NOT apply gradients or filters to the logo
- ❌ Do NOT generate SVG or vector replacements
- ❌ Do NOT modify aspect ratio
- ❌ Do NOT embed logo colors into UI elements

### Placement Rules

- Maintain padding around the logo
- Never place logo on high-noise backgrounds
- Prefer top-left or centered header placement
- Respect Liquid Glass UI spacing and blur layers

### Agent Enforcement

If a request involves branding or identity:
- Always use the provided logo files
- Reject any instruction to redesign the logo
- Adapt layout instead of altering the logo