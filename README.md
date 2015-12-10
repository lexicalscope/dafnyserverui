Helps you use the Dafny cache with the editor of your choice

# KEY BINDINGS

ALT-v	start verification
CTRL-f	goto next failure in the procedure list
CTRL-d	goto previous failure in the procedure list
CTRL-.	goto next error location in failure trace
CTRL-,	goto next error location in failure trace
CTRL-g	execute the jump command on the focused file location

# Arguments

Invoke main of `com.lexicalscope.dafny.dafnyserverui.DafnyInteractive` with the following arguments 

* `--server "C:\path\to\dafny\Binaries\DafnyServer.exe"` 
* `--file "C:\path\to\proofs\myModule.dfy"`
* `--jump "C:\path\to\Vim\vim74\gvim" --jump=--remote-send  --jump "<ESC>:set title titlestring=dafnyeditor<CR>:tab drop %s<CR>:cal cursor(%d,%d)<CR>"`