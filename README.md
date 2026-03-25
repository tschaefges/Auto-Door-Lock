# Auto Door Lock

A Hubitat Elevation parent/child app that automatically locks doors that have been left unlocked for a specified number of minutes.

## Requirements

- Hubitat Elevation C-7
- A smart lock with the `lock` capability
- A contact sensor with the `contactSensor` capability

## Installation

1. In the Hubitat UI, go to **Apps Code** and create a new app. Paste the contents of `AutoDoorLock-Parent.groovy` and save.
2. Create a second new app. Paste the contents of `AutoDoorLock-Child.groovy` and save.
3. Go to **Apps** and click **Add User App**. Select **Auto Door Lock**.
4. Inside the parent app, click **Add a Lock** for each door you want to control.

## Configuration

Each child app has three settings:

| Setting | Description |
|---|---|
| **Lock** | The smart lock device to control. |
| **Door Contact** | The contact sensor on the same door as the lock. |
| **Lock Delay** | How many minutes to wait before locking (default: 5). |

Enable **Debug Logging** during setup or troubleshooting. Leave it off during normal use.

## Behavior

1. When the lock becomes unlocked and the door is closed, the lock timer starts.
2. If the door opens, the timer is cancelled and the lock remains unlocked.
3. When the door closes again, the timer restarts.
4. When the timer fires, the lock is locked (provided the door is still closed).
5. If the lock is manually locked before the timer fires, the timer is cancelled.

## Files

- `AutoDoorLock-Parent.groovy` — parent app; installs and manages child instances
- `AutoDoorLock-Child.groovy` — child app; one instance per lock
