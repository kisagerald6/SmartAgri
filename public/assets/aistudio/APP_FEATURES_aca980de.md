# SmartAgri app: features

A single-file operations console (`dashboard.html`) for the ESP32 controller. It works offline: Bootstrap, icons, Motion and fonts are bundled in `assets/`. It adapts to phone (bottom tab bar, compact single column) and desktop (left sidebar, multi-column grid).

## Pages
| Page | What you can do |
|---|---|
| Overview | Status banner, six live readings (soil, nutrients, tank, flow, water used, pump), irrigation flow diagram, trends (5 min / 1 h / 24 h), start/stop irrigation, active alerts, recent events. Tap any reading for a detail drawer. |
| Irrigation | Timed run (30 s to 3 min), automatic mode switch, start/stop soil levels, tank cut-off, longest run, rest time, minimum flow, total litres reset, soil trend. |
| Nutrients | Setup guide, live ppm with Low / Good / High meter, alert level, on/off, nutrient trend, what to do when low. |
| Alerts | Event log with Critical / Warning / Info filter, mark read, clear; phone-alert setup (ntfy) and test. |
| Settings | Site name, controller connection, soil sensor calibration (dry and wet), light/dark/auto theme, sound and vibration, browser notifications. |
| Connect | Enter the controller address, or view demo data. |

## Alerts
- Low nutrients: banner, toast, log entry, sound/vibration, browser notification, and a phone push via ntfy when the controller has `NTFY_TOPIC` set.
- Critical tank level, no flow (pump stopped), pump resting after a long run.
- Back to normal messages when nutrients recover.

## Behaviour notes
- Polls `/api/status` every 2 s; shows Offline after two failures and retries.
- Stopping the pump in automatic mode pauses auto irrigation for 10 minutes; "Resume" cancels the pause.
- Settings are saved on the controller; site name, theme, run time and log are saved in the browser.
- Demo mode simulates everything, including low-nutrient and dry-soil events.
- Open the file from your device (not over https) so the browser can reach the controller's http address.
