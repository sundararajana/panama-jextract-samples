param(
  [Parameter(Mandatory=$true, HelpMessage="The path python install")]
  [string]$pythonPath
)

. ../shared_windows.ps1

$java = find-tool("java")

& $java `
  -D"foreign.restricted=permit" `
  --add-modules jdk.incubator.foreign `
  -D"java.library.path=$pythonPath" `
  PythonMain.java
