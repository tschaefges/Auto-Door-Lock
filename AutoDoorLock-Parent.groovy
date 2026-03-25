/**
 * Auto Door Lock - Parent App
 *
 * Installs and manages child apps, one per lock to be controlled.
 * Future: warning devices to notify for doors left open and unlocked.
 */

definition(
    name: "Auto Door Lock",
    namespace: "tschaefges",
    author: "Tom Schaefges",
    description: "Automatically locks doors that have been left unlocked for a specified number of minutes.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    singleInstance: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Auto Door Lock", install: true, uninstall: true) {
        section {
            app(name: "childApps", appName: "Auto Door Lock - Child", namespace: "tschaefges",
                title: "Add a Lock", multiple: true)
        }
    }
}

def installed() {
    log.debug "Auto Door Lock Parent installed"
}

def updated() {
    log.debug "Auto Door Lock Parent updated"
}
