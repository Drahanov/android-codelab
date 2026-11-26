#!/usr/bin/env bash
# Moves the device/emulator location to the given point, to test location reminders without walking there.
#
# Usage:   scripts/simulate-location.sh <latitude> <longitude>
#          scripts/simulate-location.sh --reset        (removes the fake location again)
# Example: scripts/simulate-location.sh 52.52000 13.40500
#
# The coordinates of a memo are shown on its detail screen. Put the app in the background before running it.
# Requires adb (Android SDK platform-tools) and one connected device or emulator.
set -euo pipefail

PROVIDERS="gps network"

adb get-state >/dev/null 2>&1 || { echo "No device found. Start an emulator or connect a device."; exit 1; }

if [[ "${1:-}" == "--reset" ]]; then
  for p in $PROVIDERS; do adb shell cmd location providers remove-test-provider "$p" 2>/dev/null || true; done
  adb shell appops set com.android.shell android:mock_location default
  echo "Fake location removed."
  exit 0
fi

if [[ $# -ne 2 ]]; then
  sed -n '4,6p' "$0" | sed 's/^# //'
  exit 1
fi
LAT=$1
LNG=$2

# Allow adb to provide test locations and replace the real providers
adb shell appops set com.android.shell android:mock_location allow
for p in $PROVIDERS; do
  adb shell cmd location providers add-test-provider "$p" 2>/dev/null || true
  adb shell cmd location providers set-test-provider-enabled "$p" true
done

set_location() {
  for p in $PROVIDERS; do
    adb shell cmd location providers set-test-provider-location "$p" --location "$1,$2" --accuracy 5
  done
}

# A reminder is triggered by *entering* the area, so start about 10 km away
echo "Moving away..."
FAR_LAT=$(awk "BEGIN { print $LAT + 0.1 }")
for _ in 1 2 3; do set_location "$FAR_LAT" "$LNG"; sleep 2; done

# Geofences are evaluated on location updates, so keep sending the target point for a while
echo "Arriving at $LAT, $LNG - the reminder should appear within a few seconds..."
for _ in $(seq 1 10); do set_location "$LAT" "$LNG"; sleep 3; done

echo "Done. Run 'scripts/simulate-location.sh --reset' to remove the fake location."
