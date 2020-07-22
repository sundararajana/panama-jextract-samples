$jdk = $Env:JAVA_HOME

function find-tool($tool) {
  if (Test-Path "$jdk\bin\$tool.exe") {
    $func = {
      & "$jdk\bin\$tool.exe" $args;
      if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: $tool exited with non-zero exit code: $LASTEXITCODE"
        exit
      }
    }.GetNewClosure()
    & $func.Module Set-Variable jdk $jdk
    return $func
  } else {
    Write-Host "ERROR: Could not find $tool executable in %JAVA_HOME%\bin."
    exit
  }
}
