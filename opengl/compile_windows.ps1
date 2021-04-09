param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the freeglut installation")]
  [string]$freeglutPath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -I "$freeglutPath\include" `
  --dump-includes 'includes_all.conf' `
  -- `
  "$freeglutPath\include\GL\glut.h"
  
filter_file 'includes_all.conf' '(GL|GLU)' 'includes_filtered.conf'

& $jextract `
  -I "$freeglutPath\include" `
  "-l" opengl32 `
  "-l" glu32 `
  "-l" freeglut `
  "-t" "opengl" `
  '@includes_filtered.conf' `
  "--" `
  "$freeglutPath\include\GL\glut.h"
