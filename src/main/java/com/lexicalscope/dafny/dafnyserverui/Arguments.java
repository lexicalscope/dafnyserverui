package com.lexicalscope.dafny.dafnyserverui;

import java.util.List;

import com.lexicalscope.jewel.cli.Option;

public interface Arguments
{
  @Option(description="the location of the dafny server")
  String server();

  @Option(description="the file you wish to verify")
  String file();

  @Option(description="time limit for verification of a procedure in seconds", defaultValue="30")
  int timeLimit();

  boolean isJump();
  @Option(description="command to exec that will jump to a file, must work in a String.format(command, file, line, column) call")
  List<String> jump();

  boolean isPop();
  @Option(description="on jump pop the window with this name (MS Windows only)")
  String pop();
}