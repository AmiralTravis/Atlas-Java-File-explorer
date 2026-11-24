#Requires AutoHotkey v2.0

SetTitleMatchMode 2
DetectHiddenWindows true

#HotIf GetKeyState("LShift", "P")

; SHOW / BRING TO FOREGROUND
a & s::
{
    if WinExist("mamu")
    {
        wasHidden := !(WinGetStyle("mamu") & 0x10000000)

        WinShow("mamu")
        WinRestore("mamu")
        WinActivate("mamu")

        ; Only send the menu command when revealing a hidden window
        if (wasHidden)
        {
            if WinWaitActive("mamu",, 2)
            {
                SendText("show search by name menu")
                Send("{Enter}")
            }
        }
    }
}


;HIDE
a & h::
{
    if WinExist("mamu")
    {
        if (WinGetStyle("mamu") & 0x10000000)
        {
            WinHide("mamu")
        }
    }
}

#HotIf