# Lombard Frontend Style Guide

## Colors

- Primary action: `--red`
- Primary action hover: `--red-dark`
- Surface/background: `--white`, `--bg`
- Text: `--text`, `--black`
- Muted text: `--muted`
- Borders: `--border`

All main tokens are defined in `src/styles.css` and switched for dark mode via `[data-theme="dark"]`.

## Components

- `button`: red action button, medium radius, hover state.
- `.cta-link`: compact red CTA for header and inline actions.
- `.card`: base content card with subtle elevation and hover lift.
- `.panel`: neutral content container for forms and grouped blocks.
- `.form`: default form container style.
- `.skeleton`: loading placeholder for async views.
- `ToastContainer`: global notifications (`success` / `error`).

## Interaction Rules

- Use `showToast()` instead of `alert()`.
- Keep one primary action per block.
- Prefer concise headings and short supporting text.
- Use skeleton placeholders while loading data-heavy sections.

## Theme

- Theme is toggled in header.
- Current theme is persisted in `localStorage` (`theme` key).
