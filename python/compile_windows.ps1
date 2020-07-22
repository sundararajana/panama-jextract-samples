param(
  [Parameter(Mandatory=$true, HelpMessage="The path python install")]
  [string]$pythonPath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -I "$pythonPath\include" `
  "-l" python3 `
  "-t" "org.python" `
  -J-Xmx2G `
  -J"-Djextract.log=true" `
  -J"-Djextract.debug=true" `
  "--" `
  "$pythonPath\include\Python.h"
