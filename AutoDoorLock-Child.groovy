/**
 * Auto Door Lock - Child App
 *
 * Controls a single lock. When the lock becomes unlocked, it will be
 * automatically locked after Lock Delay minutes as long as the door
 * stays closed. If the door is opened, the lock remains unlocked.
 * When the door closes, the timer restarts. The timer is cancelled if
 * the lock is manually locked before the timer fires.
 *
 * Version History:
 *   1.0.0 (2026-03-29) - Initial release. Core auto-lock logic with
 *                         lock delay and contact sensor support.
 *   1.1.0 (2026-03-29) - Added active mode support. App fully
 *                         unsubscribes when mode goes inactive and
 *                         re-evaluates device state on re-activation.
 *   1.2.0 (2026-03-30) - Added lock verification: if the lock does not
 *                         confirm "locked" within 30 seconds of the lock
 *                         command, a notification is sent. Added
 *                         info-level logging for the lock command (always
 *                         on, not gated by debug). Enforced minimum lock
 *                         delay of 1 minute. Added notification device input
 *                         (capability.notification) for lock failure alerts;
 *                         notification is optional if no device is configured.
 */

definition(
    name: "Auto Door Lock - Child",
    namespace: "tschaefges",
    author: "Tom Schaefges",
    description: "Child app controlling a single lock.",
    category: "Convenience",
    parent: "tschaefges:Auto Door Lock",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    section("Devices") {
        input "lock", "capability.lock", title: "Lock", required: true
        input "contact", "capability.contactSensor", title: "Door Contact", required: true
    }
    section("Settings") {
        input "lockDelay", "number", title: "Lock Delay (minutes)", required: true, defaultValue: 5, range: "1..*"
        paragraph "Use the selector below to restrict this lock to specific modes. The 'Set for specific modes' option at the bottom of this page is a Hubitat system feature -- leave it blank."
        input "activeModes", "mode", title: "Run only in these modes (leave blank to run in all modes)", multiple: true, required: false
        input "notificationDevice", "capability.notification", title: "Notification Device (for lock failure alerts)", multiple: true, required: false
        input "debugLogging", "bool", title: "Enable Debug Logging", defaultValue: false
    }
}

def logDebug(msg) {
    if (debugLogging) log.debug msg
}

def installed() {
    logDebug "${app.label}: installed"
    initialize()
}

def updated() {
    logDebug "${app.label}: updated"
    unsubscribe()
    unschedule()
    initialize()
}

def isActiveMode() {
    return !activeModes || activeModes.isEmpty() || activeModes.contains(location.mode)
}

def initialize() {
    subscribe(location, "mode", modeHandler)
    if (isActiveMode()) {
        subscribe(lock, "lock", lockHandler)
        subscribe(contact, "contact", contactHandler)
    } else {
        logDebug "${app.label}: Current mode (${location.mode}) is not active -- not subscribing"
    }
}

// Called when the location mode changes
def modeHandler(evt) {
    if (isActiveMode()) {
        logDebug "${app.label}: Mode changed to ${evt.value} -- activating"
        // Subscribe only to what was dropped on deactivation. The mode subscription
        // was never unsubscribed, so calling initialize() here is unnecessary and
        // would add a redundant location subscription on each activation cycle.
        subscribe(lock, "lock", lockHandler)
        subscribe(contact, "contact", contactHandler)
        if (contact.currentContact == "closed" && lock.currentLock == "unlocked") {
            logDebug "${app.label}: Door closed and unlocked on activation -- starting lock timer"
            startLockTimer()
        }
    } else {
        logDebug "${app.label}: Mode changed to ${evt.value} -- deactivating"
        unsubscribe(lock)
        unsubscribe(contact)
        unschedule(lockDoor)
    }
}

// Called when the lock state changes
def lockHandler(evt) {
    if (evt.value == "unlocked") {
        logDebug "${app.label}: Lock unlocked"
        if (contact.currentContact == "closed") {
            logDebug "${app.label}: Door is closed -- starting lock timer"
            startLockTimer()
        } else {
            logDebug "${app.label}: Door is open -- waiting for door to close"
        }
    } else if (evt.value == "locked") {
        logDebug "${app.label}: Lock locked -- cancelling timer"
        unschedule(lockDoor)
    }
}

// Called when the contact sensor state changes
def contactHandler(evt) {
    if (evt.value == "closed") {
        if (lock.currentLock == "unlocked") {
            logDebug "${app.label}: Door closed and lock is unlocked -- starting lock timer"
            startLockTimer()
        }
    } else if (evt.value == "open") {
        logDebug "${app.label}: Door opened -- cancelling timer"
        unschedule(lockDoor)
    }
}

def startLockTimer() {
    unschedule(lockDoor)
    def delaySeconds = lockDelay * 60
    logDebug "${app.label}: Locking in ${lockDelay} minute(s)"
    runIn(delaySeconds, lockDoor)
}

def lockDoor() {
    // By spec: timer is cancelled if the lock is manually locked before firing,
    // so if we reach here the lock is still unlocked. Only lock if door is still closed.
    if (contact.currentContact == "closed") {
        log.info "${app.label}: Timer fired -- locking door"
        lock.lock()
        runIn(30, verifyLocked)
    } else {
        logDebug "${app.label}: Timer fired but door is open -- skipping lock"
    }
}

def verifyLocked() {
    if (lock.currentLock != "locked") {
        log.warn "${app.label}: Lock command may have failed -- lock reports ${lock.currentLock}"
        notificationDevice?.each { it.deviceNotification("Auto Door Lock: ${app.label} -- lock command may have failed. Door may still be unlocked.") }
    }
}
