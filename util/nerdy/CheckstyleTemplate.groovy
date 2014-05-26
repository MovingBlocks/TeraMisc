package nerdy;

public class CheckstyleTemplate {

    // Defaults
    public String message = "Message"
    public String priority = "LOW"
    public String key = "12345"
    public String startLine = "1"
    public String endLine = "1"
    public String lineNumber = "1"
    public String fileName = "File"
    public String packageName = "Package"
    public String category = "Category"
    public String type = "Type"
    public String hash = "12345"
    public String path = "Path"

    public String toString() {
        // Template to fill values into. Has to be defined *after* the variables are ready
        return """\
  <warning>
    <message>${message}</message>
    <priority>${priority}</priority>
    <key>${key}</key>
    <lineRanges>
      <range>
        <start>${startLine}</start>
        <end>${endLine}</end>
      </range>
    </lineRanges>
    <primaryLineNumber>${lineNumber}</primaryLineNumber>
    <fileName>${fileName}</fileName>
    <moduleName></moduleName>
    <packageName>${packageName}</packageName>
    <category>${category}</category>
    <type>${type}</type>
    <contextHashCode>${hash}</contextHashCode>
    <origin>checkstyle</origin>
    <pathName>${path}</pathName>
    <primaryColumnStart>0</primaryColumnStart>
    <primaryColumnEnd>0</primaryColumnEnd>
  </warning>\n"""
    }

}