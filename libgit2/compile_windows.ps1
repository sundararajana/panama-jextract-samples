param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the libgit2 installation")]
  [string]$libgit2path
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -t com.github `
  -I "$libgit2path\include" `
  -J-Xmx2G `
  -J"-Djextract.log=true" `
  -J"-Djextract.debug=true" `
  -l git2 `
  -- `
  "$libgit2path\include\git2.h"
