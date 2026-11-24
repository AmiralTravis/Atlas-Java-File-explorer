# ==========================================
# JavaAtlas Startup Launcher
# ==========================================

$project = "C:\Users\asbia\Downloads\coding\java\test_space\JavaAtlas"

# AutoHotkey executable
$ahk = "C:\Program Files\AutoHotkey\v2\AutoHotkey64.exe"

# AutoHotkey script
$ahkScript = Join-Path $project "atlas.ahk"

# Validate paths
if (!(Test-Path $ahk)) {
    exit 1
}

if (!(Test-Path $ahkScript)) {
    exit 1
}

if (!(Test-Path $project)) {
    exit 1
}

# Start AutoHotkey
Start-Process -FilePath $ahk `
    -ArgumentList "`"$ahkScript`"" `
    -WindowStyle Hidden

# Start JavaAtlas with an interactive, hidden CMD
Start-Process -FilePath "cmd.exe" `
    -ArgumentList '/d /k "title mamu && mvn exec:java -Dexec.mainClass=atlas.Atlas"' `
    -WorkingDirectory $project `
    -WindowStyle Hidden
