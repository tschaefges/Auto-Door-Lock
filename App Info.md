# App Info

Name: **Auto Door Lock**
Language: **Groovy**
Environment: **Hubitat Elevation C-7**
App Type: **Standalone User App**

## Description

### Purpose

The purpose of this app is to lock doors that have been left unlocked for a specified number of minutes.

### Function

This app uses the Hubitat parent-child app structure. There will be one parent app and a child for each lock that is to be controlled.

The parent app will be used to install and maintain the children and will eventually be used to set warning devices to notify for doors being left open and unlocked.

Each child app will have three settings that are set in the preferences:

| Device           | Function                                                        |
|------------------|-----------------------------------------------------------------|
| **Lock**         | The lock device to be controlled by this child.                 |
| **Door Contact** | The contact sensor for the door in which the lock is installed. |
| **Lock Delay**   | The number of minutes the lock will allow to be unlocked.       |

1. When the **Lock** becomes unlocked, it will be automatically locked after **Lock Delay** minutes as long as the door stays closed.

2. If the door is opened, the **Lock** will remain unlocked.

3. When the door is closed, the **Lock** will be automatically locked after **Lock Delay** minutes as long as the door stays closed. If the door is opened again, loop back to step 2. The timer resets each time the door closes.

 If the lock is manually locked before the timer fires the timer should be cancelled.

 (We have deadbolts, so if the lock is locked while the door is open, it will not be physically possible to close it because the bolt will be in the way.)