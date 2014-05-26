import groovy.json.JsonSlurper

// Script assumes the FF sessionstore.js file exists in /firefox
println "Copying in the session file"
File sourceFile = new File("firefox/sessionstore.js")
def slurp = new JsonSlurper().parse(new FileReader(sourceFile))

// There may technically be more than one window, but not really worried about that
def windows = slurp.windows
println "Found " + windows.size() + " windows (only parsing the first one)"

// Not sure if there's ever more than one of these (per window)
def tabs = windows.tabs
println "Found a tabs object. Expecting there'll only be one, survey says...: " + tabs.size()

// Container that holds actual tab entries
def entries = tabs.entries
println "Found the entries object inside - again thinking there'll only be one: " + tabs.size()

// URL history per tab - note that it gets parsed out wrapped in an extra set, we only want the inner item so [0]
def url = entries.url[0]
println "Finally got the actual URL entries with a total of: " + url.size()
//println url

println "************************* TABS *************************************"

def totalTabsPlusHistory = 0
File urlsOut = new File("firefox/urls.txt")
urlsOut.delete()

url.each {
    println "This tab held " + it.size() + " past URLs"
    totalTabsPlusHistory += it.size()

    // Since Firefox stores each previous URL visited in the tab (up to a limit, maybe? 50?) we want the last one
    println "Active URL: " + it[-1]
    urlsOut << it[-1] + "\r\n"
}

println "Total tabs plus history held a grand total of " + totalTabsPlusHistory + " urls."
println "With " + url.size() + " total tabs that's average history of " + totalTabsPlusHistory / url.size() + " entries per tab, Whee!"