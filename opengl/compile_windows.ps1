param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the freeglut installation")]
  [string]$freeglutPath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -I "$freeglutPath\include" `
  "-l" opengl32 `
  "-l" glu32 `
  "-l" freeglut `
  "-t" "opengl" `
  -J-Xmx2G `
  -J"-Djextract.log.cursors=true" `
  -J"-Djextract.debug=true" `
  --filter 'GL' `
  --filter 'GLU' `
  "--" `
  "$freeglutPath\include\GL\glut.h"
