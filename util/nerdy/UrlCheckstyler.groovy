String sourceFile = "firefox/urls.txt"
File urls = new File("firefox/urls.txt")
File out = new File("firefox.xml")
out.delete()

// Output file header
out << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
out << "<checkstyle version=\"5.5\">\r\n"
out << "  <file name=\"$sourceFile\">\r\n"

int i = 1

// Go through the given file's entries as a list and do stuff
urls.eachLine {

    println "Processing: " + it

    int fakeyLineNumber = i
    // Reminder: There's also a valid "ignore" severity that could be used to provide a filter to keep private URLs
    String severity = "info"

    // Severity can be upped for specific URLs
    if (it.contains("movingblocks") || it.contains("github")) {
        severity = "error"
    } else if (it.contains("eveuniversity") || it.contains("nanoware")) {
        severity = "warning"
    }

    String message = "See file entry for URL"
    String source = "browser.firefox.TabToProcess"

    out << "    <error line=\"$fakeyLineNumber\" severity=\"$severity\" message=\"$message\" source=\"$source\"/>\r\n"

    i += 1
}

// Output file footer
out << "  </file>\r\n"
out << "</checkstyle>"