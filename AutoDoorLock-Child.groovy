/**
 * Auto Door Lock - Child App
 *
 * Controls a single lock. When the lock becomes unlocked, it will be
 * automatically locked after Lock Delay minutes as long as the door
 * stays closed. If the door is opened, the lock remains unlocked.
 * When the door closes, the timer restarts. The timer is cancelled if
 * the lock is manually locked before the timer fires.
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
        input "lockDelay", "number", title: "Lock Delay (minutes)", required: true, defaultValue: 5
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

def initialize() {
    subscribe(lock, "lock", lockHandler)
    subscribe(contact, "contact", contactHandler)
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
        logDebug "${app.label}: Timer fired -- locking door"
        lock.lock()
    } else {
        logDebug "${app.label}: Timer fired but door is open -- skipping lock"
    }
}
