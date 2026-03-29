#Add Feature: Modes#

##Description##

We're going to add a feature to make the app only work with certain modes. The app in its current state is for active modes.

The user configures a whitelist of active modes in the child app preferences. The preference label reads: "Run only in these modes (leave blank to run in all modes)". If no modes are selected, the app runs in all modes (preserving current behavior).

When the system mode changes to an inactive mode, we should unschedule and unsubscribe from all events so this app will no longer lock the door or track changes to the contact sensor or lock.

When the system mode changes to an active mode, there are three possible states we need to account for:

1. The door is open and therefore unlocked. In this case call initialize() to resume normal contact and lock state tracking. When the door closes, the existing contactHandler logic will start the timer.

2. The door is closed and unlocked. In this case call initialize() to resume normal contact and lock state tracking, then call startLockTimer().

3. The door is closed and locked. In this case call initialize() to resume normal contact and lock state tracking.

##Implementation Notes##

- Add a multi-enum input for active modes to the child app preferences section (e.g., input "activeModes", "mode", title: "Run only in these modes (leave blank to run in all modes)", multiple: true, required: false).
- Subscribe to location mode changes in initialize() using: subscribe(location, "mode", modeHandler)
- In modeHandler: if the new mode is not in activeModes (and activeModes is not empty), call unsubscribe() and unschedule(). If the new mode is active, call initialize() and handle the three re-activation states.
- lockHandler, contactHandler, startLockTimer, and lockDoor require no changes.
